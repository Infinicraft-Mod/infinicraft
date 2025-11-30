serverPrompt = """
You are an API that takes a combination of items in the form of "item + item" and finds a suitable single JSON output that combines the two. Output ONLY in ENGLISH.

REQUIRED PARAMETERS:

item (String): The output word (in English). Items can be physical things, or concepts such as time or justice. Be creative, and don't shy away from pop culture. (e.g: chat + robot = chatgpt, show + sponge = spongebob)

description (String): A visual description of the item in English, formatted like alt text. Do not include vague ideas.  INCLUDE THE ITEM NAME IN ENGLISH IN THE DESCRIPTION.

rarity (String): The rarity of the item. THE ONLY AVAILABLE RARITIES ARE: "common", "uncommon", "rare", "epic", "legendary". Almost all items are common, higher rarities are given to items that are incredibly hard to obtain. Do not distribute very many non-"common"s. In the base game, only items such as totem's of undying, nether stars, and enchanted golden apples have higher rarities.

throwable (Boolean): If the item is throwable or not. Throwable items include small objects that make sense to be thrown.

nutritionalValue (Number): A number between 0 and 1 representing how nutritious the item would be to consume. Items with 0 nutrition are not consumable. If the item should not be eaten, please put 0! Very nutritious items have a value of 1, such as a steak.

color (String): The main color of the item. Please keep this as one word, all lowercase, such as blue, green, black, grey, or cyan.

toolType (String): ALWAYS include this field. If the item is a tool, ALWAYS specify what type of tool it is from the following options: "pickaxe", "axe", "shovel", "hoe", "sword", "bow", "trident", "shield", "shears", "bucket", "flint_and_steel". If the item is NOT a tool, set this to "none". Pick the closest matching tool type. For example, a "Hatchet" is an "axe", a "Dagger" is a "sword", a "Drill" is a "pickaxe", and a "Spade" is a "shovel".

miningLevel (Number): If the item is a pickaxe, specify its mining level as an integer between 0 and 4. 0 = Wood (Coal/Stone), 1 = Stone (Iron/Lapis), 2 = Iron (Diamond/Redstone/Gold), 3 = Diamond (Obsidian/Ancient Debris), 4 = Netherite. This field is ignored if the item is not a pickaxe. Most pickaxes should be 2 or 3 unless they are very weak or very strong.

miningSpeed (Number): If the item is a tool, specify its mining speed as a number greater than 0. This field is ignored if the item is not a tool. Avoid setting this value too high, the highest value in the base game is 12.0 for diamond and netherite pickaxes. The smallest is 1.0 for wooden pickaxes. Most pickaxes fall between 2.0 and 6.0.

attackSpeed (Number): If the item is a tool, specify its attack speed, how quickly the item can be used to attack as a number greater than 0. This field is ignored if the item is not a tool. Avoid setting this value too high, the highest value in the base game is 4.0 for diamond and netherite hoes. The smallest is 0.8 for wooden and stone axes. Most tools fall between 1.0 and 1.6.

attackDamage (Number): If the item is a tool, specify the damage that the item can deal as a number between -1 and 20. Negative numbers apply a debuff. This field is ignored if the item is not a tool. Avoid setting this value too high, the highest value in the base game is 10.0 for netherite swords. The smallest is 1.0 for all hoes. Most swords fall between 4.0 and 7.0.

durability (Number): If the item is a tool, specify its max durability as a positive integer. This represents how many uses the tool has before it breaks. This field is ignored if the item is not a tool. For reference, wooden tools have 59 durability, stone tools have 131, iron tools have 250, diamond tools have 1561, and netherite tools have 2031 durability. Always try to pick a durability that makes sense for the type of tool and its material.

EXAMPLE INPUT:
Animal + Water

EXAMPLE OUTPUT:
{
"item": "Fish",
"description": "A large blue fish with black eyes and a big fin.",
"rarity": "common",
"throwable": true,
"nutritionalValue": 0.8,
"color": "blue",
"toolType": "none"
}

EXAMPLE TRIDENT OUTPUT:
{
"item": "Poseidon's Trident",
"description": "A majestic trident, shimmering with the power of the ocean.",
"rarity": "uncommon",
"throwable": true,
"nutritionalValue": 0,
"color": "blue",
"toolType": "trident",
"miningSpeed": 1.0,
"attackSpeed": 1.1,
"attackDamage": 9.0,
"durability": 250
}

MISC EXAMPLES:
Player Head + Bone = Body
Show + Sponge = Spongebob
Sand + Sand = Desert
Diamond + Iron Sword = Diamond Sword
"""

ollamaUrl = "http://localhost:11434/api/chat"

ollamaModel = "gemma2:2b"

serverPort = 17707


###################################################################################################


from flask import Flask, request
import requests
import json
import ast
import re
import os
import struct
from base64 import b64encode
import torch
from libs.BitRoss import genLib
from tqdm import tqdm

# Always access files relative to the script location
script_dir = os.path.dirname(os.path.abspath(__file__))


def rel_path(path):
    if path and not os.path.isabs(path):
        return os.path.join(script_dir, path)
    return path


bitross_pth_path = rel_path("libs/BitRoss.pth")
items_json_path = rel_path("items.json")

if not os.path.exists(bitross_pth_path):
    print("BitRoss.pth not found. Downloading...")
    try:
        # Send a GET request to the file URL with stream=True to download the file in chunks
        response = requests.get(
            "https://github.com/OVAWARE/BitRoss/releases/download/BitRoss/BitRoss.pth",
            stream=True,
        )

        # Check if the request was successful (status code 200)
        if response.status_code == 200:
            # Get the total size of the file from the response headers
            total_size = int(response.headers.get("content-length", 0))

            # Create a tqdm progress bar
            with open(bitross_pth_path, "wb") as file:
                for data in tqdm(
                    response.iter_content(chunk_size=1024),
                    total=total_size // 1024,
                    unit="KB",
                    desc="BitRoss.pth",
                ):
                    file.write(data)
            print("BitRoss.pth downloaded successfully.")
        else:
            print(
                f"Failed to download BitRoss.pth. Status code: {response.status_code}"
            )
    except requests.exceptions.RequestException as e:
        print(f"An error occurred while downloading BitRoss.pth: {e}")

app = Flask(__name__)  # Create flask endpoint


def get_json_value(key: str) -> any:
    # Create items.json if it doesn't exist
    if not os.path.exists(items_json_path):
        with open(items_json_path, "w") as file:
            json.dump({}, file)
        print("items.json did not exist and has been created with an empty JSON object.")
        return 0

    try:
        with open(items_json_path, "r") as file:
            content = file.read()
            if not content:
                return 0
            data = json.loads(content)
            return data[key] if key in data else 0
    except json.JSONDecodeError:
        print("Error: Invalid JSON file")
        return 0


def add_json_entry(key: str, value: any) -> bool:
    try:
        # Create items.json if it doesn't exist
        if not os.path.exists(items_json_path):
            with open(items_json_path, "w") as file:
                json.dump({}, file)
            print("items.json did not exist and has been created with an empty JSON object.")

        # Read existing data or start with empty dict if file is empty
        with open(items_json_path, "r") as file:
            content = file.read()
            data = json.loads(content) if content else {}

        # Add/update the key-value pair
        data[key] = value

        # Write back to file
        with open(items_json_path, "w") as file:
            json.dump(data, file, indent=4)
        return True

    except Exception as e:
        print(f"Error: Failed to add entry - {str(e)}")
        return False


def get_icon(name):
    try:
        with open(items_json_path, "r") as file:
            content = file.read()
            if not content:
                return 0
            data = json.loads(content)

            for key, val in data.items():
                if val["name"] == name:
                    return val["iconToSend"]
            return 0

    except json.JSONDecodeError:
        print("Error: Invalid JSON file")


def update_icon_by_item_name(name, value):
    # Create items.json if it doesn't exist
    if not os.path.exists(items_json_path):
        with open(items_json_path, "w") as file:
            json.dump({}, file)
        print(
            f"items.json did not exist and has been created with an empty JSON object."
        )

    try:
        with open(items_json_path, "r") as file:
            content = file.read()
            if not content:
                return None
            data = json.loads(content)

            for key, val in data.items():
                if val["name"] == name:
                    add_json_entry(
                        key,
                        {
                            "name": name,
                            "messageToSend": val["messageToSend"],
                            "iconToSend": value,
                        },
                    )

    except json.JSONDecodeError:
        print("Error: Invalid JSON file")


def wrap_text(text, width):
    if not text or width <= 0:
        return text

    words = text.split()
    lines = []
    current_line = []
    current_length = 0

    for word in words:
        # Check if adding this word exceeds the width
        word_length = len(word)
        if current_length + word_length + len(current_line) <= width:
            current_line.append(word)
            current_length += word_length
        else:
            # Start a new line if current line would exceed width
            if current_line:
                lines.append(" ".join(current_line))
            current_line = [word]
            current_length = word_length

    # Add the last line if it exists
    if current_line:
        lines.append(" ".join(current_line))

    return "\n".join(lines)


def cleanResponse(res: str) -> str:
    extractedJSON = "{" + res.split("{")[1].split("}")[0].replace("\n", "") + "}"
    fixed = re.sub(r"\btrue\b", "True", extractedJSON)
    fixed = re.sub(r"\bfalse\b", "False", fixed)
    fixed = re.sub(r"\bnull\b", "None", fixed)
    fixed = re.sub(r"(?<=\w)'(?=\w)", r"\\'", fixed)
    obj = ast.literal_eval(fixed)
    return obj


@app.route("/gen", methods=["POST"])
def handle_post_request():
    req = request.json  # Get model and messages
    savedItem = get_json_value(req["recipe"])
    if savedItem == 0:
        res = requests.post(
            ollamaUrl,
            json={
                "model": ollamaModel,
                "messages": [
                    {"role": "system", "content": serverPrompt},
                    {"role": "user", "content": req["recipe"]},
                ],
                "stream": False,
            },
        )  # Send Ollama request
        print("Recieved response:\n" + res.json()["message"]["content"])
        cleanedReturn = cleanResponse(res.json()["message"]["content"])
        cleanedReturn["description"] = wrap_text(cleanedReturn["description"], 25)
        out = json.dumps({"message": json.dumps(cleanedReturn)})
        # Format output properly for infinicraft to understand
        add_json_entry(
            req["recipe"],
            {
                "name": cleanedReturn["item"],
                "messageToSend": out,
                "iconToSend": 0,
            },
        )
        return json.loads(out)  # Send as JSON
    else:
        return json.loads(savedItem["messageToSend"])


device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


def encode_image(image):
    texture: list[int] = []
    for x in range(16):
        for y in range(16):
            red, green, blue, alpha = image.getpixel((y, x))
            if alpha < 10:
                texture.append(-1)
                continue
            rgb = red
            rgb = (rgb << 8) + green
            rgb = (rgb << 8) + blue
            texture.append(rgb)
    encoded_data = b64encode(struct.pack(">{}i".format(len(texture)), *texture)).decode(
        "utf-8"
    )
    return encoded_data


@app.route("/img", methods=["GET"])
def generate():
    description = request.args.get("itemDescription")
    name = description.split(" -")[0]

    savedIcon = get_icon(name)

    if savedIcon == 0:
        color = request.args.get("itemColor")

        newIcon = encode_image(
            genLib(device, bitross_pth_path, color + " - " + description)
        )
        update_icon_by_item_name(name, newIcon)
        return {"success": True, "image": newIcon}
    else:
        return savedIcon


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=serverPort)
