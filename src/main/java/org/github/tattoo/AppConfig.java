package org.github.tattoo;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ComponentScan(basePackages = "org.github.tattoo")
public class AppConfig {
  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler ret = new ThreadPoolTaskScheduler();
    ret.setThreadFactory(Thread::new);
    ret.setPoolSize(2);
    ret.setThreadNamePrefix("TournamentThread");
    ret.setErrorHandler(t -> LoggerFactory.getLogger(AppConfig.class).error("Tournament failed", t));
    return ret;
  }
}
