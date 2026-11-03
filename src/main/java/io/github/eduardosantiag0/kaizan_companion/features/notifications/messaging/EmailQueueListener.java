package io.github.eduardosantiag0.kaizan_companion.features.notifications.messaging;

import com.rabbitmq.client.Channel;
import io.github.eduardosantiag0.kaizan_companion.infra.providers.email.EmailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class EmailQueueListener {

    private final EmailService emailService;

    public EmailQueueListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(
            queues = "${rabbitmq.queue.external.email}",
            ackMode = "MANUAL"
    )
    public void listen(
            @Payload EmailNotificationEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {


        try {
            emailService.sendEmail(event, channel, deliveryTag);
        }  catch (MessagingException e) {
            // Falha ao montar a mensagem (endereço inválido, anexos, template)
            log.error("Falha ao montar o e-mail de análise para {}: {}", event.to(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);

        }
        catch (MailAuthenticationException e) {
            log.error("Falha de autenticação SMTP ao enviar e-mail para {}", event.to(), e);
            channel.basicNack(deliveryTag, false, false);

        }
        catch (MailException e) {
            // Falha ao falar com o servidor SMTP (conexão, timeout) — transitória.
            log.error("Falha ao enviar e-mail de análise para {}: {}", event.to(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);

        }
        catch (RuntimeException e) {
            log.error("Falha inesperada ao enviar e-mail de análise para {}", event.to(), e);
            channel.basicNack(deliveryTag, false, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
