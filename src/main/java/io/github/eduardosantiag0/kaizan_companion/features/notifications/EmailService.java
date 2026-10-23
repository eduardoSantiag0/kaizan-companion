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
    private static final GamePhase[] PHASES = {GamePhase.opening, GamePhase.middlegame, GamePhase.endgame};

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

    public void sendEmail(String to, byte[][] sgfInBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setFrom(supportEmail);

            helper.setTo(to);
            helper.setSubject("Kaizen Companion - Game Analysis");

            Context context = new Context();
            context.setVariable("title", "Game Analysis Complete");
            context.setVariable("message", "Your SGF files are attached");

            String html = templateEngine.process(
                    "email-with-file",
                    context
            );

            helper.setText(html, true);

            for (int i = 0; i < sgfInBytes.length; i++) {
                byte[] sgf = sgfInBytes[i];
                if (sgf == null) {
                    continue;
                }
                helper.addAttachment(
                        FilenamePattern.resolveFileName(sgf, PHASES[i]),
                        new ByteArrayResource(sgf), SGF_CONTENT_TYPE
                );
            }

            mailSender.send(message);
            log.info("SGF analysis email accepted by SMTP server: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send SGF analysis email to {}: {}", to, e.getMessage());
        }
    }
}
