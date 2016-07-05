package com.sub.sample.apis.jetty;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;

import static org.mockito.Mockito.*;

/**
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JettyServerCustomizerImplTest {

   @Mock
   private MBeanContainer mBeanContainer;

   @Mock
   private Server server;

   @Mock
   private QueuedThreadPool threadPool;

   @Mock
   private ServerBeanRegistrar serverBeanRegistrar;

   private JettyServerCustomizerImpl customizer;

   @Before
   public void setUp() {

      when(server.getThreadPool()).thenReturn(threadPool);

      customizer = new JettyServerCustomizerImpl(ImmutableList.of(mock(HandlerWrapper.class)), mBeanContainer,
            serverBeanRegistrar);
   }

   @Test
   public void shouldAddEventListenersAndAddBeanToTheServer() {

      customizer.customize(server);

      verify(server).addEventListener(mBeanContainer);
      verify(server).addBean(mBeanContainer);
      verify(server).addEventListener(serverBeanRegistrar);
   }

   @Test
   public void shouldNotAddAnyHandlersIfNoHandlersArePresent() {

      customizer = new JettyServerCustomizerImpl(ImmutableList.of(), mBeanContainer, serverBeanRegistrar);
      customizer.customize(server);

      verify(server, never()).setHandler(argThat(instanceOf(Handler.class)));
   }

   @Test
   public void shouldChainHandlersIfPresent() {

      HandlerWrapper h0 = mock(HandlerWrapper.class);
      HandlerWrapper h1 = mock(HandlerWrapper.class);
      HandlerWrapper h2 = mock(HandlerWrapper.class);
      Handler existingHandler = mock(Handler.class);

      when(server.getHandler()).thenReturn(existingHandler);

      customizer = new JettyServerCustomizerImpl(ImmutableList.of(h0, h1, h2), mBeanContainer, serverBeanRegistrar);
      customizer.customize(server);

      InOrder inOrder = inOrder(h0, h1, h2, server);
      inOrder.verify(h0).setHandler(h1);
      inOrder.verify(h1).setHandler(h2);
      inOrder.verify(h2).setHandler(existingHandler);
      inOrder.verify(server).setHandler(h0);
   }
}
