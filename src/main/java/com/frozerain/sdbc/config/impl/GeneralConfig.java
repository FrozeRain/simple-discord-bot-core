package com.frozerain.sdbc.config.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.frozerain.sdbc.annotation.JsonConfigEntity;
import com.frozerain.sdbc.config.BotConfig;
import com.frozerain.sdbc.util.DefaultConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonConfigEntity(file = "bot_general_config.json")
public class GeneralConfig implements BotConfig {

    private String configName = DefaultConstants.GENERAL_CONFIG_NAME;

    @JsonProperty(defaultValue = "")
    private String configDescription = "";

    @JsonProperty(defaultValue = "")
    private String botToken = "";

    @JsonProperty
    private Map<String, List<String>> properties = new HashMap<>();

    @Override
    public String getConfigName() {
        return this.configName;
    }

    @Override
    public List<String> getProperty(String propertyKey) {
        return properties.get(propertyKey);
    }

    public String getBotToken() {
        return botToken;
    }
}
