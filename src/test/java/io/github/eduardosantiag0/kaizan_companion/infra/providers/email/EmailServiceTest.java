package io.github.eduardosantiag0.kaizan_companion.infra.providers.email;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging.EmailNotificationEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Test
    void sendEmail_mustPropagateAFailure_insteadOfSwallowingItAndReturningNormally() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));

        JavaMailSender mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        TemplateEngine templateEngine = mock(TemplateEngine.class);
        when(templateEngine.process(any(String.class), any(Context.class))).thenReturn("<html></html>");

        Channel channel = mock(Channel.class);
        long deliveryTag = 12345L;

        EmailService emailService = new EmailService(templateEngine, "support@kaizen.test", mailSender);

        EmailNotificationEvent event = new EmailNotificationEvent(
                "\"unterminated", new byte[3][], "Alice vs Bob", "tester");

        assertThrows(Exception.class, () -> emailService.sendEmail(event, channel, deliveryTag),
                "a malformed recipient address was silently swallowed instead of being "
                        + "reported, so EmailQueueListener will ack a delivery whose email was never sent");
    }
}
