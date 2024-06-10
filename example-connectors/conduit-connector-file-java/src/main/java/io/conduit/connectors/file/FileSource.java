package io.conduit.connectors.file;

import java.util.Map;

import io.conduit.sdk.record.Position;
import io.conduit.sdk.record.Record;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileSource implements io.conduit.sdk.Source {
    @Override
    public void configure(Map<String, String> configMap) {

    }

    @Override
    public void open(Position position) {

    }

    @Override
    public Record read() {
        return null;
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
    public Class<Object> configClass() {
        return null;
    }
}
