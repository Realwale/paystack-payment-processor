package com.example.paymentprocessor.repository;

import com.example.paymentprocessor.constant.TransactionStatus;
import com.example.paymentprocessor.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReference(String reference);
    List<Transaction> findByEmail(String email);
    List<Transaction> findByStatus(TransactionStatus status);
}
