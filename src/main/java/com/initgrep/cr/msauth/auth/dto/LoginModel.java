package com.initgrep.cr.msauth.auth.dto;

import lombok.Data;

import javax.validation.constraints.Email;

import static com.initgrep.cr.msauth.auth.constants.ValidationConstants.EMAIL_PATTERN;

@Data
public class LoginModel {
    @Email(regexp = EMAIL_PATTERN)
    private String email;
    private String phoneNumber;
    private String password;
    private OtpInfoModel otpInfo;
}
