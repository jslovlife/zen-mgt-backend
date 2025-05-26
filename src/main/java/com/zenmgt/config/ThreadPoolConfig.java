package com.zenmgt.config;

import com.zenmgt.util.TraceIdUtil;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * Custom thread factory for creating named threads
     */
    @Bean
    public ThreadFactory customThreadFactory() {
        return new CustomThreadFactory("ZenMgt-Thread");
    }

    /**
     * Task decorator that propagates MDC context (including trace IDs) to async threads
     */
    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return new TaskDecorator() {
            @Override
            public Runnable decorate(Runnable runnable) {
                // Capture MDC context from the current thread
                Map<String, String> contextMap = MDC.getCopyOfContextMap();
                return () -> {
                    try {
                        // Set the captured MDC context in the async thread
                        if (contextMap != null) {
                            MDC.setContextMap(contextMap);
                        }
                        runnable.run();
                    } finally {
                        // Clean up MDC in the async thread
                        MDC.clear();
                    }
                };
            }
        };
    }

    /**
     * Main task executor for async operations with MDC propagation
     */
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ZenMgt-Async-");
        executor.setThreadFactory(customThreadFactory());
        executor.setTaskDecorator(mdcTaskDecorator()); // Add MDC propagation
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Security-aware task executor for operations that need security context and MDC
     */
    @Bean(name = "securityTaskExecutor")
    public TaskExecutor securityTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ZenMgt-Security-");
        executor.setThreadFactory(customThreadFactory());
        executor.setTaskDecorator(mdcTaskDecorator()); // Add MDC propagation
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        
        // Wrap with security context delegation
        return new DelegatingSecurityContextTaskExecutor(executor);
    }

    /**
     * MFA-specific task executor for MFA operations with MDC propagation
     */
    @Bean(name = "mfaTaskExecutor")
    public TaskExecutor mfaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("ZenMgt-MFA-");
        executor.setThreadFactory(customThreadFactory());
        executor.setTaskDecorator(mdcTaskDecorator()); // Add MDC propagation
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Custom thread factory implementation
     */
    public static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final ThreadGroup group;

        public CustomThreadFactory(String namePrefix) {
            this.group = Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            
            // Set thread properties
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            
            // Add uncaught exception handler
            t.setUncaughtExceptionHandler((thread, ex) -> {
                System.err.println("Uncaught exception in thread " + thread.getName() + ": " + ex.getMessage());
                ex.printStackTrace();
            });
            
            return t;
        }
    }
} 