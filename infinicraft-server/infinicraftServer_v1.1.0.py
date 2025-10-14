serverPrompt = """
You are an API that takes a combination of items in the form of "item + item" and finds a suitable single JSON output that combines the two. Output ONLY in ENGLISH.

REQUIRED PARAMETERS:

item (String): The output word (in English). Items can be physical things, or concepts such as time or justice. Be creative, and don't shy away from pop culture. (e.g: chat + robot = chatgpt, show + sponge = spongebob)

description (String): A visual description of the item in English, formatted like alt text. Do not include vague ideas.  INCLUDE THE ITEM NAME IN ENGLISH IN THE DESCRIPTION.

throwable (Boolean): If the item is throwable or not. Throwable items include small objects that make sense to be thrown.

nutritionalValue (Number): A number between 0 and 1 representing how nutritious the item would be to consume. Items with 0 nutrition are not consumable. If the item should not be eaten, please put 0! Very nutritious items have a value of 1, such as a steak. Items should rarely be edible, most items should NOT be consumable.

isPoisonous (Boolean): If true, eating the item will cause a poison effect (like a spider's eye or rotten flesh). Poisoned items should be described as causing nausea, damage over time, or other negative symptoms when consumed. Most items should NOT be poisonous unless it makes sense for the combination or is funny in the situation.

attack (Number): A number between 0 and 1 representing the damage that can be dealt by the item. This can also be interpreted as "hardness". Feathers have 0, rocks have 0.5. Most items should have a value above 0.

color (String): The main color of the item. Please keep this as one word, all lowercase, such as blue, green, black, grey, or cyan.

EXAMPLE INPUT:
Animal + Water

EXAMPLE OUTPUT:
{
"item": "Fish",
"description": "A large blue fish with black eyes and a big fin.",
"throwable": true,
"nutritionalValue": 0.8,
"isPoisonous": false,
"attack": 0.2,
"color": "blue"
}

MISC EXAMPLES:
Player Head + Bone = Body
Show + Sponge = Spongebob
Sand + Sand = Desert
"""

ollamaUrl = "http://localhost:11434/api/chat"

serverPort = 17707


###################################################################################################


from flask import Flask, request
import requests
import json
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
    print(f"BitRoss.pth not found. Downloading...")
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
            print(f"BitRoss.pth downloaded successfully.")
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
        print(
            f"items.json did not exist and has been created with an empty JSON object."
        )
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
            print(
                f"items.json did not exist and has been created with an empty JSON object."
            )

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


@app.route("/gen", methods=["POST"])
def handle_post_request():
    req = request.json  # Get model and messages
    savedItem = get_json_value(req["recipe"])
    if savedItem == 0:
        res = requests.post(
            ollamaUrl,
            json={
                "model": "llama3",
                "messages": [
                    {"role": "system", "content": serverPrompt},
                    {"role": "user", "content": req["recipe"]},
                ],
                "stream": False,
            },
        )  # Send Ollama request
        print("Recieved response:\n" + res.json()["message"]["content"])
        cleanedReturn = json.loads(
            "{"
            + res.json()["message"]["content"]
            .replace("\n", "")
            .replace('"', "'")
            .replace("'", "\0")
            .replace("\0", '"')
            .split("{")[1]
            .split("}")[0]
            + "}"
        )
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
