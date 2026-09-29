package io.github.eduardosantiag0.kaizan_companion.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "telegram_chat_db")
@Getter
public class TelegramChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "first_name", nullable = false)
    private String firstName;


    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "type")
    private String type;

    @Column(name = "ogs_account_name")
    private String ogsAccountName;

    @Column(name = "email")
    private String email;

    @Column(name = "waiting_for_analysis")
    private boolean waitingForAnalysis;
}
