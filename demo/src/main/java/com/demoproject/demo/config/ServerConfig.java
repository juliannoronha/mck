package com.demoproject.demo.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.catalina.LifecycleException;

/**
 * Server configuration class for setting up Tomcat servlet container.
 * This class configures HTTP and HTTPS connectors for the server.
 */
@Configuration
public class ServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);

    @Value("${server.tomcat.max-connections:10000}")
    private int maxConnections;

    @Value("${server.tomcat.accept-count:100}")
    private int acceptCount;

    @Value("${server.tomcat.connection-timeout:20000}")
    private int connectionTimeout;

    /**
     * Creates and configures a ServletWebServerFactory bean.
     * 
     * @return A configured TomcatServletWebServerFactory with additional connectors.
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(connector -> {
            connector.addLifecycleListener(new LifecycleListener() {
                @Override
                public void lifecycleEvent(LifecycleEvent event) {
                    if (Lifecycle.STOP_EVENT.equals(event.getType())) {
                        try {
                            ((Connector) event.getLifecycle()).destroy();
                        } catch (LifecycleException e) {
                            logger.error("Error during connector destruction", e);
                        }
                    }
                }
            });
        });
        tomcat.addAdditionalTomcatConnectors(createRedirectConnector());
        return tomcat;
    }

    /**
     * Creates a Connector for HTTP that redirects to HTTPS.
     * 
     * @return A configured Connector for HTTP redirection.
     */
    private Connector createRedirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        connector.setProperty("connectionTimeout", "20000");
        connector.setProperty("maxThreads", "150");
        connector.setProperty("acceptCount", "100");
        return connector;
    }

    // TODO: Consider adding configuration for HTTPS connector
    // TODO: Implement rate limiting for DDoS protection
}