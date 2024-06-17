import json
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from diffusers import StableDiffusionPipeline
from rembg import remove
import time

print("Loading SD...")
pipeline = StableDiffusionPipeline.from_pretrained("runwayml/stable-diffusion-v1-5", use_safetensors=True)
print("Loaded SD, loading LoRA...")
pipeline.load_lora_weights("./models/",weight_name="lora.safetensors")
pipeline.to("cuda")
pipeline.enable_xformers_memory_efficient_attention()

def dummy(images, **kwargs):
    return images, [False]*len(images)
pipeline.safety_checker = dummy
print("Models loaded.")

# Caches
texture_cache = []

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
            
            missing_textures = [item for item in items if item.get('custom') and not item.get('texture') and item['item'] not in texture_cache]

            for item in missing_textures:
                texture_cache.append(item['item'])
                texture(item)

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

    except Exception as e:
        print('Error:', e)

if __name__ == "__main__":
    event_handler_items = ItemsHandler()
    observer = Observer()
    observer.schedule(event_handler_items, '.', recursive=False)
    observer.start()
    
    try:
        print('Server ready.')
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()
