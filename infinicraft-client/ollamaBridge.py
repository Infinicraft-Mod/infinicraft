from flask import Flask, request
import requests
import json
import yaml

with open("settings.yaml", "r") as f:  # Load Ollama Endpoint
    settings = yaml.safe_load(f)

app = Flask(__name__)  # Create flask endpoint

@app.route("/<path:path>", methods=["POST"])  # Accept any path
def handle_post_request(path):
    req = request.json  # Get model and messages
    res = requests.post(
        settings["ollamaEndpoint"],
        json={"model": req["model"], "messages": req["messages"], "stream": False},
    )  # Send Ollama request
    out = (
        '{"message": "{'
        + res.json()["message"]["content"]
        .replace("\n", "")
        .replace("'", "\0")
        .replace('"', "'")
        .replace("\0", '"')
        .split("{")[1]
        + '"}'
    )  # Format output properly for infinicraft to understand
    return json.loads(out)  # Send as JSON


if __name__ == "__main__":
    app.run(port=60371)  # Port no one uses
