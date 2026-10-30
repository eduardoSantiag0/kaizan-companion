package io.github.eduardosantiag0.kaizan_companion.config;

import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import io.github.eduardosantiag0.kaizan_companion.features.analysis.messaging.OutboxPublisher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration
public class DebeziumConfig {

    private final OutboxPublisher outboxPublisher;

    private final String hostname;
    private final int port;
    private final String user;
    private final String password;
    private final String dbName;
    private final String slotName;
    private final String topicPrefix;
    private final String tableIncludeList;
    private final String offsetStorageFile;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "debezium-outbox-connector");
        thread.setDaemon(true);
        return thread;
    });

    private DebeziumEngine<?> engine;

    public DebeziumConfig(
            OutboxPublisher outboxPublisher,
            @Value("${debezium.database.hostname}") String hostname,
            @Value("${debezium.database.port}") int port,
            @Value("${debezium.database.user}") String user,
            @Value("${debezium.database.password}") String password,
            @Value("${debezium.database.dbname}") String dbName,
            @Value("${debezium.slot.name}") String slotName,
            @Value("${debezium.topic.prefix}") String topicPrefix,
            @Value("${debezium.table.include.list}") String tableIncludeList,
            @Value("${debezium.offset.storage.file.filename}") String offsetStorageFile
    ) {
        this.outboxPublisher = outboxPublisher;
        this.hostname = hostname;
        this.port = port;
        this.user = user;
        this.password = password;
        this.dbName = dbName;
        this.slotName = slotName;
        this.topicPrefix = topicPrefix;
        this.tableIncludeList = tableIncludeList;
        this.offsetStorageFile = offsetStorageFile;
    }

    @PostConstruct
    public void start() throws IOException {
        Path offsetPath = Path.of(offsetStorageFile);
        if (offsetPath.getParent() != null) {
            Files.createDirectories(offsetPath.getParent());
        }

//        https://debezium.io/documentation/reference/stable/connectors/postgresql.html
        Properties props = new Properties();
        props.setProperty("name", "kaizan-outbox-connector");
        props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");

        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", offsetPath.toString());
        props.setProperty("offset.flush.interval.ms", "0");

        props.setProperty("database.hostname", hostname);
        props.setProperty("database.port", String.valueOf(port));
        props.setProperty("database.user", user);
        props.setProperty("database.password", password);
        props.setProperty("database.dbname", dbName);
        props.setProperty("topic.prefix", topicPrefix);

        props.setProperty("plugin.name", "pgoutput");
        props.setProperty("slot.name", slotName);
        props.setProperty("publication.autocreate.mode", "all_tables");
        props.setProperty("snapshot.mode", "no_data");
        props.setProperty("table.include.list", tableIncludeList);
        props.setProperty("tombstones.on.delete", "false");

        props.setProperty("key.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("value.converter", "org.apache.kafka.connect.json.JsonConverter");
        props.setProperty("key.converter.schemas.enable", "false");
        props.setProperty("value.converter.schemas.enable", "false");

        props.setProperty("bootstrap.servers", "localhost:9092");

        engine = DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(outboxPublisher::handleChangeEvent)
                .build();

        executor.execute(engine);

        log.info("Debezium outbox connector iniciado (slot={}, tabela={})", slotName, tableIncludeList);
    }

    @PreDestroy
    public void stop() throws IOException {
        if (engine != null) {
            engine.close();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
