package com.training.mybank.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.mybank.entity.Account;
import com.training.mybank.entity.AccountStatus;
import com.training.mybank.entity.Admin;
import com.training.mybank.entity.Role;
import com.training.mybank.entity.Transaction;
import com.training.mybank.entity.User;
import com.training.mybank.exception.AuthenticationFailedException;
import com.training.mybank.exception.BankingException;
import com.training.mybank.repository.AccountRepository;
import com.training.mybank.repository.AdminRepository;
import com.training.mybank.repository.TransactionRepository;
import com.training.mybank.repository.UserRepository;
import com.training.mybank.util.PasswordUtil;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public AdminService(AdminRepository adminRepository,
                       UserRepository userRepository,
                       AccountRepository accountRepository,
                       TransactionRepository transactionRepository,
                       AuditLogService auditLogService) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogService = auditLogService;
    }



    @Transactional(readOnly = true)
    public Admin login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid admin username"));

        if (!PasswordUtil.matches(password, admin.getPassword())) {
            auditLogService.log(username, "ADMIN_LOGIN_FAILED", "Invalid password attempt");
            throw new AuthenticationFailedException("Incorrect password for admin: " + username);
        }

        auditLogService.log(username, "ADMIN_LOGIN_SUCCESS", "Admin logged in successfully");
        return admin;
    }

    @Transactional
    public Admin registerAdmin(String username, String password, String secretCode) {
        if (!username.startsWith("admin")) {
            throw new BankingException("Admin username must start with 'admin'");
        }
        if (!"ramadmin".equals(secretCode)) {
            throw new BankingException("Invalid admin secret code");
        }
        if (adminRepository.existsByUsername(username)) {
            throw new BankingException("Username already exists");
        }

        
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(PasswordUtil.hash(password));

        Admin savedAdmin = adminRepository.save(admin);
        auditLogService.log(username, "ADMIN_REGISTERED", "New admin account created");
        return savedAdmin;
    }

   

    public void verifyAdminExists(String username) throws AuthenticationFailedException {
        if (!adminRepository.existsByUsername(username)) {
            throw new AuthenticationFailedException("Admin username not found: " + username);
        }
    }

    public Admin performAdminLogin(String username, String password) throws AuthenticationFailedException {
        return login(username, password);
    }

    @Transactional
    public void resetAdminPassword(String username, String secretCode,
                                   String newPassword, String confirmPassword) {
        if (!"ramadmin".equals(secretCode)) {
            throw new BankingException("Invalid admin secret code");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new BankingException("Passwords do not match");
        }
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("Admin not found: " + username));
        admin.setPassword(PasswordUtil.hash(newPassword));
        adminRepository.save(admin);
        auditLogService.log(username, "ADMIN_PASSWORD_RESET", "Admin reset their password");
    }


    @Transactional
    public User addUser(String adminUsername, String username, String password, String fullName, String email, BigDecimal initialBalance) {
        if (userRepository.existsByUsername(username)) {
            throw new BankingException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BankingException("Email already registered");
        }

        PasswordUtil.validateStrength(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setUsername(username);
        account.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        account.setAccountNumber(generateAccountNumber());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        auditLogService.log(adminUsername, "ADMIN_ADD_USER", "Created user: " + username + " with balance: " + initialBalance);
        return savedUser;
    }

    private static final Random RANDOM = new Random();

    private String generateAccountNumber() {
        int min = 10000000;
        int max = 99999999;
        String accountNumber;
        while (true) {
            int number = min + RANDOM.nextInt(max - min + 1);
            accountNumber = String.valueOf(number);
            if (accountRepository.existsByAccountNumber(accountNumber)) {
                break;
            }
        }
        return accountNumber;
    }

    @Transactional
    public void deleteUser(String adminUsername, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("User not found: " + username));

        Account account = accountRepository.findByUsername(username).orElse(null);

        if (account != null) {
            accountRepository.delete(account);
        }

        userRepository.delete(user);

        auditLogService.log(adminUsername, "ADMIN_DELETE_USER", "Deleted user: " + username);
    }

    @Transactional
    public void freezeUser(String adminUsername, String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("Account not found for user: " + username));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Account is already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        accountRepository.save(account);

        auditLogService.log(adminUsername, "ADMIN_FREEZE_USER", "Frozen user account: " + username);
    }

    @Transactional
    public void unfreezeUser(String adminUsername, String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("Account not found for user: " + username));

        if (account.getStatus() == AccountStatus.ACTIVE) {
            throw new BankingException("Account is not frozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);

        auditLogService.log(adminUsername, "ADMIN_UNFREEZE_USER", "Unfrozen user account: " + username);
    }


    @Transactional(readOnly = true)
    public BankSummary getBankSummary() {
        List<Account> allAccounts = accountRepository.findAll();

        BigDecimal totalBalance = allAccounts.stream()
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalUsers = userRepository.count();
        long frozenUsers = allAccounts.stream()
                .filter(account -> account.getStatus() == AccountStatus.FROZEN)
                .count();

        return new BankSummary(totalBalance, totalUsers, frozenUsers);
    }

    @Transactional(readOnly = true)
    public UserDetails getUserDetails(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("User not found: " + username));

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("Account not found for user: " + username));

        List<Transaction> transactions = transactionRepository.findByAccountId(account.getId());

        return new UserDetails(user, account, transactions);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public static class BankSummary {
        private final BigDecimal totalBalance;
        private final long totalUsers;
        private final long frozenUsers;

        public BankSummary(BigDecimal totalBalance, long totalUsers, long frozenUsers) {
            this.totalBalance = totalBalance;
            this.totalUsers = totalUsers;
            this.frozenUsers = frozenUsers;
        }

        public BigDecimal getTotalBalance() {
            return totalBalance;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public long getFrozenUsers() {
            return frozenUsers;
        }
    }

    public static class UserDetails {
        private final User user;
        private final Account account;
        private final List<Transaction> transactions;

        public UserDetails(User user, Account account, List<Transaction> transactions) {
            this.user = user;
            this.account = account;
            this.transactions = transactions;
        }

        public User getUser() {
            return user;
        }

        public Account getAccount() {
            return account;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }
    }
}
