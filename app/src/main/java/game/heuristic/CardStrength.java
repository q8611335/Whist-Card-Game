package game.heuristic;

import ch.aplu.jcardgame.Card;
import game.rules.TrickRules;
import java.util.Comparator;
/**
 * Wizard > Trump > Lead suit > Other suits, then higher rank.
 * Tie-break (same category and rank): suit order, SPADES weakest ... CLUBS strongest.
 */
public class CardStrength implements Comparator<Card>{
    private final TrickState state;
    private final TrickRules rules;

    public CardStrength(TrickState state, TrickRules rules) {
        this.state = state;
        this.rules = rules;
    }

    @Override
    public int compare(Card a, Card b) {
        // compare category
        int byCategory = Integer.compare(category(a), category(b));
        if (byCategory != 0) {
            return byCategory;
        }

        // same category, compare rank
        int byRank = Integer.compare(b.getRankId(), a.getRankId()); // reverse as ACE=0
        if (byRank != 0) {
            return byRank;
        }
        // same rank, compare suit
        return Integer.compare(a.getSuitId(), b.getSuitId());

    }
    // 3 = wizard, 2 = trump, 1 = lead suit, 0 = other
    private int category(Card card) {
        boolean isWizard = rules.isWizard(card, state.trump(), state.wizardRank());
        boolean isTrump = card.getSuit() == state.trump();
        boolean isLead = card.getSuit() == state.lead();
        if (isWizard){
            return 3;
        } else if (isTrump) {
            return 2;
        } else if (isLead) {
            return 1;
        }
        return 0;


    }
    }

