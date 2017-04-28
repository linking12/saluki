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

import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToResourceIdXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ResourceIdEntity;
import com.quancheng.saluki.gateway.oauth2.repository.ResourceIdRepository;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.*;

@Controller
@RequestMapping("/resourceIds")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ResourceIdAdminController {

    @Autowired
    private ResourceIdRepository resourceIdRepository;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAll(Model model, Pageable pageable) {

        model.addAttribute("resIds", resourceIdRepository.findAll(pageable));
        return "clients/resourceIds";
    }

    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                      MediaType.APPLICATION_XHTML_XML_VALUE })
    public String create(@RequestParam("resId") String resId, RedirectAttributes attributes) {

        if (RESOURCE_ID_PATTERN.matcher(resId).matches()) {
            if (resourceIdRepository.findOneByValue(resId).isPresent()) {
                // error message
                addErrorMessage(attributes, "资源ID " + resId + " 已经存在。");
                attributes.addFlashAttribute("resId", resId);
            } else {
                resourceIdRepository.save(ResourceIdEntity.builder().value(resId).build());
                // success message
                addSuccessMessage(attributes, "已成功添加 " + resId + " 资源ID。");
            }
        } else {
            addErrorMessage(attributes, "资源ID " + resId + " 含有非法字符。（只能使用[a-zA-Z0-9_]）");
            attributes.addFlashAttribute("resId", resId);
        }

        return "redirect:/resourceIds";
    }

    @RequestMapping(path = "/_remove/{resId}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                        MediaType.APPLICATION_XHTML_XML_VALUE })
    public String remove(@PathVariable("resId") String resId, RedirectAttributes attributes) {

        resourceIdRepository.findOneByValue(resId).map(resourceIdEntity -> {

            Set<ClientDetailsToResourceIdXrefEntity> xref = resourceIdEntity.getClientDetailsToResourceIdXrefs();
            if (xref.isEmpty()) {

                resourceIdRepository.delete(resourceIdEntity);
                addSuccessMessage(attributes, "已成功删除 " + resId + " 资源ID。");

            } else {
                addErrorMessage(attributes, "有" + xref.size() + "个客户端正在使用该资源ID，无法删除。");
            }

            return resourceIdEntity;

        }).orElseGet(() -> {
            addWarningMessage(attributes, "没有找到 " + resId + " 该资源ID。");
            return null;
        });

        return "redirect:/resourceIds";
    }

}
