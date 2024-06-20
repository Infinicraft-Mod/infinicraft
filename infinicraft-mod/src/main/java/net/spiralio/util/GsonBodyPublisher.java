package net.spiralio.util;

import com.google.gson.Gson;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

public class GsonBodyPublisher {
    public static HttpRequest.BodyPublisher ofJson(Gson gson, Object value) {
        return HttpRequest.BodyPublishers.ofString(JsonHandler.GSON.toJson(value), StandardCharsets.UTF_8);
    }
}
