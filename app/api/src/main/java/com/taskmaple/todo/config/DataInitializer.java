package com.taskmaple.todo.config;

import com.taskmaple.todo.infrastructure.support.SeedService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * On boot, guarantees the default "General" folder exists. When {@code todo.seed=true}
 * (default), also loads the sample dataset so a fresh in-memory instance mirrors the wireframe.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final SeedService seed;
    private final boolean seedEnabled;

    public DataInitializer(SeedService seed,
                           @org.springframework.beans.factory.annotation.Value("${todo.seed:true}") boolean seedEnabled) {
        this.seed = seed;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (seedEnabled) {
            seed.seedSampleIfEmpty();
        } else {
            seed.ensureDefault();
        }
    }
}
