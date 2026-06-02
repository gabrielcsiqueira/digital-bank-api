package com.test.digitalbankapi.repository;

import com.test.digitalbankapi.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
