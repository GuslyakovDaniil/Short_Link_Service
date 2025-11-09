package ru.promo_it.linkshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("FATAL: Cannot find config.properties");
                return;
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public long getLinkTtlSeconds() {
        return Long.parseLong(properties.getProperty("link.ttl.seconds"));
    }

    public int getDefaultClickLimit() {
        return Integer.parseInt(properties.getProperty("default.click.limit"));
    }
}