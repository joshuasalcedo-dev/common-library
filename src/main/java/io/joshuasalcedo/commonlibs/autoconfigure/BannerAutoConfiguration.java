package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.implementation.CustomBanner;
import io.joshuasalcedo.commonlibs.properties.BannerProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@AutoConfigurationPackage
@EnableConfigurationProperties(BannerProperties.class)
@ConditionalOnProperty(prefix = "io.joshuasalcedo.banner", name = "enabled", matchIfMissing = true)
public class BannerAutoConfiguration {

    private final BannerProperties properties;

    public BannerAutoConfiguration(BannerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Banner customBanner() {
        return new CustomBanner(this.properties);
    }
}