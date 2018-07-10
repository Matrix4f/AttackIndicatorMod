package me.stonepiano.cooldownfix.installer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InstallerGui {

    private Scene scene;
    private Stage stage;
    private TabPane tabPane;
    private Tab infoTab, installTab, uninstallTab;

    public InstallerGui(Stage stage) {
        this.stage = stage;
        infoTab = new Tab("Information");
        installTab = new Tab("Install");
        uninstallTab = new Tab("Uninstall");
        initInfoTab(infoTab);
        initUninstallTab(uninstallTab);
        initInstallTab(installTab);

        tabPane = new TabPane(infoTab, installTab, uninstallTab);
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));

        scene = new Scene(tabPane, Main.WIDTH, Main.HEIGHT);
        scene.getStylesheets().add("style.css");
    }

    private Label newHeader(String title) {
        Label header = new Label(title);
        header.setId("header");
        return header;
    }

    private Text newParagraph(String data) {
        Text paragraph = new Text(data);
        paragraph.setId("paragraph");
        paragraph.setWrappingWidth(Main.WIDTH - 20);
        return paragraph;
    }

    private void initInfoTab(Tab info) {
        VBox box = new VBox();
        box.setId("vbox");
        int padding = 10;

        box.setSpacing(padding);
        box.setPadding(new Insets(padding, padding, padding, padding));

        Text welcome = newParagraph("Many Minecraft servers run on the vanilla jar file, and this preserves the " +
                "purity of the game. However, quite often, combat is jarring as the server's " +
                "tick rate is different from the steady vanilla attack indicator.\n\n" +
                "AttackIndicatorFix (AIF) is a mod developed by stonepiano which automatically synchronizes " +
                "the clientside attack indicator with the server's tick rate, by injecting " +
                "a small bit of code into Minecraft upon launch. It's a very lightweight mod, " +
                "and can be bundled with OptiFine, Vanilla, 5zig, and Forge.");
        Text how2install =
                newParagraph("Select the install tab. Click the text box labeled \"Minecraft Home " +
                        "Directory\" to select your .minecraft folder.\nThen, use the dropdown " +
                        "menu and choose whichever option you want to install AIF into. Some " +
                        "supported versions are: Vanilla, OptiFine, 5zig, and Forge. Selecting " +
                        "versions which point to a non-1.12.2 version of Minecraft will cause " +
                        "the mod to malfunction, unfortunately.\n" +
                        "If you wish to directly embed AIF into the selected version, you can leave " +
                        "the next text field blank. However, if you don't want to modify this version, " +
                        "but instead make a copy, the AIF installer can do that for you. Simply enter " +
                        "the name of the new 'copy' version to be created, and all info from the " +
                        "inherited version will be copied along with the AIF injections.\n" +
                        "Press the next button, and installation will commence.");

        VBox how2use = new VBox();
        how2use.setSpacing(10);

        TableView<Command> commands = new TableView<>();

        TableColumn<Command, String> commandColumn = new TableColumn<>("Command");
        commandColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getName()));

        TableColumn<Command, String> resultColumn = new TableColumn<>("Result");
        resultColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDesc()));

        commands.getColumns().addAll(commandColumn, resultColumn);
        commands.setItems(FXCollections.observableArrayList(
                new Command("/attackindicator exact", "Very precise indicator, works with lag spikes."),
                new Command("/attackindicator tickrate", "Uses the server's average tickrate."),
                new Command("/attackindicator vanilla", "The vanilla behavior of the attack indicator."),
                new Command("/attackindicator", "View the current setting.")
        ));
        commands.setMaxHeight(125);

        how2use.getChildren().add(commands);

        box.getChildren().add(newHeader("Welcome!"));
        box.getChildren().add(welcome);
        box.getChildren().add(newHeader("Installation"));
        box.getChildren().add(how2install);
        box.getChildren().add(newHeader("Commands"));
        box.getChildren().add(how2use);


        ScrollPane scrollPane = new ScrollPane(box);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        info.setContent(scrollPane);
    }

    private void initInstallTab(Tab installTab) {
        Settings settings = Settings.INSTANCE;
        VBox box = new VBox();
        box.setId("vbox");
        int padding = 10;

        box.setSpacing(padding);
        box.setPadding(new Insets(padding, padding, padding, padding));

        TextField mcDirField = new TextField();
        TextField newVersionField = new TextField(settings.getString("create-new-version", ""));
        newVersionField.setPromptText("Create new version? Leave blank or enter version name. (Ex: AttackFix)");
        ComboBox<String> versionsSelector = new ComboBox<>();

        mcDirField.textProperty().addListener((observable, oldValue, newValue) -> {
            versionsSelector.setItems(FXCollections.observableArrayList(FileUtils.getMinecraftVersions(mcDirField.getText())));
            if (versionsSelector.getItems().size() > 0 && versionsSelector.getSelectionModel().getSelectedIndex() == -1)
                versionsSelector.getSelectionModel().select(0);
        });
        mcDirField.setText(settings.getString("mc-dir", FileUtils.getMinecraftDir()));

        mcDirField.setFont(Font.font(mcDirField.getFont().getFamily(), FontPosture.ITALIC, mcDirField.getFont().getSize()));
        mcDirField.setEditable(false);
        mcDirField.setOnMouseClicked(event -> {
            DirectoryChooser dialog = new DirectoryChooser();
            dialog.setInitialDirectory(new File(mcDirField.getText()));
            dialog.setTitle("Open minecraft directory");

            File file = dialog.showDialog(stage);
            if (file != null) {
                mcDirField.setText(file.getAbsolutePath());
            }
        });
        Button next = new Button("Install");

        box.getChildren().add(newHeader("Install AIF"));
        box.getChildren().add(newParagraph("Minecraft home directory: (ex %appdata%/.minecraft)"));
        box.getChildren().add(mcDirField);
        box.getChildren().add(newParagraph("Inherit from version (Minecraft version must be 1.12.2):"));
        box.getChildren().add(versionsSelector);
        box.getChildren().add(newVersionField);
        box.getChildren().add(next);

        String lastVersion = settings.getString("inherit-from", "1.12.2");
        if (versionsSelector.getItems().contains(lastVersion))
            versionsSelector.getSelectionModel().select(lastVersion);

        next.setOnAction(e -> {
            String version = versionsSelector.getSelectionModel().getSelectedItem();

            settings.set("mc-dir", mcDirField.getText());

            settings.set("inherit-from", version);

            String newVersionName = newVersionField.getText().trim();
            while (newVersionName.contains("  "))
                newVersionName = newVersionName.replace("  ", " ");
            settings.set("create-new-version", newVersionName);

            settings.save();
            boolean isVanilla = version != null && (version.matches("1[.]\\d+[.]\\d+") || version.matches("1[.]\\d+"));
            if (isVanilla && newVersionName.isEmpty()) {
                AlertUtils.error("Installation", "Cannot install directly into a vanilla version such as " + version + "." +
                        "Instead, select another version or type in a custom version name" +
                        "such as AttackIndicatorFix-1.12.2-" + Main.VERSION + ".", null);
            } else {
                Installer installer = new Installer();
                installTab.setContent(installer.getGUI());
                new Thread(installer::install).start();
            }
        });
        installTab.setContent(box);
    }

    private void initUninstallTab(Tab uninstallTab) {
        Settings settings = Settings.INSTANCE;
        VBox box = new VBox();
        int padding = 10;

        box.setSpacing(padding);
        box.setPadding(new Insets(padding, padding, padding, padding));

        TextField mcDirField = new TextField();
        ComboBox<String> versionsSelector = new ComboBox<>();

        mcDirField.textProperty().addListener((observable, oldValue, newValue) -> {
            versionsSelector.setItems(FXCollections.observableArrayList(FileUtils.getMinecraftVersions(mcDirField.getText())));
            if (versionsSelector.getItems().size() > 0 && versionsSelector.getSelectionModel().getSelectedIndex() == -1)
                versionsSelector.getSelectionModel().select(0);
        });
        mcDirField.setText(settings.getString("mc-dir", FileUtils.getMinecraftDir()));

        mcDirField.setFont(Font.font(mcDirField.getFont().getFamily(), FontPosture.ITALIC, mcDirField.getFont().getSize()));
        mcDirField.setEditable(false);
        mcDirField.setOnMouseClicked(event -> {
            DirectoryChooser dialog = new DirectoryChooser();
            dialog.setInitialDirectory(new File(mcDirField.getText()));
            dialog.setTitle("Open minecraft directory");

            File file = dialog.showDialog(stage);
            if (file != null) {
                mcDirField.setText(file.getAbsolutePath());
            }
        });

        Button uninstallBtn = new Button("Uninstall");
        Button uninstallAllBtn = new Button("Uninstall All");

        versionsSelector.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int i = newValue.intValue();
            if (i == -1)
                uninstallBtn.setDisable(true);
            else
                uninstallBtn.setDisable(false);
        });

        uninstallBtn.setOnAction(event -> {
            String version = versionsSelector.getSelectionModel().getSelectedItem();
            settings.set("mc-dir", mcDirField.getText());
            settings.set("inherit-from", version);
            settings.save();
            Uninstaller uninstaller = new Uninstaller(version);
            uninstaller.uninstall();
            boolean errorOccurred = uninstaller.getError() != null;
            if (!errorOccurred && uninstaller.didUninstall())
                AlertUtils.info("Success", "Removed all elements of AIF from " + version + ".");
            else if (errorOccurred)
                AlertUtils.error("Uninstalling " + version, "" + uninstaller.getError(), null);
            else
                AlertUtils.info("No Action Needed", "AIF wasn't found in version " + version + ".");
        });

        uninstallAllBtn.setOnAction(event -> {
            settings.set("mc-dir", mcDirField.getText());

            List<String> didUninstall = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String version : versionsSelector.getItems()) {
                Uninstaller uninstaller = new Uninstaller(version);
                uninstaller.uninstall();

                boolean error = uninstaller.getError() != null;
                if(uninstaller.didUninstall())
                    didUninstall.add(version);
                if(error)
                    errors.add(version);
            }

            StringBuilder msg = new StringBuilder("Uninstalled ")
                    .append(didUninstall.size())
                    .append(" instances of AIF:");
            for(String version : didUninstall)
                msg.append(version).append('\n');
            msg.append('\n')
                    .append("Errors (")
                    .append(errors.size())
                    .append("):");
            for(String err : errors)
                msg.append(err).append('\n');

            AlertUtils.info("Bulk Uninstall Information", msg.substring(0, msg.length()-1));
        });

        String lastVersion = settings.getString("inherit-from", "1.12.2");
        if (versionsSelector.getItems().contains(lastVersion))
            versionsSelector.getSelectionModel().select(lastVersion);

        box.getChildren().add(newHeader("Uninstall AIF"));
        box.getChildren().add(newParagraph("Minecraft home directory: (ex %appdata%/.minecraft)"));
        box.getChildren().add(mcDirField);
        box.getChildren().add(newParagraph("Uninstall from version:"));
        box.getChildren().add(versionsSelector);
        box.getChildren().add(uninstallBtn);
        box.getChildren().add(uninstallAllBtn);
        box.setId("vbox");
        uninstallTab.setContent(box);
    }

    public void init() {
        stage.setScene(scene);
    }
}
