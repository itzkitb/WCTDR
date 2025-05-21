package lol.sillyapps.WCTDR.scenes;

import static lol.sillyapps.WCTDR.Engine.Debug;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import lol.sillyapps.WCTDR.Engine;
import lol.sillyapps.WCTDR.Main;
import lol.sillyapps.WCTDR.Settings;
import lol.sillyapps.WCTDR.Statistics;

public class Game {
    private Engine engine;
    private String bgMusic;
    private String rocket;
    private Vector2 rocketPos = new Vector2(0, 0);
    private Vector2 rocketVelocity = new Vector2(0, 0);
    private float fuel = 100f;
    private float protection = 100f;
    private float distance = 0f;
    private float timeElapsed = 0f;
    private boolean gameOver = false;
    private boolean disposed = false;
    private boolean isGameRunning;

    private String hitSnd = Main.audioEngine.loadSound("sounds/hit.mp3");
    private String fuelSnd = Main.audioEngine.loadSound("sounds/fuel_pickup.mp3");

    private List<String> asteroids = new ArrayList<>();
    private List<String> fuels = new ArrayList<>();
    private List<String> engineParticles = new ArrayList<>();
    private Timer spawnTimer = new Timer();

    // UI Elements
    private String distanceText;
    private String fuelText;
    private String fuelIcon;
    private String protectText;
    private String protectIcon;
    private String leftButton;
    private String rightButton;

    // Engine controls
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    public void Show(Engine engine) {
        this.engine = engine;
        SetupScene();
        InitializeRocket();
        InitializeUI();
        StartGameLoop();

        if (Settings.Music) {
            bgMusic = Main.audioEngine.loadMusic("music/cosmic_rhythms.mp3");
            Main.audioEngine.playMusic(bgMusic, true);
        }

        String fadein = engine.AddImage(
            "black.png",
            engine.GetCurrentScreen(),
            0,
            0,
            10000,
            100000,
            100000,
            0,
            1
        );
        engine.SetItemAlignment(fadein, Engine.Alignment.CENTER);
        engine.new Animator().Alpha(fadein, 1f, 0.25f, () -> {
            engine.new Animator().Alpha(fadein, 0f, 1f, () -> {});
        });
    }

    private void SetupScene() {
        engine.ChangeScreen(engine.AddScreen());
        engine.SetSceneBackground(new Color(0.1f, 0.1f, 0.2f, 1f));

        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!gameOver && !disposed) {
                    Gdx.app.postRunnable(() -> SpawnStar());
                }
            }
        }, 0, 200);
    }

    private void SpawnStar() {
        float x = (float) (Math.random() * Gdx.app.getGraphics().getWidth() - Gdx.app.getGraphics().getWidth()/2);
        float y = rocketPos.y + Gdx.app.getGraphics().getHeight();
        float size = (float)(5 + Math.random() * 30);

        String star = engine.AddImage(
            "star.png",
            engine.GetCurrentScreen(),
            x,
            y,
            0,
            size,
            size,
            (float) Math.random() * 360,
            0.5f
        );
        engine.SetItemAlignment(star, Engine.Alignment.CENTER);

        engine.new Animator().Move(star, x, -Gdx.app.getGraphics().getHeight() - 5 + rocketPos.y, 35f/size*10f, () -> {
            engine.DeleteItem(star);
        });
    }

    private void InitializeRocket() {
        rocket = engine.AddImage(
            "rocket/in_game_all.png",
            engine.GetCurrentScreen(),
            0,
            0,
            100,
            82,
            117,
            0,
            1f
        );
        rocketPos.set(0, 0);
        engine.SetItemAlignment(rocket, Engine.Alignment.CENTER);
    }

    private void InitializeUI() {
        // Distance display
        distanceText = engine.AddText(
            "Distance: 0m",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            -10,
            10,
            1f,
            0,
            Color.WHITE
        );
        engine.SetItemAlignment(distanceText, Engine.Alignment.TOP_CENTER);

        // Fuel display
        fuelIcon = engine.AddImage(
            "fuel_icon.png",
            engine.GetCurrentScreen(),
            -10,
            -10,
            10,
            30,
            30,
            0,
            1f
        );
        fuelText = engine.AddText(
            "100%",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -50,
            -10,
            10,
            1f,
            0,
            Color.WHITE
        );
        protectIcon = engine.AddImage(
            "shield.png",
            engine.GetCurrentScreen(),
            -10,
            -50,
            10,
            30,
            30,
            0,
            1f
        );
        protectText = engine.AddText(
            "100%",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            -50,
            -50,
            10,
            1f,
            0,
            Color.WHITE
        );
        engine.SetItemAlignment(fuelIcon, Engine.Alignment.TOP_RIGHT);
        engine.SetItemAlignment(fuelText, Engine.Alignment.TOP_RIGHT);
        engine.SetItemAlignment(protectIcon, Engine.Alignment.TOP_RIGHT);
        engine.SetItemAlignment(protectText, Engine.Alignment.TOP_RIGHT);

        // On-screen buttons
        if(Settings.ScreenButtons) {
            leftButton = engine.AddButton(
                "button/left_normal.png",
                "fonts/tiny5.fnt",
                engine.GetCurrentScreen(),
                10,
                10,
                10,
                120,
                120,
                "",
                (Engine.Button btn) -> {
                    leftPressed = true;
                },
                (Engine.Button btn) -> {
                    engine.ChangeImage(btn.imageItem.uuid, "button/left_pressed.png");
                },
                (Engine.Button btn) -> {
                    leftPressed = false;
                    engine.ChangeImage(btn.imageItem.uuid, "button/left_normal.png");
                }
            );
            engine.SetItemAlignment(leftButton, Engine.Alignment.BOTTOM_LEFT);

            rightButton = engine.AddButton(
                "button/right_normal.png",
                "fonts/tiny5.fnt",
                engine.GetCurrentScreen(),
                -10,
                10,
                10,
                120,
                120,
                "",
                (Engine.Button btn) -> {
                    rightPressed = true;
                },
                (Engine.Button btn) -> {
                    engine.ChangeImage(btn.imageItem.uuid, "button/right_pressed.png");
                },
                (Engine.Button btn) -> {
                    rightPressed = false;
                    engine.ChangeImage(btn.imageItem.uuid, "button/right_normal.png");
                }
            );
            engine.SetItemAlignment(rightButton, Engine.Alignment.BOTTOM_RIGHT);
        }
    }

    private void StartGameLoop() {
        isGameRunning = true;

        // Spawn asteroids timer
        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!gameOver && !disposed && isGameRunning) {
                    Gdx.app.postRunnable(() -> SpawnAsteroid());
                }
            }
        }, 0, 1000);

        // Spawn fuel timer
        spawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!gameOver && !disposed && isGameRunning && fuel > 0) {
                    Gdx.app.postRunnable(() -> SpawnFuel());
                }
            }
        }, 5000, 8000);

        // Physics update loop
        Gdx.input.setInputProcessor(new InputHandler());

        engine.SetUpdateCallback(delta -> {
            if(!gameOver && !disposed) {
                UpdateGame(delta);
            }
        });
    }

    private void UpdateGame(float delta) {
        if(gameOver || disposed) return;
        rocketVelocity.lerp(new Vector2(0, 0), 0.05f);

        if (!gameOver && !disposed) {
            if (leftPressed) {
                rocketVelocity.x -= 15f * delta;
                rocketVelocity.y += 2.5f * Statistics.Speed * delta;
                fuel -= delta * (1f / Statistics.FuelReserve) * 5f;
            }

            if (rightPressed) {
                rocketVelocity.x += 15f * delta;
                rocketVelocity.y += 2.5f * Statistics.Speed * delta;
                fuel -= delta * (1f / Statistics.FuelReserve) * 5f;
            }

            if (leftPressed && !rightPressed)
            {
                engine.ChangeImage(rocket, "rocket/in_game_left.png");
            }
            else if (!leftPressed && rightPressed)
            {
                engine.ChangeImage(rocket, "rocket/in_game_right.png");
            }
            else
            {
                engine.ChangeImage(rocket, "rocket/in_game_all.png");
            }

            rocketPos.x += rocketVelocity.x;
            rocketPos.y += rocketVelocity.y;

            /*
            float angle;
            float epsilon = 0.01f;

            if (Math.sqrt(rocketVelocity.x * rocketVelocity.x + rocketVelocity.y * rocketVelocity.y) < epsilon) {
                angle = 0f;
            } else {
                float adjustedY = -rocketVelocity.y;

                angle = (float) Math.atan2(adjustedY, rocketVelocity.x) * MathUtils.radiansToDegrees;

                //angle -= 90f;
                if (rocketVelocity.x < 0) {
                    angle += 180f;
                }
            }

            engine.SetItemRotation(rocket, angle);
            */

            engine.SetItemPosition(rocket, rocketPos.x, rocketPos.y);

            engine.SetItemPosition(distanceText, 0, rocketPos.y - 10 + 100);
            engine.SetItemPosition(leftButton, 10, rocketPos.y + 10 + 100);
            engine.SetItemPosition(rightButton, -10, rocketPos.y + 10 + 100);
            engine.SetItemPosition(rightButton, -10, rocketPos.y + 10 + 100);
            engine.SetItemPosition(fuelText, -50, rocketPos.y - 10 + 100);
            engine.SetItemPosition(fuelIcon, -10, rocketPos.y - 10 + 100);
            engine.SetItemPosition(protectIcon, -10, rocketPos.y - 50 + 100);
            engine.SetItemPosition(protectText, -50, rocketPos.y - 50 + 100);

            float targetY = rocketPos.y + 100;
            engine.SetSceneOffset(0, targetY);
        }

        distance += rocketVelocity.y * delta;
        timeElapsed += delta;
        ((Engine.TextItem)engine.GetItem(distanceText)).text = "Distance: " + (int)distance + "m";

        if(fuel < 0) fuel = 0;
        ((Engine.TextItem)engine.GetItem(fuelText)).text = (int)(fuel) + "%";
        ((Engine.TextItem)engine.GetItem(protectText)).text = (int)(protection) + "%";

        if(fuel <= 0) {
            GameOver();
        }

        UpdateThrusters();
        CleanupOffscreenItems();
        CheckCollisions();

        float halfScreenWidth = Gdx.graphics.getWidth() / 2f;
        rocketPos.x = MathUtils.clamp(rocketPos.x,
            -halfScreenWidth + 20,
            halfScreenWidth - 20);
    }

    private void UpdateThrusters() {
        if(gameOver || disposed) return;
        for(int i = 0; i < engineParticles.size(); i++) {
            String particle = engineParticles.get(i);
            boolean active = (i == 0 ? leftPressed : rightPressed);
            Engine.ImageItem item = (Engine.ImageItem)engine.GetItem(particle);
            item.alpha = active ? 1f : 0.3f;
            item.rotation = active ? (i == 0 ? -15 : 15) : 0;
        }
    }

    private void CheckCollisions() {
        if(gameOver || disposed) return;
        Rectangle rocketBounds = new Rectangle(
            rocketPos.x - 41,
            rocketPos.y - 58.5f,
            82,
            117
        );

        for(String asteroid : asteroids) {
            Engine.ImageItem ast = (Engine.ImageItem)engine.GetItem(asteroid);
            Rectangle astBounds = new Rectangle(
                ast.x - ast.width/2,
                ast.y - ast.height/2,
                ast.width,
                ast.height
            );

            if(rocketBounds.overlaps(astBounds)) {
                if(protection > 0) {
                    int damage = Math.max(1, 5 - (Statistics.Protection / 10));
                    protection -= damage;
                    engine.DeleteItem(asteroid);
                    asteroids.remove(asteroid);
                    if (Settings.Sounds) {
                        Main.audioEngine.playSound(hitSnd, 1f);
                    }
                } else {
                    GameOver();
                    return;
                }
                break;
            }
        }

        Iterator<String> iterator = fuels.iterator();
        while (iterator.hasNext()) {
            String fuelItem = iterator.next();
            Engine.ImageItem f = (Engine.ImageItem) engine.GetItem(fuelItem);
            Rectangle fBounds = new Rectangle(
                f.x - f.width/2,
                f.y - f.height/2,
                f.width,
                f.height
            );

            if (rocketBounds.overlaps(fBounds)) {
                fuel = Math.min(fuel + 30, 100);
                engine.DeleteItem(fuelItem);
                iterator.remove();
                if (Settings.Sounds) {
                    Main.audioEngine.playSound(fuelSnd, 1f);
                }
            }
        }
    }

    private void GameOver() {
        if(gameOver || disposed) return;
        gameOver = true;

        if(Settings.Music) {
            Main.audioEngine.stopMusic(bgMusic);
            String sound = Main.audioEngine.loadSound("sounds/explosion.mp3");
            Main.audioEngine.playSound(sound, 1f);
        }

        Statistics.Credits += (int)(distance / 100 / timeElapsed * 500);
        if(distance > Statistics.BestRecord) {
            Statistics.BestRecord = (int)distance;
        }
        Statistics.Save();

        ShowGameOverUI();
    }

    private void ShowGameOverUI() {
        engine.SetSceneOffset(0, 0);
        engine.ClearScene(engine.GetCurrentScreen());
        engine.SetSceneBackground(Color.BLACK);
        Gdx.input.setInputProcessor(engine.inputMultiplexer);

        String gameOverText = engine.AddText(
            "ИГРА ОКОНЧЕНА",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            -20,
            10,
            2f,
            0,
            Color.RED
        );
        engine.SetItemAlignment(gameOverText, Engine.Alignment.TOP_CENTER);

        String stats = engine.AddText(
            "Пройдено: " + (int)distance + "м\n" +
                "Время: " + (int)timeElapsed + "с\n" +
                "Кредиты: " + (int)(distance / 100 * timeElapsed * 500) + "\n" +
                "Рекорд: " + Statistics.BestRecord + "м",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            0,
            10,
            1f,
            0,
            Color.WHITE
        );
        engine.SetItemAlignment(stats, Engine.Alignment.CENTER);

        String menuButton = engine.AddButton(
            "button/normal.png",
            "fonts/tiny5.fnt",
            engine.GetCurrentScreen(),
            0,
            20,
            10,
            200,
            50,
            "Main menu",
            (Engine.Button btn) -> {
                ChangeButtonImage(btn.uuid, "button/pressed.png");
                if(Settings.Sounds) {
                    String sound = Main.audioEngine.loadSound("sounds/button.mp3");
                    Main.audioEngine.playSound(sound, 1f);
                }

                String fadeout = engine.AddImage(
                    "black.png",
                    engine.GetCurrentScreen(),
                    0,
                    0,
                    10000,
                    10000,
                    10000,
                    0,
                    0
                );
                engine.SetItemAlignment(fadeout, Engine.Alignment.CENTER);
                engine.new Animator().Alpha(fadeout, 1f, 1f, () -> {
                        Dispose();
                        new MainMenu().Show(engine);
                });
            },
            (Engine.Button btn) -> {
                ChangeButtonImage(btn.uuid, "button/hovered.png");
            },
            (Engine.Button btn) -> {
                ChangeButtonImage(btn.uuid, "button/normal.png");
            }
        );
        engine.SetItemAlignment(menuButton, Engine.Alignment.BOTTOM_CENTER);
    }

    private void ChangeButtonImage(String UUID, String Path) {
        engine.ChangeImage(((Engine.Button)engine.GetItem(UUID)).imageItem.uuid, Path);
    }

    private void SpawnAsteroid() {
        float x = (float)(Math.random() * Gdx.app.getGraphics().getWidth() - Gdx.app.getGraphics().getWidth()/2);
        float y = rocketPos.y + Gdx.app.getGraphics().getHeight();
        float size = (float)(30 + Math.random() * 200);

        String asteroid = engine.AddImage(
            "asteroid.png",
            engine.GetCurrentScreen(),
            x,
            y,
            5,
            size,
            size,
            (float) Math.random() * 360,
            1f
        );
        engine.SetItemAlignment(asteroid, Engine.Alignment.CENTER);
        asteroids.add(asteroid);

        float speed = 230f/size*10f;
        engine.new Animator().Move(asteroid, x, -Gdx.app.getGraphics().getHeight() - size + rocketPos.y, speed, () -> {
            engine.DeleteItem(asteroid);
            asteroids.remove(asteroid);
        });
    }

    private void SpawnFuel() {
        float x = (float)(Math.random() * Gdx.app.getGraphics().getWidth() - Gdx.app.getGraphics().getWidth()/2);
        float y = rocketPos.y + Gdx.app.getGraphics().getHeight();

        String fuelItem = engine.AddImage(
            "fuel_canister.png",
            engine.GetCurrentScreen(),
            x,
            y,
            5,
            30,
            30,
            (float) Math.random() * 360,
            1f
        );
        engine.SetItemAlignment(fuelItem, Engine.Alignment.CENTER);
        fuels.add(fuelItem);

        float speed = (float)(5 + Math.random() * 10);
        engine.new Animator().Move(fuelItem, x, -Gdx.app.getGraphics().getHeight() - 30 + rocketPos.y, speed, () -> {
            engine.DeleteItem(fuelItem);
            fuels.remove(fuelItem);
        });
    }

    public void Dispose() {
        disposed = true;
        gameOver = true;
        isGameRunning = false;
        spawnTimer.cancel();
        engine.ClearUpdateCallback();
        engine.ClearScene(engine.GetCurrentScreen());
    }

    private void CleanupOffscreenItems() {
        float offscreenY = rocketPos.y - Gdx.app.getGraphics().getHeight() * 1.5f;

        List<String> toRemoveAsteroids = new ArrayList<>();
        for(String asteroid : asteroids) {
            Engine.ImageItem item = (Engine.ImageItem)engine.GetItem(asteroid);
            if (item != null && item.y < offscreenY) toRemoveAsteroids.add(asteroid);
        }
        for(String uuid : toRemoveAsteroids) {
            engine.DeleteItem(uuid);
            asteroids.remove(uuid);
        }

        List<String> toRemoveFuels = new ArrayList<>();
        for(String fuel : fuels) {
            Engine.ImageItem item = (Engine.ImageItem)engine.GetItem(fuel);
            if(item != null && item.y < offscreenY) toRemoveFuels.add(fuel);
        }
        for(String uuid : toRemoveFuels) {
            engine.DeleteItem(uuid);
            fuels.remove(uuid);
        }
    }

    private class InputHandler implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            Debug(String.valueOf(keycode), "KEY_DOWN");

            if(keycode == 21) { // Left arrow
                leftPressed = true;
                return true;
            }
            if(keycode == 22) { // Right arrow
                rightPressed = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            Debug(String.valueOf(keycode), "KEY_UP");

            if(keycode == 21) { // Left arrow
                leftPressed = false;
                return true;
            }
            if(keycode == 22) { // Right arrow
                rightPressed = false;
                return true;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }
    }
}
