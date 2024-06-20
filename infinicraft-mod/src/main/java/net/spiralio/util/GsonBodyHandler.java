package net.spiralio.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

@SuppressWarnings("DuplicatedCode")
public class GsonBodyHandler {
    public static <T> HttpResponse.BodyHandler<Supplier<T>> ofJson(Gson gson, Class<T> cls) {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), inputStream -> () -> {
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return gson.fromJson(reader, cls);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> HttpResponse.BodyHandler<Supplier<T>> ofJson(Gson gson, TypeToken<T> tt) {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(), inputStream -> () -> {
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return gson.fromJson(reader, tt);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
