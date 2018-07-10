package me.stonepiano.cooldownfix.installer;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import javafx.scene.control.Alert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CrashReports {

    public static String timestamp() {
        GregorianCalendar calendar = new GregorianCalendar();
        String[] months = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };
        String month = months[calendar.get(Calendar.MONTH)];
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return month + "-" + day + "-" + year + "_" + hour + "." + minute + "." + second;
    }

    public static String save(Exception e) throws Exception {
        String filename = "crash_" + timestamp() + ".txt";
        ByteOutputStream sos = new ByteOutputStream();
        e.printStackTrace(new PrintStream(sos));

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write("AttackIndicator Installer " + Main.VERSION + " Crash Report.");
        writer.newLine();
        writer.write("Timestamp: " + timestamp());
        writer.newLine();
        writer.newLine();
        writer.write("System info: ");
        writer.newLine();
        writer.write("    os.name= " + System.getProperty("os.name"));
        writer.newLine();
        writer.write("    java.version= " + System.getProperty("java.version"));
        writer.newLine();
        writer.newLine();

        writer.write("Stacktrace:");
        writer.newLine();
        writer.write(new String(sos.getBytes()));
        writer.close();

        return new File(filename).getAbsolutePath();
    }
}