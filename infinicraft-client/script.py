import json
import requests
import openai
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from diffusers import StableDiffusionPipeline
from rembg import remove
import time

print("Loading SD...")
pipeline = StableDiffusionPipeline.from_pretrained("runwayml/stable-diffusion-v1-5", use_safetensors=True)
print("Loaded SD, loading LoRA...")
pipeline.load_lora_weights("./models/",weight_name="lora_1.safetensors")
pipeline.to("cuda")
#pipeline.enable_xformers_memory_efficient_attention()

print("Models loaded.")

def dummy(images, **kwargs):
    return images, [False]*len(images)
pipeline.safety_checker = dummy
# doing dummy generation to speed up
pipeline("funny kitten", width=256, height=256, num_inference_steps=5)

# Load API keys
try:
    with open('apiKeys.json', 'r', encoding='utf-8') as f:
        apiKeys = json.load(f)
        openAiKey = apiKeys.get('openai')
        openAiBaseURL = apiKeys.get('api_base')
except (json.JSONDecodeError, FileNotFoundError) as e:
    print("Error loading apiKeys.json:", e)
    openAiKey = ""
    openAiBaseURL = "https://api.openai.com/v1"

client = openai.OpenAI(api_key=openAiKey,base_url=openAiBaseURL)

# Settings
url = "local"  # The texture server's URL (leave None for no server)

# Caches
craft_cache = []
texture_cache = []

class CraftQueueHandler(FileSystemEventHandler):
    def on_modified(self, event):
        if event.src_path.endswith('craftQueue.json'):
            with open('craftQueue.json', 'r', encoding='utf-8') as f:
                file_content = f.read()
            if not file_content:
                return
            
            queue = json.loads(file_content)
            if not queue:
                return

            for recipe in queue:
                recipe_str = ' + '.join(recipe)
                if recipe_str in craft_cache:
                    continue
                craft_cache.append(recipe_str)
                craft(recipe)
            
            with open('craftQueue.json', 'w', encoding='utf-8') as f:
                f.write('[]')

class ItemsHandler(FileSystemEventHandler):
    def on_modified(self, event):
        if event.src_path.endswith('items.json'):
            with open('items.json', 'r', encoding='utf-8') as f:
                file_content = f.read()
            if not file_content:
                return
            
            try:
                items = json.loads(file_content)
            except json.JSONDecodeError as e:
                print(e)
                return

            if url is None:
                return
            
            missing_textures = [item for item in items if item.get('custom') and not item.get('texture') and item['item'] not in texture_cache]

            for item in missing_textures:
                texture_cache.append(item['item'])
                texture(item)

def craft(items):
    recipe = ' + '.join(items)
    print('Crafting:', recipe)

    with open('prompt.txt', 'r', encoding='utf-8') as f:
        prompt = f.read()

    messages = [
        {"role": "system", "content": prompt},
        {"role": "user", "content": recipe}
    ]

    try:
        completion = client.chat.completions.create(
            model='gpt-3.5-turbo',
            messages=messages,
            temperature=0.75
        )
        
        output = json.loads(completion.choices[0].message.content)

        itemName = output['item']
        itemColor = output['color']
        
        print(f'Item crafted: {recipe} = {itemName}')

        with open('recipes.json', 'r', encoding='utf-8') as f:
            recipes = json.load(f)
        
        recipes.append({
            'input': items,
            'output': itemName,
            'color': itemColor
        })

        with open('recipes.json', 'w', encoding='utf-8') as f:
            json.dump(recipes, f, indent=4)

        with open('items.json', 'r', encoding='utf-8') as f:
            items_list = json.load(f)

        if any(itemObject['item'] == itemName for itemObject in items_list):
            return
        items_list.append(output)

        with open('items.json', 'w', encoding='utf-8') as f:
            json.dump(items_list, f, indent=4)

    except Exception as e:
        print("Error during crafting:", e)

def texture(itemObject):
    print('Requesting texture for:', itemObject['item'])
    try:
        im = pipeline("Minecraft item, " + itemObject['description'] + " white background.", guidance_scale=8, width=256, height=256, num_inference_steps=20).images[0]
        im = remove(im)
        im = im.resize((16,16)).convert("RGBA").rotate(90)
        texture = []
        for x in range(16):
            line = []
            for y in range(16):
                red,green,blue,alpha = im.getpixel((x,y))
                if alpha < 10:
                    line.append(-1)
                    continue
                rgb = red
                rgb = (rgb << 8) + green
                rgb = (rgb << 8) + blue
                line.append(rgb)
            texture.append(line)

        with open('items.json', 'r', encoding='utf-8') as f:
            items = json.load(f)
        
        for item in items:
            if item['item'] == itemObject['item']:
                item['texture'] = texture

        with open('items.json', 'w', encoding='utf-8') as f:
            json.dump(items, f, indent=4)

    except requests.RequestException as e:
        print('Error:', e)
        texture(itemObject)

if __name__ == "__main__":
    event_handler_craft = CraftQueueHandler()
    event_handler_items = ItemsHandler()
    observer = Observer()
    observer.schedule(event_handler_craft, '.', recursive=False)
    observer.schedule(event_handler_items, '.', recursive=False)
    observer.start()

    try:
        print('Listening for file changes...')
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()
