package com.sub.sample.apis.jetty;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.NotThreadSafe;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;

/**
 * @author vijay.daniel
 *
 */
@NotThreadSafe
@ParametersAreNonnullByDefault
public class JettyServerCustomizerImpl implements JettyServerCustomizer {

   private static final Logger LOGGER = LoggerFactory.getLogger(JettyServerCustomizerImpl.class);

   private final List<HandlerWrapper> handlers;
   private final MBeanContainer mBeanContainer;
   private final ServerBeanRegistrar serverBeanRegistrar;

   public JettyServerCustomizerImpl(List<HandlerWrapper> handlers, MBeanContainer mBeanContainer,
         ServerBeanRegistrar serverBeanRegistrar) {

      this.mBeanContainer = mBeanContainer;
      this.handlers = handlers;
      this.serverBeanRegistrar = serverBeanRegistrar;
   }

   @Override
   public void customize(Server server) {

      LOGGER.info("Customizing jetty server...");

      // Enable JMX for Jetty
      server.addEventListener(mBeanContainer);
      server.addBean(mBeanContainer);

      // Listen for lifecycle events
      server.addEventListener(serverBeanRegistrar);

      if (!handlers.isEmpty()) {

         // Chain the handlers. Jetty handlers act like a linked list.
         // handlers: HW0, HW1, HW2
         // server.handler: Hs
         // This will become: HW0 -> HW1 -> HW2 -> Hs
         // server.handler: HW0

         int handlersSizeMinusOne = handlers.size() - 1;
         for (int i = 0; i < handlersSizeMinusOne; ++i) {

            handlers.get(i).setHandler(handlers.get(i + 1));
         }
         Handler oldHandler = server.getHandler();
         handlers.get(handlersSizeMinusOne).setHandler(oldHandler);
         server.setHandler(handlers.get(0));
      }
   }
}
