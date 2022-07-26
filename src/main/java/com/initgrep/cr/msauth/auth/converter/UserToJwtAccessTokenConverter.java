package com.initgrep.cr.msauth.auth.converter;

import com.initgrep.cr.msauth.auth.dto.TokenModel;
import com.initgrep.cr.msauth.auth.dto.UserModel;
import com.initgrep.cr.msauth.auth.util.UtilMethods;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.initgrep.cr.msauth.auth.constants.AuthConstants.RESOURCE_SERVER;
import static com.initgrep.cr.msauth.auth.constants.JwtExtendedClaimNames.SCOPE;

@RequiredArgsConstructor
@Setter
@Component
public class UserToJwtAccessTokenConverter implements Converter<Authentication, TokenModel> {

    private final AuthorityToScopeConverter authorityToScopeConverter;
    private final JwtEncoder accessTokenEncoder;

    @Value("${spring.application.name}")
    private String issuerApp;

    @Value("${app.access-token.expiry-duration-min}")
    private int accessTokenExpiryMinutes;


    @Override
    public TokenModel convert(Authentication authentication) {
        UserModel user = (UserModel) authentication.getPrincipal();
        Instant now = Instant.now();
        String jit =  UtilMethods.guid();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .id(jit)
                .issuer(issuerApp)
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiryMinutes, ChronoUnit.MINUTES))
                .subject(user.getIdentifier())
                .audience(List.of(RESOURCE_SERVER))
                .claim(SCOPE, authorityToScopeConverter.convert(user.getGrantedAuthorities()))
                .build();

        String token = accessTokenEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
        return  new TokenModel(jit, token);
    }


}
