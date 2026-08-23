package com.metionlabs.stocksimulator.repository;

import com.metionlabs.stocksimulator.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {



}

