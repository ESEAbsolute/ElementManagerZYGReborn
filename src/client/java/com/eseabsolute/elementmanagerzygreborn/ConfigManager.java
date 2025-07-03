package com.eseabsolute.elementmanagerzygreborn;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigManager {
    private static Logger logger = LogManager.getLogger("EleManager");
    private static final String CONFIG_FILE_NAME = "elemanager-zyg.properties";
    private final Path configPath;
    private final Properties properties;

    public ConfigManager() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        this.properties = new Properties();
        loadConfig();
    }

    private void loadConfig() {
        if (Files.exists(configPath)) {
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                properties.load(inputStream);
                logger.info("Configuration file loaded successfully");
            } catch (IOException e) {
                logger.error("Couldn't read configuration file", e);
            }
        } else {
            logger.error("Configuration file not found, using default values");
            setDefaultProperties();
            saveConfig();
        }
    }

    public String readConfig(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public void writeConfig(String key, String value) {
        properties.setProperty(key, value);
    }

    private void setDefaultProperties() {
        properties.setProperty("Enabled", "False");
        properties.setProperty("Metal", "Store");
        properties.setProperty("Wood", "Store");
        properties.setProperty("Water", "Store");
        properties.setProperty("Fire", "Store");
        properties.setProperty("Earth", "Store");
        properties.setProperty("CurrentItems", "Store");
        properties.setProperty("IntervalMode", "NormalMode");
    }

    public void saveConfig() {
        try (OutputStream outputStream = Files.newOutputStream(configPath)) {
            properties.store(outputStream, "EleManager Configuration");
            logger.info("Configuration saved successfully");
        } catch (IOException e) {
            logger.error("Couldn't write configuration file", e);
        }
    }
}
