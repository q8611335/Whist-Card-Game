package game.ai;

import java.util.Optional;

/** Abstraction over an LLM provider; implementations must never throw. */
public interface LLMService {
    /** Returns the card (SUIT-RANK) suggested by the model, or empty if unavailable or unusable. */
    Optional<String> suggestCard(String systemPrompt, String userPrompt);
}
