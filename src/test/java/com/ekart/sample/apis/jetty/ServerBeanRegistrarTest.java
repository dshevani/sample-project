package com.sub.sample.apis.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.Container;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sub.sample.apis.jetty.AggregatedLifeCycleListener;
import com.sub.sample.apis.jetty.ServerBeanRegistrar;

import static org.mockito.Mockito.*;

/**
 * @author vijay.daniel
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerBeanRegistrarTest {

   @Mock
   private AggregatedLifeCycleListener lifeCycleListener;

   @Mock
   private Server parent;

   private ServerBeanRegistrar serverBeanRegistrar;

   @Before
   public void setUp() {

      serverBeanRegistrar = new ServerBeanRegistrar(lifeCycleListener);
   }

   @Test
   public void shouldRegisterConnectorsWhenBeanIsAdded() {

      Connector child = mock(Connector.class);
      serverBeanRegistrar.beanAdded(parent, child);

      verify(lifeCycleListener).register(child);
   }

   @Test
   public void shouldNotRegisterNonConnectorsWhenBeanIsAdded() {

      Object child = mock(Container.class);
      serverBeanRegistrar.beanAdded(parent, child);

      verifyZeroInteractions(lifeCycleListener);
   }

   @Test
   public void shouldNotRegisterConnectorsIfParentIsNotServerWhenBeanIsAdded() {

      Container nonServerParent = mock(Container.class);
      Connector child = mock(Connector.class);
      serverBeanRegistrar.beanAdded(nonServerParent, child);

      verifyZeroInteractions(lifeCycleListener);
   }

   @Test
   public void shouldDeregisterConnectorsWhenBeanIsRemoved() {

      Connector child = mock(Connector.class);
      serverBeanRegistrar.beanRemoved(parent, child);

      verify(lifeCycleListener).deregister(child);
   }

   @Test
   public void shouldNotDeregisterNonConnectorsWhenBeanIsRemoved() {

      Object child = mock(Container.class);
      serverBeanRegistrar.beanRemoved(parent, child);

      verifyZeroInteractions(lifeCycleListener);
   }

   @Test
   public void shouldNotDeregisterConnectorsIfParentIsNotServerWhenBeanIsRemoved() {

      Container nonServerParent = mock(Container.class);
      Connector child = mock(Connector.class);
      serverBeanRegistrar.beanRemoved(nonServerParent, child);

      verifyZeroInteractions(lifeCycleListener);
   }
}
