package app.vcampus.client;

/**
 * Launcher class to start the JavaFX application.
 * This is a workaround for JavaFX runtime components not being present on the module path in a fat JAR.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}