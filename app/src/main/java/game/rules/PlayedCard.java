package game.rules;

import ch.aplu.jcardgame.Card;

public record PlayedCard(
        int playerId,
        Card card
) {
}
