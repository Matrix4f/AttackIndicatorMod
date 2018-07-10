package me.stonepiano.cooldownfix.installer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Stage;

public class AlertUtils {

    public static Stage parent = null;

    public static void crash(String where, String why, Exception e) {
        String crashReportFile;
        try {
            crashReportFile = CrashReports.save(e);
        } catch (Exception ex) {
            error(where, "Unable to save crash report. Instead printed into stdout stream.");
            ex.printStackTrace();
            return;
        }
        String text = String.format("Reason: %s\nCrash Report: " + crashReportFile, why);

        Pane pane = new Pane("Fatal Error: " + where, "error.png", text, "Exit", () -> System.exit(0));
        pane.showAndWait();
    }

    public static void error(String where, String why, Runnable onClose) {
        String text = String.format("Reason: %s", why);
        Pane pane = new Pane("Error: " + where, "error.png", text, "Close", onClose);
        pane.showAndWait();
    }

    public static void info(String title, String text) {
        Pane pane = new Pane(title, "info.png", text, "Close", null);
        pane.showAndWait();
    }

    public static void error(String where, String why) {
        error(where, why, () -> System.exit(0));
    }

    private static class Pane extends Stage {

        public Pane(String title, String iconFile, String text, String buttonText, Runnable onClose) {
            setTitle(title);
            setResizable(false);

            Image img = new Image(Main.class.getResourceAsStream("/icons/" + iconFile), 64, 64, false, true);

            ImageView icon = new ImageView(img);

            Text label = new Text(text);
            label.setBoundsType(TextBoundsType.LOGICAL);
            label.setWrappingWidth(400);
            Button button = new Button(buttonText);
            button.setAlignment(Pos.CENTER);
            button.setOnAction(e -> {
                if (onClose != null)
                    onClose.run();
                Pane.this.close();
            });

            GridPane pane = new GridPane();
            pane.setHgap(10);
            pane.setVgap(10);
            pane.setPadding(new Insets(10, 10, 10, 10));

            ScrollPane labelWrap = new ScrollPane(label);
            labelWrap.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            labelWrap.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            pane.add(icon, 1, 1, 2, 2);
            pane.add(label, 3, 1);
            pane.add(button, 3, 2);

            Scene scene = new Scene(pane, 500, 150);
            super.setScene(scene);
            if (parent != null)
                super.initOwner(parent);
            super.getIcons().add(img);
        }
    }
}
