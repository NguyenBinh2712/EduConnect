package com.example.DATN.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MailService {
    JavaMailSender javaMailSender;

    public void sendEmail(String toEmail,String subject, String body){
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom("nguyenthanhbinh271204@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

}
