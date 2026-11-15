package game.ai;

import ch.aplu.jcardgame.Card;
import game.BotMove;
import game.BotPlayer;
import ch.aplu.jcardgame.Hand;
import game.heuristic.HeuristicPlayer;
import game.heuristic.HeuristicPlayerFactory;
import game.heuristic.TrickState;
import game.rules.TrickRules;

import java.util.List;
import java.util.Optional;

/**
 * AI bot: asks an LLM for a card (at most once per round) and falls back to a
 * heuristic bot (NAIVE filter, EFFICIENT selection) whenever the LLM cannot be
 * used or does not return a legal card.
 */
public class AIPlayer implements BotPlayer {
    public static final String SOURCE = "AI";
    public static final String FALLBACK_SOURCE = "AI_FALLBACK";

    private final String mode;
    private final LLMService llm;
    private final HeuristicPlayer fallback;
    private final GameHistory history;
    private final RoundRequestLimiter limiter;
    private final PromptBuilder promptBuilder = new PromptBuilder();

    public AIPlayer(int playerId, String mode, LLMService llm, GameHistory history, RoundRequestLimiter limiter) {
        this.mode = mode;
        this.llm = llm;
        this.history = history;
        this.limiter = limiter;
        this.fallback = HeuristicPlayerFactory.create(playerId, "NAIVE", "EFFICIENT");
    }

    @Override
    public BotMove chooseCard(Hand hand, TrickState state, TrickRules rules, int round, int[] scores) {
        // Legal cards (lead, trump or wizard) come from the rules, as clarified by staff on Ed (post #68).
        List<Card> legal = rules.getLegalCards(hand, state.lead(), state.trump(), state.wizardRank());
        // A forced move needs no LLM request.
        if (legal.size() == 1) {
            return new BotMove(legal.get(0), SOURCE);
        }
        if (limiter.tryAcquire(round)) {
            String user = promptBuilder.userPrompt(mode, round, state.trump(), state.wizardRank(),
                    hand.getCardList(), legal, state.playedCards(), history.getTricks(), scores);
            Optional<String> suggestion = llm.suggestCard(promptBuilder.systemPrompt(), user);
            if (suggestion.isPresent()) {
                for (Card card : legal) {
                    if (card.toString().equalsIgnoreCase(suggestion.get())) {
                        return new BotMove(card, SOURCE);
                    }
                }
            }
        }
        return new BotMove(fallback.chooseCard(hand, state, rules), FALLBACK_SOURCE);
    }
}
