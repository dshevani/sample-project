package com.sub.sample.apis.metrics;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.lang.management.RuntimeMXBean;

import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sub.sample.apis.metrics.ElbStatisticsCollector;

import static org.mockito.Mockito.*;

/**
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ElbStatisticsCollectorTest {

   private static final int MAX_QUEUE_SIZE = 6;
   private static final int NON_WORKER_THREADS = 4;
   private static final int MAX_THREADS = 10;

   @Mock
   private RuntimeMXBean runtimeMXBean;

   @Mock
   private StatisticsHandler statisticsHandler;

   @Mock
   private QueuedThreadPool threadPool;

   private ElbStatisticsCollector collector;

   @Before
   public void setUp() {

      when(threadPool.getMaxThreads()).thenReturn(MAX_THREADS);

      collector = new ElbStatisticsCollector(statisticsHandler, threadPool, runtimeMXBean, NON_WORKER_THREADS,
            MAX_QUEUE_SIZE);
   }

   @Test
   public void shouldReturnUptimeInSeconds() {

      when(runtimeMXBean.getUptime()).thenReturn(101900L);

      assertThat(collector.getUptimeInSeconds(), is(101L));
   }

   @Test
   public void shouldReturnTotalRequestsSinceUptime() {

      when(statisticsHandler.getRequests()).thenReturn(200);

      assertThat(collector.getTotalRequestsSinceUptime(), is(200L));
   }

   @Test(expected = IllegalStateException.class)
   public void shouldRaiseExceptionDuringConstructionIfTotalCapacityIs0ConsideringNonWorkerThreads() {

      when(threadPool.getMaxThreads()).thenReturn(NON_WORKER_THREADS);

      new ElbStatisticsCollector(statisticsHandler, threadPool, runtimeMXBean, NON_WORKER_THREADS, NON_WORKER_THREADS);
   }

   @Test(expected = IllegalStateException.class)
   public void shouldRaiseExceptionDuringPostConstructionIfTotalCapacityIsInsufficientForNonWorkerThreads() {

      when(threadPool.getMaxThreads()).thenReturn(NON_WORKER_THREADS - 1);

      collector = new ElbStatisticsCollector(statisticsHandler, threadPool, runtimeMXBean, NON_WORKER_THREADS,
            NON_WORKER_THREADS - 1);
   }

   @Test
   public void shouldReturn0CapacityWhenThreadPoolIsLowOnThreads() {

      when(threadPool.isLowOnThreads()).thenReturn(true);

      assertThat(collector.getCapacity(), is(0L));
   }

   @Test
   public void shouldReturn100PercentCapacityWhenNoActiveAndPendingRequests() {

      when(statisticsHandler.getRequestsActive()).thenReturn(0);
      when(threadPool.getQueueSize()).thenReturn(0);

      assertThat(collector.getCapacity(), is(100L));
   }

   @Test
   public void shouldCalculateCapacityWhenPendingRequestsAreLessThanNonWorkerThreads() {

      when(statisticsHandler.getRequestsActive()).thenReturn(4);
      when(threadPool.getQueueSize()).thenReturn(NON_WORKER_THREADS - 1);

      assertThat(collector.getCapacity(), is(50L));
   }

   @Test
   public void shouldCalculateCapacityWhenPendingRequestsAreEqualToMaxQueueSize() {

      when(statisticsHandler.getRequestsActive()).thenReturn(4);
      when(threadPool.getQueueSize()).thenReturn(MAX_QUEUE_SIZE);

      assertThat(collector.getCapacity(), is(25L));
   }

   @Test
   public void shouldCalculateCapacityWhenPendingRequestsAreGreaterThanNonWorkerThreadsButLessThanMaxQueueSize() {

      when(statisticsHandler.getRequestsActive()).thenReturn(4);
      when(threadPool.getQueueSize()).thenReturn(NON_WORKER_THREADS + 1);

      assertThat(collector.getCapacity(), is(38L));
   }

   @Test
   public void shouldReturn0WhenAllCapacityIsUsedUp() {

      when(statisticsHandler.getRequestsActive()).thenReturn(6);
      when(threadPool.getQueueSize()).thenReturn(MAX_QUEUE_SIZE);

      assertThat(collector.getCapacity(), is(0L));
   }

   @Test
   public void shouldReturn0WhenUsedCapacityExceeds100Percent() {

      when(statisticsHandler.getRequestsActive()).thenReturn(MAX_THREADS);
      when(threadPool.getQueueSize()).thenReturn(MAX_QUEUE_SIZE + 1);
      assertThat(collector.getCapacity(), is(0L));
   }
}
