package game.rules;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.Game.Rank;
import game.Game.Suit;
import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

public class ThroneBreakerRules implements TrickRules{
    private final NormalRules normalRules = new NormalRules();

    @Override
    public List<Card> getLegalCards(Hand hand, Suit leadSuit, Suit trumpSuit, Rank wizardRank){
        if(leadSuit == null){
            return new ArrayList<>(hand.getCardList());
        }
        List<Card> legalCards = new ArrayList<>();
        for(Card card : hand.getCardList()){
            if(card.getSuit() == leadSuit || card.getSuit() == trumpSuit||isWizard(card, trumpSuit, wizardRank)){
                legalCards.add(card);
            }
        }

        if(legalCards.isEmpty()){
            return new ArrayList<>(hand.getCardList());
        }
        return legalCards;
    }
    @Override
    public boolean isWizard(Card card, Suit trumpSuit, Rank wizardRank){
        return wizardRank != null && card.getRank() == wizardRank && card.getSuit() != trumpSuit;
    }

    @Override
    public boolean usesWizardRank(){
        return true;
    }

    @Override
    public TrickResult determineWinner(
            List<PlayedCard> playedCards,
            Suit trumpSuit,
            Rank wizardRank
    ){
        if(playedCards.isEmpty()){
            throw new IllegalArgumentException("Cannot determine winner for an empty trick.");
        }
        if(isRevolution(playedCards, trumpSuit, wizardRank)){
            return determineRevolutionWinner(playedCards);
        }
        for(int i = playedCards.size() - 1; i >= 0; i--){
            PlayedCard playedCard = playedCards.get(i);
            if(isWizard(playedCard.card(), trumpSuit, wizardRank)){
                return new TrickResult(playedCard.playerId(), false);
            }
        }
        return normalRules.determineWinner(playedCards, trumpSuit, wizardRank);
    }

    private boolean isRevolution(List<PlayedCard> playedCards,  Suit trumpSuit, Rank wizardRank){
        Suit leadSuit = (Suit) playedCards.get(0).card().getSuit();
        Set<Suit> playedSuits = EnumSet.noneOf(Suit.class);
        boolean hasOrdinaryOffSuit = false;
        for(PlayedCard playedCard : playedCards){
            Card card = playedCard.card();
            playedSuits.add((Suit)card.getSuit());
            if(isOrdinaryOffSuit(card, leadSuit, trumpSuit, wizardRank)){
                hasOrdinaryOffSuit = true;
            }
        }
        return hasOrdinaryOffSuit && playedSuits.size() == Suit.values().length;
    }

    private boolean isOrdinaryOffSuit(Card card, Suit leadSuit, Suit trumpSuit, Rank wizardRank){
        return card.getSuit() != leadSuit && card.getSuit() != trumpSuit && !isWizard(card, trumpSuit, wizardRank);
    }

    private TrickResult determineRevolutionWinner(
            List<PlayedCard> playedCards
    ){
        PlayedCard winner = playedCards.get(0);
        for(int i = 1; i < playedCards.size(); i++){
            PlayedCard candidate = playedCards.get(i);
            if(candidate.card().getRankId() <= winner.card().getRankId()){
                winner = candidate;
            }
        }
        return new TrickResult(winner.playerId(), true);
    }
}
