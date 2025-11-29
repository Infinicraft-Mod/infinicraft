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

if not os.path.exists(items_json_path):
    with open(items_json_path, "w") as file:
        json.dump({}, file)
    print(f"items.json did not exist and has been created with an empty JSON object.")

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
