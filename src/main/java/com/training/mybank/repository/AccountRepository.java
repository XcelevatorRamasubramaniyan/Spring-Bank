package com.training.mybank.repository;

import com.training.mybank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Random;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Random RANDOM = new Random();
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByUsername(String username);
    Optional<Account> findByUsernameAndAccountNumber(String username, String accountNumber);

}
