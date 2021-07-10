package me.robertlit.runcode;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.robertlit.runcode.data.ExecutionResponse;
import me.robertlit.runcode.data.Language;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CodeExecutor {

    private static final String EXECUTE_URL = "https://emkc.org/api/v2/piston/execute";
    private static final String VERSIONS_URL = "https://emkc.org/api/v2/piston/runtimes";

    private final Gson gson = new Gson();

    private final boolean aliases;
    private final Set<String> languages = new HashSet<>();

    public CodeExecutor(boolean aliases) {
        this.aliases = aliases;
    }

    public CompletableFuture<ExecutionResponse> execute(@NotNull String language, @NotNull String source) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(EXECUTE_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                JsonObject json = new JsonObject();
                json.addProperty("language", language);
                json.addProperty("version", "*");
                JsonArray files = new JsonArray();
                JsonObject file = new JsonObject();
                file.addProperty("content", source);
                files.add(file);
                json.add("files", files);
                try (OutputStream out = connection.getOutputStream()) {
                    byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);
                    out.write(data, 0, data.length);
                }
                if (connection.getResponseCode() != 200) {
                    return ExecutionResponse.FAILED;
                }
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    return gson.fromJson(response.toString(), ExecutionResponse.class);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return ExecutionResponse.FAILED;
            }
        });
    }

    public void loadLanguages() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(VERSIONS_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            Language[] result = gson.fromJson(response.toString(), Language[].class);
            for (Language language : result) {
                languages.add(language.getName());
                if (aliases) {
                    languages.addAll(language.getAliases());
                }
            }
        }
    }

    public Set<String> getLanguages() {
        return languages;
    }
}
