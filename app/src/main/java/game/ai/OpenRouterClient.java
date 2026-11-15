package game.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Calls OpenRouter for an AI suggestion; the game validates and selects the card. */
public final class OpenRouterClient implements LLMService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json");
    private static final Pattern CARD_PATTERN = Pattern.compile(
            "\\b(SPADES|HEARTS|DIAMONDS|CLUBS)-"
                    + "(ACE|KING|QUEEN|JACK|TEN|NINE|EIGHT|SEVEN|SIX|FIVE|FOUR|THREE|TWO)\\b",
            Pattern.CASE_INSENSITIVE);

    private final String apiKey;
    private final String model;
    private final OkHttpClient client;

    public OpenRouterClient(String apiKey, String model, int timeoutMs) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public Optional<String> suggestCard(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", model);
            body.addProperty("temperature", 0.0);
            // Reasoning models count hidden thinking against this budget. A very small
            // budget can finish with content=null before the model emits the card.
            body.addProperty("max_tokens", 1024);
            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", systemPrompt);
            messages.add(system);
            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", userPrompt);
            messages.add(user);
            body.add("messages", messages);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return Optional.empty();
                return parseSuggestedCard(response.body().string());
            }
        } catch (Exception ignored) {
            // The caller falls back to the heuristic bot.
        }
        return Optional.empty();
    }

    /** Extracts a card without assuming that every provider returns the same content shape. */
    static Optional<String> parseSuggestedCard(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return Optional.empty();

        try {
            JsonElement rootElement = JsonParser.parseString(responseBody);
            if (!rootElement.isJsonObject()) return Optional.empty();

            JsonArray choices = arrayMember(rootElement.getAsJsonObject(), "choices");
            if (choices == null || choices.isEmpty() || !choices.get(0).isJsonObject()) {
                return Optional.empty();
            }

            JsonObject message = objectMember(choices.get(0).getAsJsonObject(), "message");
            if (message == null) return Optional.empty();

            String content = contentText(message.get("content"));
            Matcher matcher = CARD_PATTERN.matcher(content);
            return matcher.find()
                    ? Optional.of(matcher.group().toUpperCase())
                    : Optional.empty();
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private static JsonArray arrayMember(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : null;
    }

    private static JsonObject objectMember(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static String contentText(JsonElement content) {
        if (content == null || content.isJsonNull()) return "";
        if (content.isJsonPrimitive() && content.getAsJsonPrimitive().isString()) {
            return content.getAsString();
        }
        if (!content.isJsonArray()) return "";

        StringBuilder text = new StringBuilder();
        for (JsonElement part : content.getAsJsonArray()) {
            if (!part.isJsonObject()) continue;
            JsonElement partText = part.getAsJsonObject().get("text");
            if (partText != null && partText.isJsonPrimitive()
                    && partText.getAsJsonPrimitive().isString()) {
                text.append(partText.getAsString()).append(' ');
            }
        }
        return text.toString();
    }
}
