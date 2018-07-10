package me.stonepiano.cooldownfix.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static me.stonepiano.cooldownfix.installer.Main.VERSION;

public class Installer {

    public static final String NAME = "me.stonepiano:AttackIndicatorFix:" + VERSION;
    public static final String NAME_NOARGS = "me.stonepiano:AttackIndicatorFixNoArgs:" + VERSION;
    public static final String URL = "me/stonepiano/AttackIndicatorFix/" + VERSION + "/AttackIndicatorFix-" + VERSION + ".jar";
    public static final String URL_NOARGS = "me/stonepiano/AttackIndicatorFixNoArgs/" + VERSION + "/AttackIndicatorFixNoArgs-" + VERSION + ".jar";

    public static final String TWEAKINFO = "--tweakClass me.stonepiano.cooldownfix.Tweaker";

    private VBox box;
    private TextArea log;

    public Installer() {
        box = new VBox();
        box.setId("vbox");
        box.setSpacing(10);
        box.setPadding(new Insets(10, 10, 10, 10));

        log = new TextArea();
        log.setEditable(false);
        log.setPrefHeight(250);
        log("- - Installer Messages - -");

        box.getChildren().add(log);
    }

    private void log(String info) {
        log.appendText(info + "\n");
    }

    private void writeToLog(Exception e) {
        e.printStackTrace(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                if (b != -1)
                    log.appendText(String.valueOf((char) b));
            }
        }));
    }

    public void install() {
        long ms = System.currentTimeMillis();
        log("Starting installation.");
        Settings settings = Settings.INSTANCE;
        log("Validating MC directory...");
        File mcDir = new File(settings.getString("mc-dir", ""));
        File versionsDir = new File(mcDir, "versions");
        File librariesDir = new File(mcDir, "libraries");
        {
            if (!mcDir.exists()) {
                log(mcDir.getAbsolutePath() + " (mc dir) doesn't exist.");
                return;
            }
            if (!versionsDir.exists()) {
                log(versionsDir.getAbsolutePath() + " (versions dir) doesn't exist.");
                return;
            }
            if (!librariesDir.exists()) {
                log(librariesDir.getAbsolutePath() + " (libraries dir) doesn't exist.");
                return;
            }
        }
        log("Validating inherit-from and create-new-version properties...");
        String inheritFrom = settings.getString("inherit-from", null);
        String newVersionName = settings.getString("create-new-version", "");
        boolean installDirectly = newVersionName.isEmpty();
        File inheritFromFile;

        {
            if (inheritFrom == null) {
                log("No version selected to install into (inherit from).");
                return;
            }
            inheritFromFile = new File(versionsDir, inheritFrom);
            if (!inheritFromFile.exists() || !inheritFromFile.isDirectory()) {
                log(inheritFrom + " is an invalid version.");
                return;
            }
        }
        log("Obtaining JSON of inherited version...");
        JsonObject json;
        boolean inheritedFromTweaker = false;
        try {
            String raw = FileUtils.read(new File(inheritFromFile, inheritFrom + ".json"));
            json = new JsonParser()
                    .parse(raw)
                    .getAsJsonObject();
        } catch (Exception e) {
            log("Unable to load JSON from the inherited version: " + e.getClass().getSimpleName() + " " + e.getMessage());
            writeToLog(e);
            return;
        }
        {
            json.addProperty("id", installDirectly ? inheritFrom : newVersionName);
            json.addProperty("mainClass", "net.minecraft.launchwrapper.Launch");
        }
        {
            String mcArgs = json.get("minecraftArguments").getAsString();
            if (!mcArgs.endsWith(" "))
                mcArgs += " ";

            Pattern pattern = Pattern.compile("--tweakClass \\S+");
            Matcher matcher = pattern.matcher(mcArgs);
            boolean alreadyHasTweaker = false;
            while (matcher.find()) {
                String group = matcher.group();
                if (!group.equals(TWEAKINFO)) {
                    inheritedFromTweaker = true;
                    log("Found other tweaker: " + group.split("[ ]")[1]);
                } else {
                    log("Already contains tweaker declaration. No need to insert it.");
                    alreadyHasTweaker = true;
                }
            }

            if (!alreadyHasTweaker)
                json.addProperty("minecraftArguments", mcArgs + TWEAKINFO);
            log("Inserted tweaker declaration.");
        }
        {
            JsonArray libraries = json.getAsJsonObject()
                    .getAsJsonArray("libraries");
            List<JsonElement> removals = new LinkedList<>();
            for (JsonElement library : libraries) {
                String libPath = library.getAsJsonObject()
                        .get("name")
                        .getAsString();
                if (libPath.equals(NAME) || libPath.equals(NAME_NOARGS)) {
                    log("Found a preexisting library declaration. Removing it and reinserting.");
                    removals.add(library);
                }
            }
            for (JsonElement library : libraries) {
                if (library.getAsJsonObject()
                        .get("name")
                        .getAsString()
                        .equals("net.minecraft:launchwrapper:1.12")) {
                    removals.add(library);
                }
            }
            removals.forEach(libraries::remove);

            JsonObject library = new JsonObject();
            library.addProperty("name", inheritedFromTweaker ? NAME_NOARGS : NAME);
            libraries.add(library);

            JsonObject launchwrapperLib = new JsonObject();
            launchwrapperLib.addProperty("name", "net.minecraft:launchwrapper:1.12");
            libraries.add(launchwrapperLib);
            log("Inserted library declaration.");
        }
        try {
            File libraryParent = new File(librariesDir, (inheritedFromTweaker ? URL_NOARGS : URL) + "/");
            libraryParent.getParentFile().mkdirs();

            log("Installing libraries...");
            JarInputStream istream = new JarInputStream(Main.class.getResourceAsStream("/data.jar"));
            JarOutputStream ostream = new JarOutputStream(new FileOutputStream(libraryParent));
            JarEntry jarEntry;
            while ((jarEntry = istream.getNextJarEntry()) != null) {
                byte[] bytes = FileUtils.isToBytes(istream);

                ostream.putNextEntry(new JarEntry(jarEntry));
                ostream.write(bytes);
                ostream.closeEntry();
            }

            if (inheritedFromTweaker) {
                ostream.putNextEntry(new JarEntry("dont-add-args.txt"));
                ostream.closeEntry();
            }

            istream.close();
            ostream.close();
            log("Done!");
        } catch (Exception e) {
            log("Unable to install library: " + e.getClass().getSimpleName() + " " + e.getMessage());
            writeToLog(e);
            return;
        }
        log("Editing/creating version files.");
        File newVersionDir;
        if (installDirectly) {
            newVersionDir = inheritFromFile;
            log("Installing directly into " + newVersionDir.getAbsolutePath() + ".");
        } else {
            newVersionDir = new File(versionsDir, newVersionName + "/");
            if (newVersionDir.exists()) {
                try {
                    FileUtils.deleteDirectory(newVersionDir);
                    log("Deleted " + newVersionDir.getAbsolutePath() + ", as it already existed.");
                    newVersionDir.mkdir();
                } catch (Exception e) {
                    log("Unable to delete already-existing version directory " + newVersionName + ".");
                    writeToLog(e);
                }
            }
            if (!newVersionDir.mkdir() && !newVersionDir.exists()) {
                log("Unable to create directory " + newVersionDir.getAbsolutePath() + " for new version.");
                return;
            }
        }

        try {
            log("Writing JSON...");
            File jsonSave = new File(newVersionDir, newVersionDir.getName() + ".json");
            FileUtils.write(jsonSave, Main.GSON.toJson(json));
        } catch (IOException e) {
            log("Unable to write new JSON: " + e.getClass().getSimpleName() + " " + e.getMessage());
            writeToLog(e);
            return;
        }

        if (!installDirectly) {
            log("Writing JAR...");
            File srcJar = new File(inheritFromFile, inheritFrom + ".jar");
            File destJar = new File(newVersionDir, newVersionName + ".jar");
            try {
                FileUtils.copy(srcJar, destJar, 16384);
            } catch (IOException e) {
                log("Unable to copy inherited version jar: " + e.getClass().getSimpleName() + " " + e.getMessage());
                writeToLog(e);
                return;
            }
            log("Done!");
        }
        log("Finished installing AttackIndicatorFix-" + Main.VERSION + " into " + inheritFrom + " in " + (System.currentTimeMillis() - ms) + "ms.");
    }

    public Node getGUI() {
        return box;
    }
}
