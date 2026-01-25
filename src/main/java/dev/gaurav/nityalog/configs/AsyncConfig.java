package dev.gaurav.nityalog.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 *
 * <h3>Configuration class to enable Spring's asynchronous method execution.</h3>
 *
 * <p>With @EnableAsync, any method annotated with @Async will run in a separate
 * thread instead of blocking the caller thread.
 *
 * <p>This is useful for:
 * <ul>
 *   <li>Sending emails</li>
 *   <li>Calling external services</li>
 *   <li>Long-running or non-blocking operations</li>
 * </ul>
 *
 * <p><b>Note:</b> Without this configuration, {@code @Async} annotations
 * are ignored and all methods execute synchronously.
 *
 * <p>By default, Spring uses a simple task executor unless a custom
 * {@link java.util.concurrent.Executor Executor} bean is defined.
 *
 */

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
