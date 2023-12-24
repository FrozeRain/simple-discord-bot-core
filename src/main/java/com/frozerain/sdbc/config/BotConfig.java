package com.frozerain.sdbc.config;

import java.util.List;
import java.util.Map;

public interface BotConfig {

    String getConfigName();

    List<String> getProperty(String propertyKey);
}
