package game;

import ch.aplu.jcardgame.Card;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GameCallback {
    private final String logFilePath = "Log.txt";
    private FileWriter fileWriter;
    private final StringBuilder logBuilder = new StringBuilder();

    public GameCallback() {
        try {
            fileWriter = new FileWriter(new File(logFilePath));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void writeString(String line) {
        logBuilder.append(line).append("\n");
        if (fileWriter != null) {
            try {
                fileWriter.write(line);
                fileWriter.write("\n");
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAllLog() {
        return logBuilder.toString();
    }

    public void roundStarted(int round, Enum<?> trump) {
        writeString(String.format("[Round %d] Trump: %s", round, trump));
    }

    public void wizardRankSelected(int round, Enum<?> wizardRank) {
        writeString(String.format("[Round %d] WizardRank: %s", round, wizardRank));
    }

    public void cardPlayed(int round, int player, Card card, String source) {
        writeString(String.format("[Round %d][Player %d] Plays: %s source=%s",
                round, player, card, source));
    }

    public void roundWinner(int round, int winner) {
        roundWinner(round, winner, false);
    }

    public void roundWinner(int round, int winner, boolean revolution) {
        writeString(String.format("[Round %d] Winner: Player %d%s", round, winner, revolution ? " Revolution" : ""));
    }

    public void roundScores(int round, int[] scores) {
        writeString(String.format("[Round %d] Scores: P0=%d,P1=%d,P2=%d,P3=%d",
                round, scores[0], scores[1], scores[2], scores[3]));
    }

    // Kept for compatibility with the original starter callback contract.
    public void trickWon(int trickNumber, int winner) {
        writeString("Trick " + trickNumber + " winner: Player " + winner);
    }

    public void endOfGame(String result) {
        writeString(result);
    }
}
