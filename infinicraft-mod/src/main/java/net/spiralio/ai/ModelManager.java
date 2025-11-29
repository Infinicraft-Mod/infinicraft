package net.spiralio.ai;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModelManager {

    private static final Path BASE_DIR = FabricLoader.getInstance().getGameDir();

    private static final Path LLAMA_DIR = BASE_DIR.resolve("llama-cli");
    private static final Path MODEL_DIR = BASE_DIR.resolve("infinicraft-models");
    private static final Path MODEL_PATH = MODEL_DIR.resolve("llm.gguf");

    private static final int SERVER_PORT = 17706;

    private static Boolean available = null;
    private static Process serverProcess = null;

    // ✅ SYSTEM ROLE INJECTION
    private static final String SYSTEM_PROMPT = """
            You are an API that takes a combination of items in the form of "item + item" and finds a suitable single JSON output that combines the two. Output ONLY in ENGLISH.
            
            REQUIRED PARAMETERS:
            
            item (String): The output word (in English). Items can be physical things, or concepts such as time or justice. Be creative, and don't shy away from pop culture. (e.g: chat + robot = chatgpt, show + sponge = spongebob)
            
            description (String): A visual description of the item in English, formatted like alt text. Do not include vague ideas.  INCLUDE THE ITEM NAME IN ENGLISH IN THE DESCRIPTION.
            
            throwable (Boolean): If the item is throwable or not. Throwable items include small objects that make sense to be thrown.
            
            nutritionalValue (Number): A number between 0 and 1 representing how nutritious the item would be to consume. Items with 0 nutrition are not consumable. If the item should not be eaten, please put 0! Very nutritious items have a value of 1, such as a steak.
            
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
            "attack": 0.2,
            "color": "blue"
            }
            
            MISC EXAMPLES:
            Player Head + Bone = Body
            Show + Sponge = Spongebob
            Sand + Sand = Desert
            """;

    private static void ensureModelExists() throws IOException {
        if (Files.exists(MODEL_PATH)) return;

        Files.createDirectories(MODEL_DIR);

        try (InputStream in = ModelManager.class.getClassLoader().getResourceAsStream("assets/infinicraft/llm.gguf")) {

            if (in == null) {
                throw new FileNotFoundException("Resource model not found: assets/infinicraft/llm.gguf");
            }

            Files.copy(in, MODEL_PATH, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✅ llm.gguf copied from mod resources.");
        }
    }


    // ✅ Call once at startup or lazily before inference
    public static synchronized void init() {
        try {
            Files.createDirectories(LLAMA_DIR);
            Files.createDirectories(MODEL_DIR);

            // ✅ Ensure model exists FIRST
            ensureModelExists();

            Path binary = getServerBinaryPath();
            if (Files.notExists(binary)) {
                downloadAndInstall();
            }

            available = Files.exists(binary);

            if (available) {
                startServerIfNeeded();
            }

        } catch (Exception e) {
            available = false;
            e.printStackTrace();
        }
    }

    public static String extractContentRaw(String response) {
        String start = "\"content\":\"";
        String end = "\"}}],\"created\"";

        int startIndex = response.indexOf(start);
        int endIndex = response.indexOf(end);

        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            return null;
        }

        startIndex += start.length();

        return response.substring(startIndex, endIndex).replace("\\n", "\n").replace("\\\"", "\"");
    }


    // ✅ Main inference method (ONE ARGUMENT)
    public static synchronized String infer(String prompt) {
        if (available == null) init();
        if (available == null || !available) {
            return "llama.cpp not available";
        }

        try {

            // ✅ Inject system role + user message
            String jsonBody = """
                    {
                      "messages": [
                        { "role": "system", "content": "%s" },
                        { "role": "user", "content": "%s" }
                      ],
                      "temperature": 0.7,
                      "seed": 1
                    }
                    """.formatted(escapeJson(SYSTEM_PROMPT), escapeJson(prompt));

            URL url = new URL("http://127.0.0.1:" + SERVER_PORT + "/v1/chat/completions");
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                return extractContentRaw(response.toString());
            } catch (Exception e) {
                return "error";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"llama-server request failed\"}";
        }
    }

    // =============================
    // SERVER CONTROL
    // =============================

    private static void startServerIfNeeded() throws Exception {
        if (serverProcess != null && serverProcess.isAlive()) return;

        Path binary = getServerBinaryPath();
        System.out.println("Binary Path: " + binary.toAbsolutePath());
        System.out.println("Model Path: " + MODEL_PATH.toAbsolutePath());
        System.out.println("Working Dir: " + LLAMA_DIR.toAbsolutePath());

        if (!Files.exists(binary)) {
            throw new RuntimeException("Binary does NOT exist: " + binary);
        }

        if (!Files.exists(MODEL_PATH)) {
            throw new RuntimeException("Model does NOT exist: " + MODEL_PATH);
        }

        ProcessBuilder pb = new ProcessBuilder(binary.toAbsolutePath().toString(), "-m", MODEL_PATH.toAbsolutePath().toString(), "--port", String.valueOf(SERVER_PORT));

        pb.directory(LLAMA_DIR.toFile());
        pb.redirectErrorStream(true);

        serverProcess = pb.start();

        // ✅ ALWAYS consume output or it may deadlock
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[SERVER] " + line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // ✅ Detect instant crash
        Thread.sleep(1000);

        if (!serverProcess.isAlive()) {
            int exitCode = serverProcess.exitValue();
            throw new RuntimeException("Server process exited immediately with code: " + exitCode);
        }

        System.out.println("✅ Server started successfully.");
    }

    // =============================
// SERVER SHUTDOWN (FABRIC SAFE)
// =============================

    public static synchronized void stopServer() {
        if (serverProcess != null && serverProcess.isAlive()) {
            System.out.println("🛑 Stopping llama server...");

            serverProcess.destroy(); // graceful

            try {
                if (!serverProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.out.println("⚠️ Forcing llama server shutdown...");
                    serverProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                serverProcess.destroyForcibly();
            }

            System.out.println("✅ Llama server stopped.");
        }
    }


    // =============================
    // OS + DOWNLOAD HANDLING
    // =============================

    private static Path getServerBinaryPath() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) return LLAMA_DIR.resolve("llama-server.exe").toAbsolutePath();
        return LLAMA_DIR.resolve("llama-server").toAbsolutePath();
    }

    private static void downloadAndInstall() throws Exception {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        Path zipPath = LLAMA_DIR.resolve("llama.zip");

        if (os.contains("win")) {
            // ✅ Load from resources: assets/infinicraft/llama.zip
            try (InputStream in = ModelManager.class.getClassLoader().getResourceAsStream("assets/infinicraft/llama.zip")) {

                if (in == null) {
                    throw new RuntimeException("Missing resource: assets/infinicraft/llama.zip");
                }

                Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } else {
            // ✅ Download for macOS/Linux
            String url;
            if (os.contains("mac")) {
                url = "https://github.com/ggerganov/llama.cpp/releases/download/b7192/llama-b7192-bin-macos-x64.zip";
            } else {
                url = "https://github.com/ggerganov/llama.cpp/releases/download/b7192/llama-b7192-bin-ubuntu-x64.zip";
            }

            try (InputStream in = URI.create(url).toURL().openStream()) {
                Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        unzip(zipPath, LLAMA_DIR);
        Files.deleteIfExists(zipPath);

        // ✅ Make executable on Linux/macOS only
        if (!os.contains("win")) {
            new ProcessBuilder("chmod", "+x", getServerBinaryPath().toString()).inheritIO().start().waitFor();
        }
    }


    private static void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = targetDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(targetDir)) continue;

                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream out = Files.newOutputStream(outPath)) {
                        zis.transferTo(out);
                    }
                }
            }
        }
    }

    // =============================
    // JSON ESCAPING
    // =============================

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
