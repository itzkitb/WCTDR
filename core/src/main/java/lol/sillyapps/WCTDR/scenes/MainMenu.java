package lol.sillyapps.WCTDR.scenes;

import static lol.sillyapps.WCTDR.Engine.Debug;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import lol.sillyapps.WCTDR.Main;
import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Settings;

public class MainMenu {
    private Map<String, List<String>> scenes = new HashMap<>();
    private Map<String, String> menuItems = new HashMap<>();
    private List<String> menuItemsList = new ArrayList<>();
    private List<String> clouds = new ArrayList<>();
    private String  currentScreen = "main";
    private boolean changingScreen = false;
    public  Timer   timer = new Timer();
    private boolean completed = false;
    private boolean Disposed = false;
    private String  backgroundMusic;
    public  Engine  engine;

    private static float COLOR_R = 103f / 255f;
    private static float COLOR_G = 107f / 255f;
    private static float COLOR_B = 127f / 255f;
    private static final float ALPHA_THRESHOLD = 0.001f;
    private static final float STAGE_TWO_TRIGGER = 0.1f;

    public void Show(Engine engine_global) {
        if (Settings.Music) {
            backgroundMusic = Main.audioEngine.loadMusic("music/stardust_dreams.mp3");
            Main.audioEngine.playMusic(backgroundMusic, true);
        }

        if (Disposed) return;
        engine = engine_global;
        setupScreen();
        engine.SetSceneBackground(new Color(103f / 255f, 107f / 255f, 127f / 255f, 1f));

        InititalizeMenu();
        InitializeAbout();
        InitializeSettings();

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

        engine.new Animator().Alpha(fadein, 0f, 0.5f, ()->{
            engine.new Animator().Alpha(fadein, 0f, 1f, () -> {});
        });

        StageTwo();
    }

    private void setupScreen() {
        if (Disposed) return;
        String previousScreen = engine.GetCurrentScreen();
        String newScreen = engine.AddScreen();
        engine.DeleteScreen(previousScreen);
        engine.ChangeScreen(newScreen);
        engine.SetSceneBackground(Color.BLACK);
    }

    private void InititalizeMenu()
    {
        if (Disposed) return;
        List<String> logoFrames = Arrays.asList("logo/0.png", "logo/1.png", "logo/2.png");
        String logoImage = engine.AddAnimatedImage(
            logoFrames,
            engine.GetCurrentScreen(),
            10,
            -Gdx.app.getGraphics().getHeight()-165,
            10,
            240,
            165,
            0,
            1f,
            0.5f
        );
        menuItemsList.add(logoImage);
        menuItems.put("logoImage", logoImage);

        String settingsButton = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            10,
            -Gdx.app.getGraphics().getHeight()-50-10-50,
            10,
            200,
            50,
            "Settings",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                ChangeScreen("settings");
                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Settings pressed: " + btn.uuid, "MainMenu");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Settings hovered: " + btn.uuid, "MainMenu");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Settings leaved: " + btn.uuid, "MainMenu");
            }
        );
        menuItemsList.add(settingsButton);
        menuItems.put("settingsButton", settingsButton);

        String aboutButton = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            10,
            -Gdx.app.getGraphics().getHeight()-50,
            10,
            200,
            50,
            "About",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                ChangeScreen("about");
                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("About pressed: " + btn.uuid, "MainMenu");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("About hovered: " + btn.uuid, "MainMenu");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("About leaved: " + btn.uuid, "MainMenu");
            }
        );
        menuItemsList.add(aboutButton);
        menuItems.put("aboutButton", aboutButton);

        List<String> rocketFrames = Arrays.asList("rocket/fly_0_0.png", "rocket/fly_1_1.png", "rocket/fly_1_0.png", "rocket/fly_0_1.png");
        String rocket = engine.AddAnimatedImage(
            rocketFrames,
            engine.GetCurrentScreen(),
            -50,
            -Gdx.app.getGraphics().getHeight(),
            -1,
            247,
            525,
            0,
            1f,
            0.5f
        );
        menuItems.put("rocket", rocket);

        String copyrightText = engine.AddText(
            "(C) 2025 SillyApps | v." + Main.version,
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -10,
            -Gdx.app.getGraphics().getHeight(),
            10,
            0.5f,
            0,
            Color.BLACK
        );
        menuItems.put("copyrightText", copyrightText);

        String startButton = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            10,
            -Gdx.app.getGraphics().getHeight()-50-10-10-50-50,
            10,
            200,
            50,
            "Start",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                changingScreen = true;

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

                if (Settings.Music) {
                    Main.audioEngine.fadeOut(backgroundMusic, 0.5f, () -> {

                    });
                }
                engine.new Animator().Alpha(fadeout, 1f, 0.5f, () -> {
                    engine.SetSceneBackground(Color.BLACK);
                    Dispose();
                    engine.DeleteScreen(engine.GetCurrentScreen());
                    new GameStart().Main(engine);
                });

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Start pressed: " + btn.uuid, "MainMenu");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Start hovered: " + btn.uuid, "MainMenu");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Start leaved: " + btn.uuid, "MainMenu");
            }
        );
        menuItemsList.add(startButton);
        menuItems.put("startButton", startButton);

        engine.SetItemAlignment(logoImage, Engine.Alignment.TOP_LEFT);
        engine.SetItemAlignment(startButton, Engine.Alignment.BOTTOM_LEFT);
        engine.SetItemAlignment(settingsButton, Engine.Alignment.BOTTOM_LEFT);
        engine.SetItemAlignment(aboutButton, Engine.Alignment.BOTTOM_LEFT);
        engine.SetItemAlignment(rocket, Engine.Alignment.MIDDLE_RIGHT);
        engine.SetItemAlignment(copyrightText, Engine.Alignment.BOTTOM_RIGHT);

        engine.SetItemAlpha(logoImage, 0);
        engine.SetItemAlpha(startButton, 0);
        engine.SetItemAlpha(settingsButton, 0);
        engine.SetItemAlpha(aboutButton, 0);
        engine.SetItemAlpha(rocket, 0);
        engine.SetItemAlpha(copyrightText, 0);

        Debug("Initialized", "Menu");
    }

    private void StageTwo() {
        if (Disposed) return;
        engine.new Animator().Move(menuItems.get("logoImage"), 10, -10, 1f, () -> {});
        engine.new Animator().Move(menuItems.get("copyrightText"), -10, 20, 1f, () -> {});
        engine.new Animator().Move(menuItems.get("startButton"), 10, 10+10+10+50+50, 1f, () -> {});
        engine.new Animator().Move(menuItems.get("settingsButton"), 10, 10+10+50, 1f, () -> {});
        engine.new Animator().Move(menuItems.get("aboutButton"), 10, 10, 1f, () -> {});
        engine.new Animator().Move(menuItems.get("rocket"), -50, 0, 1f, () -> {
            RocketShake(menuItems.get("rocket"));
            RocketRotate(menuItems.get("rocket"));
        });

        engine.SetItemAlpha(menuItems.get("logoImage"), 1);
        engine.SetItemAlpha(menuItems.get("startButton"), 1);
        engine.SetItemAlpha(menuItems.get("settingsButton"), 1);
        engine.SetItemAlpha(menuItems.get("aboutButton"), 1);
        engine.SetItemAlpha(menuItems.get("rocket"), 1);
        engine.SetItemAlpha(menuItems.get("copyrightText"), 1);

        for (int i = 0; i < 25; i++)
        {
            SpawnCloud();
        }
        scenes.put("main", menuItemsList);
    }

    public void InitializeAbout()
    {
        if (Disposed) return;
        List<String> items = new ArrayList<>();
        String title = engine.AddText(
            "About",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            -20,
            10,
            1f,
            0,
            Color.BLACK
        );
        items.add(title);

        String text = engine.AddText(
            "Game by megableh\n\nLibraries:\n- java.util,\n- java.text,\n- com.badlogic.gdx\n\n<3",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            0,
            10,
            1f,
            0,
            Color.BLACK
        );
        items.add(text);

        String back = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            10,
            10,
            10,
            200,
            50,
            "Back",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                ChangeScreen("main");
                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Back pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Back hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Back leaved: " + btn.uuid, "About");
            }
        );
        items.add(back);

        engine.SetItemAlignment(title, Engine.Alignment.TOP_CENTER);
        engine.SetItemAlignment(text, Engine.Alignment.CENTER);
        engine.SetItemAlignment(back, Engine.Alignment.BOTTOM_LEFT);

        engine.SetItemAlpha(title, 0);
        engine.SetItemAlpha(text, 0);
        engine.SetItemAlpha(back, 0);

        scenes.put("about", items);

        Debug("Initialized", "About");
    }

    public void InitializeSettings()
    {
        if (Disposed) return;
        List<String> items = new ArrayList<>();
        String title = engine.AddText(
            "Settings",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            -20,
            10,
            1f,
            0,
            Color.BLACK
        );
        items.add(title);

        String back = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            10,
            10,
            10,
            200,
            50,
            "Back",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                ChangeScreen("main");
                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Back pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Back hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Back leaved: " + btn.uuid, "About");
            }
        );
        items.add(back);

        String vsync = engine.AddButton(
            GetStatus(Settings.VSync),
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            60+60,
            10,
            300,
            50,
            "VSync",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                Settings.VSync = !Settings.VSync;
                Settings.SaveSettings();
                ChangeButtonImage(btn.uuid, GetStatus(Settings.VSync));
                Debug("Vsync pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetHoverStatus(Settings.VSync));
                Debug("Vsync hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetStatus(Settings.VSync));
                Debug("Vsync leaved: " + btn.uuid, "About");
            }
        );
        items.add(vsync);

        String clouds = engine.AddButton(
            GetStatus(Settings.Clouds),
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            60,
            10,
            300,
            50,
            "Clouds",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                Settings.Clouds = !Settings.Clouds;

                if (Settings.Clouds)
                {
                    for (int i = 0; i < 25; i++)
                    {
                        SpawnCloud();
                    }
                }

                Settings.SaveSettings();
                ChangeButtonImage(btn.uuid, GetStatus(Settings.Clouds));
                Debug("Clouds pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetHoverStatus(Settings.Clouds));
                Debug("Clouds hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetStatus(Settings.Clouds));
                Debug("Clouds leaved: " + btn.uuid, "About");
            }
        );
        items.add(clouds);

        String music = engine.AddButton(
            GetStatus(Settings.Music),
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            0,
            10,
            300,
            50,
            "Music",
            (Engine.Button btn) -> {
                if (Disposed) return;
                PressButton();
                Settings.Music = !Settings.Music;
                Settings.SaveSettings();

                if (Settings.Music) {
                    if (backgroundMusic == null) {
                        backgroundMusic = Main.audioEngine.loadMusic("music/stardust_dreams.mp3");
                    }
                    Main.audioEngine.playMusic(backgroundMusic, true);
                }
                else {
                    Main.audioEngine.stopMusic(backgroundMusic);
                }

                ChangeButtonImage(btn.uuid, GetStatus(Settings.Music));
                Debug("Music pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetHoverStatus(Settings.Music));
                Debug("Music hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetStatus(Settings.Music));
                Debug("Music leaved: " + btn.uuid, "About");
            }
        );
        items.add(music);

        String sounds = engine.AddButton(
            GetStatus(Settings.Sounds),
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            -60,
            10,
            300,
            50,
            "Sounds",
            (Engine.Button btn) -> {
                if (Disposed) return;
                Settings.Sounds = !Settings.Sounds;
                Settings.SaveSettings();
                PressButton();
                ChangeButtonImage(btn.uuid, GetStatus(Settings.Sounds));
                Debug("Sounds pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetHoverStatus(Settings.Sounds));
                Debug("Sounds hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetStatus(Settings.Sounds));
                Debug("Sounds leaved: " + btn.uuid, "About");
            }
        );
        items.add(sounds);
        /*
        String screenButtons = engine.AddButton(
            GetStatus(Settings.ScreenButtons),
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            -120,
            10,
            300,
            50,
            "Buttons",
            (Engine.Button btn) -> {
                if (Disposed) return;
                Settings.ScreenButtons = !Settings.ScreenButtons;
                Settings.SaveSettings();
                PressButton();
                ChangeButtonImage(btn.uuid, GetStatus(Settings.ScreenButtons));
                Debug("ScreenButtons pressed: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetHoverStatus(Settings.ScreenButtons));
                Debug("ScreenButtons hovered: " + btn.uuid, "About");
            },
            (Engine.Button btn) -> {
                if (Disposed) return;
                ChangeButtonImage(btn.uuid, GetStatus(Settings.ScreenButtons));
                Debug("ScreenButtons leaved: " + btn.uuid, "About");
            }
        );
        items.add(screenButtons);
        */

        engine.SetItemAlignment(title, Engine.Alignment.TOP_CENTER);
        engine.SetItemAlignment(back, Engine.Alignment.BOTTOM_LEFT);
        engine.SetItemAlignment(vsync, Engine.Alignment.CENTER);
        engine.SetItemAlignment(clouds, Engine.Alignment.CENTER);
        engine.SetItemAlignment(music, Engine.Alignment.CENTER);
        engine.SetItemAlignment(sounds, Engine.Alignment.CENTER);
        //engine.SetItemAlignment(screenButtons, Engine.Alignment.CENTER);

        engine.SetItemAlpha(title, 0);
        engine.SetItemAlpha(vsync, 0);
        engine.SetItemAlpha(back, 0);
        engine.SetItemAlpha(clouds, 0);
        engine.SetItemAlpha(music, 0);
        engine.SetItemAlpha(sounds, 0);
        //engine.SetItemAlpha(screenButtons, 0);

        scenes.put("settings", items);

        Debug("Initialized", "Settings");
    }

    public String GetStatus(boolean param)
    {
        if (param)
        {
            return "checkmarks/sel.png";
        }
        return "checkmarks/unsel.png";
    }

    public String GetHoverStatus(boolean param)
    {
        if (param)
        {
            return "checkmarks/selhover.png";
        }
        return "checkmarks/unselhover.png";
    }

    public void ChangeScreen(String screen)
    {
        if (changingScreen || Disposed) return;
        changingScreen = true;

        final boolean[] runned = {false};
        List<String> disappearItems = scenes.get(currentScreen);

        for (String item : disappearItems)
        {
            engine.new Animator().Alpha(item, 0f, 0.5f, () -> {
                if (runned[0]) return;
                runned[0] = true;

                List<String> appearItems = scenes.get(screen);
                for (String item2 : appearItems)
                {
                    engine.new Animator().Alpha(item2, 1f, 0.5f, () -> {changingScreen = false; currentScreen = screen;});
                }
            });
        }
    }

    public void ChangeButtonImage(String UUID, String Path)
    {
        if (Disposed) return;
        engine.ChangeImage(((Engine.Button)engine.GetItem(UUID)).imageItem.uuid, Path);
    }

    public void RocketShake(String UUID)
    {
        if (Disposed) return;

        final float shake = 2;

        float x = (float)(-50 + (Math.random() * shake * 2 - shake));
        float y = (float)((Math.random() * shake * 2 - shake));

        engine.new Animator().Move(UUID, x, y, 0.1f * (float) Math.random(), () -> {
            RocketShake(UUID);
        });
    }

    public void RocketRotate(String UUID)
    {
        if (Disposed) return;

        engine.new Animator().Rotate(UUID, ((float) Math.random() * 1 - 0.5f), (float) Math.random() * 0.2f, () -> {
            RocketRotate(UUID);
        });
    }

    public void SpawnCloud()
    {
        if (!Settings.Clouds || Disposed) return;

        float width = Gdx.app.getGraphics().getWidth() / 2;
        float x = (float)(Math.random() * width * 2 - width);

        float osize = (float)(1000 + Math.random() * 1000);

        String cloud = engine.AddImage(
            "clouds/" + (int)(Math.random() * 3) + ".png",
            engine.GetCurrentScreen(),
            x,
            osize,
            -(int)(Math.random() * 3),
            osize,
            osize,
            0,
            (float)(0.5f + Math.random() * 0.5f)
            );

        clouds.add(cloud);

        engine.SetItemAlignment(cloud, Engine.Alignment.TOP_CENTER);
        engine.new Animator().Move(cloud, x, -Gdx.app.getGraphics().getHeight()-osize, (float)(0.45f + Math.random() * 0.45f), () -> {
            RespawnCloud(cloud);
        });
    }

    public void RespawnCloud(String UUID)
    {
        if (!Settings.Clouds || Disposed) return;

        float width = Gdx.app.getGraphics().getWidth() / 2;
        float x = (float)(Math.random() * width * 2 - width);

        float osize = (float)(1000 + Math.random() * 1000);
        Engine.ImageItem cloud = (Engine.ImageItem)engine.GetItem(UUID);

        cloud.x = x;
        cloud.y = osize;
        cloud.width = osize;
        cloud.height = osize;
        cloud.z = -(int)(Math.random() * 3);
        cloud.alpha = (float)(0.5f + Math.random() * 0.5f);

        engine.new Animator().Move(UUID, x, -Gdx.app.getGraphics().getHeight()-osize, (float)(0.45f + Math.random() * 0.45f), () -> {
            RespawnCloud(UUID);
        });
    }

    public void Dispose()
    {
        Disposed = true;
    }

    private void PressButton() {
        if (Settings.Sounds) {
            String sound = Main.audioEngine.loadSound("sounds/button.mp3");
            Main.audioEngine.playSound(sound, 1f);
        }
    }
}
