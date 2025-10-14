import torch
import torch.nn as nn
from torchvision import transforms
from PIL import Image
from transformers import BertTokenizer, BertModel
import argparse
import numpy as np
import os
import time  # Import the time module

# Always access files relative to the script location
script_dir = os.path.dirname(os.path.abspath(__file__))

def rel_path(path):
    if path and not os.path.isabs(path):
        return os.path.join(script_dir, path)
    return path

# Import the model architecture from train.py
from libs.train import CVAE, TextEncoder, LATENT_DIM, HIDDEN_DIM

# Initialize the BERT tokenizer
tokenizer = BertTokenizer.from_pretrained("bert-base-uncased")


def clean_image(image, threshold=0.75):
    """
    Clean up the image by setting pixels with opacity <= threshold to 0% opacity
    and pixels above the threshold to 100% visibility.
    """
    np_image = np.array(image)
    alpha_channel = np_image[:, :, 3]
    alpha_channel[alpha_channel <= int(threshold * 255)] = 0
    alpha_channel[alpha_channel > int(threshold * 255)] = 255  # Set to 100% visibility
    return Image.fromarray(np_image)


def generate_image(model, text_prompt, device, input_image=None, img_control=0.5):
    # Encode text prompt using BERT tokenizer
    encoded_input = tokenizer(
        text_prompt, padding=True, truncation=True, return_tensors="pt"
    )
    input_ids = encoded_input["input_ids"].to(device)
    attention_mask = encoded_input["attention_mask"].to(device)

    # Generate text encoding
    with torch.no_grad():
        text_encoding = model.text_encoder(input_ids, attention_mask)

    # Sample from the latent space
    z = torch.randn(1, LATENT_DIM).to(device)

    # Generate image
    with torch.no_grad():
        generated_image = model.decode(z, text_encoding)

    if input_image is not None:
        input_image = input_image.convert("RGBA").resize(
            (16, 16), resample=Image.NEAREST
        )
        input_image = transforms.ToTensor()(input_image).unsqueeze(0).to(device)
        generated_image = (
            img_control * input_image + (1 - img_control) * generated_image
        )

    # Convert the generated tensor to a PIL Image
    generated_image = generated_image.squeeze(0).cpu()
    generated_image = (generated_image + 1) / 2  # Rescale from [-1, 1] to [0, 1]
    generated_image = generated_image.clamp(0, 1)
    generated_image = transforms.ToPILImage()(generated_image)

    return generated_image


def genLib(device, model_path, prompt):
    # Initialize model
    text_encoder = TextEncoder(hidden_size=HIDDEN_DIM, output_size=HIDDEN_DIM)
    model = CVAE(text_encoder).to(device)

    # Load the trained model
    model.load_state_dict(torch.load(model_path, map_location=device))
    model.eval()

    # Generate image from prompt
    generated_image = generate_image(model, prompt, device, None, 0)

    # Clean up the image if the flag is set
    generated_image = clean_image(generated_image)

    # Resize the generated image
    generated_image = generated_image.resize((16, 16), resample=Image.NEAREST)

    return generated_image


def main():
    parser = argparse.ArgumentParser(
        description="Generate an image from a text prompt using the trained CVAE model(s)."
    )
    parser.add_argument("--prompt", type=str, help="Text prompt for image generation")
    parser.add_argument(
        "--prompt_file", type=str, help="File containing prompts, one per line"
    )
    parser.add_argument(
        "--output",
        type=str,
        default="generated_images",
        help="Output directory or file for generated images",
    )
    parser.add_argument(
        "--model_paths", type=str, nargs="*", help="Paths to the trained model(s)"
    )
    parser.add_argument("--model_path", type=str, help="Path to a single trained model")
    parser.add_argument(
        "--clean",
        action="store_true",
        help="Clean up the image by removing low opacity pixels",
    )
    parser.add_argument(
        "--size", type=int, default=16, help="Size of the generated image"
    )
    parser.add_argument(
        "--input_image", type=str, help="Path to the input image for img2img generation"
    )
    parser.add_argument(
        "--img_control",
        type=float,
        default=0.5,
        help="Control how much the input image influences the output (0 to 1)",
    )
    args = parser.parse_args()

    if not args.prompt and not args.prompt_file:
        parser.error("Either --prompt or --prompt_file must be provided")

    if args.model_paths and args.model_path:
        parser.error("Specify either --model_paths or --model_path, not both")

    model_paths = args.model_paths if args.model_paths else [args.model_path]
    model_paths = [rel_path(p) for p in model_paths if p]

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # Check if --output is a file or directory
    output_path = rel_path(args.output)
    is_folder_output = os.path.isdir(output_path)

    if is_folder_output:
        # Ensure output directory exists if it's not a file
        os.makedirs(output_path, exist_ok=True)

    # Load input image if provided
    input_image = None
    if args.input_image:
        input_image = Image.open(rel_path(args.input_image)).convert("RGBA")

    # Process single prompt or batch of prompts
    if args.prompt:
        prompts = [args.prompt]
    else:
        with open(rel_path(args.prompt_file), "r") as f:
            prompts = [line.strip() for line in f if line.strip()]

    for model_path in model_paths:
        # Initialize model
        text_encoder = TextEncoder(hidden_size=HIDDEN_DIM, output_size=HIDDEN_DIM)
        model = CVAE(text_encoder).to(device)

        # Load the trained model
        model.load_state_dict(torch.load(model_path, map_location=device))
        model.eval()

        model_name = os.path.splitext(os.path.basename(model_path))[0]

        for i, prompt in enumerate(prompts):
            start_time = time.time()  # Start timing the generation

            # Generate image from prompt
            generated_image = generate_image(
                model, prompt, device, input_image, args.img_control
            )

            # End timing the generation
            end_time = time.time()
            generation_time = end_time - start_time  # Calculate the generation time

            # Clean up the image if the flag is set
            if args.clean:
                generated_image = clean_image(generated_image)

            # Resize the generated image
            generated_image = generated_image.resize(
                (args.size, args.size), resample=Image.NEAREST
            )

            if not is_folder_output:
                # Save the generated image to the specified file
                output_file = output_path
            else:
                # Save the generated image to the output directory
                output_file = os.path.join(
                    output_path, f"{model_name}_{prompt}_{i:03d}.png"
                )

            generated_image.save(output_file)
            print(
                f"Generated image for prompt '{prompt}' using model '{model_name}' saved as {output_file}"
            )
            print(
                f"Generation time: {generation_time:.10f} seconds"
            )  # Print the generation time


if __name__ == "__main__":
    main()
