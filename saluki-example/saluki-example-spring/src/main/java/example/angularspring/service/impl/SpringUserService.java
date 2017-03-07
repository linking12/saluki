package example.angularspring.service.impl;

import example.angularspring.dto.Person;
import example.angularspring.dto.User;
import example.angularspring.service.PersonService;
import example.angularspring.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserService that accesses the spring credentials.
 */
@Service
public class SpringUserService implements UserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return new User("");
        }

        return new User(((UserDetails) authentication.getPrincipal()).getUsername());
    }
}
