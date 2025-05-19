package lol.sillyapps.WCTDR.scenes;

import com.badlogic.gdx.graphics.Color;

import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;

public class Game {
    private Engine engine;
    private String bgMusic;

    public void Main(Engine engine) {
        String previousScreen = engine.GetCurrentScreen();
        String screen = engine.AddScreen();
        engine.ChangeScreen(screen);
        engine.DeleteScreen(previousScreen);
        engine.SetSceneBackground(new Color(33f/255f, 39f/255f, 63f/255f, 1f));
        this.engine = engine;

        bgMusic = Main.audioEngine.loadMusic("music/gravity_calling.mp3");
        Main.audioEngine.playMusic(bgMusic, true);

        Main.audioEngine.fadeIn(bgMusic, 1f, ()->{});

        Initialize();
    }

    private void Initialize()
    {
        String fadein = engine.AddImage(
            "black.png",
            engine.GetCurrentScreen(),
            0,
            0,
            1000,
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
                i*855-2565,
                0,
                0,
                855,
                100,
                0,
                1
            );

            engine.SetItemAlignment(planet, Engine.Alignment.BOTTOM_CENTER);
        }

        String base = engine.AddImage(
            "base.png",
            engine.GetCurrentScreen(),
            0,
            50,
            0,
            440,
            200,
            0,
            1
        );

        engine.SetItemAlignment(fadein, Engine.Alignment.CENTER);
        engine.SetItemAlignment(base, Engine.Alignment.BOTTOM_CENTER);

        engine.new Animator().Alpha(fadein, 0f, 1f, () -> {});
    }
}
