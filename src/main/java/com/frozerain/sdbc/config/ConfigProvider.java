package com.frozerain.sdbc.config;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frozerain.sdbc.annotation.JsonConfigEntity;
import com.frozerain.sdbc.config.impl.GeneralConfig;
import com.frozerain.sdbc.event.processor.impl.SimpleEventProcessor;
import com.frozerain.sdbc.util.DefaultConstants;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ConfigProvider {

    private static final Logger log = Loggers.getLogger(SimpleEventProcessor.class);

    private static String CONFIG_ROOT = "";
    private static ConfigProvider INSTANCE;

    private Map<String, BotConfig> configFiles;
    private Configuration packageConfiguration;

    public static ConfigProvider init(@Nonnull String configRoot) {
        if (INSTANCE == null) {
            log.debug("Initialize new config provider with config root {}", configRoot);
            INSTANCE = new ConfigProvider(configRoot);
        }
        return INSTANCE;
    }

    public static ConfigProvider instance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Config Provider not initialized!");
        }
        return INSTANCE;
    }

    public static ConfigProvider reload() {
        return init(CONFIG_ROOT);
    }

    public static BotConfig getConfig(String configName) {
        return instance().getConfigEntity(configName);
    }

    public static GeneralConfig getGeneral() {
        return (GeneralConfig) instance().getConfigEntity(DefaultConstants.GENERAL_CONFIG_NAME);
    }

    private ConfigProvider(@Nonnull String configRoot) {
        if (!CONFIG_ROOT.equals(configRoot)) {
            CONFIG_ROOT = configRoot;
        }
        this.configFiles = new HashMap<>();
        try {
            GeneralConfig config = this.loadGeneralConfig();
            List<String> customPackages = config.getProperty(PropertyKey.CONFIG_ROOT_PACKAGE);
            if (customPackages != null && !customPackages.isEmpty()) {
                this.packageConfiguration = new ConfigurationBuilder().forPackages(customPackages.toArray(new String[]{}));
                this.loadConfigEntities(this.packageConfiguration);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BotConfig getConfigEntity(String configName) {
        return this.configFiles.get(configName);
    }

    private GeneralConfig loadGeneralConfig() throws IOException {
        String configFile = GeneralConfig.class.getAnnotation(JsonConfigEntity.class).file();
        BotConfig config = this.loadConfigEntity(new ObjectMapper(), GeneralConfig.class, configFile);
        this.configFiles.put(config.getConfigName(), config);
        return (GeneralConfig) config;
    }

    private void loadConfigEntities(Configuration packageConfiguration) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Reflections reflections = new Reflections(packageConfiguration);
        Set<Class<?>> types = reflections.getTypesAnnotatedWith(JsonConfigEntity.class);

        for (Class<?> clazz : types) {
            String configName = clazz.getAnnotation(JsonConfigEntity.class).file();
            BotConfig config = this.loadConfigEntity(mapper, clazz, configName);
            if (config == null) {
                throw new RuntimeException("Cannot load config file: " + configName);
            }
            this.configFiles.put(config.getConfigName(), config);
        }
    }

    private BotConfig loadConfigEntity(ObjectMapper mapper, Class<?> clazz, String configName) throws IOException {
        File configFile = Paths.get(CONFIG_ROOT + "/" + configName).toFile();
        if (configFile.exists()) {
            return this.readConfig(mapper, clazz, configFile);
        } else {
            return this.writeNewConfigToFile(mapper, clazz, configFile);
        }
    }

    private BotConfig readConfig(ObjectMapper mapper, Class<?> clazz, File configFile) throws IOException {
        Object config = mapper.readValue(configFile, clazz);
        if (config instanceof BotConfig) {
            return (BotConfig) config;
        } else {
            throw new RuntimeException(String.format("Config class %s not implements BotConfig interface.", clazz.getSimpleName()));
        }
    }

    private BotConfig writeNewConfigToFile(ObjectMapper mapper, Class<?> clazz, File configFile) {
        BotConfig newConfig = null;
        File configDir = new File(CONFIG_ROOT);
        try {
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            newConfig = (BotConfig) clazz.newInstance();
            mapper.writer(new DefaultPrettyPrinter()).writeValue(configFile, newConfig);
        } catch (IllegalAccessException | InstantiationException | IOException e) {
            e.printStackTrace();
        }
        return newConfig;
    }
}
