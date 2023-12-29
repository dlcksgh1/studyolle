package com.studyolle.modules.notification;

import com.studyolle.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Long countByAccountAndChecked(Account account, boolean check);
}

