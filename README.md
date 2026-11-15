# Whist: Heuristic and LLM Card Players

SWEN30006 Software Modelling and Design (University of Melbourne), Project 2, team project.

A Java card game of Whist built on JGameGrid. Each seat can be a human, a random bot, a configurable heuristic bot, or an AI player that asks a large language model (through OpenRouter) which card to play.

## Team

- Chufan Li — Feature 1
- Ya-Chu Yang — Feature 2
- Devi Tara Avalokita — Feature 3

## Run it

Requires Java 21. Gradle downloads the other dependencies.

```bash
./gradlew run                                            # uses properties/game1.properties
./gradlew run --args="properties/game2.properties"       # another configuration
./gradlew test
```

Game settings (player types, heuristic filter and selector, AI model, timing) are in `app/src/main/resources/properties/`.

### AI player setup

The AI player needs an OpenRouter API key. The key is **not** in this repository.

1. Copy `app/src/main/resources/.env.example` to `.env` in the same folder.
2. Replace the placeholder with your own key.

`.env` is listed in `.gitignore`. Without a key, the AI player cannot call the model.

## Package layout

- `game`: Game, Driver, BotPlayer, BotMove, BrokeRuleException, GameCallback, PropertiesLoader
- `game.ai`: AIPlayer, LLMService, OpenRouterClient, PromptBuilder, GameHistory, RoundRequestLimiter, EnvironmentFile
- `game.heuristic`: HeuristicPlayer, HeuristicPlayerFactory, CardFilterStrategy, CardSelectorStrategy, NaiveLegalFilter, TrumpSavingFilter, AggressiveSelector, EfficientSelector, CardStrength, TrickState
- `game.rules`: TrickRules, NormalRules, ThroneBreakerRules, PlayedCard, TrickResult

## Documentation

Static and dynamic design models (UML) are in `documentation/`.
