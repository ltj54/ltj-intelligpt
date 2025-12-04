package no.ltj.intelligpt.gpt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAiClient {

    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    public static String rewriteFile(String apiKey,
                                     String model,
                                     String instruction,
                                     String fileContent) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        JsonObject root = new JsonObject();
        root.addProperty("model", model);

        JsonArray messages = new JsonArray();

        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content",
                "You are an assistant that rewrites source files. " +
                        "Return ONLY the new full file contents, " +
                        "with no markdown, no explanation – just the raw file.");
        messages.add(system);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");

        StringBuilder sb = new StringBuilder();
        sb.append("Instruction from user:\n")
                .append(instruction)
                .append("\n\n")
                .append("Current file contents:\n")
                .append(fileContent);

        user.addProperty("content", sb.toString());
        messages.add(user);

        root.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(root.toString()))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        if (status != 200) {
            throw new RuntimeException("OpenAI error " + status + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray choices = json.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new RuntimeException("OpenAI response had no choices: " + response.body());
        }

        JsonObject message = choices
                .get(0).getAsJsonObject()
                .getAsJsonObject("message");

        if (message == null || !message.has("content")) {
            throw new RuntimeException("OpenAI response had no message.content: " + response.body());
        }

        return message.get("content").getAsString();
    }
}
