package com.training.mybank.ui;

import com.training.mybank.entity.Account;
import com.training.mybank.entity.Transaction;
import com.training.mybank.entity.User;
import com.training.mybank.exception.BankingException;
import com.training.mybank.service.AuthService;
import com.training.mybank.service.TransactionService;
import com.training.mybank.service.UserService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class UserMenuUI {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final Scanner scanner;
    private final TransactionService transactionService;
    private final UserService userService;
    private final AuthService authService;

    public UserMenuUI(
            Scanner scanner,
            TransactionService transactionService,
            UserService userService,
            AuthService authService
    ) {
        this.scanner = scanner;
        this.transactionService = transactionService;
        this.userService = userService;
        this.authService = authService;
    }

    public void show(String username) {

        while (true) {
            System.out.println("\n========== USER DASHBOARD ==========");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Balance");
            System.out.println("5. History");
            System.out.println("6. My Profile");
            System.out.println("7. Logout");
            System.out.print("Choose: ");

            int choice = readChoice();

            try {
                switch (choice) {
                    case 1:
                        deposit(username);
                        break;
                    case 2:
                        withdraw(username);
                        break;
                    case 3:
                        transfer(username);
                        break;
                    case 4:
                        balance(username);
                        break;
                    case 5:
                        history(username);
                        break;
                    case 6:
                        showProfileMenu(username);
                        break;
                    case 7:
                        authService.logout(username);
                        return;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (BankingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }

            pause();
        }
    }

    private void showProfileMenu(String username) {
        while (true) {
            System.out.println("\n========== MY PROFILE MENU ==========");
            System.out.println("1. View Profile");
            System.out.println("2. Update Profile");
            System.out.println("3. Change Password");
            System.out.println("4. Back to Dashboard");
            System.out.print("Choose: ");

            int choice = readChoice();
            switch (choice) {
                case 1:
                    viewProfile(username);
                    break;
                case 2:
                    updateProfile(username);
                    break;
                case 3:
                    changePassword(username);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option.");
                    break;
            }
        }
    }

    private void deposit(String username) {
        try {
            System.out.print("Enter deposit amount: ");
            BigDecimal amount = readBigDecimal();

            transactionService.deposit(username, amount);

            System.out.println("\nDeposit Successful");
            System.out.println("Amount Deposited : " + amount);

        } catch (BankingException e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private void withdraw(String username) {
        try {
            System.out.print("Enter withdrawal amount: ");
            BigDecimal amount = readBigDecimal();

            transactionService.withdraw(username, amount);

            System.out.println("\nWithdrawal Successful");
            System.out.println("Amount Withdrawn : " + amount);

        } catch (BankingException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private void transfer(String username) {
        try {
            System.out.print("Enter receiver username: ");
            String toUser = scanner.nextLine().trim();

            if (toUser.isEmpty()) {
                throw new BankingException("Receiver username cannot be empty");
            }

            System.out.print("Enter transfer amount: ");
            BigDecimal amount = readBigDecimal();

            transactionService.transfer(username, toUser, amount);

            System.out.println("\nTransfer Successful");
            System.out.println("Transferred " + amount + " to " + toUser);

        } catch (BankingException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void balance(String username) {
        BigDecimal balance = transactionService.checkBalance(username);

        System.out.println("\n========== ACCOUNT BALANCE ==========");
        System.out.println("Available Balance : " + balance);
        System.out.println("====================================");
    }

    private void history(String username) {
        List<Transaction> transactions = transactionService.getTransactionHistory(username);
        Account myAccount = transactionService.getAccountByUsername(username);

        if (transactions.isEmpty()) {
            System.out.println("\nℹ No transactions found in your history.");
            return;
        }

        System.out.println("                         TRANSACTION HISTORY");
        System.out.printf("%-5s %-12s %-15s %-15s %-20s%n",
                "NO", "TYPE", "AMOUNT", "BALANCE", "DATE");

        int count = 1;
        for (Transaction tx : transactions) {
            BigDecimal displayBalance = null;

            if (tx.getFromAccount() != null && tx.getFromAccount().getId().equals(myAccount.getId())) {
                displayBalance = tx.getFromBalanceAfter();
            } else if (tx.getToAccount() != null && tx.getToAccount().getId().equals(myAccount.getId())) {
                displayBalance = tx.getToBalanceAfter();
            }

            String balanceStr = (displayBalance != null) ? String.format("%.2f", displayBalance) : "N/A";
            String formattedDate = tx.getCreatedAt().format(DATE_FORMATTER);

            System.out.printf("%-5d %-12s %-15.2f %-15s %-20s%n",
                    count++,
                    tx.getTransactionType(),
                    tx.getAmount(),
                    balanceStr,
                    formattedDate);
        }
    }

    private void viewProfile(String username) {
        User user = userService.getProfile(username);
        System.out.println("\n========== MY PROFILE ==========");
        System.out.println("Username  : " + user.getUsername());
        System.out.println("Full Name : " + user.getFullName());
        System.out.println("Email     : " + user.getEmail());
        System.out.println("Role      : " + user.getRole());
        System.out.println("Created   : " + user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        System.out.println("=================================");
    }

    private void updateProfile(String username) {
        System.out.println("\n--- Update Profile ---");
        System.out.print("Enter New Full Name (leave blank to keep current): ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter New Email (leave blank to keep current): ");
        String email = scanner.nextLine().trim();

        try {
            if (name.isEmpty() && email.isEmpty()) {
                System.out.println("Nothing updated");
            } else {
                userService.updateProfile(username, name, email);
                System.out.println(" Profile updated successfully.");
            }

        } catch (BankingException e) {
            System.out.println(e.getMessage());
        }
    }

    private void changePassword(String username) {
        System.out.println("\n--- Change Password ---");
        System.out.print("Enter Old Password: ");
        String oldPass = scanner.nextLine();
        System.out.print("Enter New Password: ");
        String newPass = scanner.nextLine();

        try {
            userService.changePassword(username, oldPass, newPass);
            System.out.println("Password changed successfully.");
        } catch (BankingException e) {
            System.out.println(e.getMessage());
        }
    }

    private BigDecimal readBigDecimal() {
        while (!scanner.hasNextBigDecimal()) {
            System.out.print("Enter a valid number: ");
            scanner.next();
        }
        BigDecimal value = scanner.nextBigDecimal();
        scanner.nextLine();
        return value;
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
