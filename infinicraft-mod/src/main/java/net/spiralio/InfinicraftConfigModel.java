package net.spiralio;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "infinicraft")
@Config(name = "infinicraft-config", wrapperName = "InfinicraftConfig")
public class InfinicraftConfigModel {
    public String CHAT_API_KEY = "sk-123456789";
    public String CHAT_API_BASE = "https://api.openai.com/v1";
}