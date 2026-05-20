package com.training.mybank.ui;

import com.training.mybank.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class MenuUI {

    private final Scanner scanner = new Scanner(System.in);

    private final AuthService authService;
    private final RegistrationService registrationService;
    private final ForgotPasswordService forgotPasswordService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final AdminService adminService;

    private MainMenuUI mainMenuUI;
    private UserMenuUI userMenuUI;
    private AdminMenuUI adminMenuUI;

    public MenuUI(
            AuthService authService
            , RegistrationService registrationService
            , ForgotPasswordService forgotPasswordService
            , TransactionService transactionService
            , UserService userService
            , AdminService adminService
    ) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.forgotPasswordService = forgotPasswordService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.adminService = adminService;
    }

    public void start() {

        mainMenuUI = new MainMenuUI(
                scanner,
                authService,
                registrationService,
                forgotPasswordService,
                adminService,
                this
        );

        userMenuUI = new UserMenuUI(
                scanner,
                transactionService,
                userService,
                authService
        );

        adminMenuUI = new AdminMenuUI(
                scanner,
                adminService
        );

        mainMenuUI.show();
        shutdown();
    }

    public void openUserMenu(String username) {
        userMenuUI.show(username);
    }

    public void openAdminMenu(String adminUsername) {
        adminMenuUI.show(adminUsername);
    }

    private void shutdown() {
        scanner.close();
    }
}
