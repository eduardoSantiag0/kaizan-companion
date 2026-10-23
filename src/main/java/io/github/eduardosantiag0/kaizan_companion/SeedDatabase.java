package io.github.eduardosantiag0.kaizan_companion;

import io.github.eduardosantiag0.kaizan_companion.domain.entities.TelegramChatEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.entities.UserInfoEntity;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.TelegramChatRepository;
import io.github.eduardosantiag0.kaizan_companion.domain.repositories.UserInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDatabase implements ApplicationRunner {

    @Value("${PERSONAL_OGS}")
    private String ogsAccount;

    @Value("${PERSONAL_OGS_ID}")
    private Long ogsId;


    @Value("${PERSONAL_EMAIL}")
    private String email;

    private final TelegramChatRepository chatRepository;

    private final UserInfoRepository userInfoRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean isDataBasEmpty = chatRepository.count() == 0;

        if (isDataBasEmpty) {
            createChatSeed();
        }
    }

    @Profile("dev")
    @Transactional
    public void createChatSeed() {
        log.info("\nExecuting development seed...");

        TelegramChatEntity chatEntity = new TelegramChatEntity(
                4923L, "du", "dudu", "private", false
        );

        chatEntity = chatRepository.save(chatEntity);

        UserInfoEntity userInfo = new UserInfoEntity(
                chatEntity, ogsAccount, ogsId, email
        );

        userInfoRepository.save(userInfo);

        log.debug("Criado o perfil: " + userInfo.toString());

    }
}
