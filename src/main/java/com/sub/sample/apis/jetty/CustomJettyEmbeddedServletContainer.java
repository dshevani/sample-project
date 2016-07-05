package com.sub.sample.apis.jetty;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainer;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * This is again almost a complete copy of JettyEmbeddedServletContainer because
 * of its explicit reference to JettyEmbeddedWebAppContext for checking
 * handlers. For our case, we need to check against
 * CustomJettyEmbeddedWebAppContext, and there is no simple and safe way
 * possible other tha copying code and replacing that word alone
 * 
 * @author vijay.daniel
 *
 */
public class CustomJettyEmbeddedServletContainer implements EmbeddedServletContainer {

   private static final Log LOGGER = LogFactory.getLog(JettyEmbeddedServletContainer.class);

   private final Server server;

   private final boolean autoStart;

   private Connector[] connectors;

   /**
    * Create a new {@link JettyEmbeddedServletContainer} instance.
    * 
    * @param server
    *           the underlying Jetty server
    */
   public CustomJettyEmbeddedServletContainer(Server server) {
      this(server, true);
   }

   /**
    * Create a new {@link JettyEmbeddedServletContainer} instance.
    * 
    * @param server
    *           the underlying Jetty server
    * @param autoStart
    *           if auto-starting the container
    */
   public CustomJettyEmbeddedServletContainer(Server server, boolean autoStart) {
      this.autoStart = autoStart;
      Assert.notNull(server, "Jetty Server must not be null");
      this.server = server;
      initialize();
   }

   private synchronized void initialize() {

      try {
         // Cache and clear the connectors to prevent requests being handled
         // before
         // the application context is ready
         this.connectors = this.server.getConnectors();
         this.server.setConnectors(null);

         // Start the server so that the ServletContext is available
         this.server.start();
         this.server.setStopAtShutdown(false);
      } catch (Exception ex) {
         // Ensure process isn't left running
         stopSilently();
         throw new EmbeddedServletContainerException("Unable to start embedded Jetty servlet container", ex);
      }
   }

   @SuppressWarnings("checkstyle:emptyblock")
   private void stopSilently() {

      try {
         this.server.stop();
      } catch (Exception ex) {
         // Ignore
      }
   }

   @Override
   public void start() throws EmbeddedServletContainerException {

      this.server.setConnectors(this.connectors);
      if (!this.autoStart) {
         return;
      }
      try {
         this.server.start();
         for (Handler handler : this.server.getHandlers()) {
            handleDeferredInitialize(handler);
         }
         Connector[] serverConnectors = this.server.getConnectors();
         for (Connector connector : serverConnectors) {
            connector.start();
         }
         CustomJettyEmbeddedServletContainer.LOGGER.info("Jetty started on port(s) " + getActualPortsDescription());
      } catch (Exception ex) {
         throw new EmbeddedServletContainerException("Unable to start embedded Jetty servlet container", ex);
      }
   }

   private String getActualPortsDescription() {

      StringBuilder ports = new StringBuilder();
      for (Connector connector : this.server.getConnectors()) {
         ports.append(ports.length() == 0 ? "" : ", ");
         ports.append(getLocalPort(connector) + getProtocols(connector));
      }
      return ports.toString();
   }

   private Integer getLocalPort(Connector connector) {

      try {
         // Jetty 9 internals are different, but the method name is the same
         return (Integer) ReflectionUtils.invokeMethod(ReflectionUtils.findMethod(connector.getClass(), "getLocalPort"),
               connector);
      } catch (Exception ex) {
         CustomJettyEmbeddedServletContainer.LOGGER.info("could not determine port ( " + ex.getMessage() + ")");
         return 0;
      }
   }

   private String getProtocols(Connector connector) {

      try {
         List<String> protocols = connector.getProtocols();
         return " (" + StringUtils.collectionToDelimitedString(protocols, ", ") + ")";
      } catch (NoSuchMethodError ex) {
         // Not available with Jetty 8
         return "";
      }

   }

   private void handleDeferredInitialize(Handler... handlers) throws Exception {

      for (Handler handler : handlers) {
         if (handler instanceof CustomJettyEmbeddedWebAppContext) {
            ((CustomJettyEmbeddedWebAppContext) handler).deferredInitialize();
         } else if (handler instanceof HandlerWrapper) {
            handleDeferredInitialize(((HandlerWrapper) handler).getHandler());
         } else if (handler instanceof HandlerCollection) {
            handleDeferredInitialize(((HandlerCollection) handler).getHandlers());
         }
      }
   }

   @Override
   public synchronized void stop() {

      try {
         this.server.stop();
      } catch (InterruptedException ex) {
         Thread.currentThread().interrupt();
      } catch (Exception ex) {
         throw new EmbeddedServletContainerException("Unable to stop embedded Jetty servlet container", ex);
      }
   }

   @Override
   public int getPort() {

      Connector[] serverConnectors = this.server.getConnectors();
      for (Connector connector : serverConnectors) {
         // Probably only one...
         return getLocalPort(connector);
      }
      return 0;
   }

   /**
    * Returns access to the underlying Jetty Server.
    * 
    * @return the Jetty server
    */
   public Server getServer() {

      return this.server;
   }

}
