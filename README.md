# Infinicraft Project

Thank you to everyone who has contributed to the mod!

The Minecraft Infinicraft mod from the Blucubed video. Feel free to use this code as long as you credit Blucubed (Spiralio).
Blucubed Discord Server (for answering setup questions or crediting): https://discord.gg/TveN5TTE7J  

## Setup

### Mod Setup
Download the mod's jar from `Releases` tab. The mod requires OWO lib and Mod Menu. Also, you must have an OpenAI API key (or a key to any other OpenAI-like API). You should edit key and API base in mod's config.  
  
**[Free Way №1]** If you don't have an OpenAI API key, you may use ShuttleAI (5 requests/min for free). Register [here](https://shuttleai.app/), create an API key and paste the created key and ShuttleAI's base URL (`https://api.shuttleai.app/v1`) in the mod's config.  
  
**[Free Way №2]** If you don't have an OpenAI API key, you may use PawanKrd API. Join [discord](https://discord.gg/pawan), create an API key using /key in #Bot channel and paste the created key and PawanKrd API's base URL (`https://api.pawan.krd/gpt-3.5-unfiltered/v1`) in the mod's config.  
### Image Generator Setup
1. Create an `infinicraft` folder inside of your Minecraft build's `config` folder. 
2. Download `infinicraft-client` and move its contents into the new `infinicraft` folder. 
3. Download [Plixel LoRA](https://civitai.com/models/102368/plixel-minecraft) (or any other minecraft item LoRA), put it into `models` folder and rename LoRA file to `lora.safetensors` 
4. Install Python 3.11. Run `pip install -r requirements.txt` in `infinicraft` folder.  
5. Run the `clear.py` script before first launch.
6. Launch the `script.py`.  
P.S. On launch, script will download SD1.5 weights, it would take ~5 GB.
7. Launch Minecraft with the mod.

## Credits

Blucubed; original mod

PatelRahil; tooltips

timaaos; new fork (integrated script into mod!)
    
## Basic Structure
This system works in two parts.

### Minecraft Mod (Fabric 1.20.4)
The mod has two items: infinicraft:infinite and infinicraft:infinicrafter. The first acts as a "universal item" and can take on any texture. Textures are stored in a 16x16 2D array of integers representing colors (in decimal form), with -1 representing transparency (see `items.json` for sample arrays). The infinicrafter block takes two items and attempt to find a recipe for them.

If a recipe is not stored, the mod sends request to OpenAI API (configurable) and adds item into `items.json`. The mod also detects which items in `items.json` are "custom" (that is, not a vanilla item/block) and marks them as `custom: true`. This prompts the client code to generate a texture and add it to `items.json`, which the mod then reads and loads.

### Server Code (Python)
A script runs in the background and handles generation of new textures.  
The server iterates through `items.json` to find custom items (`custom: true`) without a `texture` value. The server generates textures using `diffusers` (SD1.5 + LoRA)
