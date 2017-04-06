package com.quancheng.saluki.gateway.controller;

import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.addErrorMessage;
import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.addSuccessMessage;
import static com.quancheng.saluki.gateway.controller.RedirectMessageHelper.addWarningMessage;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsLimitEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToScopesXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.RedirectUriEntity;
import com.quancheng.saluki.gateway.oauth2.repository.ClientDetailsRepository;
import com.quancheng.saluki.gateway.oauth2.repository.GrantTypeRepository;
import com.quancheng.saluki.gateway.oauth2.repository.ResourceIdRepository;
import com.quancheng.saluki.gateway.oauth2.repository.ScopeRepository;
import com.quancheng.saluki.gateway.oauth2.service.OAuth2DatabaseClientDetailsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/clientDetails.html")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ClientDetailsAdminController {

    @Autowired
    private ClientDetailsRepository            clientDetailsRepository;

    @Autowired
    private GrantTypeRepository                grantTypeRepository;

    @Autowired
    private ScopeRepository                    scopeRepository;

    @Autowired
    private ResourceIdRepository               resourceIdRepository;

    @Autowired
    private OAuth2DatabaseClientDetailsService clientDetailsService;

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                             MediaType.APPLICATION_XHTML_XML_VALUE })
    public String listAll(@RequestParam(name = "type", required = false) String editType,
                          @RequestParam(name = "edit", required = false) String editClientDetails, Model model,
                          Pageable pageable) {

        if (!StringUtils.isEmpty(editType)) {
            if (!StringUtils.isEmpty(editClientDetails)) {
                clientDetailsRepository.findOneByClientId(editClientDetails).map(clientDetailsEntity -> {

                    model.addAttribute("clientId", clientDetailsEntity.getClientId());
                    model.addAttribute("accessTokenValiditySeconds",
                                       clientDetailsEntity.getAccessTokenValiditySeconds());
                    model.addAttribute("refreshTokenValiditySeconds",
                                       clientDetailsEntity.getRefreshTokenValiditySeconds());
                    model.addAttribute("selectedGrantTypes",
                                       clientDetailsEntity.getAuthorizedGrantTypeXrefs().stream().map(xref -> xref.getGrantType().getValue()).collect(Collectors.toList()));
                    model.addAttribute("selectedScopes",
                                       clientDetailsEntity.getScopeXrefs().stream().map(xref -> xref.getScope().getValue()).collect(Collectors.toList()));
                    model.addAttribute("selectedAutoApproveScopes",
                                       clientDetailsEntity.getScopeXrefs().stream().filter(ClientDetailsToScopesXrefEntity::getAutoApprove).map(xref -> xref.getScope().getValue()).collect(Collectors.toList()));
                    model.addAttribute("selectedResourceIds",
                                       clientDetailsEntity.getResourceIdXrefs().stream().map(xref -> xref.getResourceId().getValue()).collect(Collectors.toList()));
                    model.addAttribute("redirectUris",
                                       clientDetailsEntity.getRedirectUris().stream().map(RedirectUriEntity::getValue).collect(Collectors.joining(System.lineSeparator())));
                    ClientDetailsLimitEntity limit = clientDetailsEntity.getClientLimit();
                    if (limit != null) {
                        model.addAttribute("intervalInMills", limit.getIntervalInMills());
                        model.addAttribute("limits", limit.getLimits());
                    }
                    return null;
                });
            }
            model.addAttribute("grantTypes", grantTypeRepository.findAll());
            model.addAttribute("scopes", scopeRepository.findAll());
            model.addAttribute("resourceIds", resourceIdRepository.findAll());
            return "clients/clientDetail";
        }
        model.addAttribute("clientDetailsList", clientDetailsRepository.findAll(pageable));
        return "clients/clientDetails";
    }

    private static final Pattern CLIENT_ID_PATTERN     = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PASSWORD_WORD_PATTERN = Pattern.compile("^[a-zA-Z0-9]{6,}$");

    @RequestMapping(path = "/_create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String create(@RequestParam("clientId") String clientId, @RequestParam("clientSecret") String clientSecret,
                         @RequestParam(name = "accessTokenValiditySeconds", required = false) Integer accessTokenValiditySeconds,
                         @RequestParam(name = "refreshTokenValiditySeconds", required = false) Integer refreshTokenValiditySeconds,
                         @RequestParam(name = "grantTypes", defaultValue = "") List<String> grantTypes,
                         @RequestParam(name = "scopes", defaultValue = "") List<String> scopes,
                         @RequestParam(name = "autoApproveAll", defaultValue = "false") boolean autoApproveAll,
                         @RequestParam(name = "autoApproveScopes", defaultValue = "") List<String> autoApproveScopes,
                         @RequestParam(name = "resourceIds", defaultValue = "") List<String> resourceIds,
                         @RequestParam("redirectUris") String redirectUris,
                         @RequestParam("intervalInMills") Integer intervalInMills,
                         @RequestParam("limits") Integer limits, RedirectAttributes attributes) {

        if (!CLIENT_ID_PATTERN.matcher(clientId).matches()) {
            addErrorMessage(attributes, "客户端ID " + clientId + " 含有非法字符。（只能使用[a-zA-Z0-9_]）");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        if (clientDetailsRepository.findOneByClientId(clientId).isPresent()) {
            addErrorMessage(attributes, "客户端ID " + clientId + " 已存在。");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        if (!PASSWORD_WORD_PATTERN.matcher(clientSecret).matches()) {
            addErrorMessage(attributes, "客户端密码含有非法字符。（只能使用[a-zA-Z0-9]，至少6位）");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        if (accessTokenValiditySeconds != null && accessTokenValiditySeconds < 0) {
            addErrorMessage(attributes, "AccessToken有效秒数不能小于零。");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        if (refreshTokenValiditySeconds != null && refreshTokenValiditySeconds < 0) {
            addErrorMessage(attributes, "RefreshToken有效秒数不能小于零。");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        if (intervalInMills != null && intervalInMills < 0) {
            addErrorMessage(attributes, "限流间隔时间不能小于零");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }
        if (limits != null && limits < 0) {
            addErrorMessage(attributes, "限流间隔次数不能小于零");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }
        // 检查授权方式
        if (!checkGrantTypeValidation(grantTypes, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        // 检查授权范围
        if (!checkScopeValidation(scopes, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        // 检查自动授权范围
        if (!checkScopeValidation(autoApproveScopes, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        // 检查资源ID
        if (!checkResourceIdValidation(resourceIds, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=add";
        }

        Set<String> redirectUrisList = new HashSet<>();
        if (!StringUtils.isEmpty(redirectUris)) {
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(redirectUris));
            String line;
            try {
                while ((line = lineNumberReader.readLine()) != null) {
                    redirectUrisList.add(line);
                }
            } catch (IOException e) {
                log.warn("IOException while parsing redirect Uris: " + redirectUris, e);
            }
        }

        BaseClientDetails baseClientDetails = new BaseClientDetails();
        baseClientDetails.setClientId(clientId);
        baseClientDetails.setClientSecret(clientSecret);
        baseClientDetails.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        baseClientDetails.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
        baseClientDetails.setAuthorizedGrantTypes(grantTypes);
        baseClientDetails.setScope(scopes);
        if (autoApproveAll) {
            baseClientDetails.setAutoApproveScopes(Collections.singleton("true"));
        } else {
            baseClientDetails.setAutoApproveScopes(autoApproveScopes);
        }
        baseClientDetails.setResourceIds(resourceIds);
        baseClientDetails.setRegisteredRedirectUri(redirectUrisList);

        clientDetailsService.addClientDetails(baseClientDetails);
        // 每隔open_api 10秒内最多只能调用3次api
        ClientDetailsEntity detailEntity = clientDetailsRepository.findOneByClientId(baseClientDetails.getClientId()).get();
        ClientDetailsLimitEntity limitEntity = ClientDetailsLimitEntity.builder().intervalInMills(10000L).limits(3L).build();
        detailEntity.setClientLimit(limitEntity);
        limitEntity.setClientDetail(detailEntity);
        clientDetailsRepository.save(detailEntity);
        addSuccessMessage(attributes, "客户端 " + clientId + " 注册成功。");

        return "redirect:/clientDetails.html";
    }

    @RequestMapping(path = "/_update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                                                                         MediaType.APPLICATION_XHTML_XML_VALUE })
    public String update(@RequestParam("clientId") String clientId,
                         @RequestParam(name = "clientSecret", required = false) String clientSecret,
                         @RequestParam(name = "accessTokenValiditySeconds", required = false) Integer accessTokenValiditySeconds,
                         @RequestParam(name = "refreshTokenValiditySeconds", required = false) Integer refreshTokenValiditySeconds,
                         @RequestParam(name = "grantTypes", defaultValue = "") List<String> grantTypes,
                         @RequestParam(name = "scopes", defaultValue = "") List<String> scopes,
                         @RequestParam(name = "autoApproveAll", defaultValue = "false") boolean autoApproveAll,
                         @RequestParam(name = "autoApproveScopes", defaultValue = "") List<String> autoApproveScopes,
                         @RequestParam(name = "resourceIds", defaultValue = "") List<String> resourceIds,
                         @RequestParam("redirectUris") String redirectUris,
                         @RequestParam("intervalInMills") Integer intervalInMills,
                         @RequestParam("limits") Integer limits, RedirectAttributes attributes) {

        if (!clientDetailsRepository.findOneByClientId(clientId).isPresent()) {
            addErrorMessage(attributes, "找不到客户端ID " + clientId);
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        if (!StringUtils.isEmpty(clientSecret)) {
            if (!PASSWORD_WORD_PATTERN.matcher(clientSecret).matches()) {
                addErrorMessage(attributes, "客户端密码含有非法字符。（只能使用[a-zA-Z0-9]，至少6位）");
                resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes,
                                   scopes, autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                                   intervalInMills, limits);
                return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
            }
        }

        if (accessTokenValiditySeconds != null && accessTokenValiditySeconds < 0) {
            addErrorMessage(attributes, "AccessToken有效秒数不能小于零。");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        if (refreshTokenValiditySeconds != null && refreshTokenValiditySeconds < 0) {
            addErrorMessage(attributes, "RefreshToken有效秒数不能小于零。");
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        // 检查授权方式
        if (!checkGrantTypeValidation(grantTypes, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        // 检查授权范围
        if (!checkScopeValidation(scopes, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        // 检查自动授权范围
        if (!checkScopeValidation(autoApproveScopes, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        // 检查资源ID
        if (!checkResourceIdValidation(resourceIds, attributes)) {
            resetRequestParams(clientId, accessTokenValiditySeconds, refreshTokenValiditySeconds, grantTypes, scopes,
                               autoApproveAll, autoApproveScopes, resourceIds, redirectUris, attributes,
                               intervalInMills, limits);
            return "redirect:/clientDetails.html?type=edit&edit=" + clientId;
        }

        Set<String> redirectUrisList = new HashSet<>();
        if (!StringUtils.isEmpty(redirectUris)) {
            LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(redirectUris));
            String line;
            try {
                while ((line = lineNumberReader.readLine()) != null) {
                    redirectUrisList.add(line);
                }
            } catch (IOException e) {
                log.warn("IOException while parsing redirect Uris: " + redirectUris, e);
            }
        }

        BaseClientDetails baseClientDetails = (BaseClientDetails) clientDetailsService.loadClientByClientId(clientId);

        baseClientDetails.setClientId(clientId);
        baseClientDetails.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
        baseClientDetails.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
        baseClientDetails.setAuthorizedGrantTypes(grantTypes);
        baseClientDetails.setScope(scopes);
        if (autoApproveAll) {
            baseClientDetails.setAutoApproveScopes(Collections.singleton("true"));
        } else {
            baseClientDetails.setAutoApproveScopes(autoApproveScopes);
        }
        baseClientDetails.setResourceIds(resourceIds);
        baseClientDetails.setRegisteredRedirectUri(redirectUrisList);

        clientDetailsService.updateClientDetails(baseClientDetails);
        // 每隔open_api 10秒内最多只能调用3次api
        ClientDetailsEntity detailEntity = clientDetailsRepository.findOneByClientId(baseClientDetails.getClientId()).get();
        if (detailEntity != null) {
            detailEntity.getClientLimit().setIntervalInMills(intervalInMills.longValue());
            detailEntity.getClientLimit().setLimits(limits.longValue());
            clientDetailsRepository.save(detailEntity);
        }
        if (!StringUtils.isEmpty(clientSecret)) {
            clientDetailsService.updateClientSecret(clientId, clientSecret);
        }
        addSuccessMessage(attributes, "客户端 " + clientId + " 更新成功。");
        return "redirect:/clientDetails.html";
    }

    private boolean checkGrantTypeValidation(List<String> grantTypes, RedirectAttributes attributes) {
        if (grantTypes.isEmpty()) {
            addErrorMessage(attributes, "授权方式至少选择一个");
            return false;
        } else {
            List<String> invalidGrantTypes = new ArrayList<>();
            grantTypes.forEach(grantType -> {
                if (!grantTypeRepository.findOneByValue(grantType).isPresent()) {
                    invalidGrantTypes.add(grantType);
                }
            });
            invalidGrantTypes.forEach(grantType -> addErrorMessage(attributes, "授权方式 " + grantType + " 无效。"));
            return invalidGrantTypes.isEmpty();
        }

    }

    private boolean checkScopeValidation(List<String> scopes, RedirectAttributes attributes) {
        if (scopes.isEmpty()) {
            addErrorMessage(attributes, "授权范围至少选择一个");
            return false;
        }else{
            List<String> invalidScopes = new ArrayList<>();
            scopes.forEach(scope -> {
                if (!scopeRepository.findOneByValue(scope).isPresent()) {
                    invalidScopes.add(scope);
                }
            });

            invalidScopes.forEach(scope -> addErrorMessage(attributes, "授权范围 " + scope + " 无效。"));

            return invalidScopes.isEmpty();
        }
       
    }

    private boolean checkResourceIdValidation(List<String> resourceIds, RedirectAttributes attributes) {
        List<String> invalidResourceIds = new ArrayList<>();
        resourceIds.forEach(resourceId -> {
            if (!resourceIdRepository.findOneByValue(resourceId).isPresent()) {
                invalidResourceIds.add(resourceId);
            }
        });

        invalidResourceIds.forEach(resourceId -> addErrorMessage(attributes, "资源ID " + resourceId + " 无效。"));

        return invalidResourceIds.isEmpty();
    }

    private void resetRequestParams(String clientId, Integer accessTokenValiditySeconds,
                                    Integer refreshTokenValiditySeconds, List<String> grantTypes, List<String> scopes,
                                    boolean autoApproveAll, List<String> autoApproveScopes, List<String> resourceIds,
                                    String redirectUris, RedirectAttributes attributes, Integer intervalInMills,
                                    Integer limits) {

        attributes.addFlashAttribute("clientId", clientId);
        attributes.addFlashAttribute("accessTokenValiditySeconds", accessTokenValiditySeconds);
        attributes.addFlashAttribute("refreshTokenValiditySeconds", refreshTokenValiditySeconds);
        attributes.addFlashAttribute("selectedGrantTypes", grantTypes);
        attributes.addFlashAttribute("selectedScopes", scopes);
        attributes.addFlashAttribute("autoApproveAll", autoApproveAll);
        attributes.addFlashAttribute("selectedAutoApproveScopes", autoApproveScopes);
        attributes.addFlashAttribute("selectedResourceIds", resourceIds);
        attributes.addFlashAttribute("redirectUris", redirectUris);
        attributes.addFlashAttribute("intervalInMills", intervalInMills);
        attributes.addFlashAttribute("limits", limits);

    }

    @RequestMapping(path = "/_remove/{clientId}", method = RequestMethod.GET, produces = { MediaType.TEXT_HTML_VALUE,
                                                                                           MediaType.APPLICATION_XHTML_XML_VALUE })
    public String delete(@PathVariable("clientId") String clientId, RedirectAttributes attributes) {

        try {
            clientDetailsService.removeClientDetails(clientId);
        } catch (NoSuchClientException e) {
            addWarningMessage(attributes, "没有找到客户端ID " + clientId + " 对应的客户端。");
        }

        return "redirect:/clientDetails.html";
    }

}
