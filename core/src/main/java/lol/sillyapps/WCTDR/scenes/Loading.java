package lol.sillyapps.WCTDR.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.Arrays;
import java.util.List;

import lol.sillyapps.WCTDR.Engine;

public class Loading {
    public static void Main(Engine engine)
    {
        String previousScreen = engine.GetCurrentScreen();
        String screen = engine.AddScreen();
        engine.ChangeScreen(screen);
        engine.DeleteScreen(previousScreen);
        engine.SetSceneBackground(Color.BLACK);

        List<String> rocketFrames = Arrays.asList("rocket/fly_0_0.png", "rocket/fly_1_1.png", "rocket/fly_1_0.png", "rocket/fly_0_1.png");
        String rocket = engine.AddAnimatedImage(
            rocketFrames,
            screen,
            -33,
            10,
            100,
            41,
            87,
            0,
            1f,
            0.5f
        );

        engine.SetItemAlignment(rocket, Engine.Alignment.BOTTOM_RIGHT);
        engine.SetItemAlpha(rocket, 0);
        engine.new Animator().Alpha(rocket, 1f, 0.5f, () -> {

        });
    }
}
