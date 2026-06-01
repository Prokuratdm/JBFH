package com.par.jbfh.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class UserPrincipal extends User {

    private final UUID userId;

    public UserPrincipal(String username, UUID userId, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.userId = userId;
    }
}