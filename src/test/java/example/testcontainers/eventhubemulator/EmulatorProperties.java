package example.testcontainers.eventhubemulator;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class EmulatorProperties {
    public static final String COMPOSER_FILE = "COMPOSER_FILE";
    public static final String ACCEPT_EULA = "ACCEPT_EULA";
    public static final String CONFIG_FILE = "CONFIG_FILE";
    private final Properties properties;

    private EmulatorProperties(Properties properties) {
        this.properties = properties;
    }

    public String composerFile() {
        return properties.getProperty(COMPOSER_FILE);
    }

    public String acceptEula() {
        return properties.getProperty(ACCEPT_EULA);
    }

    public String configFile() {
        return properties.getProperty(CONFIG_FILE);
    }

    public static EmulatorProperties get() {
        return new EmulatorProperties(getProperties());
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = EmulatorProperties.class.getClassLoader().getResourceAsStream("eventhub-emulator.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // validate properties
        if (!"Y".equalsIgnoreCase(properties.getProperty(ACCEPT_EULA)) && "Y".equalsIgnoreCase(System.getProperty(ACCEPT_EULA))) {
            properties.setProperty(ACCEPT_EULA, System.getProperty(ACCEPT_EULA));
        }
        if (!"Y".equalsIgnoreCase(properties.getProperty(ACCEPT_EULA))) {
            throw new RuntimeException("You must accept the EULA by setting a property in the eventhub-emulator.properties file or by setting environment variable ACCEPT_EULA");
        }
        if (!properties.containsKey(CONFIG_FILE) || Files.notExists(Path.of(properties.getProperty(CONFIG_FILE)))) {
            throw new RuntimeException("The emulator configuration file does not exist.");
        }

        try {
            String configFile = properties.getProperty(CONFIG_FILE);
            String configFileCanonicalPath = Path.of(configFile).toFile().getCanonicalPath();
            if (!configFile.equals(configFileCanonicalPath)) {
                properties.setProperty(CONFIG_FILE, configFileCanonicalPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error resolving configuration file canonical path", e);
        }

        return properties;
    }
}
