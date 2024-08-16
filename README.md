# Infinicraft Project

Thank you to everyone who has contributed to the mod!

The Minecraft Infinicraft mod from the Blucubed video. Feel free to use this code as long as you credit Blucubed (Spiralio).
Blucubed Discord Server (for answering setup questions or crediting): https://discord.gg/TveN5TTE7J

## Setup

### Mod Setup

IMPORTANT: THIS MOD IS IN A DEVELOPEMENT BUILD! IF YOU AREN'T FAMILIAR WITH PROGRAMMING/MODDING CONCEPTS, IT IS RECOMMENDED TO WAIT FOR A STABLE RELEASE BUILD.

Download the mod's jar from Discord. The mod requires OWO lib and Mod Menu. Also, you must have an OpenAI API key (or a key to any other OpenAI-like API). You should edit key and API base in mod's config.

**[Free Way №1]** If you don't have an OpenAI API key, you may use ShuttleAI (5 requests/min for free). Register [here](https://shuttleai.app/), create an API key and paste the created key and ShuttleAI's base URL (`https://api.shuttleai.app/v1`) in the mod's config.

**[Free Way №2]** If you don't have an OpenAI API key, you may use PawanKrd API. Join [discord](https://discord.gg/pawan), create an API key using /key in #Bot channel and paste the created key and PawanKrd API's base URL (`https://api.pawan.krd/gpt-3.5-unfiltered/v1`) in the mod's config.

**[Free Way №3]** If you don't have an OpenAI API key, you may use Ollama. Download and run [Ollama](https://ollama.com/download), then check "Is Ollama" in the mod's config and set the model to a supported Llama model like `llama3`. Download `infinicraft-client` and move it anywhere. Install Python 3.11. Run `pip install -r requirements.txt` in the `infinicraft-client` folder. If needs be, edit your Ollama endpoint in settings.yml. Finally, launch ollamaBridge in the background and keep it running as you play.

### Image Generator Setup

1. Create an `infinicraft` folder inside of your Minecraft build's `config` folder if it does not already exist.
2. Download `infinicraft-client` and move it anywhere.
3. Install Python 3.11 or higher.
4. Run `pip install -r requirements.txt` in the `infinicraft-client` folder.
5. If you want a quick and simple icon generator that does an... ok job for the most part run `repaint.py`. If you want to try out stable diffusion (which is also quite iffy), you can try running the `stableDiffusion.py` file. On launch, the script will download SD1.5 weights, and will take ~5 GB.
6. Launch Minecraft with the mod.

## Credits

Blucubed; original mod

PatelRahil; tooltips

timaaos; new fork (integrated script into mod!)

Brian Dean Ullery (NonzeroCornet34); Lot's of things

## Basic Structure

This system works in two parts.

### Minecraft Mod (Fabric 1.20.4)

The mod has two items: infinicraft:infinite and infinicraft:infinicrafter. The first acts as a "universal item" and can take on any texture. Textures are stored in a 256-length array of integers representing colors (in decimal form) of a 16x16 sprite, with -1 representing transparency (see `items.json` for sample arrays). The infinicrafter block takes two items and attempt to find a recipe for them.

If a recipe is not stored, the mod sends request to OpenAI API (configurable) and adds item into `items.json`. Then it sends an HTTP request to the backend server (infinicraft-client) to generate a texture for the item with Stable Diffusion.

### Server Code (Python)

A script runs in the background and handles generation of new textures.
It's an HTTP server (default port 17707) that expects a GET request to `/generate` in the format:

    /generate?itemColor=Some+color&itemDescription=Some+item+description+here

It will respond with JSON in the format `{"success":true,"image":"base64 encoded pixel colors"}`

If you use Ollama, the ollamaBridge script runs in the background (default port 8283) and handles communication between infinicraft and Ollama. Unless you changed it yourself, the base URL for Ollama is `https://127.0.0.1:11434`
