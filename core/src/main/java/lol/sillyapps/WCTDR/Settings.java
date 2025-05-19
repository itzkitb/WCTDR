package lol.sillyapps.WCTDR;

import java.io.*;
import java.util.Properties;

public class Settings {
    public static boolean VSync = false;
    public static boolean Clouds = false;
    public static boolean Music = false;
    public static boolean Sounds = false;
    public static boolean ScreenButtons = false;
    public static int MusicLevel = 0;
    public static int SoundsLevel = 0;

    private static final String SETTINGS_FILE = "settings.properties";

    public static void LoadSettings() {
        Properties props = new Properties();
        File file = new File(System.getProperty("user.SETTINGS"), SETTINGS_FILE);

        try (InputStream is = new FileInputStream(file)) {
            if (file.exists()) {
                props.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load values with defaults
        VSync = Boolean.parseBoolean(props.getProperty("VSync", "true"));
        Clouds = Boolean.parseBoolean(props.getProperty("Clouds", "true"));
        Music = Boolean.parseBoolean(props.getProperty("Music", "true"));
        Sounds = Boolean.parseBoolean(props.getProperty("Sounds", "true"));
        ScreenButtons = Boolean.parseBoolean(props.getProperty("ScreenButtons", "false"));
        MusicLevel = Integer.parseInt(props.getProperty("MusicLevel", "3"));
        SoundsLevel = Integer.parseInt(props.getProperty("SoundsLevel", "3"));
    }

    public static void SaveSettings() {
        Properties props = new Properties();
        props.setProperty("VSync", String.valueOf(VSync));
        props.setProperty("Clouds", String.valueOf(Clouds));
        props.setProperty("Music", String.valueOf(Music));
        props.setProperty("Sounds", String.valueOf(Sounds));
        props.setProperty("MusicLevel", String.valueOf(MusicLevel));
        props.setProperty("SoundsLevel", String.valueOf(SoundsLevel));
        props.setProperty("ScreenButtons", String.valueOf(ScreenButtons));

        File file = new File(System.getProperty("user.SETTINGS"), SETTINGS_FILE);

        try (OutputStream os = new FileOutputStream(file)) {
            props.store(os, "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
