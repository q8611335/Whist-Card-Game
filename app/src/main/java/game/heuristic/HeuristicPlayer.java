package game.heuristic;

import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Hand;
import game.BotMove;
import game.BotPlayer;
import game.rules.TrickRules;

import java.util.List;
/**
 * heuristic-based bot: STEP 1 filter, STEP 2 select.
 * Both steps are strategies, so any filter can be combined with any selector.
 */
public class HeuristicPlayer implements BotPlayer {
    public static final String SOURCE = "HEURISTIC";

    private final int playerId;
    private final CardFilterStrategy filter;
    private final CardSelectorStrategy selector;

    public HeuristicPlayer(int playerId, CardFilterStrategy filter, CardSelectorStrategy selector) {
        this.playerId = playerId;
        this.filter = filter;
        this.selector = selector;
    }

    @Override
    public BotMove chooseCard(Hand hand, TrickState state, TrickRules rules, int round, int[] scores) {
        return new BotMove(chooseCard(hand, state, rules), SOURCE);
    }

    public Card chooseCard(Hand hand, TrickState state, TrickRules rules) {
        List<Card> candidates = filter.filter(hand, state, rules);
        return selector.select(candidates, state, rules, playerId);
    }

}
