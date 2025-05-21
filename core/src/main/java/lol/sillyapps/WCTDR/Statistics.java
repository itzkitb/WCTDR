package lol.sillyapps.WCTDR;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Statistics {
    public static int Credits = 0;
    public static int Protection = 0;
    public static int FuelReserve = 0;
    public static int Speed = 0;
    public static int Braking = 0;
    public static int BestRecord = 0;

    private static final String FILE = "game.data";

    public static void Load() {
        Properties props = new Properties();
        File file = new File(System.getProperty("GAME"), FILE);

        try (InputStream is = new FileInputStream(file)) {
            if (file.exists()) {
                props.load(is);
            }
            else
            {
                Save();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Save();
        }

        // Load values with defaults
        Credits = Integer.parseInt(props.getProperty("0", "1000"));
        Protection = Integer.parseInt(props.getProperty("1", "1"));
        FuelReserve = Integer.parseInt(props.getProperty("2", "1"));
        Speed = Integer.parseInt(props.getProperty("3", "1"));
        Braking = Integer.parseInt(props.getProperty("4", "1"));
        BestRecord = Integer.parseInt(props.getProperty("5", "0"));
    }

    public static void Save() {
        Properties props = new Properties();
        props.setProperty("0", String.valueOf(Credits));
        props.setProperty("1", String.valueOf(Protection));
        props.setProperty("2", String.valueOf(FuelReserve));
        props.setProperty("3", String.valueOf(Speed));
        props.setProperty("4", String.valueOf(Braking));
        props.setProperty("5", String.valueOf(BestRecord));

        File file = new File(System.getProperty("GAME"), FILE);

        try (OutputStream os = new FileOutputStream(file)) {
            props.store(os, "Application Data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
