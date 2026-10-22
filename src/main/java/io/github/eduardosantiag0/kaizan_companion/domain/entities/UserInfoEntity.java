package io.github.eduardosantiag0.kaizan_companion.domain.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChatEntity chat;

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
    }
}
