package com.test.digitalbankapi.repository;

import com.test.digitalbankapi.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
