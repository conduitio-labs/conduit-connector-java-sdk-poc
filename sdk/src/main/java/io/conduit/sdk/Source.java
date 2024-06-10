package io.conduit.sdk;

import java.util.Map;

import io.conduit.sdk.record.Position;
import io.conduit.sdk.record.Record;

// Source fetches records from 3rd party resources and sends them to Conduit.
public interface Source extends Configurable {

    /**
     * Configure is the first function to be called in a connector. It provides the
     * connector with the configuration that needs to be validated and stored.
     * In case the configuration is not valid it should return an error.
     * Testing if your connector can reach the configured data source should be
     * done in Open, not in Configure.
     */
    void configure(Map<String, String> configMap);

    // Open is called after Configure to signal the plugin it can prepare to
    // start producing records. If needed, the plugin should open connections in
    // this function. The position parameter will contain the position of the
    // last record that was successfully processed, Source should therefore
    // start producing records after this position. The context passed to Open
    // will be cancelled once the plugin receives a stop signal from Conduit.
    void open(Position position);

    // Read returns a new Record and is supposed to block until there is either
    // a new record or the context gets cancelled. It can also return the error
    // ErrBackoffRetry to signal to the SDK it should call Read again with a
    // backoff retry.
    // If Read receives a cancelled context or the context is cancelled while
    // Read is running it must stop retrieving new records from the source
    // system and start returning records that have already been buffered. If
    // there are no buffered records left Read must return the context error to
    // signal a graceful stop. If Read returns ErrBackoffRetry while the context
    // is cancelled it will also signal that there are no records left and Read
    // won't be called again.
    // After Read returns an error the function won't be called again (except if
    // the error is ErrBackoffRetry, as mentioned above).
    // Read can be called concurrently with Ack.
    Record read();

    // Ack signals to the implementation that the record with the supplied
    // position was successfully processed. This method might be called after
    // the context of Read is already cancelled, since there might be
    // outstanding acks that need to be delivered. When Teardown is called it is
    // guaranteed there won't be any more calls to Ack.
    // Ack can be called concurrently with Read.
    void ack(Position position);

    // Teardown signals to the plugin that there will be no more calls to any
    // other function. After Teardown returns, the plugin should be ready for a
    // graceful shutdown.
    void teardown();

    // -- Lifecycle events -----------------------------------------------------

    // LifecycleOnCreated is called after Configure and before Open when the
    // connector is run for the first time. This call will be skipped if the
    // connector was already started before. This method can be used to do some
    // initialization that needs to happen only once in the lifetime of a
    // connector (e.g. create a logical replication slot). Anything that the
    // connector creates in this method is considered to be owned by this
    // connector and should be cleaned up in LifecycleOnDeleted.
    void lifecycleOnCreated(Map<String, String> config);

    // LifecycleOnUpdated is called after Configure and before Open when the
    // connector configuration has changed since the last run. This call will be
    // skipped if the connector configuration did not change. It can be used to
    // update anything that was initialized in LifecycleOnCreated, in case the
    // configuration change affects it.
    void lifecycleOnUpdated(Map<String, String> configBefore, Map<String, String> configAfter);

    // LifecycleOnDeleted is called when the connector was deleted. It will be
    // the only method that is called in that case. This method can be used to
    // clean up anything that was initialized in LifecycleOnCreated.
    void lifecycleOnDeleted(Map<String, String> config);
}
