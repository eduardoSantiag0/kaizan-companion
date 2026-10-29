package io.github.eduardosantiag0.kaizan_companion.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AMQPConfig {

//    O Direct Exchange decide para qual fila a mensagem vai.
//    A Quorum Queue define como essa fila armazena e protege as mensagens.

    private final String EXCHANGE_NAME;
    private final String QUEUE_NAME;
    private final String DLQ_NAME;
    private final String ANALYSIS_CREATED_ROUTING_KEY;

    private final String DEAD_LETTER_EXCHANGE;
    private final String DEAD_LETTER_ROUTING_KEY;

    @Value("${rabbitmq.queue.external.email}")
    private String EMAIL_QUEUE;
    @Value("${rabbitmq.queue.external.email.routing-key}")
    private String EMAIL_ROUTING_KEY;

    @Value("${rabbitmq.queue.external.drive}")
    private String DRIVE_QUEUE;
    @Value("${rabbitmq.queue.external.drive.routing-key}")
    private String DRIVE_ROUTING_KEY;


    @Value("${job.retry.max:3}")
    private int MAX_RETRIES;


    public AMQPConfig(
            @Value("${rabbitmq.exchange.analysis.exchange}")
            String exchangeName,
            @Value("${rabbitmq.queue.analysis}")
            String queueName,
            @Value("${rabbitmq.queue.analysis.dlq}")
            String dlqName,
            @Value("${rabbitmq.exchange.analysis.routing-key}")
            String routingKey,
            @Value("${rabbitmq.dlq.exchange}")
            String DEAD_LETTER_EXCHANGE,
            @Value("${rabbitmq.dlq.routing-key}")
            String DEAD_LETTER_ROUTING_KEY
    ) {
        this.EXCHANGE_NAME = exchangeName;
        this.QUEUE_NAME = queueName;
        this.DLQ_NAME = dlqName;
        this.ANALYSIS_CREATED_ROUTING_KEY = routingKey;
        this.DEAD_LETTER_EXCHANGE=DEAD_LETTER_EXCHANGE;
        this.DEAD_LETTER_ROUTING_KEY = DEAD_LETTER_ROUTING_KEY;
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
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME);
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




    // QUEUE
    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .quorum()
                .deliveryLimit(MAX_RETRIES)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .build();
    }


    @Bean
    public Queue driveQueue() {
        return QueueBuilder.durable(DRIVE_QUEUE)
                .build();
    }

    @Bean
    public Binding bindingQueueToExchange() {
        return BindingBuilder.bind(analysisQueue())
                .to(topicExchange())
                .with(ANALYSIS_CREATED_ROUTING_KEY);
    }

    // BINDING
    @Bean
    public Binding bindingEmailQueueToExchange() {
        return BindingBuilder.bind(emailQueue())
                .to(topicExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding bindingDriveQueueToExchange() {
        return BindingBuilder.bind(driveQueue())
                .to(topicExchange())
                .with(DRIVE_ROUTING_KEY);
    }


    // DLQ

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding bindingDlqToExchange() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(topicExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

//    @Bean
//    public StatelessRetryOperationsInterceptor retryInterceptor() {
//        return RetryInterceptorBuilder.stateless()
//                .maxRetries(3)
//                .backOffOptions(1000, 2.0, 10000)
//                .recoverer(new RejectAndDontRequeueRecoverer())
//                .build();
//    }
//
//    @Bean
//    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
//            ConnectionFactory connectionFactory,
//            SimpleRabbitListenerContainerFactoryConfigurer configurer,
//            RetryOperationsInterceptor retryInterceptor
//    ) {
//        var factory = new SimpleRabbitListenerContainerFactory();
//
//        configurer.configure(factory, connectionFactory);
//
//        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
//        factory.setAdviceChain(retryInterceptor);
//
//        return factory;
//    }

}
