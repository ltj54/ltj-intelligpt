package no.ltj.intelligpt.backend;

import com.google.gson.Gson;
import com.intellij.openapi.ui.Messages;
import no.ltj.intelligpt.settings.IntelliGPTSettingsState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BackendClient {

    private static final Gson gson = new Gson();

    public static String send(String type, String input) {

        try {
            String backendUrl = IntelliGPTSettingsState.getInstance().backendUrl;

            if (backendUrl == null || backendUrl.isBlank()) {
                Messages.showErrorDialog(
                        "Backend URL is not configured.\n" +
                                "Open Settings → LTJ IntelliGPT and set a backend address.",
                        "LTJ IntelliGPT");
                return null;
            }

            String endpoint = backendUrl + "/api/intelligpt/generate";

            // Build JSON body
            Map<String, String> body = new HashMap<>();
            body.put("type", type);
            body.put("input", input);

            String json = gson.toJson(body);

            // Create HTTP connection
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send JSON
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                Messages.showErrorDialog(
                        "Backend returned HTTP " + responseCode +
                                "\nCheck backend logs or configuration.",
                        "LTJ IntelliGPT Backend Error");
                return null;
            }

            // Read response
            StringBuilder response = new StringBuilder();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            conn.disconnect();

            // Parse response JSON -> extract "result"
            Map resultMap = gson.fromJson(response.toString(), Map.class);
            Object result = resultMap.get("result");

            return result == null ? "" : result.toString();

        } catch (Exception e) {
            Messages.showErrorDialog(
                    "Unable to connect to backend:\n" + e.getMessage(),
                    "LTJ IntelliGPT Connection Error");
            return null;
        }
    }
}
