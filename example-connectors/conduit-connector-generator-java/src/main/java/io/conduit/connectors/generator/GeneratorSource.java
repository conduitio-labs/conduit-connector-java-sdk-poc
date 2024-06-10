package io.conduit.connectors.generator;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import io.conduit.sdk.Source;
import io.conduit.sdk.record.Change;
import io.conduit.sdk.record.Position;
import io.conduit.sdk.record.Record;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeneratorSource implements Source {
    @Override
    public void configure(Map<String, String> configMap) {

    }

    @Override
    public void open(Position position) {

    }

    @Override
    public Record read() {
        String uuid = UUID.randomUUID().toString();
        Change payload = new Change(
            null,
            () -> ("position-" + uuid).getBytes(StandardCharsets.UTF_8)
        );
        return Record.builder()
            .key(() -> ("key-" + uuid).getBytes(StandardCharsets.UTF_8))
            .position(() -> ("position-" + uuid).getBytes(StandardCharsets.UTF_8))
            .payload(payload)
            .build();
    }

    @Override
    public void ack(Position position) {

    }

    @Override
    public void teardown() {

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
        return null;
    }
}
