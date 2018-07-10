package me.stonepiano.cooldownfix.installer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static final String VERSION = "1.0";
    public static final int WIDTH = 400, HEIGHT = 400;
    @Override
    public void start(Stage window) throws Exception {
        AlertUtils.parent = window;
        Settings.INSTANCE.load();

        InstallerGui gui = new InstallerGui(window);
        gui.init();
        window.getIcons().add(new Image(Main.class.getResourceAsStream("/icons/attackindicator.png")));
        window.setTitle("Attack Indicator Fix - Mod Version v" + VERSION);
        window.show();
        window.setResizable(false);
    }

    public static void main(String[] args) {
	    launch(args);
    }
}
