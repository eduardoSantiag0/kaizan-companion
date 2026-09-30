package io.github.eduardosantiag0.kaizan_companion.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "telegram_chat_db")
@Getter
@AllArgsConstructor
public class TelegramChatEntity {

    public TelegramChatEntity() {
    }

    @Id
    @Column(name = "id")
    private Long chatId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "type")
    private String type;

    @Setter
    @Column(name = "ogs_account_name")
    private String ogsAccountName;

    @Setter
    @Column(name = "email")
    private String email;

    @Setter
    @Column(name = "waiting_for_analysis")
    private boolean waitingForAnalysis;

}
