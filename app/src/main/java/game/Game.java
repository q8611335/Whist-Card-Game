package game;

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;
import game.ai.AIPlayer;
import game.ai.EnvironmentFile;
import game.ai.GameHistory;
import game.ai.OpenRouterClient;
import game.ai.RoundRequestLimiter;
import game.rules.*;
import game.heuristic.HeuristicPlayerFactory;
import game.heuristic.TrickState;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("serial")
public class Game extends CardGame {
	
  public enum Suit
  {
    SPADES, HEARTS, DIAMONDS, CLUBS
  }

  public enum Rank
  {
    // Reverse order of rank importance (see rankGreater() below)
	// Order of cards is tied to card images
	ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO
  }

  public enum Mode
    {
        NORMAL,
        THRONE_BREAKER
    }
  
  final String trumpImage[] = {"bigspade.gif","bigheart.gif","bigdiamond.gif","bigclub.gif"};

  static final Random random = ThreadLocalRandom.current();
  
  // return random Enum value
  public static <T extends Enum<?>> T randomEnum(Class<T> clazz){
      int x = random.nextInt(clazz.getEnumConstants().length);
      return clazz.getEnumConstants()[x];
  }
  
  // return random Card from Hand
  public static Card randomCard(Hand hand){
      int x = random.nextInt(hand.getNumberOfCards());
      return hand.get(x);
  }
 
  // return random Card from ArrayList
  public static Card randomCard(ArrayList<Card> list){
      int x = random.nextInt(list.size());
      return list.get(x);
  }
  
  public boolean rankGreater(Card card1, Card card2) {
	  return card1.getRankId() < card2.getRankId(); // Warning: Reverse rank order of cards (see comment on enum)
  }
	 
  private final String version = "1.0";
  public final int nbPlayers = 4;
  public final int nbStartCards;
  public final int numberOfRounds;
  public final int winningScore;
  private final int handWidth = 400;
  private final int trickWidth = 40;
  private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
  private final Location[] handLocations = {
			  new Location(350, 625),
			  new Location(75, 350),
			  new Location(350, 75),
			  new Location(625, 350)
	  };
  private final Location[] scoreLocations = {
			  new Location(575, 675),
			  new Location(25, 575),
			  new Location(575, 25),
			  new Location(650, 575)
	  };
  private Actor[] scoreActors = {null, null, null, null };
  private final Location trickLocation = new Location(350, 350);
  private final Location textLocation = new Location(350, 450);
  private final int gameOverDelay;
  private final Suit configuredTrump;
  private final int configuredLeadPlayer;
  private final String dealCardsSpec;
  private final java.util.Map<Integer, Rank> wizardRanks = new java.util.HashMap<>();
  private Rank currentWizardRank;
  private final String[] playerTypes = new String[nbPlayers];
  // Feature 3: one AI bot per seat (null if not AI); they share the game history and request budget
  private final GameHistory gameHistory = new GameHistory();
  private Hand[] hands;
  private boolean handsInitialised;
  private boolean cardListenerInstalled;
  private Location hideLocation = new Location(-500, - 500);
  private Location trumpsActorLocation = new Location(50, 80);
  private boolean enforceRules=false;
  private final GameCallback gameCallback;
  private final boolean autoPlayer0;
  private final int simulationPeriod;
  private final int trickDelay;
  private final Location[] playerLabelLocations = {
      new Location(350, 540), new Location(165, 350),
      new Location(350, 195), new Location(525, 350)
  };
  private final Actor[] playerLabelActors = {null, null, null, null};
  private Actor wizardLabelActor;
  private final java.util.Map<Integer, java.util.Deque<String>> scriptedMoves = new java.util.HashMap<>();

  // added mode variables
  private final Mode mode;
  private final TrickRules trickRules;
  // One bot per seat (heuristic or AI), null if the seat is not a bot
  private final BotPlayer[] botPlayers = new BotPlayer[nbPlayers];

  public void setStatus(String string) { setStatusText(string); }
  
private int[] scores = new int[nbPlayers];

Font bigFont = new Font("Serif", Font.BOLD, 36);

private void initScore() {
	 for (int i = 0; i < nbPlayers; i++) {
		 scores[i] = 0;
		 scoreActors[i] = new TextActor("0", Color.WHITE, bgColor, bigFont);
		 addActor(scoreActors[i], scoreLocations[i]);
	 }
  }

private void updateScore(int player) {
	removeActor(scoreActors[player]);
	scoreActors[player] = new TextActor(String.valueOf(scores[player]), Color.WHITE, bgColor, bigFont);
	addActor(scoreActors[player], scoreLocations[player]);
}

private Card selected;

private void initRound() {
         boolean firstDeal = !handsInitialised;
         if (!handsInitialised) {
             hands = dealInitialHands();
             handsInitialised = true;
         }
		 // The live hands already have their views after the initial deal. Redrawing
		 // every remaining card at the start of each round makes the UI replay the
		 // full hand animation and overwhelms the configured simulation period.
		 if (firstDeal) {
			 for (int i = 0; i < nbPlayers; i++) {
			   hands[i].sort(Hand.SortType.SUITPRIORITY, true);
			 }
		 }
		 // Set up human player for interaction
		if (!cardListenerInstalled) {
		CardListener cardListener = new CardAdapter()  // Human Player plays card
			    {
			      public void leftDoubleClicked(Card card) { selected = card; hands[0].setTouchEnabled(false); }
			    };
		 hands[0].addCardListener(cardListener);
		 cardListenerInstalled = true;
		}
		 // graphics
		 if (!firstDeal) return;
	    RowLayout[] layouts = new RowLayout[nbPlayers];
	    for (int i = 0; i < nbPlayers; i++) {
	      layouts[i] = new RowLayout(handLocations[i], handWidth);
	      layouts[i].setRotationAngle(90 * i);
	      // layouts[i].setStepDelay(10);
	      hands[i].setView(this, layouts[i]);
		      TargetArea cardMoveTarget = new TargetArea(trickLocation);
		      // Keep the travel time short when the simulation tick is large.
		      // Otherwise the default small slide step makes a move take many
		      // simulation ticks and appear frozen.
		      cardMoveTarget.setSlideStep(Math.max(1, simulationPeriod));
		      hands[i].setTargetArea(cardMoveTarget);
		      hands[i].draw();
		    }
		    showPlayerLabels();
	    
//	    for (int i = 1; i < nbPlayers; i++)  // This code can be used to visually hide the cards in a hand (make them face down)
//	      hands[i].setVerso(true);
	    // End graphics
 }

private Hand[] dealInitialHands() {
    if (dealCardsSpec == null || dealCardsSpec.isBlank()) {
        return deck.dealingOut(nbPlayers, nbStartCards);
    }

    Hand[] dealt = new Hand[nbPlayers];
    for (int player = 0; player < nbPlayers; player++) {
        dealt[player] = new Hand(deck);
    }
    for (String playerSpec : dealCardsSpec.split(";")) {
        String[] parts = playerSpec.trim().split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Deal.cards entry: " + playerSpec);
        }
        int player = Integer.parseInt(parts[0].trim());
        if (player < 0 || player >= nbPlayers) {
            throw new IllegalArgumentException("Deal.cards player out of range: " + player);
        }
        for (String cardCode : parts[1].split(",")) {
            String[] cardParts = cardCode.trim().split("-", 2);
            if (cardParts.length != 2) {
                throw new IllegalArgumentException("Invalid card in Deal.cards: " + cardCode);
            }
            Suit suit = Suit.valueOf(cardParts[0].trim().toUpperCase());
            Rank rank = Rank.valueOf(cardParts[1].trim().toUpperCase());
            dealt[player].insert(new Card(deck, suit, rank), false);
        }
    }
    return dealt;
}

private void showPlayerLabels() {
    for (int i = 0; i < nbPlayers; i++) {
        if (playerLabelActors[i] != null) removeActor(playerLabelActors[i]);
        playerLabelActors[i] = new TextActor("P" + i, Color.WHITE, bgColor,
                new Font("SansSerif", Font.BOLD, 20));
        addActor(playerLabelActors[i], playerLabelLocations[i]);
    }
}

private void updateWizardLabel() {
    if (wizardLabelActor != null) {
        removeActor(wizardLabelActor);
        wizardLabelActor = null;
    }

    if (currentWizardRank == null) return;
    wizardLabelActor = new TextActor("WIZARD: " + currentWizardRank,
            Color.WHITE, bgColor, new Font("SansSerif", Font.BOLD, 18));
    addActor(wizardLabelActor, new Location(530, 80));
}

// resolve wizrd rank
private Rank resolveWizardRank(int round) {
    if(!trickRules.usesWizardRank()){
        return null;
    }
    return wizardRanks.computeIfAbsent(round, ignored -> randomEnum(Rank.class));
}

private void loadScriptedMoves(java.util.Properties properties) {
    for (int i = 0; i < nbPlayers; i++) {
        String moves = properties.getProperty("Player" + i + ".moves");
        if (moves != null && !moves.isBlank()) {
            scriptedMoves.put(i, new java.util.ArrayDeque<>(java.util.Arrays.asList(moves.split(","))));
        }
    }
}

private Card nextScriptedCard(int player, Hand hand) {
    java.util.Deque<String> queue = scriptedMoves.get(player);
    if (queue == null || queue.isEmpty()) {
        return null;
    }
    String code = queue.pollFirst();
    return findCardByCode(hand, code);
}

private static Card findCardByCode(Hand hand, String code) {
    String[] parts = code.trim().split("-");
    for (Card card : hand.getCardList()) {
        if (card.getSuit().toString().equalsIgnoreCase(parts[0]) && card.getRank().toString().equalsIgnoreCase(parts[1])) {
            return card;
        }
    }
    return null;
}

private boolean isAiPlayer(int player) {
    return "AI".equalsIgnoreCase(playerTypes[player]);
}

private boolean isBot(int player) {
    return botPlayers[player] != null;
}

// Gather the trick details for the bot, let it choose a card, and return the card with its log source
private BotMove chooseBotMove(int player, int round, Suit trumps, List<PlayedCard> playedCards) {
    TrickState state = new TrickState(trumps, currentWizardRank, playedCards);
    return botPlayers[player].chooseCard(hands[player], state, trickRules, round, scores.clone());
}

private List<Card> legalCards(Hand hand, Suit trump, Suit lead) {
    return trickRules.getLegalCards(hand, lead, trump, currentWizardRank);
}

private Card randomLegalCard(Hand hand, Suit trump, Suit lead) {
    List<Card> legal = legalCards(hand, trump, lead);
    return legal.get(random.nextInt(legal.size()));
}

private Optional<Integer> playRound(int round, Suit trumps, int leadPlayer) {  // Returns winner, if any
	// Trump is selected once for the whole game and passed into every round.
	    final Actor trumpsActor = new Actor("sprites/"+trumpImage[trumps.ordinal()]);
	    addActor(trumpsActor, trumpsActorLocation);
        currentWizardRank = resolveWizardRank(round);
	    updateWizardLabel();
	    gameCallback.roundStarted(round, trumps);

        if(currentWizardRank != null) {
            gameCallback.wizardRankSelected(round, currentWizardRank);
        }
	// End trump suit
	Hand trick;
	int winner;
//	Card winningCard;
	Suit lead;
	int nextPlayer = leadPlayer;
	// A round is one trick. Cards left in each hand remain for later rounds.
	for (int i = 0; i < 1; i++) {
		String source = "RANDOM";
		trick = new Hand(deck);
        List<PlayedCard> playedCards = new ArrayList<>();
    	selected = nextScriptedCard(nextPlayer, hands[nextPlayer]);
		if (selected == null) {
		    if (isBot(nextPlayer)) {
               setStatusText("Player " + nextPlayer + " thinking...");
               delay(simulationPeriod);
               BotMove move = chooseBotMove(nextPlayer, round, trumps, playedCards);
               selected = move.card();
               source = move.source();
            } else if (0 == nextPlayer && !autoPlayer0) {  // Select lead depending on player type
				source = "HUMAN";
        		hands[0].setTouchEnabled(true);
        		setStatus("Player 0 double-click on card to lead.");
        		while (null == selected) delay(100);
            } else {
        		setStatusText("Player " + nextPlayer + " thinking...");
	                delay(simulationPeriod);
                selected = randomCard(hands[nextPlayer]);
			}
        }
		gameCallback.cardPlayed(round, nextPlayer, selected, source);
        // Lead with selected card
	        trick.setView(this, new RowLayout(trickLocation, (trick.getNumberOfCards()+2)*trickWidth));
			trick.draw();
			selected.setVerso(false);
			// No restrictions on the card being lead
			lead = (Suit) selected.getSuit();
            selected.transfer(trick, true);
            playedCards.add(new PlayedCard(nextPlayer, selected));
		// End Lead
		for (int j = 1; j < nbPlayers; j++) {
			if (++nextPlayer >= nbPlayers) nextPlayer = 0;  // From last back to first
			source = "RANDOM";
			selected = nextScriptedCard(nextPlayer, hands[nextPlayer]);
	        if (selected == null) {
	            if (isBot(nextPlayer)) {
                    setStatusText("Player " + nextPlayer + " thinking...");
                    delay(simulationPeriod);
                    BotMove move = chooseBotMove(nextPlayer, round, trumps, playedCards);
                    selected = move.card();
                    source = move.source();
	            } else if (0 == nextPlayer && !autoPlayer0) {
				source = "HUMAN";
                List<Card> legal = legalCards(hands[0], trumps, lead);
                while(true) {
                    selected = null;
                    hands[0].setTouchEnabled(true);
                    setStatus("Player 0 double-click on card to follow.");

                    while (selected == null) {
                        delay(100);
                    }

                    if (legal.contains(selected)) {
                        break;
                    }
                    setStatus("Illegal card. Please choose again.");
                    delay(800);
                }
		        } else {
			        setStatusText("Player " + nextPlayer + " thinking...");
			        delay(simulationPeriod);
			        selected = randomLegalCard(hands[nextPlayer], trumps, lead);
		        }
	        }
			gameCallback.cardPlayed(round, nextPlayer, selected, source);
	        // Follow with selected card
		        trick.setView(this, new RowLayout(trickLocation, (trick.getNumberOfCards()+2)*trickWidth));
				trick.draw();
				selected.setVerso(false);  // In case it is upside down
				// Check: Following card must follow suit if possible
//					boolean ordinaryOffSuit = selected.getSuit() != lead && selected.getSuit() != trumps
//                            && !trickRules.isWizard(selected, trumps, currentWizardRank);
                    selected.transfer(trick, true);
                    playedCards.add(new PlayedCard(nextPlayer, selected));
			// End Follow
		}
		TrickResult trickResult = trickRules.determineWinner(playedCards, trumps, currentWizardRank);
        winner = trickResult.winnerId();
        gameHistory.recordTrick(round, playedCards, winner);
        nextPlayer = winner;
        gameCallback.roundWinner(round, winner, trickResult.revolution());
		setStatusText("Player " + nextPlayer + " wins trick.");
		scores[nextPlayer]++;
		updateScore(nextPlayer);
		gameCallback.roundScores(round, scores);
		delay(trickDelay);
		trick.setView(this, new RowLayout(hideLocation, 0));
		trick.draw();
	}
	return Optional.of(nextPlayer);
}

  public Game(GameCallback gameCallback, java.util.Properties properties)
  {
    super(700, 700, 30);
    this.gameCallback = gameCallback;

    String configuredMode = properties.getProperty("Mode", "NORMAL");
    this.mode = Mode.valueOf(configuredMode.trim().toUpperCase());
    this.trickRules = switch (mode){
        case NORMAL -> new NormalRules();
        case THRONE_BREAKER -> new ThroneBreakerRules();
    };

    this.nbStartCards = Integer.parseInt(properties.getProperty("StartCards", "13"));
    this.numberOfRounds = Integer.parseInt(properties.getProperty("NumberOfRounds", "3"));
    this.winningScore = Integer.parseInt(properties.getProperty("WinningScore", "24"));
    this.autoPlayer0 = Boolean.parseBoolean(properties.getProperty("Player0.isAuto", "false"));
    for (int player = 0; player < nbPlayers; player++) {
        playerTypes[player] = properties.getProperty("Player" + player + ".type", "RANDOM");
        // Feature 2: get filter and selection from property files and create heuristic player according it.
        if ("HEURISTIC".equalsIgnoreCase(playerTypes[player])) {
            String filterName = properties.getProperty("Player" + player + ".filter", "NAIVE");
            String selectionName = properties.getProperty("Player" + player + ".selection", "EFFICIENT");
            botPlayers[player] = HeuristicPlayerFactory.create(player, filterName, selectionName);
        }
    }

    // Feature 3: the LLM client is only created if some seat is an AI bot
    RoundRequestLimiter limiter = new RoundRequestLimiter();
    OpenRouterClient llm = null;
    for (int player = 0; player < nbPlayers; player++) {
        if (!isAiPlayer(player)) continue;
        if (llm == null) {
            String apiKey = EnvironmentFile.get("AI.apiKeyEnv");
            String model = properties.getProperty("AI.model", "@preset/assignment");
            // Fail fast so a slow or unavailable LLM falls back to the heuristic bot without freezing the game.
            int aiTimeout = Integer.parseInt(properties.getProperty("AI.timeoutMs", "20000"));
            llm = new OpenRouterClient(apiKey, model, aiTimeout);
        }
        botPlayers[player] = new AIPlayer(player, mode.name(), llm, gameHistory, limiter);
    }
    this.simulationPeriod = resolvePositivePeriod(properties, "Simulation.period", 300);
    this.trickDelay = resolveNonNegativePeriod(properties, "Trick.delay", 600);
    this.gameOverDelay = resolveNonNegativePeriod(properties, "GameOver.delay", 1000);
    this.configuredTrump = resolveTrump(properties);
    this.configuredLeadPlayer = resolveLeadPlayer(properties);
    this.dealCardsSpec = properties.getProperty("Deal.cards");
    for (int round = 1; round <= numberOfRounds; round++) {
        String wizardRank = properties.getProperty("WizardRank.round" + round);
        if (wizardRank != null && !wizardRank.isBlank()) {
            wizardRanks.put(round, Rank.valueOf(wizardRank.trim().toUpperCase()));
        }
    }
    setSimulationPeriod(simulationPeriod);
    loadScriptedMoves(properties);
    setTitle("Whist (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
    setStatusText("Initializing...");
    initScore();
  }

  private static Suit resolveTrump(java.util.Properties properties) {
    String trump = properties.getProperty("Trump");
    return trump == null || trump.isBlank() ? null : Suit.valueOf(trump.trim().toUpperCase());
  }

  private static int resolveLeadPlayer(java.util.Properties properties) {
    String lead = properties.getProperty("Deal.leadPlayer");
    if (lead == null || lead.isBlank()) return -1;
    int player = Integer.parseInt(lead.trim());
    if (player < 0 || player >= 4) throw new IllegalArgumentException("Deal.leadPlayer must be 0-3");
    return player;
  }

  private static int resolvePositivePeriod(java.util.Properties properties, String key, int defaultValue) {
    int value = Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)).trim());
    if (value <= 0) throw new IllegalArgumentException(key + " must be positive");
    return value;
  }

  private static int resolveNonNegativePeriod(java.util.Properties properties, String key, int defaultValue) {
    int value = Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)).trim());
    if (value < 0) throw new IllegalArgumentException(key + " must not be negative");
    return value;
  }

  private String buildGameResult(){
    int highestScore = Arrays.stream(scores).max().orElse(0);
    List<String> winners = new ArrayList<>();
    for (int player = 0; player < nbPlayers; player++) {
        if(scores[player] == highestScore){
            winners.add(String.valueOf(player));
        }
    }
    return "GameOver Winner: Player " + String.join(",", winners);
  }

  public String runApp() {
    initRound();
    Suit trump = configuredTrump == null ? randomEnum(Suit.class) : configuredTrump;
    int leadPlayer = configuredLeadPlayer < 0 ? random.nextInt(nbPlayers) : configuredLeadPlayer;
    Optional<Integer> winner = Optional.empty();
    for (int round = 1; round <= numberOfRounds; round++) {
      winner = playRound(round, trump, leadPlayer);
      leadPlayer = winner.get();
      if (round < numberOfRounds) initRound();
    }
    delay(gameOverDelay);
    addActor(new Actor("sprites/gameover.gif"), textLocation);
    String result = buildGameResult();
    setStatusText(result);
    gameCallback.endOfGame(result);
    refresh();
    return gameCallback.getAllLog();
  }

}
