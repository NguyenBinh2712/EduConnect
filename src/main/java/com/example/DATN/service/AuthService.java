package com.example.DATN.service;

import com.example.DATN.dto.auth.*;
import com.example.DATN.entity.InvalidToken;
import com.example.DATN.entity.User;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.InvalidRepository;
import com.example.DATN.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
//import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class AuthService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    InvalidRepository invalidRepository;

    @Value("${jwt.valid-duration}") @NonFinal
    long VALID_DURATION;

    @Value("${jwt.refresh-duration}")
            @NonFinal
    long REFRESH_DURATION;

    @Value("${jwt.secretKey}")
            @NonFinal
    String SECRET_KEY;

    public AuthResponse login(AuthRequest request){
        User user=userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean isAuthenticated=passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!isAuthenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if(!user.isActive()){
            throw new RuntimeException("Account not Active");
        }
        String token=generateToken(user);
        user.setStatus(true);
        userRepository.save(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    private String generateToken(User user){
        JWSHeader jwsHeader=new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet=new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer(user.getEmail())
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("scope",buildScope(user))
                .build();

        Payload payload=new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject=new JWSObject(jwsHeader,payload);
        try{
            jwsObject.sign(new MACSigner(SECRET_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (KeyLengthException e) {
            throw new RuntimeException(e);
        } catch (JOSEException e) {

            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner=new StringJoiner("");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add(role.getName());
            });
        }
        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token,boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier jwsVerifier=new MACVerifier(SECRET_KEY.getBytes());
        SignedJWT signedJWT= SignedJWT.parse(token);
        var verify=signedJWT.verify(jwsVerifier);
        Date exp=(isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION,ChronoUnit.DAYS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        if(!(verify&&exp.after(new Date()))){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if(invalidRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    public void logout(LogoutRequest request) {
        try {

            var signedToken = verifyToken(request.getToken(), false);

            String jwtId = signedToken.getJWTClaimsSet().getJWTID();
            Date exp = signedToken.getJWTClaimsSet().getExpirationTime();

            if (invalidRepository.existsById(jwtId)) {
                return;
            }

            InvalidToken invalidToken = InvalidToken.builder()
                    .id(jwtId)
                    .exp(exp)
                    .build();

            invalidRepository.save(invalidToken);

        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public AuthResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signToken=verifyToken(request.getToken(),true);
        String jwtId=signToken.getJWTClaimsSet().getJWTID();
        Date exp=signToken.getJWTClaimsSet().getExpirationTime();

        InvalidToken invalidToken = InvalidToken.builder()
                .id(jwtId)
                .exp(exp)
                .build();

        invalidRepository.save(invalidToken);

        String email=signToken.getJWTClaimsSet().getSubject();
        User user=userRepository.findUserByEmail(email)
                .orElseThrow(()->new AppException(ErrorCode.UNAUTHENTICATED));
        String token=generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .authenticated(isValid)
                .build();
    }
}
