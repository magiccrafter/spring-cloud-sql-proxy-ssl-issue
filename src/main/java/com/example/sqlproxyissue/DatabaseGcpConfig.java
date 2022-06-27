package com.example.sqlproxyissue;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.google.cloud.sql.core.GcpConnectionFactoryProvider.ENABLE_IAM_AUTH;
import static io.r2dbc.pool.PoolingConnectionFactoryProvider.*;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

/**
 * @author vasilevn
 */
@RequiredArgsConstructor
@Configuration
@Profile("gcp")
public class DatabaseGcpConfig {

    // TODO: replace
    private static final String IAM_USER = "TODO";
    private final R2dbcProperties r2dbcProperties;


    @Bean
    public ConnectionFactory connectionFactory() {
        var options =
                ConnectionFactoryOptions.parse(r2dbcProperties.getUrl())
                .mutate()
                .option(USER, IAM_USER)
                .option(PASSWORD, "not_used_when_iam_auth_is_enabled_but_cannot_be_empty")
                .option(ENABLE_IAM_AUTH, true)
                .option(MAX_SIZE, r2dbcProperties.getPool().getMaxSize())
                .option(INITIAL_SIZE, r2dbcProperties.getPool().getInitialSize())
                .option(MAX_IDLE_TIME, r2dbcProperties.getPool().getMaxIdleTime())
                .option(MAX_LIFE_TIME, r2dbcProperties.getPool().getMaxLifeTime())
                .build();

        ConnectionFactory connectionFactory = ConnectionFactories.get(options);
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .build();
        return new ConnectionPool(configuration);
    }
}
