package game;

import ch.aplu.jcardgame.Card;

/** A card chosen by a bot, with the source label written to the game log. */
public class BotMove {
    private final Card card;
    private final String source;

    public BotMove(Card card, String source) {
        this.card = card;
        this.source = source;
    }

    public Card card() {
        return card;
    }

    public String source() {
        return source;
    }
}
