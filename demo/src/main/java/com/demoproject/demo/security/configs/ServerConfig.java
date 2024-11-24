package com.demoproject.demo.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Server configuration class for setting up Tomcat servlet container.
 * This class configures HTTP and HTTPS connectors for the server.
 */
@Configuration
public class ServerConfig {

    /**
     * Creates and configures a ServletWebServerFactory bean.
     * 
     * @return A configured TomcatServletWebServerFactory with additional connectors.
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
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
        return connector;
    }

    // TODO: Consider adding configuration for HTTPS connector
    // TODO: Implement rate limiting for DDoS protection
}