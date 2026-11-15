package game.heuristic;

import game.Game.Rank;
import game.Game.Suit;
import game.rules.PlayedCard;

import java.security.cert.LDAPCertStoreParameters;
import java.util.List;
import ch.aplu.jcardgame.Card;

/** Everything a heuristic bot needs to know about the current trick. */
public record TrickState(Suit trump, Rank wizardRank, List<PlayedCard> playedCards) {
    // True if nobody has played yet, eg. the bot is leading.
    public boolean isLeading(){
        return playedCards.isEmpty();
    }

    // Suit of the first card played, or null when bot leading.
    public Suit lead() {
        if (isLeading()){
            return null;    // no one played card yet, not lead suit     
        }

        PlayedCard firstPlayedCard = playedCards.get(0);
        Card firstCard = firstPlayedCard.card();
        Suit leadSuit = (Suit) firstCard.getSuit();
        return leadSuit;

        
    }
} 