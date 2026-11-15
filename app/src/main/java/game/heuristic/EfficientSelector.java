package game.heuristic;

import ch.aplu.jcardgame.Card;
import game.rules.TrickRules;
import game.rules.PlayedCard;
import game.rules.TrickResult;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class EfficientSelector implements CardSelectorStrategy {
    @Override
    public Card select(List<Card> candidates, TrickState state, TrickRules rules, int playerId) {
        CardStrength strength = new CardStrength(state,rules);
        if (state.isLeading()){
            return Collections.min(candidates, strength);
        }
        List<Card> winning = new ArrayList<>();
        for (Card card : candidates) {
            if (wouldWin(card, state, rules, playerId)) {
                winning.add(card);
            }
        }
        if (!winning.isEmpty()){
            return Collections.min(winning, strength);
            }
        return Collections.min(candidates, strength);
        }

    /** Simulates playing the card and asks the game rules who would be winning. */
    private boolean wouldWin(Card card, TrickState state, TrickRules rules, int playerId) {
        // copy current played card record
        List<PlayedCard> simulated = new ArrayList<>(state.playedCards());
        // check if card is ordinary off-suit card
//        boolean ordinaryOffSuit = card.getSuit() != state.lead()
//                                    && card.getSuit() != state.trump()
//                                    && !rules.isWizard(card, state.trump(), state.wizardRank());
        // simulated if card is played
        simulated.add(new PlayedCard(playerId, card));

        // check rules to determine winner
        TrickResult result = rules.determineWinner(simulated, state.trump(), state.wizardRank());
        return result.winnerId() == playerId;
    }
}
