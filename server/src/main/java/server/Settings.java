package server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class Settings {

    private static final Logger logger = Logger.getLogger(Settings.class.getName());

    private static final Settings instance = new Settings();
    public static Settings getInstance() { return Settings.instance; }

    private int port;
    private static final String PORT_PROP_NAME ="port_number";

    private String rootPath;
    private static final String ROOT_PATH_PROP_NAME ="root_path";

    private String connectionString;
    private static final String CONNECTION_STRING_PROP_NAME ="connection_string";

    private String dbUser;
    private static final String DB_USER_PROP_NAME ="db_user";

    private String dbPass;
    private static final String DB_PASS_PROP_NAME ="db_pass";

    public int getPort() {
        return port;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPass() {
        return dbPass;
    }

    private static final String PROP_FILE_NAME = "server.properties";

    private Properties properties;

    public Settings() {
        properties = new Properties();
        tryLoadProperties();
    }

    private void tryLoadProperties() {
        try(FileReader fileReader = new FileReader(PROP_FILE_NAME)){
            properties.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
            createDefaultProperties();
        }
        finally {
            parseProperties();
            logger.info("Load settings.");
        }
    }

    private void parseProperties() {
        port = Integer.parseInt(properties.getProperty(PORT_PROP_NAME), 10);
        rootPath = properties.getProperty(ROOT_PATH_PROP_NAME);
        connectionString = properties.getProperty(CONNECTION_STRING_PROP_NAME);
        dbUser = properties.getProperty(DB_USER_PROP_NAME);
        dbPass = properties.getProperty(DB_PASS_PROP_NAME);
    }

    private void createDefaultProperties() {
        properties.setProperty(PORT_PROP_NAME, "8765");
        properties.setProperty(ROOT_PATH_PROP_NAME, "server_dir");
        properties.setProperty(CONNECTION_STRING_PROP_NAME, "jdbc:mysql://localhost:3306/chat_db?autoReconnect=true");
        properties.setProperty(DB_USER_PROP_NAME, "root");
        properties.setProperty(DB_PASS_PROP_NAME, "root");
        try(FileWriter output = new FileWriter(PROP_FILE_NAME)){
            properties.store(output, "Cloud storage server properties");
            logger.info("Created default settings file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
