package com.quancheng.saluki.gateway.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.quancheng.saluki.gateway.oauth2.repository.ClientDetailsRepository;

@Controller
public class TokenController {

    @Autowired
    @Qualifier("consumerTokenServices")
    private ConsumerTokenServices   tokenServices;

    @Autowired
    private TokenStore              tokenStore;

    @Autowired
    private ClientDetailsRepository clientDetailsRepository;

    private HashMap<String, Collection<OAuth2AccessToken>> listAllTokens() {
        HashMap<String, Collection<OAuth2AccessToken>> result = new HashMap<String, Collection<OAuth2AccessToken>>();
        clientDetailsRepository.findAll().forEach(entity -> result.put(entity.getClientId(),
                                                                       enhance(tokenStore.findTokensByClientId(entity.getClientId()))));
        return result;
    }

    @RequestMapping("/oauth/tokens.html")
    public String tokenAdminPage(Model model) {
        HashMap<String, Collection<OAuth2AccessToken>> result = listAllTokens();
        model.addAttribute("tokensList", result);
        return "tokens";
    }

    @RequestMapping(value = "/oauth/tokens/revoke.html", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String revokeToken(@RequestParam("user") String user, @RequestParam("token") String token,
                              Principal principal) throws Exception {
        checkResourceOwner(user, principal);
        if (tokenServices.revokeToken(token)) {
            return "redirect:/oauth/tokens.html?revoke-success";
        } else {
            return "redirect:/oauth/tokens.html?revoke-empty";
        }
    }

    private void checkResourceOwner(String user, Principal principal) {
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            if (!authentication.isClientOnly() && !user.equals(principal.getName())) {
                throw new AccessDeniedException(String.format("User '%s' cannot obtain tokens for user '%s'",
                                                              principal.getName(), user));
            }
        }
    }

    private Collection<OAuth2AccessToken> enhance(Collection<OAuth2AccessToken> tokens) {
        Collection<OAuth2AccessToken> result = new ArrayList<OAuth2AccessToken>();
        for (OAuth2AccessToken prototype : tokens) {
            DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(prototype);
            OAuth2Authentication authentication = tokenStore.readAuthentication(token);
            if (authentication == null) {
                continue;
            }
            String userName = authentication.getName();
            if (StringUtils.isEmpty(userName)) {
                userName = "Unknown";
            }
            Map<String, Object> map = new HashMap<String, Object>(token.getAdditionalInformation());
            map.put("user_name", userName);
            token.setAdditionalInformation(map);
            result.add(token);
        }
        return result;
    }

}
