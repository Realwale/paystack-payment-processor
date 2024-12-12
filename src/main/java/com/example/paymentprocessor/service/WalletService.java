package com.example.paymentprocessor.service;

import com.example.paymentprocessor.exception.InsufficientFundsException;
import com.example.paymentprocessor.model.Wallet;
import com.example.paymentprocessor.repository.WalletRepository;
import org.springframework.stereotype.Service;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;


import java.math.BigDecimal;


@Service
@Slf4j
@Transactional
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet getOrCreateWallet(String email) {
        log.info("Getting or creating wallet for email: {}", email);
        return walletRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Creating new wallet for email: {}", email);
                    Wallet newWallet = Wallet.builder()
                            .email(email)
                            .balance(BigDecimal.ZERO)
                            .build();
                    Wallet savedWallet = walletRepository.save(newWallet);
                    log.info("New wallet created with ID: {}", savedWallet.getId());
                    return savedWallet;
                });
    }

    public void creditWallet(String email, BigDecimal amount) {
        log.info("Attempting to credit wallet for email: {} with amount: {}", email, amount);
        Wallet wallet = getOrCreateWallet(email);
        BigDecimal currentBalance = wallet.getBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        log.info("Updating balance from {} to {}", currentBalance, newBalance);
        wallet.setBalance(newBalance);
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet credited successfully. New balance: {}", savedWallet.getBalance());
    }

    public void debitWallet(String email, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(email);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds. Available balance: " + wallet.getBalance());
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        log.info("Debited wallet for {} with {}", email, amount);
    }

    public BigDecimal getBalance(String email) {
        return getOrCreateWallet(email).getBalance();
    }
}
