package lol.sillyapps.WCTDR.scenes;

import static lol.sillyapps.WCTDR.Engine.Debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.Arrays;
import java.util.List;

import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;
import lol.sillyapps.WCTDR.Settings;

public class Game {
    private Engine engine;
    private String bgMusic;
    private String rocket;
    private boolean Disposed = false;

    public void Main(Engine engine) {
        engine.ChangeScreen(engine.AddScreen());
        engine.SetSceneBackground(Color.BLACK);
        this.engine = engine;

        if (Settings.Music) {
            bgMusic = Main.audioEngine.loadMusic("music/gravity_calling.mp3");
            Main.audioEngine.playMusic(bgMusic, true);
            Main.audioEngine.fadeIn(bgMusic, 1f, ()->{});
        }

        Initialize();
    }

    private void Initialize()
    {
        String fadein = engine.AddImage(
            "black.png",
            engine.GetCurrentScreen(),
            0,
            0,
            10000,
            10000,
            10000,
            0,
            1
        );

        for (int i = 0; i < 6; i++)
        {
            String planet = engine.AddImage(
                "start_planet.png",
                engine.GetCurrentScreen(),
                i*855-855*3,
                0,
                0,
                855,
                100,
                0,
                1
            );

            engine.SetItemAlignment(planet, Engine.Alignment.BOTTOM_CENTER);
        }

        List<String> baseFrames = Arrays.asList("base/0.png", "base/1.png", "base/2.png", "base/3.png", "base/4.png", "base/5.png");
        String base = engine.AddAnimatedImage(
            baseFrames,
            engine.GetCurrentScreen(),
            0,
            50,
            0,
            440,
            200,
            0,
            1,
            0.356f
        );

        List<String> frames = Arrays.asList("rocket/0.png", "rocket/1.png");
        rocket = engine.AddAnimatedImage(
            frames,
            engine.GetCurrentScreen(),
            -48,
            50,
            0,
            165,
            260,
            0,
            1,
            1
        );

        String back = engine.AddButton(
            "button/normal.png",
            "fonts/consolas.fnt",
            engine.GetCurrentScreen(),
            10,
            10,
            10,
            200,
            50,
            "Back",
            (Engine.Button btn) -> {
                Debug("1: " + btn.uuid, "Game");
                if (Disposed) return;
                PressButton();

                Debug("2: " + btn.uuid, "Game");

                String fadeout = engine.AddImage(
                    "black.png",
                    engine.GetCurrentScreen(),
                    0,
                    0,
                    1000,
                    10000,
                    10000,
                    0,
                    0
                );

                Debug("3: " + btn.uuid, "Game");

                engine.SetItemAlignment(fadeout, Engine.Alignment.CENTER);

                if (Settings.Music) {
                    Main.audioEngine.fadeOut(bgMusic, 1f, () -> {});
                }
                engine.new Animator().Alpha(fadeout, 1f, 1f, ()->{
                    Disposed = true;
                    new MainMenu().Show(engine);
                });

                Debug("4: " + btn.uuid, "Game");

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Back pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Back hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Back leaved: " + btn.uuid, "Game");
            }
        );

        engine.SetItemAlignment(fadein, Engine.Alignment.CENTER);
        engine.SetItemAlignment(base, Engine.Alignment.BOTTOM_CENTER);
        engine.SetItemAlignment(rocket, Engine.Alignment.BOTTOM_CENTER);
        engine.SetItemAlignment(back, Engine.Alignment.BOTTOM_LEFT);

        engine.new Animator().Alpha(fadein, 1f, 0.25f, () -> {
            engine.new Animator().Alpha(fadein, 0f, 1f, () -> {});
        });

        for (int i = 0; i < 25; i++)
        {
            SpawnCloud();
        }

        engine.SetSceneBackground(new Color(103f/255f, 107f/255f, 127f/255f, 1f));
    }

    public void SpawnCloud()
    {
        if (!Settings.Clouds || Disposed) return;

        float width = Gdx.app.getGraphics().getWidth() / 2;
        float height = Gdx.app.getGraphics().getWidth() / 2;
        float x = (float)(Math.random() * width * 2 - width);
        float y = (float)(Math.random() * height * 2 - height);

        float osize = (float)(1000 + Math.random() * 1000);
        int z = (int)(Math.random()*6-5);

        String cloud = engine.AddImage(
            "clouds/" + (int)(Math.random() * 3) + ".png",
            engine.GetCurrentScreen(),
            (float)(Math.random() * width * 2 - width),
            (float)(Math.random() * height * 2 - height),
            z,
            osize,
            osize,
            0,
            0
        );

        engine.SetItemAlignment(cloud, Engine.Alignment.TOP_CENTER);
        float time = (float)(0.45f + Math.random() * 0.45f);

        engine.new Animator().Alpha(cloud, z>=0 ? 0.25f : 1f, time/2, () -> {
            engine.new Animator().Alpha(cloud, 0f, time/2, () -> {});
        });

        engine.new Animator().Move(cloud, x, y, time, () -> {
            RespawnCloud(cloud);
        });
    }

    public void RespawnCloud(String UUID)
    {
        if (Disposed) return;
        if (!Settings.Clouds) return;

        float width = Gdx.app.getGraphics().getWidth() / 2;
        float height = Gdx.app.getGraphics().getWidth() / 2;
        float x = (float)(Math.random() * width * 2 - width);
        float y = (float)(Math.random() * height * 2 - height);
        int z = (int)(Math.random()*6-5);

        float osize = (float)(1000 + Math.random() * 1000);
        Engine.ImageItem cloud = (Engine.ImageItem)engine.GetItem(UUID);

        cloud.x = (float)(Math.random() * width * 2 - width);
        cloud.y = (float)(Math.random() * height * 2 - height);
        cloud.width = osize;
        cloud.height = osize;
        cloud.z = z;
        cloud.alpha = 0f;

        float time = (float)(0.45f + Math.random() * 0.45f);

        engine.new Animator().Alpha(UUID, z>=0 ? 0.25f : 1f, time/2, () -> {
            engine.new Animator().Alpha(UUID, 0f, time/2, () -> {

            });
        });

        engine.new Animator().Move(UUID, x, y, time, () -> {
            RespawnCloud(UUID);
        });
    }

    private void PressButton() {
        if (Disposed) return;
        if (Settings.Sounds) {
            String sound = Main.audioEngine.loadSound("sounds/button.mp3");
            Main.audioEngine.playSound(sound, 1f);
        }
    }

    public void ChangeButtonImage(String UUID, String Path)
    {
        if (Disposed) return;
        engine.ChangeImage(((Engine.Button)engine.GetItem(UUID)).imageItem.uuid, Path);
    }
}
