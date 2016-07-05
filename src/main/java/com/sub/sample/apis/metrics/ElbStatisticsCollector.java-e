package com.sub.sample.apis.metrics;

import static com.google.common.base.Preconditions.*;

import java.lang.management.RuntimeMXBean;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vijay.daniel
 *
 */
@ThreadSafe
@ParametersAreNonnullByDefault
public class ElbStatisticsCollector {

   private static final Logger LOGGER = LoggerFactory.getLogger(ElbStatisticsCollector.class);

   private static final long MILLIS_PER_SECOND = 1000L;
   private static final int HUNDRED_PERCENT = 100; // Hate you checkstyle :P

   private final StatisticsHandler jettyStatisticsHandler;
   private final QueuedThreadPool jettyThreadPool;
   private final RuntimeMXBean runtimeMXBean;
   private final int nonWorkerThreads;
   private final int maxQueueSize;
   private int totalCapacity;

   public ElbStatisticsCollector(StatisticsHandler jettyStatisticsHandler, QueuedThreadPool jettyThreadPool,
         RuntimeMXBean runtimeMXBean, int nonWorkerThreads, int maxQueueSize) {

      checkNotNull(jettyStatisticsHandler);
      checkNotNull(jettyThreadPool);
      checkNotNull(runtimeMXBean);

      this.jettyStatisticsHandler = jettyStatisticsHandler;
      this.jettyThreadPool = jettyThreadPool;
      this.runtimeMXBean = runtimeMXBean;
      this.nonWorkerThreads = nonWorkerThreads;
      this.maxQueueSize = maxQueueSize;

      onPostConstruction();
   }

   public long getUptimeInSeconds() {

      return runtimeMXBean.getUptime() / MILLIS_PER_SECOND;
   }

   public long getTotalRequestsSinceUptime() {

      return jettyStatisticsHandler.getRequests();
   }

   /**
    * Currently capacity is the percentage of free threads and free blocking
    * queue space in the jetty thread pool. This will typically never return
    * 100% as one thread will always be used to service the elb-healthcheck
    * request
    * 
    * @return ()
    */
   public long getCapacity() {

      if (jettyThreadPool.isLowOnThreads()) {

         LOGGER.warn("Jetty worker threadPool is low on threads");
         return 0L;
      }

      // Total Threads = Acceptors + Selectors + Worker Threads
      // Acceptors and Selectors will always be busy. So, we'll ignore them
      int activeRequests = jettyStatisticsHandler.getRequestsActive();
      // Assuming that each acceptor and selector eats up one slot each
      int pendingWorkerRequests = withoutNonWorkers(jettyThreadPool.getQueueSize());

      int usedCapacityPercent = ((activeRequests + pendingWorkerRequests) * HUNDRED_PERCENT) / totalCapacity;
      return Math.max(HUNDRED_PERCENT - usedCapacityPercent, 0);
   }

   private void onPostConstruction() {

      LOGGER.info("Initializing... Validating ElbStatisticsCollector parameters.");
      int totalWorkerThreads = withoutNonWorkers(jettyThreadPool.getMaxThreads());
      if (totalWorkerThreads == 0) {

         String message = "The total worker threads [(maxThreads-nonWorkerThreads)] "
               + "is 0. There seems to be an error in your configuration";
         LOGGER.error(message);
         throw new IllegalStateException(message);
      }
      int totalQueueSize = withoutNonWorkers(maxQueueSize);
      totalCapacity = totalWorkerThreads + totalQueueSize;
      LOGGER.info("Total worker threads: {}, Total queue size for workers: {}", totalWorkerThreads, totalQueueSize);
   }

   private int withoutNonWorkers(int value) {

      return Math.max(value - nonWorkerThreads, 0);
   }
}
