package com.EventManagementSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.EventManagementSystem.model.AccountTransaction;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findAllByOrderByTransactionTimestampDesc();

    List<AccountTransaction> findByCustomerIdOrderByTransactionTimestampDesc(long customerId);
}