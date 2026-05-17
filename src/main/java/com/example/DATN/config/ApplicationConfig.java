package com.example.DATN.config;

import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;

import com.example.DATN.repository.RoleRepository;
import com.example.DATN.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ApplicationConfig {
    PasswordEncoder passwordEncoder;
    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository){
        return args -> {
            if(userRepository.findUserByEmail("admin@gmail.com").isEmpty()){
                var role=roleRepository.findByName("ADMIN")
                        .orElse(null);
                if(role==null){
                    log.warn("ADMIN role not found in database. Admin user not created.");
                    return;
                }
                Set<Role> roles=new HashSet<>();
                roles.add(role);
                User user= new User();
                user.setEmail("admin@gmail.com");
                user.setPassword(passwordEncoder.encode("admin"));
                user.setActive(true);
                user.setRoles(roles);
                userRepository.save(user);
                log.info("Admin user created: admin@gmail.com / admin");
            }
        };
    }
}
