import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
from torchvision import transforms
from PIL import Image
import json
import os
import subprocess
from transformers import BertTokenizer, BertModel
import wandb

# Hyperparameters
LATENT_DIM = 128
HIDDEN_DIM = 256


# Custom dataset
class Text2ImageDataset(Dataset):
    def __init__(self, image_dir, metadata_file):
        self.image_dir = image_dir
        with open(metadata_file, "r") as f:
            self.metadata = json.load(f)
        self.transform = transforms.Compose(
            [
                transforms.ToTensor(),
                transforms.Normalize((0.5, 0.5, 0.5, 0.5), (0.5, 0.5, 0.5, 0.5)),
            ]
        )

    def __len__(self):
        return len(self.metadata)

    def __getitem__(self, idx):
        item = self.metadata[idx]
        image_path = os.path.join(self.image_dir, item["file_name"])

        try:
            image = Image.open(image_path).convert("RGBA")
        except FileNotFoundError:
            print(f"Image not found: {image_path}")
            return None, None
        except Exception as e:
            print(f"Error loading image {image_path}: {e}")
            return None, None

        image = self.transform(image)
        prompt = str(item["description"])
        return image, prompt


# Text encoder
class TextEncoder(nn.Module):
    def __init__(self, hidden_size, output_size):
        super(TextEncoder, self).__init__()
        self.bert = BertModel.from_pretrained("bert-base-uncased")
        self.fc = nn.Linear(self.bert.config.hidden_size, output_size)

    def forward(self, input_ids, attention_mask):
        outputs = self.bert(input_ids=input_ids, attention_mask=attention_mask)
        return self.fc(outputs.last_hidden_state[:, 0, :])


# CVAE model
class CVAE(nn.Module):
    def __init__(self, text_encoder):
        super(CVAE, self).__init__()
        self.text_encoder = text_encoder

        # Encoder
        self.encoder = nn.Sequential(
            nn.Conv2d(4, 32, 3, stride=1, padding=1),
            nn.ReLU(),
            nn.Conv2d(32, 64, 3, stride=2, padding=1),
            nn.ReLU(),
            nn.Conv2d(64, 128, 3, stride=2, padding=1),
            nn.ReLU(),
            nn.Flatten(),
            nn.Linear(128 * 4 * 4, HIDDEN_DIM),
        )

        self.fc_mu = nn.Linear(HIDDEN_DIM + HIDDEN_DIM, LATENT_DIM)
        self.fc_logvar = nn.Linear(HIDDEN_DIM + HIDDEN_DIM, LATENT_DIM)

        # Decoder
        self.decoder_input = nn.Linear(LATENT_DIM + HIDDEN_DIM, 128 * 4 * 4)
        self.decoder = nn.Sequential(
            nn.ConvTranspose2d(128, 64, 3, stride=2, padding=1, output_padding=1),
            nn.ReLU(),
            nn.ConvTranspose2d(64, 32, 3, stride=2, padding=1, output_padding=1),
            nn.ReLU(),
            nn.Conv2d(32, 4, 3, stride=1, padding=1),
            nn.Tanh(),
        )

    def encode(self, x, c):
        x = self.encoder(x)
        x = torch.cat([x, c], dim=1)
        mu = self.fc_mu(x)
        logvar = self.fc_logvar(x)
        return mu, logvar

    def decode(self, z, c):
        z = torch.cat([z, c], dim=1)
        x = self.decoder_input(z)
        x = x.view(-1, 128, 4, 4)
        return self.decoder(x)

    def reparameterize(self, mu, logvar):
        std = torch.exp(0.5 * logvar)
        eps = torch.randn_like(std)
        return mu + eps * std

    def forward(self, x, c):
        mu, logvar = self.encode(x, c)
        z = self.reparameterize(mu, logvar)
        return self.decode(z, c), mu, logvar


# Loss function
def loss_function(recon_x, x, mu, logvar):
    BCE = nn.functional.mse_loss(recon_x, x, reduction="sum")
    KLD = -0.5 * torch.sum(1 + logvar - mu.pow(2) - logvar.exp())
    return BCE + KLD


# Updated training function
def train(model, train_loader, optimizer, device, tokenizer):
    model.train()
    train_loss = 0
    for batch_idx, (data, prompt) in enumerate(train_loader):
        data = data.to(device)
        optimizer.zero_grad()

        encoded_input = tokenizer(
            prompt, padding=True, truncation=True, return_tensors="pt"
        )
        input_ids = encoded_input["input_ids"].to(device)
        attention_mask = encoded_input["attention_mask"].to(device)

        text_encoding = model.text_encoder(input_ids, attention_mask)

        recon_batch, mu, logvar = model(data, text_encoding)
        loss = loss_function(recon_batch, data, mu, logvar)
        loss.backward()
        train_loss += loss.item()
        optimizer.step()

        # Log batch-level metrics
        wandb.log(
            {
                "batch_loss": loss.item(),
                "batch_reconstruction_loss": nn.functional.mse_loss(
                    recon_batch, data, reduction="mean"
                ).item(),
                "batch_kl_divergence": (
                    -0.5
                    * torch.sum(1 + logvar - mu.pow(2) - logvar.exp())
                    / data.size(0)
                ).item(),
            }
        )

    avg_loss = train_loss / len(train_loader.dataset)
    return avg_loss


# Updated main function
def main():

    NUM_EPOCHS = 500
    BATCH_SIZE = 128
    LEARNING_RATE = 1e-4

    # New hyperparameters
    SAVE_INTERVAL = 25  # Save model every XXX epochs
    SAVE_INTERVAL_IMAGE = 1  # Save generated image every XXX epochs
    PROJECT_NAME = "BitRoss"
    MODEL_NAME = "BitRoss"
    SAVE_DIR = "/models/BitRoss/"

    if os.path.exists(SAVE_DIR) == False:
        os.makedirs(SAVE_DIR)

    tokenizer = BertTokenizer.from_pretrained("bert-base-uncased")

    if not os.path.exists(SAVE_DIR):
        os.makedirs(SAVE_DIR)

    DATA_DIR = "./trainingData/"
    METADATA_FILE = "./trainingData/metadata.json"

    # Initialize wandb
    wandb.init(
        project=PROJECT_NAME,
        config={
            "LATENT_DIM": LATENT_DIM,
            "HIDDEN_DIM": HIDDEN_DIM,
            "NUM_EPOCHS": NUM_EPOCHS,
            "BATCH_SIZE": BATCH_SIZE,
            "LEARNING_RATE": LEARNING_RATE,
            "SAVE_INTERVAL": SAVE_INTERVAL,
            "MODEL_NAME": MODEL_NAME,
        },
    )

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    dataset = Text2ImageDataset(DATA_DIR, METADATA_FILE)
    train_loader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=True)

    text_encoder = TextEncoder(hidden_size=HIDDEN_DIM, output_size=HIDDEN_DIM)
    model = CVAE(text_encoder).to(device)
    optimizer = optim.Adam(model.parameters(), lr=LEARNING_RATE)

    # Log model architecture
    wandb.watch(model, log="all", log_freq=100)

    for epoch in range(1, NUM_EPOCHS + 1):
        train_loss = train(model, train_loader, optimizer, device, tokenizer)
        print(f"Epoch {epoch}, Loss: {train_loss:.4f}")

        # Log epoch-level metrics
        wandb.log(
            {
                "epoch": epoch,
                "train_loss": train_loss,
            }
        )

        # Generate image and save model every SAVE_INTERVAL epochs
        if epoch % SAVE_INTERVAL_IMAGE == 0:
            # Generate image
            output_image = f"{SAVE_DIR}output_epoch_{epoch}.png"

            # Generate image using the current model state
            from libs.BitRoss import generate_image

            prompt = (
                "A blue sword made of diamond"  # You can change this prompt as needed
            )
            generated_image = generate_image(model, prompt, device)
            generated_image.save(output_image)

            # Upload generated image to wandb
            wandb.log(
                {
                    "generated_image": wandb.Image(
                        output_image,
                        caption=f"Generated at epoch {epoch} with prompt {prompt}",
                    )
                }
            )

        if epoch % SAVE_INTERVAL == 0:
            model_save_path = f"{SAVE_DIR}{MODEL_NAME}_epoch_{epoch}.pth"
            torch.save(model.state_dict(), model_save_path)
            print(f"Model saved to {model_save_path}")

        # Log sample reconstructions
        if epoch % 10 == 0:
            model.eval()
            with torch.no_grad():
                sample_data, sample_prompt = next(iter(train_loader))
                sample_data = sample_data[:4].to(device)  # Take first 4 samples
                encoded_input = tokenizer(
                    sample_prompt[:4],
                    padding=True,
                    truncation=True,
                    return_tensors="pt",
                )
                input_ids = encoded_input["input_ids"].to(device)
                attention_mask = encoded_input["attention_mask"].to(device)
                text_encoding = model.text_encoder(input_ids, attention_mask)
                recon_batch, _, _ = model(sample_data, text_encoding)

                # Denormalize and convert to PIL images
                original_images = [
                    transforms.ToPILImage()((sample_data[i] * 0.5 + 0.5).cpu())
                    for i in range(4)
                ]
                reconstructed_images = [
                    transforms.ToPILImage()((recon_batch[i] * 0.5 + 0.5).cpu())
                    for i in range(4)
                ]

                wandb.log(
                    {
                        f"original_vs_reconstructed_{i}": [
                            wandb.Image(original_images[i], caption=f"Original {i}"),
                            wandb.Image(
                                reconstructed_images[i], caption=f"Reconstructed {i}"
                            ),
                        ]
                        for i in range(4)
                    }
                )

    wandb.finish()


if __name__ == "__main__":
    main()
