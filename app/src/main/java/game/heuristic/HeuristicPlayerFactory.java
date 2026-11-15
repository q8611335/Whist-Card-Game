package game.heuristic;

/** Creates heuristic bots from the names used in the properties file. */
public class HeuristicPlayerFactory {
    public static HeuristicPlayer create(int playerId, String filterName, String selectionName) {
        CardFilterStrategy filter = createFilter(filterName);
        CardSelectorStrategy selector = createSelector(selectionName);
        return new HeuristicPlayer(playerId, filter, selector);
    }

    private static CardFilterStrategy createFilter(String name) {
        switch (name.trim().toUpperCase()) {
            case "NAIVE":
                return new NaiveLegalFilter();
            case "TRUMP_SAVING":
                return new TrumpSavingFilter();
            default:
                throw new IllegalArgumentException("Unknown filter: " + name);
        }
    }

    private static CardSelectorStrategy createSelector(String name) {
        switch (name.trim().toUpperCase()) {
            case "AGGRESSIVE":
                return new AggressiveSelector();
            case "EFFICIENT":
                return new EfficientSelector();
            default:
                throw new IllegalArgumentException("Unknown selection: " + name);
        }
    }
}
