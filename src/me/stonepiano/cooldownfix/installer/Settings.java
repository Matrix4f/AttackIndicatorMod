package me.stonepiano.cooldownfix.installer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {

    public static final Settings INSTANCE = new Settings();

    private Properties properties;

    public Settings() {
        properties = new Properties();
    }

    public void set(String key, Object value) {
        properties.put(key, value.toString());
    }

    public String getString(String key, String def) {
        return properties.getProperty(key, def);
    }

    public boolean getBool(String key, boolean def) {
        String prop = properties.getProperty(key);
        if(prop == null)
            return def;
        return Boolean.parseBoolean(prop);
    }

    public void save() {
        try {
            properties.store(new FileWriter("installer-settings.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.error("Saving Settings", "Unable to save settings: " + e.getMessage());
        }
    }

    public void load() {
        File file = new File("installer-settings.properties");
        if(file.exists()) {
            try {
                properties.load(new FileReader(file));
            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.error("Loading Settings", "Unable to load settings: " + e.getMessage());
            }
        }
    }
}
