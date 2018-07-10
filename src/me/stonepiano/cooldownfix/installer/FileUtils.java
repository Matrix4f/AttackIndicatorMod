package me.stonepiano.cooldownfix.installer;

import javafx.scene.control.Alert;

import java.io.*;

public class FileUtils {

    public static byte[] isToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static void copy(File src, File dest, int bufSize) throws IOException {
        FileOutputStream out = new FileOutputStream(dest);
        FileInputStream in = new FileInputStream(src);

        int nRead;
        byte[] data = new byte[bufSize];

        while ((nRead = in.read(data, 0, data.length)) != -1)
            out.write(data, 0, nRead);

        out.close();
    }

    public static void write(File file, String data) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(data);
        bw.close();
    }

    public static String read(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder buf = new StringBuilder();
        String ln;
        while ((ln = reader.readLine()) != null)
            buf.append(ln).append(System.lineSeparator());
        reader.close();

        return buf.toString();
    }

    public static String[] getMinecraftVersions(String mcDirPath) {
        File mcDir = new File(mcDirPath);
        if (!mcDir.exists())
            return new String[0];
        File versions = new File(mcDir, "versions");
        if (!versions.exists())
            return new String[0];
        File[] files = versions.listFiles(File::isDirectory);
        String[] names = new String[files.length];
        for (int i = 0; i < names.length; i++)
            names[i] = files[i].getName();
        return names;
    }

    public static String getMinecraftDir() {
        String userHome = System.getProperty("user.home", ".");
        File dir;
        OS platform = getPlatform();
        if (platform == OS.UNKNOWN)
            AlertUtils.crash("Getting minecraft directory", "Didn't recognize " + System.getProperty("os.name") + " as a valid OS.", new RuntimeException());
        switch (platform) {
            case LINUX:
            case SOLARIS:
                dir = new File(userHome, ".minecraft/");
                break;
            case WINDOWS:
                String appdata = System.getenv("APPDATA");
                if (appdata == null)
                    AlertUtils.crash("Getting minecraft directory", "Couldn't find %appdata%", new RuntimeException());

                dir = new File(appdata, ".minecraft/");
                break;
            case MAC:
                dir = new File(userHome, "Library/Application Support/minecraft");
                break;
            default:
                dir = new File(userHome, "minecraft/");
                break;
        }
        if (!dir.exists())
            AlertUtils.crash("Getting minecraft directory", dir.getAbsolutePath() + " doesn't exist.", new RuntimeException());
        return dir.getAbsolutePath();
    }

    public static OS getPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OS.WINDOWS;
        }
        if (osName.contains("mac")) {
            return OS.MAC;
        }
        if (osName.contains("solaris")) {
            return OS.SOLARIS;
        }
        if (osName.contains("sunos")) {
            return OS.SOLARIS;
        }
        if (osName.contains("linux")) {
            return OS.LINUX;
        }
        if (!osName.contains("unix"))
            return OS.UNKNOWN;
        return OS.LINUX;
    }
}
