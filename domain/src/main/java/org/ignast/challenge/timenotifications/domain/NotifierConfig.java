package org.ignast.challenge.timenotifications.domain;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifierConfig {

    @Bean
    ConcurrentLinkedQueue<AlterSubscriptions> pipe() {
        return new ConcurrentLinkedQueue<>();
    }

    @Bean
    ExecutorService notifierExecutor(final NotifierActor notifier) {
        val singleThreadExecution = Executors.newSingleThreadExecutor();
        singleThreadExecution.submit(notifier);
        return singleThreadExecution;
    }
}
