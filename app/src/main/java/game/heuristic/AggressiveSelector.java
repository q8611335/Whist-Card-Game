package game.heuristic;

import ch.aplu.jcardgame.Card;
import game.rules.TrickRules;

import java.util.List;
import java.util.Collections;

/** Aggressive: always play the strongest candidate. */
public class AggressiveSelector implements CardSelectorStrategy {
    @Override
    public Card select(List<Card> candidates, TrickState state, TrickRules rules, int playerId) {
        CardStrength strength = new CardStrength(state,rules);

        Card strongest = Collections.max(candidates, strength);

        return strongest;
    }
}
