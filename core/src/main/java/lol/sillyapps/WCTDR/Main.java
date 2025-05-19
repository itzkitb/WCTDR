package lol.sillyapps.WCTDR;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.concurrent.CompletableFuture;

import lol.sillyapps.WCTDR.scenes.IntroLogo;
import lol.sillyapps.WCTDR.scenes.MainMenu;

public class Main extends ApplicationAdapter {
    private Engine engine;
    public  static final String version = "1.1";
    public  static AudioEngine audioEngine = new AudioEngine();

    @Override
    public void create() {
        // WHO CREATED THIS DISGUSTING ROCKET?
        // Answer: IDK lol

        // Initialize engine
        engine = new Engine();
        engine.Initialize();

        // Settings
        Settings.LoadSettings();

        // Create a new screen and set its background
        engine.AddScreen();
        engine.SetSceneBackground(Color.BLACK);

        // Little delay
        Delay(2000);

        // Intro
        IntroLogo logo = new IntroLogo(engine);
        logo.show();
    }

    public static void AfterLogo(String screenUUID, Engine engine) {
        engine.ChangeScreen(screenUUID);
        new MainMenu().Show(engine);
    }

    @Override
    public void render() {
        audioEngine.update(Gdx.graphics.getDeltaTime());

        // Render the scene via the engine.
        engine.Render();
    }

    @Override
    public void dispose() {
        // Clean up resources.
        engine.Dispose();
    }

    private static void Delay(int time) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        future.join();
    }
}
