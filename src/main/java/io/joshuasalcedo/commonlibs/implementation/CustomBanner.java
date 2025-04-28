package io.joshuasalcedo.commonlibs.implementation;

import io.joshuasalcedo.commonlibs.properties.BannerProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CustomBanner implements Banner {

    private final BannerProperties properties;

    public CustomBanner(BannerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        try {
            String bannerPath = properties.getLocation().replace("classpath:", "");
            Resource resource = new ClassPathResource(bannerPath);

            if (!resource.exists()) {
                // Fallback to default banner if custom one doesn't exist
                resource = new ClassPathResource("banner.txt");
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                String banner = FileCopyUtils.copyToString(reader);

                // Replace standard Spring Boot placeholders
                String appName = environment.getProperty("spring.application.name", "Application");
                String appVersion = environment.getProperty("spring.application.version", "Unknown Version");

                // Replace both standard Spring Boot placeholders and our custom ones
                banner = banner.replace("${application.title}", appName);
                banner = banner.replace("${application.name}", appName);
                banner = banner.replace("${application.version}", appVersion);
                banner = banner.replace("${spring-boot.formatted-version}", " (v" + SpringBootVersion.getVersion() + ")");

                // Apply color if specified
                if (!"default".equals(properties.getColor())) {
                    String color = properties.getColor().toUpperCase();
                    banner = "${AnsiColor." + color + "}" + banner + "${AnsiColor.DEFAULT}";
                }

                out.println(banner);
            }
        } catch (IOException e) {
            // If error occurs, just continue without banner
            out.println("Error loading banner: " + e.getMessage());
        }
    }
}