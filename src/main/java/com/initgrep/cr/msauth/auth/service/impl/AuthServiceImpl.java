package com.initgrep.cr.msauth.auth.service.impl;

import com.initgrep.cr.msauth.auth.dto.*;
import com.initgrep.cr.msauth.auth.service.AppUserDetailsManager;
import com.initgrep.cr.msauth.auth.service.AuthService;
import com.initgrep.cr.msauth.auth.service.TokenService;
import com.initgrep.cr.msauth.auth.util.UserMapper;
import com.initgrep.cr.msauth.auth.util.UtilMethods;
import com.initgrep.cr.msauth.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final AppUserDetailsManager userDetailsService;
    private final TokenService tokenService;
    @Qualifier("OptionalPasswordDaoAuthenticationProvider")
    private final AuthenticationProvider optionalPasswordDaoAuthenticationProvider;
    private final PasswordEncoder passwordEncoder;
    @Qualifier("jwtRefreshTokenAuthenticationProvider")
    private final AuthenticationProvider refreshTokenAuthProvider;
    private final AppConfig appConfig;

    @Transactional
    @Override
    public TokenResponse register(RegisterRequest registerRequest) {
        var userModel = UserMapper.toUserModel(registerRequest);
        var encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        userModel.setPassword(encodedPassword);
        userModel = userDetailsService.createUser(userModel);

        var authenticationToken
                = UsernamePasswordAuthenticationToken.authenticated(userModel, encodedPassword, userModel.getGrantedAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return buildTokenResponse(tokenService.provideToken(authenticationToken));
    }

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        var designatedUserName = !UtilMethods.isEmpty(loginRequest.getPhoneNumber()) ? loginRequest.getPhoneNumber() : loginRequest.getEmail();
        var unauthenticatedToken = UsernamePasswordAuthenticationToken.unauthenticated(designatedUserName, loginRequest.getPassword());
        var authenticatedToken = (UsernamePasswordAuthenticationToken) optionalPasswordDaoAuthenticationProvider.authenticate(unauthenticatedToken);
        SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
        return buildTokenResponse(tokenService.provideToken(authenticatedToken));

    }

    @Override
    public TokenResponse getNewAccessToken(RefreshTokenRequest refreshTokenRequest) {
        var authentication = refreshTokenAuthProvider.authenticate(new BearerTokenAuthenticationToken(refreshTokenRequest.getRefreshToken()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return buildTokenResponse(tokenService.provideToken(authentication));
    }

    private TokenResponse buildTokenResponse(InternalTokenModel internalTokenModel) {
        return TokenResponse.builder()
                .expiresIn(appConfig.getAccessToken().getExpiryDurationMin() * 60)
                .accessToken(internalTokenModel.getAccessToken().getToken())
                .refreshToken(internalTokenModel.getRefreshToken().getToken())
                .build();
    }

}
