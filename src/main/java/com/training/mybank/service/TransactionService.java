package com.training.mybank.service;

import com.training.mybank.repository.AccountRepository;
import com.training.mybank.repository.TransactionRepository;
import com.training.mybank.entity.Account;
import com.training.mybank.entity.AccountStatus;
import com.training.mybank.entity.Transaction;
import com.training.mybank.repository.UserRepository;
import com.training.mybank.exception.InsufficientBalanceException;
import com.training.mybank.exception.BankingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000.00");

    @Autowired
    public TransactionService(AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /* -------- DEPOSIT -------- */

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deposit(String username, BigDecimal amount) {
        validateAmount(amount);

        Account account = findAccountByUsername(username);
        
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Account is frozen. Deposit not allowed.");
        }
        
        account.setBalance(account.getBalance().add(amount));

        Transaction tx = new Transaction();
        tx.setToAccount(account);
        tx.setTransactionType("DEPOSIT");
        tx.setAmount(amount);
        tx.setFromBalanceAfter(account.getBalance());
        tx.setToBalanceAfter(account.getBalance());
        tx.setBalanceAfter(account.getBalance()); // Set for compatibility
        tx.setRemarks("Deposit");

        accountRepository.save(account);
        transactionRepository.save(tx);
        auditLogService.log(username, "DEPOSIT", "Deposited " + amount + " to account.");
    }

    /* -------- WITHDRAW -------- */

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void withdraw(String username, BigDecimal amount) {
        validateAmount(amount);

        Account account = findAccountByUsername(username);
        
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Account is frozen. Withdrawal not allowed.");
        }
        
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction tx = new Transaction();
        tx.setFromAccount(account);
        tx.setTransactionType("WITHDRAW");
        tx.setAmount(amount);
        tx.setFromBalanceAfter(account.getBalance());
        tx.setToBalanceAfter(account.getBalance());
        tx.setBalanceAfter(account.getBalance()); // Set for compatibility
        tx.setRemarks("Withdraw");

        accountRepository.save(account);
        transactionRepository.save(tx);
        auditLogService.log(username, "WITHDRAW", "Withdrew " + amount + " from account.");
    }

    /* -------- TRANSFER -------- */

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transfer(String fromUser, String toUser, BigDecimal amount) {
        if (fromUser.equals(toUser)) {
            throw new BankingException("Cannot transfer to yourself");
        }

        validateAmount(amount);

        Account from = findAccountByUsername(fromUser);
        Account to = findAccountByUsername(toUser);

        if (from.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Your account is frozen. Transfer not allowed.");
        }
        
        if (to.getStatus() == AccountStatus.FROZEN) {
            throw new BankingException("Recipient account is frozen. Transfer not allowed.");
        }

        if (userRepository.findByUsername(toUser).isPresent()) {
            if (from.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance");
            }

            from.setBalance(from.getBalance().subtract(amount));
            to.setBalance(to.getBalance().add(amount));

            Transaction tx = new Transaction();
            tx.setFromAccount(from);
            tx.setToAccount(to);
            tx.setTransactionType("TRANSFER");
            tx.setAmount(amount);
            tx.setFromBalanceAfter(from.getBalance());
            tx.setToBalanceAfter(to.getBalance());
            tx.setBalanceAfter(from.getBalance());
            tx.setRemarks("Transfer");

            accountRepository.save(from);
            accountRepository.save(to);
            transactionRepository.save(tx);
            auditLogService.log(fromUser, "TRANSFER", "Transferred " + amount + " to " + toUser);
        } else {
            throw new BankingException("User not found with username: " + toUser);
        }

    }

    /* -------- BALANCE -------- */

    @Transactional(readOnly = true)
    public BigDecimal checkBalance(String username) {
        return findAccountByUsername(username).getBalance();
    }

    /* -------- TRANSACTION HISTORY -------- */

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(String username) {
        Account account = findAccountByUsername(username);
        return transactionRepository.findByAccountId(account.getId());
    }

    private Account findAccountByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException("Account not found for username: " + username));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BankingException("Amount exceeds maximum limit of " + MAX_TRANSACTION_AMOUNT);
        }
        if (amount.stripTrailingZeros().scale() > 0) {
            throw new BankingException("Amount must be a rounded whole number (no decimals allowed)");
        }

    }

    /* ---------- UI HELPERS ---------- */

    public Account getAccountByUsername(String username) throws BankingException {
        return findAccountByUsername(username);
    }

}
