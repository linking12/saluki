package com.quancheng.saluki.gateway.controller;

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
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToAuthorizedGrantTypeXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.GrantTypeEntity;
import com.quancheng.saluki.gateway.oauth2.repository.GrantTypeRepository;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.*;

@Controller
@RequestMapping("/grantTypes.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class GrantTypeAdminController {

    @Autowired
    private GrantTypeRepository grantTypeRepository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAllGrantTypes(Model model, Pageable pageable) {

        model.addAttribute("grantTypes", grantTypeRepository.findAll(pageable));
        return "clients/grantTypes";
    }

    private static final Pattern GRANT_TYPES_PATTERN = Pattern.compile("^[a-zA-Z_]+$");

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                      MediaType.APPLICATION_XHTML_XML_VALUE })
    public String createGrantType(@RequestParam("grantTypeName") String grantTypeName, RedirectAttributes attributes) {

        if (GRANT_TYPES_PATTERN.matcher(grantTypeName).matches()) {
            if (grantTypeRepository.findOneByValue(grantTypeName.toLowerCase()).isPresent()) {
                // error message
                addErrorMessage(attributes, "授权方式 " + grantTypeName + " 已经存在。");
                attributes.addFlashAttribute("grantTypeName", grantTypeName);
            } else {
                grantTypeRepository.save(GrantTypeEntity.builder().value(grantTypeName.toLowerCase()).build());
                // success message
                addSuccessMessage(attributes, "已成功添加 " + grantTypeName + " 授权方式。");
            }
        } else {
            addErrorMessage(attributes, "授权方式 " + grantTypeName + " 含有非法字符。（只能使用[a-zA-Z_]）");
            attributes.addFlashAttribute("grantTypeName", grantTypeName);
        }

        return "redirect:/grantTypes.html";
    }

    @RequestMapping(path = "/_remove/{grantTypeName}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                MediaType.APPLICATION_XHTML_XML_VALUE })
    public String removeGrantType(@PathVariable("grantTypeName") String grantTypeName, RedirectAttributes attributes) {

        grantTypeRepository.findOneByValue(grantTypeName.toLowerCase()).map(grantTypeEntity -> {

            Set<ClientDetailsToAuthorizedGrantTypeXrefEntity> xref = grantTypeEntity.getClientDetailsToAuthorizedGrantTypeXrefs();
            if (xref.isEmpty()) {

                grantTypeRepository.delete(grantTypeEntity);
                addSuccessMessage(attributes, "已成功删除 " + grantTypeName + " 授权方式。");

            } else {
                addErrorMessage(attributes, "有" + xref.size() + "个客户端正在使用该授权方式，无法删除。");
            }

            return grantTypeEntity;

        }).orElseGet(() -> {
            addWarningMessage(attributes, "没有找到 " + grantTypeName + " 授权方式。");
            return null;
        });

        return "redirect:/grantTypes.html";
    }

}
