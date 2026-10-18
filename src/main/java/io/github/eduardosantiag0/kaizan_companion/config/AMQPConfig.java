package io.github.eduardosantiag0.kaizan_companion.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;


@Configuration
public class AMQPConfig {

//    O Direct Exchange decide para qual fila a mensagem vai.
//    A Quorum Queue define como essa fila armazena e protege as mensagens.

    public final String EXCHANGE_NAME;
    public final String QUEUE_NAME;
    public final String DLQ_NAME;
    public final String ROUTING_KEY;

    public AMQPConfig(
            @Value("${rabbitmq.exchange.analysis.exchange}")
            String exchangeName,
            @Value("${rabbitmq.exchange.analysis.queue}")
            String queueName,
            @Value("${rabbitmq.exchange.analysis.dlq}")
            String dlqName,
            @Value("${rabbitmq.exchange.analysis.routing-key}")
            String routingKey
    ) {
        EXCHANGE_NAME = exchangeName;
        QUEUE_NAME = queueName;
        DLQ_NAME = dlqName;
        ROUTING_KEY = routingKey;
    }


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
    public Queue analysisQueue(
            @Value("${rabbitmq.exchange.analysis.queue}") String queueName,
            @Value("${job.retry.max:3}") int maxRetries
    ) {
        return QueueBuilder.durable(queueName)
                .quorum()
                .deliveryLimit(maxRetries)
                .deadLetterExchange("analysis.dlx")
                .deadLetterRoutingKey("analysis.failed")
                .build();
    }

    @Bean
    public Binding bindingQueueToExchange() {
        return BindingBuilder.bind(analysisQueue())
                .to(directExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.findAndRegisterModules();

        objectMapper.setVisibility(
                PropertyAccessor.FIELD,
                JsonAutoDetect.Visibility.ANY
        );

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
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

    @Bean
    public StatelessRetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxRetries(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            RetryOperationsInterceptor retryInterceptor
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();

        configurer.configure(factory, connectionFactory);

        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setAdviceChain(retryInterceptor);

        return factory;
    }

}
