package com.example.tool.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final Map<String, String> secrets = new ConcurrentHashMap<>();

    public String setupOtpForUser(String userId) {
        return secrets.computeIfAbsent(userId, id -> gAuth.createCredentials().getKey());
    }
}
