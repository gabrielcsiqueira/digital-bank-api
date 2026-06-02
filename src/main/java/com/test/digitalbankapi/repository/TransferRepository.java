package com.test.digitalbankapi.repository;

import com.test.digitalbankapi.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
