# Infinicraft Project

Thank you to everyone who has contributed to the mod!

The Minecraft Infinicraft mod from the Blucubed video. Feel free to use this code as long as you credit Blucubed (Spiralio).
Blucubed Discord Server (for answering setup questions or crediting): [https://discord.gg/TveN5TTE7J](https://discord.gg/TveN5TTE7J)

## Setup

### Mod Setup

IMPORTANT: THIS MOD IS IN A DEVELOPMENT BUILD! IF YOU AREN'T FAMILIAR WITH PROGRAMMING/MODDING CONCEPTS, IT IS RECOMMENDED TO WAIT FOR A STABLE RELEASE BUILD.

Download the mod's jar from Discord. The mod requires OWO lib and Mod Menu. You can change its settings by clicking `Mods` on the main menu, then `Infinicraft`, then the little icon in the top right. ~~By default the mod is set to the public Infinicraft server. This can, however, be changed to a personal server, if you prefer. If you do change servers, I'd recommend purging the cached recipes so that there won't be any client-server discrepancies.~~ **THERE ISN'T A DEDICATED PUBLIC SERVER YET, SO IT'S ACTUALLY JUST SET TO LOCALHOST BUT THERE WILL BE ONE EVENTUALLY!!!**

### Personal Server Setup (DEV SAVVY'S ONLY!)

1. Install Ollama and llama3
2. Download the infinicraft-server folder here on GitHub.
3. Run the server python script.
4. It should now be running on port `17707`, though you can change that as well as the prompt inside the script itself. It's not that hard to find.

## Credits

Blucubed; original mod base

PatelRahil; a few tooltips

timaaos; deprecated fork (integrated script into mod!)

Chara; bitross

Brian Dean Ullery (NonzeroCornet34); literally everything else

## Basic Structure

This system works in two parts.

### Minecraft Mod (Fabric 1.20.4)

The mod has two items: infinicraft:infinite and infinicraft:infinicrafter. The first acts as a "universal item" and can take on any texture. Textures are stored in a 256-length array of integers representing colors (in decimal form) of a 16x16 sprite, with -1 representing transparency (see `items.json` for sample arrays). The infinicrafter block takes two items and attempts to find a cached recipe for them.

If a recipe is not cached, the mod sends a request to an Infinicraft server (configurable) and adds the new item into `items.json`. Then it sends another request to generate a texture for the item with BitRoss.
