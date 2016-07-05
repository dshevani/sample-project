package com.sub.sample.apis.logger;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class LogbackFlusher {

   private static final Logger LOGGER = LoggerFactory.getLogger(LogbackFlusher.class);

   // Since our file appender is running in async mode, we have to ask it to
   // shut down explicitly
   @PreDestroy
   public final void flushLogs() {

      LOGGER.info("Shutting down logger context");

      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      loggerContext.stop();
   }

}
