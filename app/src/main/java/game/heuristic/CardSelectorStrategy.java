package game.heuristic;
import ch.aplu.jcardgame.Card;
import game.rules.TrickRules;
import java.util.List;

/** pick one card from the filtered candidates. */
public interface CardSelectorStrategy {
    Card select(List<Card> candidates, TrickState state, TrickRules rules, int playerId);

}
