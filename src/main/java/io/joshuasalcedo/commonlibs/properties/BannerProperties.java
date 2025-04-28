package io.joshuasalcedo.commonlibs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "io.joshuasalcedo.banner")
public class BannerProperties {
    // Getters and setters
    /**
     * Enable or disable the custom banner
     */
    private boolean enabled = true;

    /**
     * Path to the custom banner resource
     */
    private String location = "classpath:banner.txt";

    /**
     * Text color of the banner
     */
    private String color = "default";

}