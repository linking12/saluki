package com.quancheng.saluki.gateway.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quancheng.saluki.gateway.oauth2.entity.UserEntity;
import com.quancheng.saluki.gateway.oauth2.entity.UserRoleXrefEntity;
import com.quancheng.saluki.gateway.oauth2.repository.RoleRepository;
import com.quancheng.saluki.gateway.oauth2.repository.UserRepository;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.*;

@Controller
@RequestMapping("/users.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserAdminController {

    @Autowired
    private UserRepository  userRepository;

    @Autowired
    private RoleRepository  roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAllUsers(@RequestParam(name = "type", required = false) String editType,
                               @RequestParam(name = "edit", required = false) String editUsername, Model model,
                               Pageable pageable) {
        model.addAttribute("roles", roleRepository.findAll());
        if (!StringUtils.isEmpty(editType)) {
            if (!StringUtils.isEmpty(editUsername)) {
                model.addAttribute("editUser", userRepository.findOneByUsername(editUsername).map(userEntity -> {
                    Map<String, Object> editUserMap = new HashMap<>();
                    editUserMap.put("username", userEntity.getUsername());
                    editUserMap.put("roles",
                                    userEntity.getRoles().stream().map(xref -> xref.getRole().getName()).collect(Collectors.toList()));
                    return editUserMap;
                }).orElse(null));
            }
            return "userrole/user";
        }
        model.addAttribute("users", userRepository.findAll(pageable));
        return "userrole/users";
    }

    private static final Pattern USER_NAME_PATTERN     = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PASSWORD_WORD_PATTERN = Pattern.compile("^[a-zA-Z0-9]{6,}$");

    @RequestMapping(path = "/_create", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE }, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String createUser(@RequestParam("username") String username, @RequestParam("password") String password,
                             @RequestParam("password-confirmation") String passwordConfirmation,
                             @RequestParam(name = "roles", defaultValue = "") List<String> roles,
                             RedirectAttributes attributes) {

        if (!USER_NAME_PATTERN.matcher(username).matches()) {
            addErrorMessage(attributes, "用户名 " + username + " 含有非法字符。（只能使用[a-zA-Z0-9_]）");
            attributes.addFlashAttribute("username", username);
            attributes.addFlashAttribute("selectedRoles", roles);
            return "redirect:/users.html";
        }

        if (userRepository.findOneByUsername(username).isPresent()) {
            addErrorMessage(attributes, "用户名 " + username + " 已被注册");
            attributes.addFlashAttribute("username", username);
            attributes.addFlashAttribute("selectedRoles", roles);
            return "redirect:/users.html";
        }

        if (!checkPasswordValidation(password, passwordConfirmation, attributes)) {
            attributes.addFlashAttribute("username", username);
            attributes.addFlashAttribute("selectedRoles", roles);
            return "redirect:/users.html";
        }

        if (!checkRoleValidation(roles, attributes)) {
            attributes.addFlashAttribute("username", username);
            attributes.addFlashAttribute("selectedRoles", roles);
            return "redirect:/users.html";
        }

        UserEntity userEntity = UserEntity.builder().username(username).password(passwordEncoder.encode(password)).build();

        userEntity.setRoles(roles.stream().map(role -> UserRoleXrefEntity.builder().user(userEntity).role(roleRepository.findOneByName(role)
                                                                                                                        // 之前都检查过了应该不会抛错
                                                                                                                        .<RuntimeException> orElseThrow(() -> new RuntimeException("角色 "
                                                                                                                                                                                   + role
                                                                                                                                                                                   + " 不存在。"))).build()).collect(Collectors.toSet()));

        userRepository.save(userEntity);

        addSuccessMessage(attributes, "用户 " + username + " 创建成功。");

        return "redirect:/users.html";
    }

    @RequestMapping(path = "/_update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String updateUser(@RequestParam("username") String username,
                             @RequestParam(name = "password", required = false) String password,
                             @RequestParam(name = "password-confirmation", required = false) String passwordConfirmation,
                             @RequestParam(name = "roles", defaultValue = "") List<String> roles,
                             RedirectAttributes attributes) {

        if (!StringUtils.isEmpty(password)) {
            if (!checkPasswordValidation(password, passwordConfirmation, attributes)) {
                return "redirect:/users.html?edit=" + username;
            }
        }

        if (!checkRoleValidation(roles, attributes)) {
            return "redirect:/users.html?edit=" + username;
        }

        userRepository.findOneByUsername(username).map(userEntity -> {

            if (!StringUtils.isEmpty(password)) {
                userEntity.setPassword(passwordEncoder.encode(password));
            }

            // removes
            List<UserRoleXrefEntity> removes = userEntity.getRoles().stream().filter(xref -> !roles.contains(xref.getRole().getName())).collect(Collectors.toList());
            // origin values
            List<String> originValues = userEntity.getRoles().stream().map(xref -> xref.getRole().getName()).collect(Collectors.toList());
            // new ones
            List<UserRoleXrefEntity> newOnes = roles.stream().filter(role -> !originValues.contains(role)).map(role -> UserRoleXrefEntity.builder().user(userEntity).role(roleRepository.findOneByName(role)
                                                                                                                                                                                        // 之前都检查过了应该不会抛错
                                                                                                                                                                                        .<RuntimeException> orElseThrow(() -> new RuntimeException("找不到 "
                                                                                                                                                                                                                                                   + role
                                                                                                                                                                                                                                                   + " 角色"))).build()).collect(Collectors.toList());

            userEntity.getRoles().removeAll(removes);
            userEntity.getRoles().addAll(newOnes);

            return userRepository.save(userEntity);
        }).orElseGet(() -> {
            addErrorMessage(attributes, "用户 " + username + " 不存在。");
            return null;
        });

        return "redirect:/users.html";
    }

    private boolean checkRoleValidation(List<String> roles, RedirectAttributes attributes) {
        List<String> invalidRoles = new ArrayList<>();
        roles.forEach(role -> {
            if (!roleRepository.findOneByName(role).isPresent()) {
                invalidRoles.add(role);
            }
        });

        invalidRoles.forEach(role -> addErrorMessage(attributes, "角色 " + role + " 不存在。"));

        return invalidRoles.isEmpty();
    }

    private boolean checkPasswordValidation(String password, String passwordConfirmation,
                                            RedirectAttributes attributes) {

        boolean invalid = false;

        if (!PASSWORD_WORD_PATTERN.matcher(password).matches()) {
            addErrorMessage(attributes, "密码含有非法字符。（只能使用[a-zA-Z0-9]，至少6位）");
            invalid = true;
        }

        if (!password.equals(passwordConfirmation)) {
            addErrorMessage(attributes, "重复密码不正确");
            invalid = true;
        }

        return !invalid;
    }

    private static final String[]     INVINCIBLE_USERS      = { "admin", "user" };
    private static final List<String> INVINCIBLE_USERS_LIST = Arrays.asList(INVINCIBLE_USERS);

    @RequestMapping(path = "/_remove/{username}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                           MediaType.APPLICATION_XHTML_XML_VALUE })
    public String deleteUser(@PathVariable("username") String username, RedirectAttributes attributes) {
        if (INVINCIBLE_USERS_LIST.contains(username)) {
            addErrorMessage(attributes, "不能删除用户：" + username);
        } else {

            userRepository.findOneByUsername(username).map(userEntity -> {
                userRepository.delete(userEntity);
                addSuccessMessage(attributes, "用户 " + username + " 已删除。");
                return userEntity;
            }).orElseGet(() -> {
                addWarningMessage(attributes, "没有找到 " + username + " 用户。");
                return null;
            });
        }

        return "redirect:/users.html";
    }

}
