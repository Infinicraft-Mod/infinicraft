package net.spiralio.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonHandler {

    // Reads a JSON array from a filepath
    public static JsonArray readArray(String filePath, String reason) {
        JsonArray result = null;
//        System.out.println("Reading array: " + reason); // minimize use of this function!

        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(filePath);
            result = gson.fromJson(reader, JsonArray.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Got an EOF error");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    // Writes a JSON array to a filepath
    public static void writeArray(String filePath, JsonArray data, String reason) {
//        System.out.println("Writing array: " + reason);  // minimize use of this function!

        try {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter(filePath);
            gson.toJson(data, writer);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
