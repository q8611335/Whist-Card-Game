package game.heuristic;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.rules.TrickRules;

import java.util.ArrayList;
import java.util.List;
// keep every playable card
public class NaiveLegalFilter implements CardFilterStrategy {


    @Override
    public List<Card> filter(Hand hand, TrickState state, TrickRules rules) {
        // bot leading then no filter (every card is a playable)
        if (state.isLeading()) {
            return new ArrayList<>(hand.getCardList());
        }

        // all playable cards (lead suit, trump, wizard)
        return rules.getLegalCards(hand, state.lead(), state.trump(), state.wizardRank());
    }
}
