package game.rules;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.Game.Rank;
import game.Game.Suit;
import java.util.List;

public interface TrickRules {
    List<Card> getLegalCards(Hand hand, Suit leadSuit, Suit trumpSuit,Rank wizardRank);

    TrickResult determineWinner(
            List<PlayedCard> playedCards,
            Suit trumpSuit,
            Rank wizardRank
    );

    default boolean isWizard(Card card, Suit trumpSuit, Rank wizardRank) {
        return false;
    }
    default boolean usesWizardRank(){
        return false;
    }
}
