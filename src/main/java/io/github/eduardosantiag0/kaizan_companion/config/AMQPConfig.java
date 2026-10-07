package io.github.eduardosantiag0.kaizan_companion.config;

import lombok.Getter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;


@Configuration
public class AMQPConfig {

//    O Direct Exchange decide para qual fila a mensagem vai.
//    A Quorum Queue define como essa fila armazena e protege as mensagens.

    public final String EXCHANGE_NAME = "analysis.exchange";
    public final String QUEUE_NAME = "analysis-queue";
    public final String DLQ_NAME = "analysis.dlq";
    public final String ROUTING_KEY = "analysis-report";


    @Bean
    public RabbitAdmin createRabbitAdmin(ConnectionFactory conn) {
        return new RabbitAdmin(conn);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> initializeAdmin(RabbitAdmin rabbitAdmin){
        return event -> rabbitAdmin.initialize();
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // Reference https://oneuptime.com/blog/post/2026-01-24-configure-rabbitmq-quorum-queues/view
    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .quorum()  // Sets x-queue-type to quorum
                .deliveryLimit(5)  // Maximum redelivery attempts
                .build();
    }
    @Bean
    public Binding bindingQueueToExchange() {
        return BindingBuilder.bind(analysisQueue())
                .to(directExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter(){
        JsonMapper objectMapper = new JsonMapper();
        objectMapper.registeredModules();
        return new JacksonJsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding bindingDlqToExchange() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(directExchange())
                .with(ROUTING_KEY + ".dlq");
    }
}
