package io.conduit.connectors.file;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import io.conduit.sdk.ConfigUtils;
import io.conduit.sdk.WriteResult;
import io.conduit.sdk.record.Record;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;

@ApplicationScoped
@Slf4j
public class FileDestination implements io.conduit.sdk.Destination {
    private static final Logger logger = Logger.getLogger(FileDestination.class);

    private FileOutputStream stream;
    private FileDestinationConfig cfg;
    private StringBuilder sb;
    private int counter;

    @Override
    public Class<FileDestinationConfig> configClass() {
        return FileDestinationConfig.class;
    }

    @Override
    public void configure(Map<String, String> configMap) {
        this.cfg = ConfigUtils.parse(configMap, configClass());
    }

    @SneakyThrows
    @Override
    public void open() {
        this.stream = new FileOutputStream(cfg.getPath(), true);
        this.counter = 0;
        this.sb = new StringBuilder();
    }

    @Override
    public WriteResult write(List<Record> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new WriteResult(0, null);
        }

        for (int i = 0; i < records.size(); i++) {
            try {
                counter++;

                log.info("adding line to batch, counter is {}, batch size is {}", counter, cfg.getBatchConfig().getSize());

                byte[] bytes = records.get(i).getPayload().getAfter().bytes();
                sb.append(new String(bytes));

                if (counter == cfg.getBatchConfig().getSize()) {
                    log.info("flushing batch");

                    stream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    stream.flush();
                    sb = new StringBuilder();
                    counter = 0;
                }
            } catch (Exception e) {
                logger.error("failed writing record", e);

                return new WriteResult(i, e);
            }
        }

        return new WriteResult(CollectionUtils.size(records), null);
    }

    @SneakyThrows
    @Override
    public void teardown() {
        if (stream != null) {
            stream.close();
        }
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
}
