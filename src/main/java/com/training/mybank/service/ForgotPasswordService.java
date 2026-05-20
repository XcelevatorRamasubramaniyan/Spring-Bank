package com.training.mybank.service;

import com.training.mybank.entity.Account;
import com.training.mybank.entity.AccountStatus;
import com.training.mybank.entity.User;
import com.training.mybank.exception.InvalidRecoveryDetailsException;
import com.training.mybank.repository.AccountRepository;
import com.training.mybank.repository.UserRepository;
import com.training.mybank.util.PasswordUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public ForgotPasswordService(UserRepository userRepository,
            AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    /* ---------- VERIFY RECOVERY DETAILS ---------- */

    private User verifyRecoveryDetails(String username,
                                       String email,
                                       String accountNumber) {

        User user = userRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new InvalidRecoveryDetailsException("Invalid username/email combination"));

        Account account = accountRepository.findByUsernameAndAccountNumber(
                username, accountNumber)
                .orElseThrow(() -> new InvalidRecoveryDetailsException("Invalid username/account number combination"));


        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new InvalidRecoveryDetailsException(
                    "Account is frozen. Password reset not allowed");
        }

        return user;
    }

    /* ---------- RESET PASSWORD ---------- */

    @Transactional
    public void resetPassword(String username,
            String email,
            String accountNumber,
            String newPassword,
            String confirmPassword) {

        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidRecoveryDetailsException("Passwords do not match");
        }

        try {
            PasswordUtil.validateStrength(newPassword);
        } catch (IllegalArgumentException e) {
            throw new InvalidRecoveryDetailsException(e.getMessage());
        }

        try {
            User user = verifyRecoveryDetails(
                    username, email, accountNumber);

            user.setPassword(PasswordUtil.hash(newPassword));

        } catch (InvalidRecoveryDetailsException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidRecoveryDetailsException(
                    "Unable to reset password. Please verify your details and try again.");
        }
    }

    /* ---------- UI HELPER ---------- */

    public void resetUserPassword(String username, String email, String accountNumber,
                                  String newPassword, String confirmPassword) throws InvalidRecoveryDetailsException {
        resetPassword(username, email, accountNumber, newPassword, confirmPassword);
    }

}
