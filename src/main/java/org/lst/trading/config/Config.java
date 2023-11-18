package org.lst.trading.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Configuration class
public class Config {
    private Properties properties;

    public Config(String configFilePath) throws IOException {
        loadProperties(configFilePath);
    }

    private void loadProperties(String filePath) throws IOException {
        try (InputStream input = new FileInputStream(filePath)) {
            properties = new Properties();
            properties.load(input);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}

