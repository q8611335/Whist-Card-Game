package game.rules;
import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.Game.Rank;
import game.Game.Suit;
import java.util.ArrayList;
import java.util.List;

public class NormalRules implements TrickRules{
    @Override
    public List<Card> getLegalCards(Hand hand, Suit leadSuit,Suit trumpSuit, Rank wizardRank) {
        if(leadSuit == null){
            return new ArrayList<>(hand.getCardList());
        }
        List<Card> legalCards = new ArrayList<>();

        for(Card card : hand.getCardList()){
            if(card.getSuit() == leadSuit || card.getSuit() == trumpSuit){
                legalCards.add(card);
            }
        }
        if(legalCards.isEmpty()){
            return new ArrayList<>(hand.getCardList());
        }
        return legalCards;
    }

    @Override
    public TrickResult determineWinner(
            List<PlayedCard> playedCards,
            Suit trumpSuit,
            Rank wizardRank
    ){
        if(playedCards.isEmpty()){
            throw new IllegalArgumentException("Cannot determine Winner of an empty trick.");
        }
        Suit leadSuit = (Suit) playedCards.get(0).card().getSuit();
        PlayedCard winner = playedCards.get(0);
        for(int i = 1; i < playedCards.size(); i++){
            PlayedCard candidate = playedCards.get(i);
            if(beats(candidate.card(),
                    winner.card(),
                    leadSuit,
                    trumpSuit))
            {winner = candidate;
        }
    }
        return new TrickResult(winner.playerId(), false);
}

private boolean beats(Card candidate, Card currentWinner, Suit leadSuit, Suit trumpSuit){
    int candidatePriority = getSuitPriority(candidate, leadSuit, trumpSuit);
    int winnerPriority = getSuitPriority(currentWinner, leadSuit, trumpSuit);
    if(candidatePriority != winnerPriority){
        return candidatePriority > winnerPriority;
    }
    return candidate.getRankId() < currentWinner.getRankId();
    }

    private int getSuitPriority(Card card, Suit leadSuit, Suit trumpSuit){
        if(card.getSuit() == trumpSuit){
            return 2;
        }
        if(card.getSuit() == leadSuit){
            return 1;
        }
        return 0;
    }
}
