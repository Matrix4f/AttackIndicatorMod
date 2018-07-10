package me.stonepiano.cooldownfix.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Uninstaller {

    private String versionName;
    private String error;
    private boolean didUninstall;

    public Uninstaller(String versionName) {
        this.versionName = versionName;
    }

    private void error(String why) {
        error = why;
    }

    public String getError() {
        return error;
    }

    public void uninstall() {
        Settings settings = Settings.INSTANCE;
        //validating mc directory
        File mcDir = new File(settings.getString("mc-dir", ""));
        File versionsDir = new File(mcDir, "versions");
        File librariesDir = new File(mcDir, "libraries");
        {
            if (!mcDir.exists()) {
                error(mcDir.getAbsolutePath() + "\n(mc dir) doesn't exist.");
                return;
            }
            if (!versionsDir.exists()) {
                error(versionsDir.getAbsolutePath() + "\n(versions dir) doesn't exist.");
                return;
            }
            if (!librariesDir.exists()) {
                error(librariesDir.getAbsolutePath() + "\n(libraries dir) doesn't exist.");
                return;
            }
        }
        //Validating version uninstalling from
        File uninstallVersionDir;
        {
            if (versionName == null) {
                error("No version selected to install into (inherit from).");
                return;
            }
            uninstallVersionDir = new File(versionsDir, versionName);
            if (!uninstallVersionDir.exists() || !uninstallVersionDir.isDirectory()) {
                error(versionName + " is an invalid version.");
                return;
            }
        }
        //Obtaining JSON of inherited version...
        JsonObject json;
        try {
            json = new JsonParser()
                    .parse(FileUtils.read(new File(uninstallVersionDir, versionName + ".json")))
                    .getAsJsonObject();
        } catch (Exception e) {
            error("Unable to load JSON from the inherited version: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return;
        }
        boolean hasTweakers;
        {
            String mcArgs = json.get("minecraftArguments").getAsString();
            didUninstall |= mcArgs.contains(Installer.TWEAKINFO);
            mcArgs = mcArgs.replace(Installer.TWEAKINFO,"");
            while(mcArgs.contains("  "))
                mcArgs = mcArgs.replace("  ", " ");
            hasTweakers = mcArgs.contains("--tweakClass");
            json.addProperty("minecraftArguments",mcArgs.trim());
        }
        {
            if(!hasTweakers)
                json.addProperty("mainClass","net.minecraft.client.main.Main");
        }
        {
            JsonArray libraries = json.getAsJsonObject()
                    .getAsJsonArray("libraries");
            List<JsonElement> removals = new LinkedList<>();
            for (JsonElement library : libraries) {
                String libPath = library.getAsJsonObject()
                        .get("name")
                        .getAsString();
                if (libPath.equals(Installer.NAME) || libPath.equals(Installer.NAME_NOARGS)) {
                    removals.add(library);
                    didUninstall = true;
                }
            }
            removals.forEach(libraries::remove);
        }

        try {
            //writing json
            File jsonSave = new File(uninstallVersionDir, uninstallVersionDir.getName() + ".json");
            FileUtils.write(jsonSave, Main.GSON.toJson(json));
        } catch (IOException e) {
            error("Unable to write new JSON: " + e.getClass().getSimpleName() + " " + e.getMessage());
            return;
        }
        return;
    }

    public boolean didUninstall() {
        return didUninstall;
    }
}
