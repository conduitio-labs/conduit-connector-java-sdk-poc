package com.meroxa.mysql;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class MySQLSourceConfig {
    private String url;
    private String username;
    private String password;
    private String table;
}
