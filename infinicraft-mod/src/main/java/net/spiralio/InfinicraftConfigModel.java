package net.spiralio;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "infinicraft")
@Config(name = "infinicraft-config", wrapperName = "InfinicraftConfig")
public class InfinicraftConfigModel {
    public String CHAT_API_KEY = "sk-123456789";
    public String CHAT_API_BASE = "https://api.openai.com/v1";
    public String CHAT_API_MODEL = "gpt-3.5-turbo";
    public String SD_DAEMON_BASE = "http://127.0.0.1:17707";
    public String PROMPT = "You are an API that takes a combination of items in the form \"item + item\" and find a suitable single JSON output that combines the two.\n" +
            "\n" +
            "REQUIRED PARAMETERS:\n" +
            "\n" +
            "item (String): The output word. Items can be physical things, or concepts such as time or justice. Be creative, and don't shy from pop culture. (e.g: chat + robot = chatgpt, show + sponge = spongebob)\n" +
            "\n" +
            "description (String): A visual description of the item, formatted like alt text. Do not include vague ideas.  INCLUDE THE ITEM IN THE DESCRIPTION.\n" +
            "\n" +
            "throwable (Boolean): If the item is throwable or not. Throwable items include small objects that make sense to be thrown.\n" +
            "\n" +
            "nutritionalValue (Number): A number between 0 and 1 representing how nutritious the item would be to consume. Items with 0 nutrition are not consumable.  Very nutritious items have a value of 1, such as a full steak.\n" +
            "\n" +
            "attack (Number): A number between 0 and 1 representing the damage dealt by the item. This can also be interpreted as \"hardness\". Feathers have 0, rocks have 0.5. Most items should have a value above 0.\n" +
            "\n" +
            "color (String): The main color of the item. Can be: black, blue, green, orange, purple, red, yellow.\n" +
            "\n" +
            "EXAMPLE INPUT:\n" +
            "Animal + Water\n" +
            "\n" +
            "EXAMPLE OUTPUT:\n" +
            "{\n" +
            "\"item\": \"Fish\",\n" +
            "\"description\": \"A large blue fish with black eyes and a big fin.\",\n" +
            "\"throwable\": true,\n" +
            "\"nutritionalValue\": 0.8,\n" +
            "\"attack\": 0.2,\n" +
            "\"color\": \"blue\"\n" +
            "}\n" +
            "\n" +
            "MISC EXAMPLES:\n" +
            "Player Head + Bone = Body\n" +
            "Show + Sponge = Spongebob\n" +
            "Sand + Sand = Desert";

}