package io.conduit.sdk;

import java.util.List;
import java.util.Map;

// Destination receives records from Conduit and writes them to 3rd party
// resources.
public interface Destination {
    // Parameters is a map of named Parameters that describe how to configure
    // the Destination.
    Map<String, Parameter> parameters();

    // Configure is the first function to be called in a connector. It provides the
    // connector with the configuration that needs to be validated and stored.
    // In case the configuration is not valid it should return an error.
    // Testing if your connector can reach the configured data source should be
    // done in Open, not in Configure.
    void configure(Map<String, String> configMap);

    // Open is called after Configure to signal the plugin it can prepare to
    // start writing records. If needed, the plugin should open connections in
    // this function.
    void open();

    // Write writes len(r) records from r to the destination right away without
    // caching. It should return the number of records written from r
    // (0 <= n <= len(r)) and any error encountered that caused the write to
    // stop early. Write must return a non-nil error if it returns n < len(r).
    WriteResult write(List<Record> records);

    // Teardown signals to the plugin that all records were written and there
    // will be no more calls to any other function. After Teardown returns, the
    // plugin should be ready for a graceful shutdown.
    void teardown();

    // -- Lifecycle events -----------------------------------------------------

    // lifecycleOnCreated is called after Configure and before Open when the
    // connector is run for the first time. This call will be skipped if the
    // connector was already started before. This method can be used to do some
    // initialization that needs to happen only once in the lifetime of a
    // connector (e.g. create a bucket). Anything that the connector creates in
    // this method is considered to be owned by this connector and should be
    // cleaned up in lifecycleOnDeleted.
    void lifecycleOnCreated(Map<String, String> config);

    // lifecycleOnUpdated is called after Configure and before Open when the
    // connector configuration has changed since the last run. This call will be
    // skipped if the connector configuration did not change. It can be used to
    // update anything that was initialized in lifecycleOnCreated, in case the
    // configuration change affects it.
    void lifecycleOnUpdated(Map<String, String> configBefore, Map<String, String> configAfter);

    // lifecycleOnDeleted is called when the connector was deleted. It will be
    // the only method that is called in that case. This method can be used to
    // clean up anything that was initialized in lifecycleOnCreated.
    void lifecycleOnDeleted(Map<String, String> config);
}
