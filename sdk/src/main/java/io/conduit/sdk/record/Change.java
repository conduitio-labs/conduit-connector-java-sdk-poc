package io.conduit.sdk.record;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Change {
    private final Data before;
    private final Data after;
}
