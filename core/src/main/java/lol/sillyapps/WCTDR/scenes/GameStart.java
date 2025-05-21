package lol.sillyapps.WCTDR.scenes;

import static lol.sillyapps.WCTDR.Engine.Debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;
import lol.sillyapps.WCTDR.Settings;
import lol.sillyapps.WCTDR.Statistics;

public class GameStart {
    private Engine engine;
    private String bgMusic;
    private String rocket;
    private boolean Disposed = false;
    private String statistics;
    private String balance;
    private String record;
    private String balanceImg;
    private String recordImg;
    private List<String> shaking = new ArrayList<>();

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
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            10,
            10,
            10,
            200,
            50,
            "Back",
            (Engine.Button btn) -> {
                PressButton();

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

                engine.SetItemAlignment(fadeout, Engine.Alignment.CENTER);

                if (Settings.Music) {
                    Main.audioEngine.fadeOut(bgMusic, 1f, () -> {});
                }
                engine.new Animator().Alpha(fadeout, 1f, 1f, ()->{
                    Disposed = true;
                    new MainMenu().Show(engine);
                });

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Back pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Back hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Back leaved: " + btn.uuid, "Game");
            }
        );

        String buySpeed = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -10,
            10+50+10+50,
            10,
            200,
            50,
            "Speed " + CalculatePrice(Statistics.Speed, 212) + "c.",
            (Engine.Button btn) -> {

                if (Statistics.Credits >= CalculatePrice(Statistics.Speed, 212))
                {
                    PressButton();
                    Statistics.Credits -= CalculatePrice(Statistics.Speed, 212);
                    Statistics.Speed += 1;
                    Statistics.Save();
                    UpdateStatistics();
                    UpdateBalance();
                    btn.textItem.text = "Speed " + CalculatePrice(Statistics.Speed, 212) + "c.";
                }
                else
                {
                    Shake(btn.uuid);
                }

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Speed pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Speed hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Speed leaved: " + btn.uuid, "Game");
            }
        );

        String buyProtection = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -10,
            10+50,
            10,
            200,
            50,
            "Protection " + CalculatePrice(Statistics.Protection, 334) + "c.",
            (Engine.Button btn) -> {

                if (Statistics.Credits >= CalculatePrice(Statistics.Protection, 334))
                {
                    PressButton();
                    Statistics.Credits -= CalculatePrice(Statistics.Protection, 334);
                    Statistics.Protection += 1;
                    Statistics.Save();
                    UpdateStatistics();
                    UpdateBalance();
                    btn.textItem.text = "Protection " + CalculatePrice(Statistics.Protection, 334) + "c.";
                }
                else
                {
                    Shake(btn.uuid);
                }

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Protection pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Protection hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Protection leaved: " + btn.uuid, "Game");
            }
        );

        String buyBraking = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -10,
            0,
            10,
            200,
            50,
            "Braking " + CalculatePrice(Statistics.Braking, 256) + "c.",
            (Engine.Button btn) -> {

                if (Statistics.Credits >= CalculatePrice(Statistics.Braking, 256))
                {
                    PressButton();
                    Statistics.Credits -= CalculatePrice(Statistics.Braking, 256);
                    Statistics.Braking += 1;
                    Statistics.Save();
                    UpdateStatistics();
                    UpdateBalance();
                    btn.textItem.text = "Braking " + CalculatePrice(Statistics.Braking, 256) + "c.";
                }
                else
                {
                    Shake(btn.uuid);
                }

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Braking pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Braking hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Braking leaved: " + btn.uuid, "Game");
            }
        );

        String buyFuelReserve = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -10,
            -10-50,
            10,
            200,
            50,
            "Fuel reserve " + CalculatePrice(Statistics.FuelReserve, 572) + "c.",
            (Engine.Button btn) -> {

                if (Statistics.Credits >= CalculatePrice(Statistics.FuelReserve, 572))
                {
                    PressButton();
                    Statistics.Credits -= CalculatePrice(Statistics.FuelReserve, 572);
                    Statistics.FuelReserve += 1;
                    Statistics.Save();
                    UpdateStatistics();
                    UpdateBalance();
                    btn.textItem.text = "Fuel reserve " + CalculatePrice(Statistics.FuelReserve, 572) + "c.";
                }
                else
                {
                    Shake(btn.uuid);
                }

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Fuel reserve pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Fuel reserve hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Fuel reserve leaved: " + btn.uuid, "Game");
            }
        );

        String start = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -10,
            10,
            10,
            200,
            50,
            "Start",
            (Engine.Button btn) -> {
                PressButton();

                if (Settings.Sounds) {
                    String sound = Main.audioEngine.loadSound("sounds/start.mp3");
                    Main.audioEngine.playSound(sound, 0.75f);
                    Main.audioEngine.fadeOut(sound, 5f, ()->{});
                }

                if (Settings.Music)
                {
                    Main.audioEngine.fadeOut(bgMusic, 1f, ()->{});
                }

                engine.new Animator().Alpha(back, 0f, 1f, ()->{});
                engine.new Animator().Alpha(btn.uuid, 0f, 1f, ()->{});
                engine.new Animator().Alpha(statistics, 0f, 1f, ()->{});
                engine.new Animator().Alpha(balance, 0f, 1f, ()->{});
                engine.new Animator().Alpha(record, 0f, 1f, ()->{});
                engine.new Animator().Alpha(buySpeed, 0f, 1f, ()->{});
                engine.new Animator().Alpha(buyFuelReserve, 0f, 1f, ()->{});
                engine.new Animator().Alpha(buyBraking, 0f, 1f, ()->{});
                engine.new Animator().Alpha(buyProtection, 0f, 1f, ()->{});
                engine.new Animator().Alpha(balanceImg, 0f, 1f, ()->{});
                engine.new Animator().Alpha(recordImg, 0f, 1f, ()->{});

                for (int i = 0; i < 25; i++) {
                    SpawnCloud(-48, 50);
                }

                List<String> fly_frames = Arrays.asList("rocket/fly_0_0.png", "rocket/fly_1_1.png", "rocket/fly_1_0.png", "rocket/fly_0_1.png");
                engine.DeleteItem(rocket);
                rocket = engine.AddAnimatedImage(
                    fly_frames,
                    engine.GetCurrentScreen(),
                    -48,
                    50,
                    0,
                    165,
                    350,
                    0,
                    1,
                    1
                );

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

                engine.SetItemAlignment(fadeout, Engine.Alignment.CENTER);
                engine.SetItemAlignment(rocket, Engine.Alignment.BOTTOM_CENTER);

                engine.new Animator().Move(rocket, -48, Gdx.app.getGraphics().getHeight()*1.5f, 10f, ()->{
                    engine.new Animator().Alpha(fadeout, 1f, 1f, ()->{
                        Disposed = true;
                        new Game().Show(engine);
                    });
                });

                ChangeButtonImage(btn.uuid, "button/pressed.png");
                Debug("Start pressed: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/hovered.png");
                Debug("Start hovered: " + btn.uuid, "Game");
            },
            (Engine.Button btn) -> {

                ChangeButtonImage(btn.uuid, "button/normal.png");
                Debug("Start leaved: " + btn.uuid, "Game");
            }
        );

        UpdateStatistics();
        UpdateBalance();
        UpdateRecord();

        engine.SetItemAlignment(fadein, Engine.Alignment.CENTER);
        engine.SetItemAlignment(base, Engine.Alignment.BOTTOM_CENTER);
        engine.SetItemAlignment(rocket, Engine.Alignment.BOTTOM_CENTER);
        engine.SetItemAlignment(back, Engine.Alignment.BOTTOM_LEFT);
        engine.SetItemAlignment(start, Engine.Alignment.BOTTOM_RIGHT);
        engine.SetItemAlignment(buySpeed, Engine.Alignment.MIDDLE_RIGHT);
        engine.SetItemAlignment(buyProtection, Engine.Alignment.MIDDLE_RIGHT);
        engine.SetItemAlignment(buyBraking, Engine.Alignment.MIDDLE_RIGHT);
        engine.SetItemAlignment(buyFuelReserve, Engine.Alignment.MIDDLE_RIGHT);

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

    public void SpawnCloud(float x, float y)
    {
        if (!Settings.Clouds || Disposed) return;

        float osize = (float)(500 + Math.random() * 500);

        String cloud = engine.AddImage(
            "clouds/" + (int)(Math.random() * 3) + ".png",
            engine.GetCurrentScreen(),
            x,
            y-400,
            5,
            osize,
            osize,
            0,
            1
        );

        float move = 300f;
        float gotoX = x + (float)Math.random() * move * 2f - move;
        float gotoY = y + (float)Math.random() * move * 2f - move;
        Debug("gx: " + gotoX + ", gy: " + gotoY, "SpawnCloud");
        engine.SetItemAlignment(cloud, Engine.Alignment.BOTTOM_CENTER);

        engine.new Animator().Alpha(cloud, 0f, 5f, () -> {});
        engine.new Animator().Move(cloud, gotoX, gotoY, 5f, () -> {
            engine.DeleteItem(cloud);
        });
    }

    public void RespawnCloud(String UUID)
    {
        if (!Settings.Clouds || Disposed) return;

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

        if (Settings.Sounds) {
            String sound = Main.audioEngine.loadSound("sounds/button.mp3");
            Main.audioEngine.playSound(sound, 1f);
        }
    }

    public void ChangeButtonImage(String UUID, String Path)
    {

        engine.ChangeImage(((Engine.Button)engine.GetItem(UUID)).imageItem.uuid, Path);
    }

    public void UpdateStatistics()
    {
        if (statistics == null)
        {
            statistics = engine.AddText(
                "",
                "fonts/tiny5.fnt",
                engine.GetCurrentScreen(),
                10,
                -20,
                10,
                1f,
                0,
                Color.BLACK
            );

            engine.SetItemAlignment(statistics, Engine.Alignment.TOP_LEFT);
        }

        ((Engine.TextItem)engine.GetItem(statistics)).text =
            "Statistics:"
                + "\nSpeed: " + Statistics.Speed
                + "\nProtection: " + Statistics.Protection
                + "\nBraking: " + Statistics.Braking
                + "\nFuel reserve: " + Statistics.FuelReserve;
    }

    private void UpdateBalance()
    {
        if (balance == null)
        {
            balance = engine.AddText(
                "",
                "fonts/tiny5.fnt",
                engine.GetCurrentScreen(),
                -45,
                -20,
                10,
                1f,
                0,
                Color.BLACK
            );

            balanceImg = engine.AddImage(
                "credits.png",
                engine.GetCurrentScreen(),
                -10,
                -15,
                10,
                25,
                25,
                0,
                1
                );

            engine.SetItemAlignment(balance, Engine.Alignment.TOP_RIGHT);
            engine.SetItemAlignment(balanceImg, Engine.Alignment.TOP_RIGHT);
        }

        ((Engine.TextItem)engine.GetItem(balance)).text = String.valueOf(Statistics.Credits);
    }

    private void UpdateRecord()
    {
        if (record == null)
        {
            record = engine.AddText(
                "",
                "fonts/tiny5.fnt",
                engine.GetCurrentScreen(),
                -45,
                -20-30,
                10,
                1f,
                0,
                Color.BLACK
            );

            recordImg = engine.AddImage(
                "record.png",
                engine.GetCurrentScreen(),
                -10,
                -15-30,
                10,
                25,
                25,
                0,
                1
            );

            engine.SetItemAlignment(record, Engine.Alignment.TOP_RIGHT);
            engine.SetItemAlignment(recordImg, Engine.Alignment.TOP_RIGHT);
        }

        ((Engine.TextItem)engine.GetItem(record)).text = String.valueOf(Statistics.BestRecord);
    }

    private int CalculatePrice(int level, int mult)
    {
        return Math.round(level * level * mult / 34.69f);
    }

    private void Shake(String item)
    {
        if (Settings.Sounds) {
            String sound = Main.audioEngine.loadSound("sounds/wrong-button.mp3");
            Main.audioEngine.playSound(sound, 0.75f);
        }

        float itemX = engine.GetItem(item).x;
        float itemY = engine.GetItem(item).y;

        float shake = 2.5f;

        if (!shaking.contains(item)) {
            shaking.add(item);
            engine.new Animator().Move(item, (float) (itemX - shake + Math.random() * shake * 2), (float) (itemY - shake + Math.random() * shake * 2), 0.1f, () -> {
                engine.new Animator().Move(item, (float) (itemX - shake + Math.random() * shake * 2), (float) (itemY - shake + Math.random() * shake * 2), 0.1f, () -> {
                    engine.new Animator().Move(item, (float) (itemX - shake + Math.random() * shake * 2), (float) (itemY - shake + Math.random() * shake * 2), 0.1f, () -> {
                        engine.new Animator().Move(item, (float) (itemX - shake + Math.random() * shake * 2), (float) (itemY - shake + Math.random() * shake * 2), 0.1f, () -> {
                            engine.new Animator().Move(item, (float) (itemX - shake + Math.random() * shake * 2), (float) (itemY - shake + Math.random() * shake * 2), 0.1f, () -> {
                                engine.new Animator().Move(item, itemX, itemY, 0.1f, () -> {
                                    shaking.remove(item);
                                });
                            });
                        });
                    });
                });
            });
        }
    }
}
