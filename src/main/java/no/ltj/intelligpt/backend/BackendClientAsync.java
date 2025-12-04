package no.ltj.intelligpt.backend;

import com.google.gson.Gson;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import no.ltj.intelligpt.settings.IntelliGPTSettingsState;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BackendClientAsync {

    private static final Gson gson = new Gson();

    public interface Callback {
        void onSuccess(String result);
        void onError(String message);
    }

    public static void sendAsync(Project project, String type, String input, Callback callback) {
        new Task.Backgroundable(project, "LTJ IntelliGPT is thinking…", true) {
            @Override
            public void run(ProgressIndicator indicator) {
                try {
                    indicator.setText("Calling LTJ backend…");

                    String backendUrl = IntelliGPTSettingsState.getInstance().backendUrl;
                    if (backendUrl == null || backendUrl.isBlank()) {
                        invokeError(callback, "Backend URL is not configured. Set it under Settings → LTJ IntelliGPT.");
                        return;
                    }

                    String endpoint = backendUrl + "/api/intelligpt/generate";

                    Map<String, String> body = new HashMap<>();
                    body.put("type", type);
                    body.put("input", input);
                    String json = gson.toJson(body);

                    URL url = new URL(endpoint);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        invokeError(callback, "Backend returned HTTP " + responseCode);
                        conn.disconnect();
                        return;
                    }

                    StringBuilder response = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                    }
                    conn.disconnect();

                    Map resultMap = gson.fromJson(response.toString(), Map.class);
                    Object result = resultMap.get("result");
                    String text = result == null ? "" : result.toString();
                    invokeSuccess(callback, text);

                } catch (Exception ex) {
                    invokeError(callback, "Error calling backend: " + ex.getMessage());
                }
            }
        }.queue();
    }

    private static void invokeSuccess(Callback cb, String result) {
        SwingUtilities.invokeLater(() -> cb.onSuccess(result));
    }

    private static void invokeError(Callback cb, String msg) {
        SwingUtilities.invokeLater(() -> cb.onError(msg));
    }
}
