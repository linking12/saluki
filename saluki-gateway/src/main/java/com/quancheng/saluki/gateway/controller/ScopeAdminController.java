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

import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToScopesXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ScopeEntity;
import com.quancheng.saluki.gateway.oauth2.repository.ScopeRepository;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.*;

@Controller
@RequestMapping("/scopes.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ScopeAdminController {

    @Autowired
    private ScopeRepository scopeRepository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAll(Model model, Pageable pageable) {

        model.addAttribute("scopes", scopeRepository.findAll(pageable));
        return "clients/scopes";
    }

    private static final Pattern SCOPE_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                      MediaType.APPLICATION_XHTML_XML_VALUE })
    public String create(@RequestParam("scopeValue") String scopeValue, RedirectAttributes attributes) {

        if (SCOPE_PATTERN.matcher(scopeValue).matches()) {
            if (scopeRepository.findOneByValue(scopeValue).isPresent()) {
                // error message
                addErrorMessage(attributes, "授权范围 " + scopeValue + " 已经存在。");
                attributes.addFlashAttribute("scopeValue", scopeValue);
            } else {
                scopeRepository.save(ScopeEntity.builder().value(scopeValue).build());
                // success message
                addSuccessMessage(attributes, "已成功添加 " + scopeValue + " 授权范围。");
            }
        } else {
            addErrorMessage(attributes, "授权范围 " + scopeValue + " 含有非法字符。（只能使用[a-zA-Z0-9_]）");
            attributes.addFlashAttribute("scopeValue", scopeValue);
        }

        return "redirect:/scopes.html";
    }

    @RequestMapping(path = "/_remove/{scopeValue}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String remove(@PathVariable("scopeValue") String scopeValue, RedirectAttributes attributes) {

        scopeRepository.findOneByValue(scopeValue).map(scopeEntity -> {

            Set<ClientDetailsToScopesXrefEntity> xref = scopeEntity.getClientDetailsToScopesXrefs();
            if (xref.isEmpty()) {

                scopeRepository.delete(scopeEntity);
                addSuccessMessage(attributes, "已成功删除 " + scopeValue + " 授权范围。");

            } else {
                addErrorMessage(attributes, "有" + xref.size() + "个客户端正在使用该授权范围，无法删除。");
            }

            return scopeEntity;

        }).orElseGet(() -> {
            addWarningMessage(attributes, "没有找到 " + scopeValue + " 授权范围");
            return null;
        });

        return "redirect:/scopes.html";
    }

}
