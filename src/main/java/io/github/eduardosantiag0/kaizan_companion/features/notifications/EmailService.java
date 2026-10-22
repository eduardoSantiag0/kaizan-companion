package io.github.eduardosantiag0.kaizan_companion.features.notifications;

import io.github.eduardosantiag0.kaizan_companion.features.analysis.dtos.GamePhase;
import io.github.eduardosantiag0.kaizan_companion.utils.FilenamePattern;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailService {

    private static final String SGF_CONTENT_TYPE = "application/x-go-sgf";

    private final JavaMailSender mailSender;
    private final String supportEmail;
    private final TemplateEngine templateEngine;

    public EmailService(TemplateEngine templateEngine,
                        @Value("${spring.mail.username}")
                        String supportEmail,
                        JavaMailSender mailSender
    ) {
        this.templateEngine = templateEngine;
        this.supportEmail = supportEmail;
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, byte[][] sgfInBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,
                "UTF-8"
        );

        helper.setFrom(supportEmail);

        helper.setTo(to);
        helper.setSubject("Kaizen Companion - Game Analysis");
        helper.setText("Your Go game analysis is ready!");

        helper.addAttachment(
                FilenamePattern.resolveFileName(sgfInBytes[0], GamePhase.opening),
                new ByteArrayResource(sgfInBytes[0]), SGF_CONTENT_TYPE
        );
        helper.addAttachment(
                FilenamePattern.resolveFileName(sgfInBytes[1], GamePhase.middlegame),
                new ByteArrayResource(sgfInBytes[1]), SGF_CONTENT_TYPE
        );
        helper.addAttachment(
                FilenamePattern.resolveFileName(sgfInBytes[2], GamePhase.endgame),
                new ByteArrayResource(sgfInBytes[2]), SGF_CONTENT_TYPE
        );

        mailSender.send(message);
        log.info("SGF analysis email accepted by SMTP server: {}", to);

    }
}
