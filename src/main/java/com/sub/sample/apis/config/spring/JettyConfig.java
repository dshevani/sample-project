package com.sub.sample.apis.config.spring;

import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;

import javax.inject.Inject;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.sub.sample.apis.jetty.AggregatedLifeCycleListener;
import com.sub.sample.apis.jetty.CustomJettyEmbeddedServletContainerFactory;
import com.sub.sample.apis.jetty.JettyServerCustomizerImpl;
import com.sub.sample.apis.jetty.ServerBeanRegistrar;
import com.sub.sample.apis.metrics.ElbStatisticsCollector;
import com.google.common.collect.ImmutableList;

@Configuration
@Import({ EnvironmentConfig.class, HealthCheckConfig.class })
public class JettyConfig {

   @Value("${jetty.workerThreads}")
   private int workerThreads;

   @Value("${jetty.blockingQueueSize}")
   private int blockingQueueSize;

   @Value("${jetty.acceptQueueSize}")
   private int acceptQueueSize;

   @Value("${jetty.acceptors}")
   private int acceptors;

   @Value("${jetty.selectors}")
   private int selectors;

   @Value("${jetty.workerThreadIdleTimeoutMs}")
   private int workerThreadIdleTimeoutMs;

   @Value("${jetty.serverPort}")
   private int serverPort;

   @Inject
   private EnvironmentConfig environmentConfig;

   @Inject
   private HealthCheckConfig healthCheckConfig;

   /**
    * NOTE: This thread pool is shared between both SSL and non-SSL connections.
    * Both have the same number of acceptors, selectors and share the same
    * thread pool. Within a connector, the same thread pool is used for
    * acceptors, selectors and handler threads.
    * 
    * If you're using SSL connections, double the number of non-worker threads
    * in ElbStatisticsConnector.
    * 
    * If you want to use a different thread pool for SSL and non-SSL
    * connections, create another thread pool and customize
    * CustomJettyEmbeddedServletContainerFactory. Also, don't forget to update
    * ElbStatisticsCollector in that case
    * 
    * @return
    */
   @Bean
   public QueuedThreadPool jettyThreadPool() {

      int totalThreads = acceptors + selectors + workerThreads;
      BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(blockingQueueSize);
      QueuedThreadPool pool = new InstrumentedQueuedThreadPool(environmentConfig.metricRegistry(), totalThreads,
            totalThreads, workerThreadIdleTimeoutMs, queue);
      pool.setName("jetty");
      return pool;
   }

   @Bean
   public CustomJettyEmbeddedServletContainerFactory embeddedServletContainerFactory() {

      return new CustomJettyEmbeddedServletContainerFactory(jettyThreadPool(), acceptors, selectors, acceptQueueSize,
            serverPort);
   }

   @Bean
   public StatisticsHandler jettyStatisticsHandler() {

      // http://www.eclipse.org/jetty/documentation/current/statistics-handler.html
      return new StatisticsHandler();
   }

   @Bean(name = "elbStatisticsCollector")
   public ElbStatisticsCollector elbStatisticsCollector() {

      // NOTE: Multiply this by 2 in case you're using SSL connections
      // TODO: Detect and do this automatically
      int nonWorkerThreads = acceptors + selectors;
      return new ElbStatisticsCollector(jettyStatisticsHandler(), jettyThreadPool(),
            ManagementFactory.getRuntimeMXBean(), nonWorkerThreads, blockingQueueSize);
   }

   @Bean
   public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {

      // http://metrics.dropwizard.io/3.1.0/manual/jetty/
      InstrumentedHandler jettyInstrumentedHandler = new InstrumentedHandler(environmentConfig.metricRegistry(),
            "jetty");
      MBeanContainer jettyMBeanContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
      AggregatedLifeCycleListener lifeCycleListener = new AggregatedLifeCycleListener(
            healthCheckConfig.masterHealthCheck());
      ServerBeanRegistrar serverBeanRegistrar = new ServerBeanRegistrar(lifeCycleListener);

      JettyServerCustomizer serverCustomizer = new JettyServerCustomizerImpl(
            ImmutableList.of(jettyInstrumentedHandler, jettyStatisticsHandler()), jettyMBeanContainer,
            serverBeanRegistrar);

      return container -> ((CustomJettyEmbeddedServletContainerFactory) container)
            .addServerCustomizers(serverCustomizer);
   }
}
