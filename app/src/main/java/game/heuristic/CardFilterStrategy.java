package game.heuristic;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.rules.TrickRules;
import java.util.List;

public interface CardFilterStrategy {
    List<Card> filter(Hand hand, TrickState state, TrickRules rules);

    
} 
    
