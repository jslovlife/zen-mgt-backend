package com.zenmgt.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                
                // Thread pool configuration
                protocol.setMaxThreads(200);           // Maximum number of worker threads
                protocol.setMinSpareThreads(25);       // Minimum number of spare threads
                protocol.setMaxConnections(8192);      // Maximum number of connections
                protocol.setAcceptCount(100);          // Maximum queue length for incoming connections
                
                // Connection timeout settings
                protocol.setConnectionTimeout(20000);   // 20 seconds connection timeout
                protocol.setKeepAliveTimeout(60000);    // 60 seconds keep-alive timeout
                protocol.setMaxKeepAliveRequests(100);  // Maximum keep-alive requests
                
                // Performance optimizations
                protocol.setTcpNoDelay(true);
                protocol.setCompression("on");
                protocol.setCompressionMinSize(1024);
                protocol.setCompressibleMimeType("text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json,application/xml");
                
                // Thread naming for better debugging
            });
            
            // Additional connector for monitoring (optional)
            factory.addAdditionalTomcatConnectors(createManagementConnector());
        };
    }

    private Connector createManagementConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        
        // Management connector settings
        connector.setPort(8081);
        protocol.setMaxThreads(50);
        protocol.setMinSpareThreads(5);
        protocol.setMaxConnections(1000);
        protocol.setConnectionTimeout(30000);
        
        return connector;
    }
} 