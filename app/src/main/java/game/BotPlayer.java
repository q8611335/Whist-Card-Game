package game;

import ch.aplu.jcardgame.Hand;
import game.heuristic.TrickState;
import game.rules.TrickRules;

/** An automated player; Game treats every bot type (heuristic, AI) the same way. */
public interface BotPlayer {
    BotMove chooseCard(Hand hand, TrickState state, TrickRules rules, int round, int[] scores);
}
