package com.orange.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orange.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
