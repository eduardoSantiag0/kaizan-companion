package io.github.eduardosantiag0.kaizan_companion.domain.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_info_db")
@Getter
@AllArgsConstructor
public class UserInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter
    private UUID id;

    @OneToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChatEntity chat;

    @Column(name = "telegram_username", nullable = false)
    private String telegramUsername;

    @Setter
    @Column(name = "ogs_account_name")
    private String ogsAccountName;

    @Setter
    @Column(name = "ogs_account_id")
    private Long ogsAccountId;


    @Setter
    @Column(name = "email_address")
    private String emailAddress;


    public UserInfoEntity() {
    }

    public UserInfoEntity(TelegramChatEntity chat) {
        this.chat = chat;
        this.telegramUsername = chat.getUsername();
    }

    public UserInfoEntity(TelegramChatEntity chat, String ogsAccountName, Long ogsAccountId, String emailAddress) {
        this.chat = chat;
        this.ogsAccountName = ogsAccountName;
        this.ogsAccountId = ogsAccountId;
        this.telegramUsername = chat.getUsername();
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return """
            %s Username: %s
            %s Email: %s
            %s OGS Account: %s
            """.formatted(
                telegramUsername != null ? "◉" : "○",
                Objects.toString(telegramUsername, "Not linked"),

                emailAddress != null ? "◉" : "○",
                Objects.toString(emailAddress, "Not linked"),

                ogsAccountName != null ? "◉" : "○",
                Objects.toString(ogsAccountName, "Not linked")
        );
    }
}
