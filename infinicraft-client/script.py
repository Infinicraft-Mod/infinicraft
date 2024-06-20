#!env python3

from base64 import b64encode
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, HTTPServer, ThreadingHTTPServer
import json
import struct
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from diffusers import StableDiffusionPipeline
from rembg import remove
import time
import urllib.parse

print("Loading SD...")
pipeline = StableDiffusionPipeline.from_pretrained("runwayml/stable-diffusion-v1-5", use_safetensors=True)
print("Loaded SD, loading LoRA...")
pipeline.load_lora_weights("./models/",weight_name="Plixel-SD-1.5.safetensors")
pipeline.to("cuda")
pipeline.enable_xformers_memory_efficient_attention()

def dummy(images, **kwargs):
    return images, [False]*len(images)
pipeline.safety_checker = dummy
print("Models loaded.")

# Caches
texture_cache = []

def texture(item_description: str):
    print('Requesting texture for:', item_description)
    im = pipeline("Minecraft item, " + item_description + " white background.", guidance_scale=8, width=256, height=256, num_inference_steps=20).images[0]
    im = remove(im)
    im = im.resize((16,16)).convert("RGBA").rotate(90)
    texture: list[int] = []
    for x in range(16):
        for y in range(16):
            red,green,blue,alpha = im.getpixel((x,y))
            if alpha < 10:
                texture.append(-1)
                continue
            rgb = red
            rgb = (rgb << 8) + green
            rgb = (rgb << 8) + blue
            texture.append(rgb)
    return texture

class HttpRequestHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        """Serve a GET request."""

        url = urllib.parse.urlparse(self.path)
        qs = urllib.parse.parse_qs(url.query)

        if url.path != '/generate':
            self.send_response(HTTPStatus.NOT_FOUND)
            #self.send_header("Content-Length", "0")
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            self.wfile.write(json.dumps({"success": False}).encode('utf-8'))
            return

        item_description = qs.get('itemDescription', None)
        if item_description is None:
            self.send_response(HTTPStatus.BAD_REQUEST)
            #self.send_header("Content-Length", "0")
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            self.wfile.write(json.dumps({"success": False}).encode('utf-8'))
            return

        try:
            texture_result = texture(item_description[0])
        except Exception as err:
            print(err)

            self.send_response(HTTPStatus.INTERNAL_SERVER_ERROR)
            #self.send_header("Content-Length", "0")
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.end_headers()
            self.wfile.write(json.dumps({"success": False}).encode('utf-8'))
            return

        self.send_response(HTTPStatus.OK)
        #self.send_header("Content-Length", "0")
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.end_headers()

        texture_bytes = struct.pack('<{}i'.format(len(texture_result)), *texture_result)
        self.wfile.write(json.dumps({"success": True, "image": b64encode(texture_bytes).decode('utf-8')}).encode('utf-8'))

if __name__ == "__main__":
    httpd = ThreadingHTTPServer(('', 17707), HttpRequestHandler)
    httpd.serve_forever()

    #try:
    #    print('Server ready.')
    #    while True:
    #        time.sleep(1)
    #except KeyboardInterrupt:
    #    pass
