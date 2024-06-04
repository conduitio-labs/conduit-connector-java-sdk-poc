package io.conduit.sdk;

import com.google.protobuf.ByteString;
import io.conduit.sdk.record.Position;

public class Utils {
    public static Position newPosition(ByteString position) {
        if (position == null) {
            return null;
        }

        return position::toByteArray;
    }
}
