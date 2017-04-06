package com.quancheng.saluki.gateway.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quancheng.saluki.gateway.oauth2.entity.RoleEntity;
import com.quancheng.saluki.gateway.oauth2.entity.UserRoleXrefEntity;
import com.quancheng.saluki.gateway.oauth2.repository.RoleRepository;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.*;

@Controller
@RequestMapping("/roles.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleAdminController {

    @Autowired
    private RoleRepository roleRepository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAllRoles(Model model, Pageable pageable) {

        model.addAttribute("roles", roleRepository.findAll(pageable));
        return "userrole/roles";
    }

    private static final Pattern ROLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                      MediaType.APPLICATION_XHTML_XML_VALUE })
    public String createRole(@RequestParam("roleName") String roleName, RedirectAttributes attributes) {
        if (ROLE_NAME_PATTERN.matcher(roleName).matches()) {
            if (roleRepository.findOneByName(roleName.toUpperCase()).isPresent()) {
                // error message
                addErrorMessage(attributes, "角色名 " + roleName + " 已存在。");
                attributes.addFlashAttribute("roleName", roleName);
            } else {
                roleRepository.save(RoleEntity.builder().name(roleName.toUpperCase()).build());
                // success message
                addSuccessMessage(attributes, "已成功添加 " + roleName + " 角色。");
            }

        } else {
            addErrorMessage(attributes, "角色名 " + roleName + " 含有非法字符。（只能使用[a-zA-Z0-9_]）");
            attributes.addFlashAttribute("roleName", roleName);
        }
        return "redirect:/roles.html";
    }

    private static final String[]     INVINCIBLE_ROLES      = { "ADMIN", "USER" };
    private static final List<String> INVINCIBLE_ROLES_LIST = Arrays.asList(INVINCIBLE_ROLES);

    @RequestMapping(path = "/_remove/{roleName}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                           MediaType.APPLICATION_XHTML_XML_VALUE })
    public String removeRole(@PathVariable("roleName") String roleName, RedirectAttributes attributes) {

        if (INVINCIBLE_ROLES_LIST.contains(roleName.toUpperCase())) {

            addErrorMessage(attributes, "该角色不可删除：" + roleName);

        } else {

            roleRepository.findOneByName(roleName.toUpperCase()).map(roleEntity -> {

                Set<UserRoleXrefEntity> xRefList = roleEntity.getUsers();
                if (xRefList.isEmpty()) {

                    roleRepository.delete(roleEntity);
                    addSuccessMessage(attributes, "已成功删除 " + roleName + " 角色。");
                } else {
                    addErrorMessage(attributes, "有" + xRefList.size() + "个用户正在使用该角色，无法删除。");
                }

                return roleEntity;
            }).orElseGet(() -> {
                addWarningMessage(attributes, "没有找到 " + roleName + " 角色。");
                return null;
            });
        }

        return "redirect:/roles.html";
    }
}
