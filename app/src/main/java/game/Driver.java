package game;

import java.util.Properties;

public class Driver {
    public static final String DEFAULT_PROPERTIES_PATH = "properties/game1.properties";

    public static void main(String[] args) {
        String propertiesPath = args.length > 0 ? args[0] : DEFAULT_PROPERTIES_PATH;
        Properties properties = PropertiesLoader.loadPropertiesFile(propertiesPath);
        GameCallback gameCallback = new GameCallback();
        Game game = new Game(gameCallback, properties);
        game.runApp();
    }
}
