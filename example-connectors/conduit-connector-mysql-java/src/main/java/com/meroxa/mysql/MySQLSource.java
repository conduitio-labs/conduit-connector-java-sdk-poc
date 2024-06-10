package com.meroxa.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Map;

import io.conduit.sdk.ConfigUtils;
import io.conduit.sdk.Source;
import io.conduit.sdk.record.Position;
import io.conduit.sdk.record.Record;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class MySQLSource implements Source {
    private MySQLSourceConfig cfg;
    private Connection connection;
    private final LinkedList<Record> buffer = new LinkedList<>();
    private final LocalDateTime lastUpdatedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    @Override
    public void configure(Map<String, String> configMap) {
        cfg = ConfigUtils.parse(configMap, MySQLSourceConfig.class);
    }

    @SneakyThrows
    @Override
    public void open(Position position) {
        connection = DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
    }


    @Override
    public Record read() {
        if (buffer.isEmpty()) {
            fillBuffer();
        }

        return buffer.pollFirst();
    }

    @SneakyThrows
    private void fillBuffer() {
        String query = String.format("SELECT * FROM %s WHERE last_updated_at > ?", cfg.getTable());
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, DateTimeFormatter.ISO_DATE_TIME.format(lastUpdatedAt));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    // Process each row
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    LocalDateTime lastUpdated = resultSet.getTimestamp("last_updated_at").toLocalDateTime();
                    log.info("ID: {}, Name: {}, Last Updated: {}", id, name, lastUpdated);
                }
            }
        }
    }

    @Override
    public void ack(Position position) {
        log.info("ack {}", position);
    }

    @SneakyThrows
    @Override
    public void teardown() {
        connection.close();
    }

    @Override
    public void lifecycleOnCreated(Map<String, String> config) {

    }

    @Override
    public void lifecycleOnUpdated(Map<String, String> configBefore, Map<String, String> configAfter) {

    }

    @Override
    public void lifecycleOnDeleted(Map<String, String> config) {

    }

    @Override
    public Class configClass() {
        return MySQLSourceConfig.class;
    }
}
