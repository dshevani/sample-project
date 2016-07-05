package com.ekart.sample.apis.jetty;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * This is a blind copy of the package private JettyEmbeddedWebAppContext class
 * for handling deferred initialization
 * 
 * @author vijay.daniel
 *
 */
public class CustomJettyEmbeddedWebAppContext extends WebAppContext {

   @Override
   protected ServletHandler newServletHandler() {

      return new JettyEmbeddedServletHandler();
   }

   public void deferredInitialize() throws Exception {

      ((JettyEmbeddedServletHandler) getServletHandler()).deferredInitialize();
   }

   private static class JettyEmbeddedServletHandler extends ServletHandler {

      @Override
      public void initialize() throws Exception {

      }

      public void deferredInitialize() throws Exception {

         super.initialize();
      }
   }
}
