package lol.sillyapps.WCTDR;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class Engine {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private String currentScreenUUID;
    private Map<String, Screen> screens;
    private Map<String, RenderableItem> items;
    private LRUCache<String, Texture> imageCache;
    private final int IMAGE_CACHE_CAPACITY = 50;
    private Set<String> allowedAssets;
    private List<Animation> animations = new ArrayList<>();
    public InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private List<Button> buttons = new ArrayList<>();
    private Array<Runnable> pendingTasks = new Array<>();
    private static double frames = 0;
    private Vector2 sceneOffset = new Vector2(0, 0);
    private UpdateCallback updateCallback;

    public static void Debug(String text, String sector) {
        System.out.printf("[%s] [%s] [%s] %s%n", sector, frames, new SimpleDateFormat("HH:mm.ss.SS").format(Calendar.getInstance().getTime()), text);
    }

    public interface UpdateCallback {
        void onUpdate(float delta);
    }

/*
    _______   _______   _______   ________   _______   _______
  //       \//   /   \//       \ /        \//   /   \//       \
 //        //        //      __/_/       ///        //        /
/        _/         /       / //         /         /        _/
\________/\__/_____/\________/ \\_______/\__/_____/\________/

>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                            ENGINE
<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
*/

    public Engine() {
        Debug("Creating an engine...", "ENGINE");
        screens = new HashMap<>();
        items = new HashMap<>();
        imageCache = new LRUCache<>(IMAGE_CACHE_CAPACITY);
        allowedAssets = new HashSet<>();

        // Initialize the SpriteBatch
        batch = new SpriteBatch();

        // Initialize the InputProcessor
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Initialize the camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
        Debug("The engine is created!", "ENGINE");
    }

    // Initializes the engine
    public void Initialize() {
        Debug("Initializing engine...", "ENGINE");
        // Clear loaded assets
        allowedAssets.clear();
        Debug("Reading assets...", "ENGINE");
        // Read the assets.txt
        FileHandle fileList = Gdx.files.internal("assets.txt");
        if (!fileList.exists()) {
            throw new RuntimeException("assets.txt not found in the assets folder.");
        }

        // Each line in assets.txt should represent a valid asset file
        String assetsContent = fileList.readString();
        String[] assetLines = assetsContent.split("\\r?\\n");
        for (String line : assetLines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.toLowerCase().endsWith(".png") || trimmed.toLowerCase().endsWith(".fnt")) {
                allowedAssets.add(trimmed);
            }
        }
        Debug("Preloading files...", "ENGINE");
        // Pre-load each found asset into the cache
        for (String assetPath : allowedAssets) {
            if (!assetPath.toLowerCase().endsWith(".fnt")) {
                Texture tex = new Texture(Gdx.files.internal(assetPath));
                imageCache.put(assetPath, tex);
            }
        }
        Debug("Initialized!", "ENGINE");
    }

    // Adds a new screen and returns its UUID
    public String AddScreen() {
        String uuid = UUID.randomUUID().toString();
        Screen s = new Screen(uuid);
        screens.put(uuid, s);
        if (currentScreenUUID == null) {
            currentScreenUUID = uuid;
        }
        Debug(String.format("Created new screen: %s", uuid), "ENGINE");
        return uuid;
    }

    // Returns currents screen UUID
    public String GetCurrentScreen() {
        return currentScreenUUID;
    }

    // Deletes a screen by UUID
    public void DeleteScreen(String uuid) {
        if (screens.containsKey(uuid)) {
            screens.get(uuid).bgColor = Color.BLACK;

            screens.remove(uuid);
            if (uuid.equals(currentScreenUUID)) {
                currentScreenUUID = screens.isEmpty() ? null : screens.keySet().iterator().next();
            }
            ClearScene(uuid);
        }

        Debug(String.format("Screen %s removed!", uuid), "ENGINE");
    }

    // Change the current screen to render
    public void ChangeScreen(String uuid) {
        if (screens.containsKey(uuid)) {
            currentScreenUUID = uuid;
        }
        Debug(String.format("%s is now the current scene", uuid), "ENGINE");
    }

    // Adds an image to a screen
    public String AddImage(String assetPath, String sceneUUID, float x, float y, float z,
                           float width, float height, float rotation, float alpha) {
        if (!allowedAssets.contains(assetPath)) {
            throw new IllegalArgumentException("Asset " + assetPath + " is not in the assets folder.");
        }
        if (!screens.containsKey(sceneUUID)) {
            throw new IllegalArgumentException("Screen with UUID " + sceneUUID + " does not exist.");
        }

        // Retrieve the texture from cache
        Texture tex = imageCache.get(assetPath);
        if (tex == null) {
            // Wtf?
            tex = new Texture(Gdx.files.internal(assetPath));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            imageCache.put(assetPath, tex);
        }
        ImageItem imageItem = new ImageItem(UUID.randomUUID().toString(), x, y, z, rotation, alpha,
            width, height, assetPath, tex);
        // Save
        items.put(imageItem.uuid, imageItem);
        screens.get(sceneUUID).items.add(imageItem);
        Debug(String.format("Added new image to scene %s: %s", sceneUUID, imageItem.uuid), "ENGINE");

        return imageItem.uuid;
    }

    // Adds text to a screen
    public String AddText(String text, String fontPath, String sceneUUID, float x, float y, float z,
                          float fontSize, float rotation, Color textColor) {
        if (fontPath != null && !allowedAssets.contains(fontPath)) {
            throw new IllegalArgumentException("Font asset " + fontPath + " is not in the assets folder.");
        }
        if (!screens.containsKey(sceneUUID)) {
            throw new IllegalArgumentException("Screen with UUID " + sceneUUID + " does not exist.");
        }
        BitmapFont font;
        if (fontPath != null) {
            // Load
            Sprite texture = new Sprite(new Texture(Gdx.files.internal(fontPath.replace(".fnt", "_0.png"))));
            font = new BitmapFont(Gdx.files.internal(fontPath), texture);
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            font.getData().setScale(fontSize);
        } else {
            // Use default
            font = new BitmapFont();
        }
        TextItem textItem = new TextItem(UUID.randomUUID().toString(), x, y, z, rotation, 1.0f,
            text, font, textColor, fontSize);
        items.put(textItem.uuid, textItem);
        screens.get(sceneUUID).items.add(textItem);
        Debug(String.format("Added new text witch font %s to scene %s: %s", fontPath, sceneUUID, textItem.uuid), "ENGINE");
        return textItem.uuid;
    }

    public String AddButton(String imageAsset, String fontAsset, String sceneUUID,
                            float x, float y, float z, float width, float height,
                            String text, Consumer<Button> onClick,
                            Consumer<Button> onHover, Consumer<Button> onLeave) {

        String imgUUID = AddImage(imageAsset, sceneUUID, x, y, 0, width, height, 0, 1);
        ImageItem imageItem = (ImageItem) items.get(imgUUID);

        String txtUUID = AddText(text, fontAsset, sceneUUID,
            x + width / 2, y + height / 2, 0, 1, 0, Color.BLACK);
        TextItem textItem = (TextItem) items.get(txtUUID);

        Button btn = new Button(UUID.randomUUID().toString(), x, y, z, width, height, imageItem, textItem);
        btn.onClick = onClick;
        btn.onHover = onHover;
        btn.onLeave = onLeave;
        buttons.add(btn);

        items.put(btn.uuid, btn);
        screens.get(sceneUUID).items.add(btn);

        inputMultiplexer.addProcessor(new InputAdapter() {
            private boolean wasHovered = false;

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (screens.get(sceneUUID) == null) return false;

                Vector3 worldPos = camera.unproject(new Vector3(screenX, screenY, 0));
                Debug("X: " + worldPos.x + ", Y: " + worldPos.y, "BUTTON");
                if (btn.isPressed(worldPos.x, worldPos.y) && btn.alpha != 0) {
                    onClick.accept(btn);
                    return true;
                }
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if (screens.get(sceneUUID) == null) return false;
                if (btn.alpha == 0) return false;

                Vector3 worldPos = camera.unproject(new Vector3(screenX, screenY, 0));
                boolean isHovered = btn.isPressed(worldPos.x, worldPos.y);
                if (isHovered && !wasHovered && onHover != null) {
                    onHover.accept(btn);
                } else if (!isHovered && wasHovered && onLeave != null) {
                    onLeave.accept(btn);
                }
                wasHovered = isHovered;
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (screens.get(sceneUUID) == null) return false;
                if (btn.alpha == 0) return false;

                Vector3 worldPos = camera.unproject(new Vector3(screenX, screenY, 0));
                boolean isHovered = btn.isPressed(worldPos.x, worldPos.y);
                if (isHovered && !wasHovered && onHover != null) {
                    onHover.accept(btn);
                } else if (!isHovered && wasHovered && onLeave != null) {
                    onLeave.accept(btn);
                }
                wasHovered = isHovered;
                return false;
            }
        });

        return btn.uuid;
    }

    public String AddAnimatedImage(List<String> assetPaths, String sceneUUID, float x, float y, float z,
                                   float width, float height, float rotation, float alpha, float frameDuration) {
        List<Texture> frames = new ArrayList<>();
        for (String path : assetPaths) {
            if (!allowedAssets.contains(path)) {
                throw new IllegalArgumentException("Asset " + path + " is not in the assets folder.");
            }
            Texture tex = imageCache.get(path);
            if (tex == null) {
                tex = new Texture(Gdx.files.internal(path));
                imageCache.put(path, tex);
            }
            frames.add(tex);
        }

        AnimatedImageItem animatedItem = new AnimatedImageItem(
            UUID.randomUUID().toString(), x, y, z, rotation, alpha, width, height, frames, frameDuration
        );

        items.put(animatedItem.uuid, animatedItem);
        screens.get(sceneUUID).items.add(animatedItem);
        Debug(String.format("Added new animated image to scene %s: %s", sceneUUID, animatedItem.uuid), "ENGINE");
        return animatedItem.uuid;
    }

    // Deletes an item by UUID
    public void DeleteItem(String itemUUID) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            // Remove from the screen that owns it
            for (Screen s : screens.values()) {
                s.items.remove(item);
            }
            Debug(String.format("Item removed: %s", itemUUID), "ENGINE");
            items.remove(itemUUID);
        }
    }

    // Clears all items from a given screen
    public void ClearScene(String sceneUUID) {
        if (screens.containsKey(sceneUUID)) {
            Screen s = screens.get(sceneUUID);
            // Also clear from global map
            for (RenderableItem item : s.items) {
                items.remove(item.uuid);
            }
            s.items.clear();
            Debug(String.format("Scene cleared: %s", sceneUUID), "ENGINE");
        }
    }

    // Sets the background color for the current screen
    public void SetSceneBackground(Color color) {
        if (currentScreenUUID != null && screens.containsKey(currentScreenUUID)) {
            screens.get(currentScreenUUID).bgColor = color;
        }
    }

    // Moves an item to a new position
    public void MoveItem(String itemUUID, float newX, float newY) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            item.x = newX;
            item.y = newY;

            if (item instanceof Button) {
                ((Button)item).setBounds(newX, newY, ((Button)item).bounds.width, ((Button)item).bounds.height);
            }
        }
    }

    // Rotates an item
    public void RotateItem(String itemUUID, float newRotation) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            item.rotation = newRotation;
        }
    }

    // Change alpha
    public void SetItemAlpha(String itemUUID, float alpha) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            item.alpha = alpha;
        }
    }

    public void SetItemRotation(String itemUUID, float angle) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            item.rotation = angle;
        }
    }

    public RenderableItem GetItem(String itemUUID)
    {
        return items.get(itemUUID);
    }

    // Changes the image size of an image item
    public void ChangeImageSize(String itemUUID, float newWidth, float newHeight) {
        RenderableItem item = items.get(itemUUID);
        if (item != null && item instanceof ImageItem) {
            ImageItem img = (ImageItem) item;
            img.width = newWidth;
            img.height = newHeight;
        }
    }

    // Replaces the image of an image item with a new asset
    public void ChangeImage(String itemUUID, String newAssetPath) {
        if (!allowedAssets.contains(newAssetPath)) {
            throw new IllegalArgumentException("Asset " + newAssetPath + " is not in the assets folder.");
        }
        RenderableItem item = items.get(itemUUID);
        if (item != null && item instanceof ImageItem) {
            ImageItem img = (ImageItem) item;
            // Get new texture from cache
            Texture newTex = imageCache.get(newAssetPath);
            if (newTex == null) {
                newTex = new Texture(Gdx.files.internal(newAssetPath));
                imageCache.put(newAssetPath, newTex);
            }
            img.assetPath = newAssetPath;
            img.texture = newTex;
            Debug("Image changed to " + newAssetPath + " for: " + itemUUID, "ENGINE");
        }
        else
        {
            Debug("E: id:" + itemUUID + ", i:" + item, "ENGINE");
        }
    }

    // Called from the render() method of the main application class
    public void Render() {
        frames++;
        if (currentScreenUUID == null || !screens.containsKey(currentScreenUUID)) {
            Debug("No screens found for rendering!", "ENGINE");
            return;
        }
        Screen currentScreen = screens.get(currentScreenUUID);

        if (updateCallback != null) {
            updateCallback.onUpdate(Gdx.graphics.getDeltaTime());
        }

        // Update the camera and set the projection matrix
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        camera.position.set(
            camera.viewportWidth/2 + sceneOffset.x,
            camera.viewportHeight/2 + sceneOffset.y,
            0
        );
        //Debug("X: " + camera.position.x + ", Y: " + camera.position.y, "CAMERA");
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Clear using the current background color
        Color bg = currentScreen.bgColor != null && !currentScreen.items.isEmpty() ? currentScreen.bgColor : new Color(0, 0, 0, 1);
        ScreenUtils.clear(bg.r, bg.g, bg.b, bg.a);

        Iterator<Animation> it = animations.iterator();
        while (it.hasNext()) {
            Animation anim = it.next();
            RenderableItem item = items.get(anim.itemUUID);
            if (item == null) {
                it.remove();
                continue;
            }

            anim.elapsedTime += Gdx.graphics.getDeltaTime();
            float progress = Math.min(anim.elapsedTime / anim.duration, 1f);

            switch(anim.type) {
                case MOVE:
                    item.x = this.new Animator().Calc(anim.startValueX, anim.endValueX, progress);
                    item.y = this.new Animator().Calc(anim.startValueY, anim.endValueY, progress);
                    break;
                case ALPHA:
                    item.alpha = this.new Animator().Calc(anim.startValueX, anim.endValueX, progress);
                    break;
                case ROTATE:
                    item.rotation = this.new Animator().Calc(anim.startValueX, anim.endValueX, progress);
            }

            if (item instanceof Button && anim.type == Animation.Type.MOVE) {
                ((Button)item).setBounds(item.x, item.y, ((Button)item).bounds.width, ((Button)item).bounds.height);
            }

            if (progress >= 1f) {
                it.remove();
                //Debug("Animation ended for item: " + item.uuid, "ENGINE");
                pendingTasks.add(anim.onComplete);
            }
        }

        for (Runnable task : pendingTasks) {
            task.run();
        }
        pendingTasks.clear();

        // Begin sprite batch drawing
        batch.begin();
        // Sort items by z-order
        List<RenderableItem> sortedItems = new ArrayList<>(currentScreen.items);
        sortedItems.sort(Comparator.comparingDouble(item -> item.z));

        for (RenderableItem item : sortedItems) {
            if (item instanceof AnimatedImageItem) {
                ((AnimatedImageItem)item).elapsedTime += Gdx.graphics.getDeltaTime();
            }

            item.render(batch, camera);
        }
        batch.end();
    }

    // Dispose resources when exiting
    public void Dispose() {
        for (Texture texture : imageCache.values()) {
            texture.dispose();
        }
        batch.dispose();
        for (RenderableItem item : items.values()) {
            if (item instanceof TextItem) {
                ((TextItem) item).font.dispose();
            }
        }
        Debug("Disposed", "ENGINE");
    }

    public void SetItemAlignment(String itemUUID, Alignment align) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) item.alignment = align;
    }

    public void SetUpdateCallback(UpdateCallback callback) {
        this.updateCallback = callback;
    }

    public void ClearUpdateCallback() {
        this.updateCallback = null;
    }

    public void SetItemPosition(String itemUUID, float x, float y) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            item.x = x;
            item.y = y;
        }
    }

    public void SetSceneOffset(int x, float y) {
        sceneOffset.set(x, y);
        camera.position.set(camera.viewportWidth/2 + sceneOffset.x,
            camera.viewportHeight/2 + sceneOffset.y, 0);
        camera.update();
    }

    /*
        _______   ______   _______   ________  ________   _______  ________
      //       \//      \ /       \\/        \/        \//       \/        \
     //        //       //        //        _/        _//        /        _/
    /       --/        //         /-        /-        /        _/-        /
    \________/\________/\___/____/\_______//\_______//\________/\_______//

    >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                                    CLASSES
    <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    */
    private class CameraAnimation {
        Vector2 startOffset;
        Vector2 targetOffset;
        float duration;
        float elapsedTime;
        Runnable onComplete;

        public CameraAnimation(Vector2 start, Vector2 target, float duration, Runnable onComplete) {
            this.startOffset = start;
            this.targetOffset = target;
            this.duration = duration;
            this.onComplete = onComplete;
        }
    }

    public class Animator {
        public void Move(String itemUUID, float targetX, float targetY, float duration, Runnable onComplete) {
            RenderableItem item = items.get(itemUUID);
            //Debug("Started move animation for item: " + itemUUID + ", tx: " + targetX + ", ty: " + targetY, "ENGINE");
            if (item != null) {
                lol.sillyapps.WCTDR.Engine.Animation anim = new lol.sillyapps.WCTDR.Engine.Animation();
                anim.type = lol.sillyapps.WCTDR.Engine.Animation.Type.MOVE;
                anim.itemUUID = itemUUID;
                anim.startValueX = item.x;
                anim.startValueY = item.y;
                anim.endValueX = targetX;
                anim.endValueY = targetY;
                anim.duration = duration;
                anim.onComplete = onComplete;
                animations.add(anim);
            }
        }

        public void Alpha(String itemUUID, float targetAlpha, float duration, Runnable onComplete) {
            RenderableItem item = items.get(itemUUID);
            //Debug("Started alpha animation for item: " + itemUUID + ", ta: " + targetAlpha, "ENGINE");
            if (item != null) {
                lol.sillyapps.WCTDR.Engine.Animation anim = new lol.sillyapps.WCTDR.Engine.Animation();
                anim.type = lol.sillyapps.WCTDR.Engine.Animation.Type.ALPHA;
                anim.itemUUID = itemUUID;
                anim.startValueX = item.alpha;
                anim.endValueX = targetAlpha;
                anim.duration = duration;
                anim.onComplete = onComplete;
                animations.add(anim);
            }
        }

        public void Rotate(String itemUUID, float targetRotate, float duration, Runnable onComplete) {
            RenderableItem item = items.get(itemUUID);
            //Debug("Started rotation animation for item: " + itemUUID + ", ta: " + targetAlpha, "ENGINE");
            if (item != null) {
                lol.sillyapps.WCTDR.Engine.Animation anim = new lol.sillyapps.WCTDR.Engine.Animation();
                anim.type = lol.sillyapps.WCTDR.Engine.Animation.Type.ROTATE;
                anim.itemUUID = itemUUID;
                anim.startValueX = item.rotation;
                anim.endValueX = targetRotate;
                anim.duration = duration;
                anim.onComplete = onComplete;
                animations.add(anim);
            }
        }

        private float Calc(float startValue, float endValue, float progress) {
            float t = progress * 2;
            float res;
            if (t < 1) {
                res = 0.5f * t * t * t;
            } else {
                t -= 2;
                res = 0.5f * (t * t * t + 2);
            }

            float result = startValue + (endValue - startValue) * res;
            //Debug(String.valueOf(result), "ENGINE");
            return result;
        }
    }

    // A text item is rendered using a BitmapFont
    public static class TextItem extends RenderableItem {
        public String text;
        public BitmapFont font;
        public Color textColor;
        public float size;

        TextItem(String uuid, float x, float y, float z, float rotation, float alpha,
                 String text, BitmapFont font, Color textColor, float size) {
            super(uuid, x, y, z, rotation, alpha);
            this.size = size;
            this.text = text;
            this.font = font;
            this.textColor = textColor;
        }

        @Override
        void render(SpriteBatch batch, OrthographicCamera camera) {
            Color oldColor = font.getColor().cpy();
            font.setColor(textColor.r, textColor.g, textColor.b, alpha);

            float viewportWidth = camera.viewportWidth;
            float viewportHeight = camera.viewportHeight;

            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, text);
            float width = layout.width;
            float height = layout.height;
            //Debug("w: " + width + ", h: " + height, "TEXT");

            float adjustedX = 0, adjustedY = 0;

            switch(alignment) {
                case TOP_LEFT:
                    adjustedY = viewportHeight;
                    break;
                case TOP_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = viewportHeight;
                    break;
                case TOP_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = viewportHeight;
                    break;
                case MIDDLE_LEFT:
                    adjustedY = (viewportHeight + height) / 2;
                    break;
                case CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = (viewportHeight + height) / 2;
                    break;
                case MIDDLE_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = (viewportHeight + height) / 2;
                    break;
                case BOTTOM_LEFT:
                    adjustedY = 0;
                    break;
                case BOTTOM_CENTER:
                    adjustedX = (viewportWidth + width) / 2;
                    adjustedY = 0;
                    break;
                case BOTTOM_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = 0;
                    break;
            }

            font.draw(batch, text, x + adjustedX, y + adjustedY);
            font.setColor(oldColor);
        }
    }

    public class Button extends RenderableItem {
        public Consumer<Button> onClick;
        public Consumer<Button> onHover;
        public Consumer<Button> onLeave;
        public ImageItem imageItem;
        public TextItem textItem;
        public Rectangle bounds;

        public Button(String uuid, float x, float y, float z, float width, float height, ImageItem imageItem, TextItem textItem) {
            super(uuid, x, y, z, 0, 1.0f);
            this.imageItem = imageItem;
            this.textItem = textItem;
            this.bounds = new Rectangle(x, y, width, height);
        }

        @Override
        void render(SpriteBatch batch, OrthographicCamera camera) {
            float viewportWidth = camera.viewportWidth;
            float viewportHeight = camera.viewportHeight;

            float height = bounds.height;
            float width = bounds.width;

            float adjustedX = 0, adjustedY = 0;

            switch(alignment) {
                case TOP_LEFT:
                    adjustedY = viewportHeight - height;
                    break;
                case TOP_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = viewportHeight - height;
                    break;
                case TOP_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = viewportHeight - height;
                    break;
                case MIDDLE_LEFT:
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case MIDDLE_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case BOTTOM_LEFT:
                    adjustedY = 0;
                    break;
                case BOTTOM_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = 0;
                    break;
                case BOTTOM_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = 0;
                    break;
            }

            imageItem.alignment = Alignment.BOTTOM_LEFT;
            textItem.alignment = Alignment.BOTTOM_LEFT;

            setBounds(x + adjustedX, y + adjustedY, bounds.width, bounds.height);

            if (imageItem != null) {
                imageItem.x = x + adjustedX;
                imageItem.y = y + adjustedY;
                imageItem.z = z;
                imageItem.alpha = alpha;
                imageItem.render(batch, camera);
            }
            else
            {
                Debug("IMAGE IS NULL", "ENGINE/BUTTON:" + uuid);
            }

            if (textItem != null) {
                GlyphLayout layout = new GlyphLayout();
                BitmapFont font = textItem.font;
                BitmapFont.BitmapFontData fontData = font.getData();

                fontData.setScale(textItem.size);

                layout.setText(font, textItem.text);
                float txtWidth = layout.width;
                float txtHeight = layout.height;

                float scaleX = 1.0f;
                float scaleY = 1.0f;

                if (txtWidth > bounds.width - 60 || txtHeight > bounds.height) {
                    scaleX = (bounds.width - 60) / txtWidth;
                    scaleY = bounds.height / txtHeight;
                    float scale = Math.min(scaleX, scaleY);
                    if (scale < 1.0f) {
                        fontData.setScale(scale);
                    }
                }

                layout.setText(font, textItem.text);
                txtWidth = layout.width;
                txtHeight = layout.height;

                textItem.x = bounds.x + (bounds.width - txtWidth) / 2;
                textItem.y = bounds.y + (bounds.height - txtHeight) / 2 + txtHeight;

                textItem.z = this.z + 1;
                textItem.alpha = this.alpha;
                //textItem.render(batch, camera);

                //fontData.setScale(oldWidth, oldHeight);
            }
            else
            {
                Debug("TEXT IS NULL", "ENGINE/BUTTON:" + this.uuid);
            }
        }

        public void setBounds(float x, float y, float width, float height) {
            /*
            this.x = x;
            this.y = y;
            */
            bounds.set(x - sceneOffset.x, y - sceneOffset.y, width, height);
        }

        public boolean isPressed(float worldX, float worldY) {
            return bounds.contains(worldX, worldY);
        }
    }

    public enum Alignment {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, CENTER, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    private static class Animation {
        enum Type { MOVE, ALPHA, SCALE, ROTATE }
        Type type;
        String itemUUID;
        float startValueX, startValueY, endValueX, endValueY, duration, elapsedTime;
        public Runnable onComplete;
        boolean isRunning = true;
    }

    // An image item is rendered via a texture
    public static class ImageItem extends RenderableItem {
        public float width, height;
        public String assetPath;
        public Texture texture;

        ImageItem(String uuid, float x, float y, float z, float rotation, float alpha,
                  float width, float height, String assetPath, Texture texture) {
            super(uuid, x, y, z, rotation, alpha);
            this.width = width;
            this.height = height;
            this.assetPath = assetPath;
            this.texture = texture;
        }

        @Override
        void render(SpriteBatch batch, OrthographicCamera camera) {
            Color oldColor = batch.getColor();
            batch.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);

            float viewportWidth = camera.viewportWidth;
            float viewportHeight = camera.viewportHeight;

            float adjustedX = 0, adjustedY = 0;

            switch(alignment) {
                case TOP_LEFT:
                    adjustedY = viewportHeight - height;
                    break;
                case TOP_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = viewportHeight - height;
                    break;
                case TOP_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = viewportHeight - height;
                    break;
                case MIDDLE_LEFT:
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case MIDDLE_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case BOTTOM_LEFT:
                    adjustedY = 0;
                    break;
                case BOTTOM_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = 0;
                    break;
                case BOTTOM_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = 0;
                    break;
            }

            batch.draw(texture, x+adjustedX, y+adjustedY, width / 2, height / 2, width, height, 1, 1, rotation,
                0, 0, texture.getWidth(), texture.getHeight(), false, false);
            batch.setColor(oldColor);
        }
    }

    public static class AnimatedImageItem extends ImageItem {
        private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> animation;
        private float elapsedTime;

        AnimatedImageItem(String uuid, float x, float y, float z, float rotation, float alpha,
                          float width, float height, List<Texture> frames, float frameDuration) {
            super(uuid, x, y, z, rotation, alpha, width, height, "", null); // assetPath и texture не нужны
            TextureRegion[] regions = new TextureRegion[frames.size()];
            for (int i = 0; i < frames.size(); i++) {
                regions[i] = new TextureRegion(frames.get(i));
            }
            this.animation = new com.badlogic.gdx.graphics.g2d.Animation<>(frameDuration, regions);
        }

        @Override
        void render(SpriteBatch batch, OrthographicCamera camera) {
            elapsedTime += Gdx.graphics.getDeltaTime();
            TextureRegion currentFrame = animation.getKeyFrame(elapsedTime, true);

            Color oldColor = batch.getColor();
            batch.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);
            float viewportWidth = camera.viewportWidth;
            float viewportHeight = camera.viewportHeight;
            float adjustedX = 0, adjustedY = 0;

            switch(alignment) {
                case TOP_LEFT:
                    adjustedY = viewportHeight - height;
                    break;
                case TOP_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = viewportHeight - height;
                    break;
                case TOP_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = viewportHeight - height;
                    break;
                case MIDDLE_LEFT:
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case MIDDLE_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = (viewportHeight - height) / 2;
                    break;
                case BOTTOM_LEFT:
                    adjustedY = 0;
                    break;
                case BOTTOM_CENTER:
                    adjustedX = (viewportWidth - width) / 2;
                    adjustedY = 0;
                    break;
                case BOTTOM_RIGHT:
                    adjustedX = viewportWidth - width;
                    adjustedY = 0;
                    break;
            }

            // Отрисовка текущего кадра
            batch.draw(currentFrame, x + adjustedX, y + adjustedY, width / 2, height / 2, width, height, 1, 1, rotation);
            batch.setColor(oldColor);
        }
    }

    // Base abstract class for items that can be rendered
    public abstract static class RenderableItem {
        public String uuid;
        public float x, y, z;
        public float rotation;
        public float alpha;
        public Alignment alignment = Alignment.TOP_LEFT;

        RenderableItem(String uuid, float x, float y, float z, float rotation, float alpha) {
            this.uuid = uuid;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotation = rotation;
            this.alpha = alpha;
        }

        abstract void render(SpriteBatch batch, OrthographicCamera camera);
    }

    private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        public LRUCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // A Screen holds a list of renderable items and a background color
    private class Screen {
        String uuid;
        Color bgColor;
        List<RenderableItem> items;

        public Screen(String uuid) {
            this.uuid = uuid;
            // Default background color.
            this.bgColor = new Color(0.15f, 0.15f, 0.2f, 1f);
            this.items = new ArrayList<>();
        }
    }
}
