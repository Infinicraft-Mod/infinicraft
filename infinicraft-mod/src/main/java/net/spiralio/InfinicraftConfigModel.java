package net.spiralio;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;

@Modmenu(modId = "infinicraft")
@Config(name = "infinicraft-config", wrapperName = "InfinicraftConfig")
public class InfinicraftConfigModel {

  @SectionHeader("mainSection")
  public boolean SHOW_RECIPE = true;

  public boolean SHOW_DESCRIPTION = true;

  @SectionHeader("chatApiSection")
  public String CHAT_API_KEY = "sk-123456789";

  public String CHAT_API_BASE = "https://api.openai.com/v1";
  public String CHAT_API_MODEL = "gpt-3.5-turbo";
  public int SECONDS_TO_TIMEOUT = 60;
  public String PROMPT =
    """
            You are an API that takes a combination of items in the form "item + item" and find a suitable single JSON output that combines the two. Output ONLY on ENGLISH.
            
            REQUIRED PARAMETERS:
            
            item (String): The output word (on English). Items can be physical things, or concepts such as time or justice. Be creative, and don't shy from pop culture. (e.g: chat + robot = chatgpt, show + sponge = spongebob)
            
            description (String): A visual description of the item on English, formatted like alt text. Do not include vague ideas.  INCLUDE THE ITEM ON ENGLISH IN THE DESCRIPTION.
            
            throwable (Boolean): If the item is throwable or not. Throwable items include small objects that make sense to be thrown.
            
            nutritionalValue (Number): A number between 0 and 1 representing how nutritious the item would be to consume. Items with 0 nutrition are not consumable.  Very nutritious items have a value of 1, such as a full steak.
            
            attack (Number): A number between 0 and 1 representing the damage dealt by the item. This can also be interpreted as "hardness". Feathers have 0, rocks have 0.5. Most items should have a value above 0.
            
            color (String): The main color of the item. Can be: black, blue, green, orange, purple, red, yellow.
            
            EXAMPLE INPUT:
            Animal + Water
            
            EXAMPLE OUTPUT:
            {
            "item": "Fish",
            "description": "A large blue fish with black eyes and a big fin.",
            "throwable": true,
            "nutritionalValue": 0.8,
            "attack": 0.2,
            "color": "blue"
            }
            
            MISC EXAMPLES:
            Player Head + Bone = Body
            Show + Sponge = Spongebob
            Sand + Sand = Desert
            """;
  public boolean IS_OLLAMA = false;

  @SectionHeader("sdApiSection")
  public boolean USE_GENERATOR = false;

  public String SD_DAEMON_BASE = "http://127.0.0.1:17707";
}
