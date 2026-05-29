package com.annotation.infrastructure.nlp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures the dedicated executor used for long-running NLP scripts.
 */
@Configuration
public class NlpAsyncConfig {

    /**
     * Builds the bounded NLP executor to keep Python processes from saturating the global Spring pool.
     *
     * @return task executor limited to two concurrent NLP jobs and ten queued jobs
     */
    @Bean(name = "nlpExecutor")
    public Executor nlpExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("nlp-run-");
        executor.initialize();
        return executor;
    }
}
