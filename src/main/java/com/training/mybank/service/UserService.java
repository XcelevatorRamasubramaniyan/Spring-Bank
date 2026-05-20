package com.training.mybank.service;

import com.training.mybank.entity.User;
import com.training.mybank.exception.BankingException;
import com.training.mybank.repository.UserRepository;
import com.training.mybank.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotBlank;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public UserService(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void changePassword(@NotBlank String username, @NotBlank String oldPassword, @NotBlank String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("User not found with username: " + username));
        if(PasswordUtil.matches(newPassword, user.getPassword())){
            auditLogService.log((username), "SAME_PASSWORD_ENTERED", "Why you came here to change password");
            throw new BankingException("You should not enter the same password to change");
        }

        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            auditLogService.log(username, "PASSWORD_CHANGE_FAILED", "Incorrect old password");
            throw new BankingException("Incorrect old password");
        }

        PasswordUtil.validateStrength(newPassword);
        user.setPassword(PasswordUtil.hash(newPassword));
        userRepository.save(user);

    }
    @Transactional
    public void updateProfile(@NotBlank String username, String fullName, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("User not found with username: " + username));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName);
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new BankingException("Email already registered: " + email);
            }
            user.setEmail(email);
        }

        userRepository.save(user);
        auditLogService.log(username, "PROFILE_UPDATED", "User updated their profile information");
    }

    @Transactional(readOnly = true)
    public User getProfile(@NotBlank String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("User not found with username: " + username));
        return user;
    }
}
