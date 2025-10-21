package org.example.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class ExternalServicesConfig {
    
    // This configuration makes external services optional for local development
    // In production, these services should be available
}
