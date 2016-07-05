package com.ekart.sample.apis.jetty;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.servlets.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.context.embedded.AbstractEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.Compression;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.embedded.Ssl.ClientAuth;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.context.embedded.jetty.ServletContextInitializerConfiguration;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * This is "copied' from JettyEmbeddedServletContainerFactory with certain
 * overrides for customizing acceptors, selectors and blockingQueueSize. It is
 * present in the same package as JettyEmbeddedServletContainerFactory to access
 * JettyEmbeddedWebAppContext for handling deferred initialize events
 * 
 * @author vijay.daniel
 *
 */
public class CustomJettyEmbeddedServletContainerFactory extends AbstractEmbeddedServletContainerFactory
      implements ResourceLoaderAware {

   private static final String GZIP_HANDLER_JETTY_9_2 = "org.eclipse.jetty.servlets.gzip.GzipHandler";
   private static final String GZIP_HANDLER_JETTY_9_3 = "org.eclipse.jetty.server.handler.gzip.GzipHandler";
   
   private static final int INIT_ORDER = 3;

   private List<Configuration> configurations = new ArrayList<>();
   private boolean useForwardHeaders;
   private List<JettyServerCustomizer> jettyServerCustomizers = new ArrayList<>();
   private ResourceLoader resourceLoader;

   private final QueuedThreadPool jettyThreadPool;
   private final int acceptors;
   private final int selectors;
   private final int acceptQueueSize;

   public CustomJettyEmbeddedServletContainerFactory(QueuedThreadPool jettyThreadPool, int acceptors, int selectors,
         int acceptQueueSize, int serverPort) {

      super(serverPort);

      this.jettyThreadPool = jettyThreadPool;
      this.acceptors = acceptors;
      this.selectors = selectors;
      this.acceptQueueSize = acceptQueueSize;
   }

   @Override
   public EmbeddedServletContainer getEmbeddedServletContainer(ServletContextInitializer... initializers) {

      this.logger.info("Using custom servlet container factory");
      int port = (getPort() >= 0 ? getPort() : 0);
      CustomJettyEmbeddedWebAppContext context = new CustomJettyEmbeddedWebAppContext();
      Server server = new Server(jettyThreadPool);
      ServerConnector nonSslConnector = new ServerConnector(server, jettyThreadPool, null, null, acceptors, selectors,
            new HttpConnectionFactory());
      InetSocketAddress socketAddress = new InetSocketAddress(getAddress(), port);
      nonSslConnector.setHost(socketAddress.getHostName());
      nonSslConnector.setPort(port);
      nonSslConnector.setAcceptQueueSize(acceptQueueSize);
      server.setConnectors(new Connector[] { nonSslConnector });

      configureWebAppContext(context, initializers);
      server.setHandler(addHandlerWrappers(context));
      this.logger.info("Server initialized with port: " + port);
      if (getSsl() != null && getSsl().isEnabled()) {
         SslContextFactory sslContextFactory = new SslContextFactory();
         configureSsl(sslContextFactory, getSsl());
         ServerConnector connector = getSslConnector(server, jettyThreadPool, sslContextFactory, acceptors, selectors);
         connector.setHost(socketAddress.getHostName());
         connector.setPort(port);
         connector.setAcceptQueueSize(acceptQueueSize);
         server.setConnectors(new Connector[] { connector });
      }
      for (JettyServerCustomizer customizer : getServerCustomizers()) {
         customizer.customize(server);
      }
      if (this.useForwardHeaders) {
         new ForwardHeadersCustomizer().customize(server);
      }
      return getJettyEmbeddedServletContainer(server);
   }

   private Handler addHandlerWrappers(Handler handler) {

      if (getCompression() != null && getCompression().getEnabled()) {
         handler = applyWrapper(handler, createGzipHandler());
      }
      if (StringUtils.hasText(getServerHeader())) {
         handler = applyWrapper(handler, new ServerHeaderHandler(getServerHeader()));
      }
      return handler;
   }

   private Handler applyWrapper(Handler handler, HandlerWrapper wrapper) {

      wrapper.setHandler(handler);
      return wrapper;
   }

   private HandlerWrapper createGzipHandler() {

      ClassLoader classLoader = getClass().getClassLoader();
      if (ClassUtils.isPresent(GZIP_HANDLER_JETTY_9_2, classLoader)) {
         return new Jetty92GzipHandlerFactory().createGzipHandler(getCompression());
      }
      if (ClassUtils.isPresent(GZIP_HANDLER_JETTY_9_3, getClass().getClassLoader())) {
         return new Jetty93GzipHandlerFactory().createGzipHandler(getCompression());
      }
      throw new IllegalStateException("Compression is enabled, but GzipHandler is not on the classpath");
   }

   /**
    * Configure the SSL connection.
    * 
    * @param factory
    *           the Jetty {@link SslContextFactory}.
    * @param ssl
    *           the ssl details.
    */
   protected void configureSsl(SslContextFactory factory, Ssl ssl) {

      factory.setProtocol(ssl.getProtocol());
      configureSslClientAuth(factory, ssl);
      configureSslPasswords(factory, ssl);
      factory.setCertAlias(ssl.getKeyAlias());
      configureSslKeyStore(factory, ssl);
      if (ssl.getCiphers() != null) {
         factory.setIncludeCipherSuites(ssl.getCiphers());
      }
      configureSslTrustStore(factory, ssl);
   }

   private void configureSslClientAuth(SslContextFactory factory, Ssl ssl) {

      if (ssl.getClientAuth() == ClientAuth.NEED) {
         factory.setNeedClientAuth(true);
         factory.setWantClientAuth(true);
      } else if (ssl.getClientAuth() == ClientAuth.WANT) {
         factory.setWantClientAuth(true);
      }
   }

   private void configureSslPasswords(SslContextFactory factory, Ssl ssl) {

      if (ssl.getKeyStorePassword() != null) {
         factory.setKeyStorePassword(ssl.getKeyStorePassword());
      }
      if (ssl.getKeyPassword() != null) {
         factory.setKeyManagerPassword(ssl.getKeyPassword());
      }
   }

   private void configureSslKeyStore(SslContextFactory factory, Ssl ssl) {

      try {
         URL url = ResourceUtils.getURL(ssl.getKeyStore());
         factory.setKeyStoreResource(Resource.newResource(url));
      } catch (IOException ex) {
         throw new EmbeddedServletContainerException("Could not find key store '" + ssl.getKeyStore() + "'", ex);
      }
      if (ssl.getKeyStoreType() != null) {
         factory.setKeyStoreType(ssl.getKeyStoreType());
      }
      if (ssl.getKeyStoreProvider() != null) {
         factory.setKeyStoreProvider(ssl.getKeyStoreProvider());
      }
   }

   private void configureSslTrustStore(SslContextFactory factory, Ssl ssl) {

      if (ssl.getTrustStorePassword() != null) {
         factory.setTrustStorePassword(ssl.getTrustStorePassword());
      }
      if (ssl.getTrustStore() != null) {
         try {
            URL url = ResourceUtils.getURL(ssl.getTrustStore());
            factory.setTrustStoreResource(Resource.newResource(url));
         } catch (IOException ex) {
            throw new EmbeddedServletContainerException("Could not find trust store '" + ssl.getTrustStore() + "'", ex);
         }
      }
      if (ssl.getTrustStoreType() != null) {
         factory.setTrustStoreType(ssl.getTrustStoreType());
      }
      if (ssl.getTrustStoreProvider() != null) {
         factory.setTrustStoreProvider(ssl.getTrustStoreProvider());
      }
   }

   /**
    * Configure the given Jetty {@link WebAppContext} for use.
    * 
    * @param context
    *           the context to configure
    * @param initializers
    *           the set of initializers to apply
    */
   protected final void configureWebAppContext(WebAppContext context, ServletContextInitializer... initializers) {

      Assert.notNull(context, "Context must not be null");
      context.setTempDirectory(getTempDirectory());
      if (this.resourceLoader != null) {
         context.setClassLoader(this.resourceLoader.getClassLoader());
      }
      String contextPath = getContextPath();
      context.setContextPath(StringUtils.hasLength(contextPath) ? contextPath : "/");
      context.setDisplayName(getDisplayName());
      configureDocumentRoot(context);
      if (isRegisterDefaultServlet()) {
         addDefaultServlet(context);
      }
      if (shouldRegisterJspServlet()) {
         addJspServlet(context);
      }
      ServletContextInitializer[] initializersToUse = mergeInitializers(initializers);
      Configuration[] contextConfigurations = getWebAppContextConfigurations(context, initializersToUse);
      context.setConfigurations(contextConfigurations);
      configureSession(context);
      postProcessWebAppContext(context);
   }

   private void configureSession(WebAppContext context) {

      SessionManager sessionManager = context.getSessionHandler().getSessionManager();
      int sessionTimeout = (getSessionTimeout() > 0 ? getSessionTimeout() : -1);
      sessionManager.setMaxInactiveInterval(sessionTimeout);
      if (isPersistSession()) {
         Assert.isInstanceOf(HashSessionManager.class, sessionManager, "Unable to use persistent sessions");
         configurePersistSession(sessionManager);
      }
   }

   private void configurePersistSession(SessionManager sessionManager) {

      try {
         ((HashSessionManager) sessionManager).setStoreDirectory(getValidSessionStoreDir());
      } catch (IOException ex) {
         throw new IllegalStateException(ex);
      }
   }

   private File getTempDirectory() {

      String temp = System.getProperty("java.io.tmpdir");
      return (temp == null ? null : new File(temp));
   }

   private void configureDocumentRoot(WebAppContext handler) {

      File root = getValidDocumentRoot();
      root = (root != null ? root : createTempDir("jetty-docbase"));
      try {
         if (!root.isDirectory()) {
            Resource resource = JarResource.newJarResource(Resource.newResource(root));
            handler.setBaseResource(resource);
         } else {
            handler.setBaseResource(Resource.newResource(root.getCanonicalFile()));
         }
      } catch (Exception ex) {
         throw new IllegalStateException(ex);
      }
   }

   /**
    * Add Jetty's {@code DefaultServlet} to the given {@link WebAppContext}.
    * 
    * @param context
    *           the jetty {@link WebAppContext}
    */
   protected final void addDefaultServlet(WebAppContext context) {

      Assert.notNull(context, "Context must not be null");
      ServletHolder holder = new ServletHolder();
      holder.setName("default");
      holder.setClassName("org.eclipse.jetty.servlet.DefaultServlet");
      holder.setInitParameter("dirAllowed", "false");
      holder.setInitOrder(1);
      context.getServletHandler().addServletWithMapping(holder, "/");
      context.getServletHandler().getServletMapping("/").setDefault(true);
   }

   /**
    * Add Jetty's {@code JspServlet} to the given {@link WebAppContext}.
    * 
    * @param context
    *           the jetty {@link WebAppContext}
    */
   protected final void addJspServlet(WebAppContext context) {

      Assert.notNull(context, "Context must not be null");
      ServletHolder holder = new ServletHolder();
      holder.setName("jsp");
      holder.setClassName(getJspServlet().getClassName());
      holder.setInitParameter("fork", "false");
      holder.setInitParameters(getJspServlet().getInitParameters());
      holder.setInitOrder(INIT_ORDER);
      context.getServletHandler().addServlet(holder);
      ServletMapping mapping = new ServletMapping();
      mapping.setServletName("jsp");
      mapping.setPathSpecs(new String[] { "*.jsp", "*.jspx" });
      context.getServletHandler().addServletMapping(mapping);
   }

   /**
    * Return the Jetty {@link Configuration}s that should be applied to the
    * server.
    * 
    * @param webAppContext
    *           the Jetty {@link WebAppContext}
    * @param initializers
    *           the {@link ServletContextInitializer}s to apply
    * @return configurations to apply
    */
   protected Configuration[] getWebAppContextConfigurations(WebAppContext webAppContext,
         ServletContextInitializer... initializers) {

      List<Configuration> allConfigurations = new ArrayList<Configuration>();
      allConfigurations.add(getServletContextInitializerConfiguration(webAppContext, initializers));
      allConfigurations.addAll(getConfigurations());
      allConfigurations.add(getErrorPageConfiguration());
      allConfigurations.add(getMimeTypeConfiguration());
      return allConfigurations.toArray(new Configuration[allConfigurations.size()]);
   }

   /**
    * Create a configuration object that adds error handlers.
    * 
    * @return a configuration object for adding error pages
    */
   private Configuration getErrorPageConfiguration() {

      return new AbstractConfiguration() {
         @Override
         public void configure(WebAppContext context) throws Exception {

            ErrorHandler errorHandler = context.getErrorHandler();
            addJettyErrorPages(errorHandler, getErrorPages());
         }
      };
   }

   /**
    * Create a configuration object that adds mime type mappings.
    * 
    * @return a configuration object for adding mime type mappings
    */
   private Configuration getMimeTypeConfiguration() {

      return new AbstractConfiguration() {
         @Override
         public void configure(WebAppContext context) throws Exception {

            MimeTypes mimeTypes = context.getMimeTypes();
            for (MimeMappings.Mapping mapping : getMimeMappings()) {
               mimeTypes.addMimeMapping(mapping.getExtension(), mapping.getMimeType());
            }
         }
      };
   }

   /**
    * Return a Jetty {@link Configuration} that will invoke the specified
    * {@link ServletContextInitializer}s. By default this method will return a
    * {@link ServletContextInitializerConfiguration}.
    * 
    * @param webAppContext
    *           the Jetty {@link WebAppContext}
    * @param initializers
    *           the {@link ServletContextInitializer}s to apply
    * @return the {@link Configuration} instance
    */
   protected Configuration getServletContextInitializerConfiguration(WebAppContext webAppContext,
         ServletContextInitializer... initializers) {

      return new ServletContextInitializerConfiguration(initializers);
   }

   /**
    * Post process the Jetty {@link WebAppContext} before it used with the Jetty
    * Server. Subclasses can override this method to apply additional processing
    * to the {@link WebAppContext}.
    * 
    * @param webAppContext
    *           the Jetty {@link WebAppContext}
    */
   protected void postProcessWebAppContext(WebAppContext webAppContext) {

   }

   /**
    * Factory method called to create the {@link JettyEmbeddedServletContainer}
    * . Subclasses can override this method to return a different
    * {@link JettyEmbeddedServletContainer} or apply additional processing to
    * the Jetty server.
    * 
    * @param server
    *           the Jetty server.
    * @return a new {@link JettyEmbeddedServletContainer} instance
    */
   protected CustomJettyEmbeddedServletContainer getJettyEmbeddedServletContainer(Server server) {

      this.logger.info("Port value currently: " + getPort() + ", " + (getPort() >= 0));
      return new CustomJettyEmbeddedServletContainer(server, getPort() >= 0);
   }

   @Override
   public void setResourceLoader(ResourceLoader resourceLoader) {

      this.resourceLoader = resourceLoader;
   }

   /**
    * Set if x-forward-* headers should be processed.
    * 
    * @param useForwardHeaders
    *           if x-forward headers should be used
    * @since 1.3.0
    */
   public void setUseForwardHeaders(boolean useForwardHeaders) {

      this.useForwardHeaders = useForwardHeaders;
   }

   /**
    * Sets {@link JettyServerCustomizer}s that will be applied to the
    * {@link Server} before it is started. Calling this method will replace any
    * existing configurations.
    * 
    * @param customizers
    *           the Jetty customizers to apply
    */
   public void setServerCustomizers(Collection<? extends JettyServerCustomizer> customizers) {

      Assert.notNull(customizers, "Customizers must not be null");
      this.jettyServerCustomizers = new ArrayList<JettyServerCustomizer>(customizers);
   }

   /**
    * Returns a mutable collection of Jetty {@link Configuration}s that will be
    * applied to the {@link WebAppContext} before the server is created.
    * 
    * @return the Jetty {@link Configuration}s
    */
   public Collection<JettyServerCustomizer> getServerCustomizers() {

      return this.jettyServerCustomizers;
   }

   /**
    * Add {@link JettyServerCustomizer}s that will be applied to the
    * {@link Server} before it is started.
    * 
    * @param customizers
    *           the customizers to add
    */
   public void addServerCustomizers(JettyServerCustomizer... customizers) {

      Assert.notNull(customizers, "Customizers must not be null");
      this.jettyServerCustomizers.addAll(Arrays.asList(customizers));
   }

   /**
    * Sets Jetty {@link Configuration}s that will be applied to the
    * {@link WebAppContext} before the server is created. Calling this method
    * will replace any existing configurations.
    * 
    * @param configurations
    *           the Jetty configurations to apply
    */
   public void setConfigurations(Collection<? extends Configuration> configurations) {

      Assert.notNull(configurations, "Configurations must not be null");
      this.configurations = new ArrayList<Configuration>(configurations);
   }

   /**
    * Returns a mutable collection of Jetty {@link Configuration}s that will be
    * applied to the {@link WebAppContext} before the server is created.
    * 
    * @return the Jetty {@link Configuration}s
    */
   public Collection<Configuration> getConfigurations() {

      return this.configurations;
   }

   /**
    * Add {@link Configuration}s that will be applied to the
    * {@link WebAppContext} before the server is started.
    * 
    * @param configurations
    *           the configurations to add
    */
   public void addConfigurations(Configuration... configurations) {

      Assert.notNull(configurations, "Configurations must not be null");
      this.configurations.addAll(Arrays.asList(configurations));
   }

   private void addJettyErrorPages(ErrorHandler errorHandler, Collection<ErrorPage> errorPages) {

      if (errorHandler instanceof ErrorPageErrorHandler) {
         ErrorPageErrorHandler handler = (ErrorPageErrorHandler) errorHandler;
         for (ErrorPage errorPage : errorPages) {
            if (errorPage.isGlobal()) {
               handler.addErrorPage(ErrorPageErrorHandler.GLOBAL_ERROR_PAGE, errorPage.getPath());
            } else {
               if (errorPage.getExceptionName() != null) {
                  handler.addErrorPage(errorPage.getExceptionName(), errorPage.getPath());
               } else {
                  handler.addErrorPage(errorPage.getStatusCode(), errorPage.getPath());
               }
            }
         }
      }
   }

   public ServerConnector getSslConnector(Server server, QueuedThreadPool networkThreadPool,
         SslContextFactory sslContextFactory, int acceptors, int selectors) {

      HttpConfiguration config = new HttpConfiguration();
      config.addCustomizer(new SecureRequestCustomizer());
      HttpConnectionFactory connectionFactory = new HttpConnectionFactory(config);
      SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory,
            HttpVersion.HTTP_1_1.asString());
      return new ServerConnector(server, networkThreadPool, null, null, acceptors, selectors, sslConnectionFactory,
            connectionFactory);
   }

   private interface GzipHandlerFactory {

      HandlerWrapper createGzipHandler(Compression compression);

   }

   private static class Jetty92GzipHandlerFactory implements GzipHandlerFactory {

      @Override
      public HandlerWrapper createGzipHandler(Compression compression) {

         GzipHandler gzipHandler = new GzipHandler();
         gzipHandler.setMinGzipSize(compression.getMinResponseSize());
         gzipHandler.setMimeTypes(new HashSet<String>(Arrays.asList(compression.getMimeTypes())));
         if (compression.getExcludedUserAgents() != null) {
            gzipHandler.setExcluded(new HashSet<String>(Arrays.asList(compression.getExcludedUserAgents())));
         }
         return gzipHandler;
      }

   }

   private static class Jetty93GzipHandlerFactory implements GzipHandlerFactory {

      @Override
      public HandlerWrapper createGzipHandler(Compression compression) {

         try {
            Class<?> handlerClass = ClassUtils.forName(GZIP_HANDLER_JETTY_9_3, getClass().getClassLoader());
            HandlerWrapper handler = (HandlerWrapper) handlerClass.newInstance();
            ReflectionUtils.findMethod(handlerClass, "setMinGzipSize", int.class).invoke(handler,
                  compression.getMinResponseSize());
            ReflectionUtils.findMethod(handlerClass, "setIncludedMimeTypes", String[].class).invoke(handler,
                  new Object[] { compression.getMimeTypes() });
            if (compression.getExcludedUserAgents() != null) {
               ReflectionUtils.findMethod(handlerClass, "setExcludedAgentPatterns", String[].class).invoke(handler,
                     new Object[] { compression.getExcludedUserAgents() });
            }
            return handler;
         } catch (Exception ex) {
            throw new RuntimeException("Failed to configure Jetty 9.3 gzip handler", ex);
         }
      }

   }

   /**
    * {@link JettyServerCustomizer} to add {@link ForwardedRequestCustomizer}.
    * Only supported with Jetty 9 (hence the inner class)
    */
   private static class ForwardHeadersCustomizer implements JettyServerCustomizer {

      @Override
      public void customize(Server server) {

         ForwardedRequestCustomizer customizer = new ForwardedRequestCustomizer();
         for (Connector connector : server.getConnectors()) {
            for (ConnectionFactory connectionFactory : connector.getConnectionFactories()) {
               if (connectionFactory instanceof HttpConfiguration.ConnectionFactory) {
                  ((HttpConfiguration.ConnectionFactory) connectionFactory).getHttpConfiguration()
                        .addCustomizer(customizer);
               }
            }
         }
      }

   }

   /**
    * {@link HandlerWrapper} to add a custom {@code server} header.
    */
   private static class ServerHeaderHandler extends HandlerWrapper {

      private static final String SERVER_HEADER = "server";

      private final String value;

      ServerHeaderHandler(String value) {
         this.value = value;
      }

      @Override
      public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

         if (!response.getHeaderNames().contains(SERVER_HEADER)) {
            response.setHeader(SERVER_HEADER, this.value);
         }
         super.handle(target, baseRequest, request, response);
      }

   }

}
