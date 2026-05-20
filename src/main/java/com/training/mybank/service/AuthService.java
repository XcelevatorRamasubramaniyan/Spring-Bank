package com.training.mybank.service;

import com.training.mybank.repository.UserRepository;
import com.training.mybank.entity.User;
import com.training.mybank.exception.AuthenticationFailedException;
import com.training.mybank.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public AuthService(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public User login(String username, String password) {

        User user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username: " + username));

        if (!PasswordUtil.matches(password, user.getPassword())) {
            auditLogService.log(username, "LOGIN_FAILED", "Invalid password");
            throw new AuthenticationFailedException("Invalid password for username: " + username);
        }

        auditLogService.log(username, "LOGIN_SUCCESS", "User logged in successfully");
        return user;
    }

    public void logout(String username) {
        auditLogService.log(username, "LOGOUT", "User logged out");
    }

    @Transactional(readOnly = true)
    public void verifyUserExists(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new AuthenticationFailedException("Invalid username: " + username);
        }
    }

    public User performLogin(String username, String password) throws AuthenticationFailedException {
        verifyUserExists(username);
        return login(username, password);
    }
}
