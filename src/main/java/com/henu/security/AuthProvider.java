package com.henu.security;

import com.henu.entity.User;
import com.henu.service.IUserService;
import com.henu.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AuthProvider implements AuthenticationProvider {
    @Autowired
    private IUserService userService;

    private final MD5Util md5Util = new MD5Util();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String inputPassword = (String) authentication.getCredentials();

        User user = userService.findByUserName(userName);
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("authError");
        }
        String passWord = md5Util.MD5EncodeUtf8(inputPassword);

        if (passWord.equals(user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        }
        throw new BadCredentialsException("authError");

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
