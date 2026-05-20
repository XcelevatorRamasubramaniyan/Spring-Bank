package com.training.mybank.ui;

import java.util.Scanner;

import com.training.mybank.entity.Admin;
import com.training.mybank.entity.User;
import com.training.mybank.exception.AuthenticationFailedException;
import com.training.mybank.exception.BankingException;
import com.training.mybank.exception.InvalidRecoveryDetailsException;
import com.training.mybank.service.AdminService;
import com.training.mybank.service.AuthService;
import com.training.mybank.service.ForgotPasswordService;
import com.training.mybank.service.RegistrationService;

public class MainMenuUI {

    private final Scanner scanner;
    private final AuthService authService;
    private final RegistrationService registrationService;
    private final ForgotPasswordService forgotPasswordService;
    private final AdminService adminService;
    private final MenuUI menuUI;

    public MainMenuUI(
            Scanner scanner,
            AuthService authService,
            RegistrationService registrationService,
            ForgotPasswordService forgotPasswordService,
            AdminService adminService,
            MenuUI menuUI) {
        this.scanner = scanner;
        this.authService = authService;
        this.registrationService = registrationService;
        this.forgotPasswordService = forgotPasswordService;
        this.adminService = adminService;
        this.menuUI = menuUI;
    }

    public void show() {

        while (true) {
            System.out.println("\n========== RAM's BANK ==========");
            System.out.println("1. Admin Register");
            System.out.println("2. Admin Login");
            System.out.println("3. User Register");
            System.out.println("4. User Login");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            int choice = readChoice();

            switch (choice) {

                case 1:
                    adminRegister();
                    pause();
                    break;

                case 2:
                    adminLogin();
                    break;

                case 3:
                    register();
                    pause();
                    break;

                case 4:
                    loginFlow();
                    break;

                case 5:
                    System.out.println("Thank you for using MyBank.");
                    return;

                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }


    private void adminRegister() {
        System.out.println("\n---- ADMIN REGISTRATION ----");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Admin Secret Code: ");
        String secretCode = scanner.nextLine();

        try {
            adminService.registerAdmin(username, password, secretCode);
            System.out.println("Admin registration successful!");
        } catch (BankingException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void adminLogin() {
        while (true) {
            System.out.println("\n---- ADMIN LOGIN ----");

            String username;
            while (true) {
                System.out.print("Username (or 'exit' to go back): ");
                username = scanner.nextLine();

                if (username.trim().equalsIgnoreCase("exit")) {
                    return;
                }

                try {
                    adminService.verifyAdminExists(username);
                    break;
                } catch (AuthenticationFailedException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Please try again or type 'exit' to return to the main menu.");
                }
            }

            System.out.print("Password: ");
            String password = scanner.nextLine();

            try {
                Admin admin = adminService.performAdminLogin(username, password);
                System.out.println("Login successful!");
                menuUI.openAdminMenu(admin.getUsername());
                return;
            } catch (AuthenticationFailedException e) {
                System.out.println(e.getMessage());

                System.out.print("Forgot password? (yes/no): ");
                String answer = scanner.nextLine().trim();
                if (answer.equalsIgnoreCase("yes")) {
                    adminForgotPassword(username);
                } else {
                    System.out.println("Returning to username prompt...");
                }
            }
        }
    }

    private void adminForgotPassword(String username) {
        System.out.println("\n---- ADMIN FORGOT PASSWORD ----");
        System.out.println("Username: " + username);

        System.out.print("New Password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();

        System.out.print("Admin Secret Code: ");
        String secretCode = scanner.nextLine();

        try {
            adminService.resetAdminPassword(username, secretCode, newPassword, confirmPassword);
            System.out.println("Password reset successful! Please log in with your new password.");
        } catch (BankingException e) {
            System.out.println("Reset failed: " + e.getMessage());
        }
    }

    private void loginFlow() {
        User user = login();
        if (user == null) {
            return;
        }
        System.out.println("Login successful!");
        menuUI.openUserMenu(user.getUsername());
    }

    private User login() {
        while (true) {
            System.out.println("\n---- USER LOGIN ----");

            String username;
            while (true) {
                System.out.print("Username (or 'exit' to go back): ");
                username = scanner.nextLine();

                if (username.trim().equalsIgnoreCase("exit")) {
                    return null;
                }

                try {
                    authService.verifyUserExists(username);
                    break;
                } catch (AuthenticationFailedException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Please try again or type 'exit' to return to the main menu.");
                }
            }


            System.out.print("Password: ");
            String password = scanner.nextLine();

            try {
                return authService.performLogin(username, password);
            } catch (AuthenticationFailedException e) {
                System.out.println(e.getMessage());

                System.out.print("Forgot password? (yes/no): ");
                String answer = scanner.nextLine().trim();
                if (answer.equalsIgnoreCase("yes")) {
                    userForgotPassword(username);
                } else {
                    System.out.println("Returning to username prompt...");
                }
            }
        }
    }

    private void register() {
        System.out.println("\n---- USER REGISTRATION ----");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        try {
            String accNo = registrationService.registerUser(username, password, fullName, email);
            System.out.println("Registration successful!");
            System.out.println("Your account number is: " + accNo);
        } catch (BankingException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void userForgotPassword(String username) {
        System.out.println("\n---- FORGOT PASSWORD ----");
        System.out.println("Username: " + username);

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Account Number: ");
        String accountNumber = scanner.nextLine();

        System.out.print("New Password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm Password: ");
        String confirmPassword = scanner.nextLine();

        try {
            forgotPasswordService.resetUserPassword(
                    username, email, accountNumber, newPassword, confirmPassword);
            System.out.println("Password reset successful! Please log in with your new password.");
        } catch (InvalidRecoveryDetailsException e) {
            System.out.println("Reset failed: " + e.getMessage());
        }
    }

    private int readChoice() {
        while (!scanner.hasNextInt()) {
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private void pause() {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
}
