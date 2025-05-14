package lol.sillyapps.WCTDR.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;

public class IntroLogo {
    private static int frame = 0;
    private static float alpha = 1.0f;
    private static float alpha_result = 0.0f;
    private static float speed = 1.1f;
    private static boolean stage = false;
    private static boolean completed = false;
    public  static Timer timer = new Timer();

    public static void Show(Engine engine) {
        String previousScreen = engine.GetCurrentScreen();
        String screen = engine.AddScreen();
        String image = engine.AddImage(GetPath(0), screen, (Gdx.app.getGraphics().getWidth() - 50)/2, (Gdx.app.getGraphics().getHeight() - 50)/2, 0, 50, 50, 0, 0);
        String text = engine.AddText("SillyApps", "fonts/consolas.fnt", screen, (Gdx.app.getGraphics().getWidth()-67)/2, Gdx.app.getGraphics().getHeight()/2-30, 100, 0.5f, 0, new Color(1f, 1f, 1f, 1f));
        engine.ChangeScreen(screen);
        engine.SetSceneBackground(Color.BLACK);

        TimerTask task = new TimerTask() {
            public void run() {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (completed) {
                            System.out.println("[IntroLogo.java] completed!");
                            timer.cancel();
                            Main.AfterLogo(previousScreen, engine);
                            return;
                        }

                        ++frame;
                        alpha /= speed;
                        if (frame > 49) frame = 0;

                        engine.ChangeImage(image, GetPath(frame));
                        engine.SetItemAlpha(image, alpha_result);
                        engine.SetItemAlpha(text, alpha_result);

                        if (alpha <= 0.001f) {
                            if (!stage) {
                                stage = true;
                                alpha = 1f;
                            } else {
                                completed = true;
                            }
                        }

                        if (stage) {
                            alpha_result = alpha;
                        } else {
                            alpha_result = 1f - alpha;
                        }

                        System.out.println("c: " + completed + ", a: " + alpha + ", ar: " + alpha_result + ", s: " + stage);
                    }
                });
            }
        };

        timer = new Timer("Timer");

        long delay = 40L;
        timer.schedule(task, delay, delay);
    }

    private static String GetPath(int frame) {
        return "lmao/" + frame + ".png";
    }
}
