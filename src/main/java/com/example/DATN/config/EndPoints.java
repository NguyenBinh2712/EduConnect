
package com.example.DATN.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class EndPoints {
    public static final String font_end_host = "http://localhost:3000";

}
