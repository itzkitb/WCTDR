package lol.sillyapps.WCTDR.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;

public class MainMenu {
    private static float alpha = 1.0f;
    private static float speed = 1.1f;
    private static boolean completed = false;
    public  static Timer timer = new Timer();

    public static void Show(Engine engine) {
        // intro
        String previousScreen = engine.GetCurrentScreen();
        String screen = engine.AddScreen();
        engine.ChangeScreen(screen);
        engine.SetSceneBackground(Color.BLACK);

        TimerTask task = new TimerTask() {
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (completed) {
                            System.out.println("[MainMenu.java] intro completed!");
                            timer.cancel();
                            return;
                        }

                        alpha /= speed;
                        float ralpha = 1.0f - alpha;

                        if (alpha < 0.001f)
                        {
                            completed = true;
                        }

                        engine.SetSceneBackground(new Color(ralpha, ralpha, ralpha, 1.0f));

                        System.out.println("c: " + completed + ", a: " + alpha);
                    }
                });
            }
        };

        timer = new Timer("Timer");

        long delay = 40L;
        timer.schedule(task, delay, delay);
    }

    private static void StageTwo(Engine engine) {

    }
}
