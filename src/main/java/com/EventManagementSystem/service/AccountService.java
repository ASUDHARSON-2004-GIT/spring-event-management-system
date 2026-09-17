package com.EventManagementSystem.service;

import java.util.List;

import com.EventManagementSystem.model.Account;
import com.EventManagementSystem.model.AccountTransaction;

public interface AccountService {

    Account createAccountForOrganizer(long organizerId);

    Account getAccountForOrganizer(long organizerId);

    List<AccountTransaction> listAllTransactions();

    List<AccountTransaction> listAllTransactionsEnriched();
}
