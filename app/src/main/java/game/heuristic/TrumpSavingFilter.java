package game.heuristic;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.rules.TrickRules;

import java.util.ArrayList;
import java.util.List;

public class TrumpSavingFilter implements CardFilterStrategy {
    @Override
    public List<Card> filter(Hand hand, TrickState state, TrickRules rules) {
        List<Card> allCards = new ArrayList<>(hand.getCardList());
        if (state.isLeading()) {
            return allCards;
        }

        List<Card> leadOrWizard = new ArrayList<>();
        List<Card> trumps = new ArrayList<>();

        for (Card card : allCards) {
            boolean isLeadSuit = card.getSuit() == state.lead();
            boolean isWizard = rules.isWizard(card, state.trump(), state.wizardRank());
            boolean isTrump = card.getSuit() == state.trump();
            if (isLeadSuit || isWizard) {
                leadOrWizard.add(card);
            } else if (isTrump) {
                trumps.add(card);
            }
        }
        if (!leadOrWizard.isEmpty()) {
            return leadOrWizard;
        }
        if (!trumps.isEmpty()) {
            return trumps;
        }
        return allCards;
    }
}
