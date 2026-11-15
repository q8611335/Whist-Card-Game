package game.ai;

import game.rules.PlayedCard;

import java.util.ArrayList;
import java.util.List;

/** Remembers the cards played in completed tricks, as "P1:CLUBS-ACE" entries per round. */
public class GameHistory {
    private final List<String> tricks = new ArrayList<>();

    public void recordTrick(int round, List<PlayedCard> playedCards, int winner) {
        StringBuilder sb = new StringBuilder("R").append(round).append(": ");
        for (PlayedCard played : playedCards) {
            sb.append('P').append(played.playerId()).append('=').append(played.card()).append(' ');
        }
        sb.append("-> winner P").append(winner);
        tricks.add(sb.toString());
    }

    public List<String> getTricks() {
        return List.copyOf(tricks);
    }
}
