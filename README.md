# Infinicraft Project
**Original mod made by Blucubed (Spiralio), new fork made by timaaos**  
The Minecraft Infinicraft mod from Blucubed video. Feel free to use this code as long as you credit me (Spiralio or Blucubed). If you want to re-release any part of this code, talk to me first.
Blucubed Discord Server (for answering setup questions or crediting): https://discord.gg/TveN5TTE7J

⚠️ This is neither polished nor accessible! You will probably need some understanding of coding to get this to work. 

## Basic Structure
This system works in two parts.

### Minecraft Mod (Fabric 1.20.4)
The mod has two items: infinicraft:infinite and infinicraft:infinicrafter. The first acts as a "universal item" and can take on any texture. Textures are stored in a 16x16 2D array of integers representing colors (in decimal form), with -1 representing transparency (see `items.json` for sample arrays). The infinicrafter block takes two items and attempt to find a recipe for them.

If a recipe is not stored, the mod sends request to OpenAI API (configurable) and adds item into `items.json`. The mod also detects which items in `items.json` are "custom" (that is, not a vanilla item/block) and marks them as `custom: true`. This prompts the client code to generate a texture and add it to `items.json`, which the mod then reads and loads.

### Server Code (Python)
A script runs in the background and handles generation of new textures.  
The server iterates through `items.json` to find custom items (`custom: true`) without a `texture` value. The server generates textures using `diffusers` (SD1.5 + LoRA)

## Setup

### Mod Setup
Download mod's jar from `Releases` tab. Mod requires OWO lib and Mod Menu. Also, you must have an OpenAI API key (or a key to any other OpenAI-like API). You should edit key and API base in mod's config.  

### Server Code Setup
1. Create a `infinicraft` folder inside of you Minecraft build's `config` folder. 
2. Download `infinicraft-client` and move its contents into the new `infinicraft` folder. 
3. Download [Plixel LoRA](https://civitai.com/models/102368/plixel-minecraft) and put it into `models` folder, rename LoRA file to `lora_1.safetensors` 
4. Install Python 3.11. Run `pip install -r requirements.txt` in `infinicraft` folder.
P.S. On launch, script will download SD1.5 weights, it would take ~5 GB.
