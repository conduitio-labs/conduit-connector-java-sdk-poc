package io.conduit.sdk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

@AllArgsConstructor
@Getter
public class WriteResult {
    private final int written;
    private final Exception error;

    public String getErrorString() {
        if (getError() == null) {
            return "";
        }
        return getError().getMessage() + "\n" + ExceptionUtils.getStackTrace(getError());
    }
}
