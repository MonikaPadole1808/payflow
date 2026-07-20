package com.monika.payflow.notification.repository;

import com.monika.payflow.notification.entity.Notification;
import com.monika.payflow.notification.entity.NotificationStatus;
import com.monika.payflow.notification.entity.NotificationType;
import com.monika.payflow.user.entity.User;
import com.monika.payflow.user.entity.UserRole;
import com.monika.payflow.user.entity.UserStatus;
import com.monika.payflow.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class NotificationRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findByUserIdOrderByCreatedAtDescReturnsOnlyUserNotifications() throws InterruptedException {
        User firstUser = userRepository.save(user("first@example.com"));
        User secondUser = userRepository.save(user("second@example.com"));
        Notification olderNotification = notificationRepository.save(notification(firstUser.id()));
        Thread.sleep(5);
        Notification newerNotification = notificationRepository.save(notification(firstUser.id()));
        notificationRepository.save(notification(secondUser.id()));

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(firstUser.id());

        assertThat(notifications)
                .extracting(Notification::id)
                .containsExactly(newerNotification.id(), olderNotification.id());
    }

    @Test
    void findByIdAndUserIdRejectsNotificationsOwnedByAnotherUser() {
        User owner = userRepository.save(user("owner@example.com"));
        User otherUser = userRepository.save(user("other@example.com"));
        Notification notification = notificationRepository.save(notification(owner.id()));

        assertThat(notificationRepository.findByIdAndUserId(notification.id(), otherUser.id()))
                .isEmpty();
    }

    private User user(String email) {
        return new User(email, "encoded-password", UserRole.USER, UserStatus.ACTIVE);
    }

    private Notification notification(UUID userId) {
        return new Notification(
                userId,
                NotificationType.DEPOSIT,
                NotificationStatus.UNREAD,
                "Deposit successful",
                "Your wallet deposit was completed successfully.",
                "NTF-" + UUID.randomUUID()
        );
    }
}
