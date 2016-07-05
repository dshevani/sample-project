package com.sub.sample.apis.jetty;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.Container.Listener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vijay.daniel
 *
 */
@ThreadSafe
@ParametersAreNonnullByDefault
public class ServerBeanRegistrar implements Listener {

   private static final Logger LOGGER = LoggerFactory.getLogger(ServerBeanRegistrar.class);

   private final AggregatedLifeCycleListener lifeCycleListener;

   public ServerBeanRegistrar(AggregatedLifeCycleListener lifeCycleListener) {

      this.lifeCycleListener = lifeCycleListener;
   }

   @Override
   public void beanAdded(Container parent, Object child) {

      LOGGER.info("Bean added to server. parent: {}, child: {}", parent, child);

      if (isEligible(parent, child)) {

         LOGGER.info("Registered {} with lifecycle listener", child);
         lifeCycleListener.register((LifeCycle) child);
      }
   }

   @Override
   public void beanRemoved(Container parent, Object child) {

      LOGGER.info("Bean removed from server. parent: {}, child: {}", parent, child);

      if (isEligible(parent, child)) {

         LOGGER.info("Deregistered {} from lifecycle listener", child);
         lifeCycleListener.deregister((LifeCycle) child);
      }
   }

   private static boolean isEligible(Container parent, Object child) {

      // JettyEmbeddedServletContainer takes care of starting the connectors
      // after the rest of the application and the server are started
      return parent instanceof Server && child instanceof Connector;
   }
}
