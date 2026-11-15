package game.ai;

import ch.aplu.jcardgame.Card;
import game.Game.Rank;
import game.Game.Suit;
import game.rules.PlayedCard;

import java.util.List;

/** Builds short prompts containing only the game information the LLM needs. */
public class PromptBuilder {
    // Legal-move wording follows the staff answer on Ed (post #68): a player may choose between a lead-suit
    // card and a trump card (or a wizard card in Throne Breaker mode), not lead suit first.
    private static final String SYSTEM = "You play 4-player Whist. Ranks from high to low: "
            + "ACE KING QUEEN JACK TEN NINE EIGHT SEVEN SIX FIVE FOUR THREE TWO. "
            + "A player holding the lead suit or the trump suit must play one of those; otherwise any card. "
            + "The trick winner is the highest card of the lead suit, unless a trump was played, "
            + "then the highest trump. Each round is one trick and the winner scores one point. "
            + "In THRONE_BREAKER mode, non-trump cards of the wizard rank can always be played and beat all lead "
            + "and trump cards (the last wizard played wins). If all four suits appear in a trick (a Revolution), "
            + "suits stop mattering: the highest rank wins and ties go to the last card played. "
            + "Choose one card from LEGAL. Reply with only SUIT-RANK, no explanation.";

    public String systemPrompt() {
        return SYSTEM;
    }

    public String userPrompt(String mode, int round, Suit trump, Rank wizardRank, List<Card> hand,
                             List<Card> legal, List<PlayedCard> currentTrick, List<String> previousTricks,
                             int[] scores) {
        StringBuilder sb = new StringBuilder();
        sb.append("mode=").append(mode).append(" round=").append(round).append(" trump=").append(trump);
        if (wizardRank != null) sb.append(" wizard=").append(wizardRank);
        sb.append("\nhand=").append(join(hand));
        sb.append("\nLEGAL=").append(join(legal));
        sb.append("\ncurrent=");
        if (currentTrick.isEmpty()) sb.append("(you lead)");
        for (PlayedCard played : currentTrick) {
            sb.append('P').append(played.playerId()).append(':').append(played.card()).append(' ');
        }
        sb.append("\nprevious=").append(previousTricks.isEmpty() ? "none" : String.join("; ", previousTricks));
        sb.append("\nscores=P0:").append(scores[0]).append(",P1:").append(scores[1])
                .append(",P2:").append(scores[2]).append(",P3:").append(scores[3]);
        return sb.toString();
    }

    private static String join(List<Card> cards) {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            if (sb.length() > 0) sb.append(',');
            sb.append(card);
        }
        return sb.toString();
    }
}
