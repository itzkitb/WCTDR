package lol.sillyapps.WCTDR.scenes;

import static lol.sillyapps.WCTDR.Engine.Debug;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;

public class IntroLogo {
    private float alpha = 1.0f;
    private boolean stage = false;
    private final Engine engine;
    private String currentScreen;
    private String previousScreen;
    private String logoImage;
    private String logoText;
    private boolean isAnimating = false;

    public IntroLogo(Engine engine) {
        this.engine = engine;
    }

    public void show() {
        previousScreen = engine.GetCurrentScreen();
        currentScreen = engine.AddScreen();

        List<String> framePaths = new ArrayList<>();

        for (int i = 0; i < 50; i++)
        {
            framePaths.add(getFramePath(i));
        }

        logoImage = engine.AddAnimatedImage(
            framePaths,
            currentScreen,
            0,
            0,
            0,
            50,
            50,
            0,
            0,
            0.1f
        );
        engine.SetItemAlignment(logoImage, Engine.Alignment.CENTER);

        logoText = engine.AddText(
            "SillyApps",
            "fonts/tiny5.fnt",
            currentScreen,
            0,
            -30,
            100,
            0.5f,
            0,
            new Color(1f, 1f, 1f, 1f)
        );
        engine.SetItemAlignment(logoText, Engine.Alignment.CENTER);

        engine.ChangeScreen(currentScreen);
        engine.SetSceneBackground(Color.BLACK);

        startAnimation();
    }

    private void startAnimation() {
        if (isAnimating) return;
        isAnimating = true;

        engine.SetItemAlpha(logoImage, 0f);
        engine.SetItemAlpha(logoText, 0f);

        engine.new Animator().Alpha(logoText, 1f, 1f, () -> {});
        engine.new Animator().Alpha(logoImage, 1f, 1f, () -> {

            engine.new Animator().Alpha(logoText, 0f, 2f, () -> {});
            engine.new Animator().Alpha(logoImage, 0f, 2f, () -> {
                Debug("Animation completed!", "IntroLogo");
                Main.AfterLogo(previousScreen, engine);
            });
        });
    }

    private String getFramePath(int frame) {
        return "lmao/" + frame + ".png";
    }
}
