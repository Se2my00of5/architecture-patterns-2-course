package ru.hits.core_service.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.hits.core_service.entity.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (balance == null) {
            balance = 0L;
        }
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }
}
