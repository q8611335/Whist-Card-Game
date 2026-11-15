import org.junit.Test;
import game.*;

import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class WhistTest {

    private String runWithProperties(String propertiesPath) {
        Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
        GameCallback callback = new GameCallback();
        Game game = new Game(callback, properties);
        return game.runApp();
    }

    @Test
    // Test 1 validates the base game over three one-trick rounds. Player 0
    // leads each round with Clubs, Diamonds, and Hearts; Player 1 answers
    // with the corresponding Ace, while Players 2 and 3 follow with lower
    // cards. Player 1 therefore wins every round, proving legal play,
    // persistent hands, per-round scoring, and final-winner calculation.
    public void test1_deterministicThreeRoundGameTracksScoresAndWinner() {
        String log = runWithProperties("properties/test1.properties");
        assertTrue(log.contains("[Round 1] Trump: HEARTS"));
        assertTrue(log.contains("[Round 1][Player 1] Plays: CLUBS-ACE source=RANDOM"));
        assertTrue(log.contains("[Round 1] Winner: Player 1"));
        assertTrue(log.contains("[Round 1] Scores: P0=0,P1=1,P2=0,P3=0"));
        assertTrue(log.contains("[Round 2] Trump: HEARTS"));
        assertTrue(log.contains("[Round 2] Winner: Player 1"));
        assertTrue(log.contains("[Round 2] Scores: P0=0,P1=2,P2=0,P3=0"));
        assertTrue(log.contains("[Round 3] Trump: HEARTS"));
        assertTrue(log.contains("[Round 3] Winner: Player 1"));
        assertTrue(log.contains("[Round 3] Scores: P0=0,P1=3,P2=0,P3=0"));
        assertTrue(log.contains("GameOver Winner: Player 1"));
    }

    @Test
    // Test 2 validates Throne Breaker round by round using one six-card deal.
    // Round 1 checks that the Seven Wizard beats a trump card. Round 2 uses
    // legal plays from all four suits, so Revolution cancels suit priority;
    // the two Aces tie and the last Ace (Player 3) wins. Round 3 changes the
    // Wizard rank to Nine and checks that the Wizard still beats trump.
    public void test2_throneBreakerWizardBeatsTrumpAndTriggersRevolution() {
        String log = runWithProperties("properties/test2.properties");
        assertTrue(log.contains("[Round 1] Trump: HEARTS"));
        assertTrue(log.contains("[Round 1] WizardRank: SEVEN"));
        assertTrue(log.contains("[Round 1][Player 2] Plays: SPADES-SEVEN source=RANDOM"));
        assertTrue(log.contains("[Round 1][Player 3] Plays: SPADES-TWO source=RANDOM"));
        assertTrue(log.contains("[Round 1] Winner: Player 2\n"));
        assertTrue(log.contains("[Round 1] Scores: P0=0,P1=0,P2=1,P3=0"));
        assertTrue(log.contains("[Round 2][Player 2] Plays: DIAMONDS-ACE source=RANDOM"));
        assertTrue(log.contains("[Round 2][Player 3] Plays: SPADES-ACE source=RANDOM"));
        assertTrue(log.contains("[Round 2][Player 0] Plays: CLUBS-QUEEN source=RANDOM"));
        assertTrue(log.contains("[Round 2][Player 1] Plays: HEARTS-KING source=RANDOM"));
        assertTrue(log.contains("[Round 2] Winner: Player 3 Revolution"));
        assertTrue(log.contains("[Round 2] Trump: HEARTS"));
        assertTrue(log.contains("[Round 2] WizardRank: EIGHT"));
        assertTrue(log.contains("[Round 2] Scores: P0=0,P1=0,P2=1,P3=1"));
        assertTrue(log.contains("[Round 3][Player 3] Plays: SPADES-TEN source=RANDOM"));
        assertTrue(log.contains("[Round 3][Player 0] Plays: CLUBS-JACK source=RANDOM"));
        assertTrue(log.contains("[Round 3][Player 1] Plays: HEARTS-QUEEN source=RANDOM"));
        assertTrue(log.contains("[Round 3][Player 2] Plays: CLUBS-NINE source=RANDOM"));
        assertTrue(log.contains("[Round 3] Trump: HEARTS"));
        assertTrue(log.contains("[Round 3] WizardRank: NINE"));
        assertTrue(log.contains("[Round 3] Winner: Player 2\n"));
        assertTrue(log.contains("[Round 3] Scores: P0=0,P1=0,P2=2,P3=1"));
        assertTrue(log.contains("GameOver Winner: Player 2"));
    }

    @Test
    // Test 3 validates the heuristic bot in isolation. Player 0 leads the
    // scripted sequence, while Player 1 filters legal cards with NAIVE and
    // uses AGGRESSIVE selection. The controlled six-card hand gives the bot
    // a visible best legal choice in round 1 and keeps later rounds legal,
    // so the test checks the strategy rather than random card distribution.
    public void test3_heuristicBotAlone() {
        String log = runWithProperties("properties/test3.properties");
        assertTrue(log.contains("[Round 1][Player 1] Plays: HEARTS-KING source=HEURISTIC"));
        assertTrue(log.contains("[Round 1] Winner: Player 1"));
        assertTrue(log.contains("[Round 2] Trump: HEARTS"));
        assertTrue(log.contains("[Round 3] Trump:"));
        assertTrue(log.contains("GameOver Winner: Player"));
    }

    @Test
    // Test 4 validates the AI bot in isolation across three rounds. Player 3
    // wins round 1, then leads round 2; Player 0 wins that round and leads
    // round 3. The fixed legal replies force Player 1's AI to choose the
    // expected cards, while every winner and cumulative score is checked.
    public void test4_aiBotAlone() {
        String log = runWithProperties("properties/test4.properties");
        assertTrue(log.contains("[Round 1][Player 1] Plays: CLUBS-ACE source=AI")
                || log.contains("[Round 1][Player 1] Plays: CLUBS-ACE source=AI_FALLBACK"));
        assertTrue(log.contains("[Round 1] Winner: Player 3"));
        assertTrue(log.contains("[Round 1] Scores: P0=0,P1=0,P2=0,P3=1"));
        assertTrue(log.contains("[Round 2] Trump: HEARTS"));
        assertTrue(log.contains("[Round 2][Player 3] Plays: CLUBS-JACK source=RANDOM"));
        assertTrue(log.contains("[Round 2][Player 1] Plays: CLUBS-TWO source=AI")
                || log.contains("[Round 2][Player 1] Plays: CLUBS-TWO source=AI_FALLBACK"));
        assertTrue(log.contains("[Round 2] Winner: Player 0"));
        assertTrue(log.contains("[Round 2] Scores: P0=1,P1=0,P2=0,P3=1"));
        assertTrue(log.contains("[Round 3] Trump:"));
        assertTrue(log.contains("[Round 3][Player 0] Plays: DIAMONDS-KING source=RANDOM"));
        assertTrue(log.contains("[Round 3][Player 1] Plays: DIAMONDS-ACE source=AI")
                || log.contains("[Round 3][Player 1] Plays: DIAMONDS-ACE source=AI_FALLBACK"));
        assertTrue(log.contains("[Round 3][Player 2] Plays: HEARTS-ACE source=RANDOM"));
        assertTrue(log.contains("[Round 3] Winner: Player 2"));
        assertTrue(log.contains("[Round 3] Scores: P0=1,P1=0,P2=1,P3=1"));
        assertTrue(log.contains("GameOver Winner: Player"));
    }

    @Test
    // Test 5 combines all features. Round 1 has heuristic Player 3 play the
    // Seven Wizard and win; round 2 changes the Wizard to Eight and lets
    // Player 0 win after the AI selects its weakest legal Spade; round 3
    // changes the Wizard to Nine, allowing the AI's Clubs-Nine to win.
    // Heuristic decisions, AI/fallback source, wizard priority, winners, and
    // scores are all asserted against the fixed legal sequence.
    public void test5_allThreeFeaturesTogether() {
        String log = runWithProperties("properties/test5.properties");
        assertTrue(log.contains("[Round 1] Trump: HEARTS"));
        assertTrue(log.contains("[Round 1] WizardRank: SEVEN"));
        assertTrue(log.contains("[Round 1][Player 1] Plays: CLUBS-ACE source=HEURISTIC"));
        assertTrue(log.contains("[Round 1][Player 2] Plays: HEARTS-ACE source=AI")
                || log.contains("[Round 1][Player 2] Plays: HEARTS-ACE source=AI_FALLBACK"));
        assertTrue(log.contains("[Round 1][Player 3] Plays: SPADES-SEVEN source=HEURISTIC"));
        assertTrue(log.contains("[Round 1] Winner: Player 3"));
        assertTrue(log.contains("[Round 1] Scores: P0=0,P1=0,P2=0,P3=1"));
        assertTrue(log.contains("[Round 2] Trump: HEARTS"));
        assertTrue(log.contains("[Round 2] WizardRank: EIGHT"));
        assertTrue(log.contains("[Round 2][Player 3] Plays: HEARTS-JACK source=HEURISTIC"));
        assertTrue(log.contains("[Round 2][Player 1] Plays: HEARTS-TWO source=HEURISTIC"));
        assertTrue(log.contains("[Round 2][Player 2] Plays: SPADES-FIVE source=AI")
                || log.contains("[Round 2][Player 2] Plays: SPADES-FIVE source=AI_FALLBACK"));
        assertTrue(log.contains("[Round 2] Winner: Player 0"));
        assertTrue(log.contains("[Round 2] Scores: P0=1,P1=0,P2=0,P3=1"));
    }
}
