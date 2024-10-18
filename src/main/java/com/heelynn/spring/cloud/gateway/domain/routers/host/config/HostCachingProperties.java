package com.heelynn.spring.cloud.gateway.domain.routers.host.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author heelynn
 * @date 2024/9/29 13:59
 */
@Data
@ConfigurationProperties(value = "spring.cloud.gateway.host-routing")
public class HostCachingProperties {
    private boolean enabled = true;
}
