package com.EventManagementSystem.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.EventManagementSystem.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByOrganizerId(long organizerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.organizerId = :organizerId")
    Optional<Account> findByOrganizerIdForUpdate(@Param("organizerId") long organizerId);
}
