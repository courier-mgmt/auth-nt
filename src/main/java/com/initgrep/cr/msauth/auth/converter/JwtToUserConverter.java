package com.initgrep.cr.msauth.auth.converter;

import com.initgrep.cr.msauth.auth.constants.JwtExtendedClaimNames;
import com.initgrep.cr.msauth.auth.dto.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;

@RequiredArgsConstructor
@Component
public class JwtToUserConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    private final ScopeToAuthorityConverter scopeToAuthorityConverter;

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        
        var userModel = UserModel.builder()
                .identifier(jwt.getSubject())
                .grantedAuthorities(scopeToAuthorityConverter.convert(jwt.getClaim(JwtExtendedClaimNames.SCOPE)))
                .build();
        return new UsernamePasswordAuthenticationToken(userModel, jwt, Collections.emptyList());
    }


}
