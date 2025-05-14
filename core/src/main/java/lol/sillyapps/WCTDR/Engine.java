package lol.sillyapps.WCTDR;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.*;

public class Engine {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private String currentScreenUUID;
    private Map<String, Screen> screens;
    private Map<String, RenderableItem> items;
    private LRUCache<String, Texture> imageCache;
    private final int IMAGE_CACHE_CAPACITY = 50;
    private Set<String> allowedAssets;

    private static void Debug(String text) {
        System.out.printf("[ENGINE] %s%n", text);
    }

    public Engine() {
        Debug("Creating an engine...");
        screens = new HashMap<>();
        items = new HashMap<>();
        imageCache = new LRUCache<>(IMAGE_CACHE_CAPACITY);
        allowedAssets = new HashSet<>();

        // Initialize the SpriteBatch
        batch = new SpriteBatch();

        // Initialize the camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
        Debug("The engine is created!");
    }

    // Initializes the engine
    public void Initialize() {
        Debug("Initializing engine...");
        // Clear loaded assets
        allowedAssets.clear();
        Debug("Reading assets...");
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
        Debug("Preloading files...");
        // Pre-load each found asset into the cache
        for (String assetPath : allowedAssets) {
            if (!assetPath.toLowerCase().endsWith(".fnt")) {
                Texture tex = new Texture(Gdx.files.internal(assetPath));
                imageCache.put(assetPath, tex);
            }
        }
        Debug("Initialized!");
    }

    // Adds a new screen and returns its UUID
    public String AddScreen() {
        String uuid = UUID.randomUUID().toString();
        Screen s = new Screen(uuid);
        screens.put(uuid, s);
        if (currentScreenUUID == null) {
            currentScreenUUID = uuid;
        }
        Debug(String.format("Created new screen: %s", uuid));
        return uuid;
    }

    // Returns currents screen UUID
    public String GetCurrentScreen() {
        return currentScreenUUID;
    }

    // Deletes a screen by UUID
    public void DeleteScreen(String uuid) {
        if (screens.containsKey(uuid)) {
            screens.remove(uuid);
            if (uuid.equals(currentScreenUUID)) {
                currentScreenUUID = screens.isEmpty() ? null : screens.keySet().iterator().next();
            }
        }
        Debug(String.format("Screen %s removed!", uuid));
    }

    // Change the current screen to render
    public void ChangeScreen(String uuid) {
        if (screens.containsKey(uuid)) {
            currentScreenUUID = uuid;
        }
        Debug(String.format("%s is now the current scene", uuid));
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
        Debug(String.format("Added new image to scene %s: %s", sceneUUID, imageItem.uuid));

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
            Sprite texture = new Sprite(new Texture(Gdx.files.internal("fonts/consolas_0.png")));
            font = new BitmapFont(Gdx.files.internal(fontPath), texture);
            font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            font.getData().setScale(fontSize);
        } else {
            // Use default
            font = new BitmapFont();
        }
        TextItem textItem = new TextItem(UUID.randomUUID().toString(), x, y, z, rotation, 1.0f,
            text, font, textColor);
        items.put(textItem.uuid, textItem);
        screens.get(sceneUUID).items.add(textItem);
        Debug(String.format("Added new text witch font %s to scene %s: %s", fontPath, sceneUUID, textItem.uuid));
        return textItem.uuid;
    }

    // Deletes an item by UUID
    public void DeleteItem(String itemUUID) {
        RenderableItem item = items.get(itemUUID);
        if (item != null) {
            // Remove from the screen that owns it
            for (Screen s : screens.values()) {
                s.items.remove(item);
            }
            Debug(String.format("Item removed: %s", itemUUID));
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
            Debug(String.format("Scene cleared: %s", sceneUUID));
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
        }
    }

    // Called from the render() method of the main application class
    public void Render() {
        if (currentScreenUUID == null || !screens.containsKey(currentScreenUUID)) {
            Debug("No screens found for rendering!");
            return;
        }
        Screen currentScreen = screens.get(currentScreenUUID);

        // Update the camera and set the projection matrix
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Clear using the current background color
        Color bg = currentScreen.bgColor != null ? currentScreen.bgColor : new Color(0, 0, 0, 1);
        ScreenUtils.clear(bg.r, bg.g, bg.b, bg.a);

        // Begin sprite batch drawing
        batch.begin();
        // Sort items by z-order
        List<RenderableItem> sortedItems = new ArrayList<>(currentScreen.items);
        sortedItems.sort(Comparator.comparingDouble(item -> item.z));

        for (RenderableItem item : sortedItems) {
            item.render(batch);
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
        Debug("Disposed");
    }

    // ***** CLASSES AND UTILS *****

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

    // Base abstract class for items that can be rendered
    private abstract static class RenderableItem {
        String uuid;
        float x, y, z;
        float rotation;
        float alpha;

        RenderableItem(String uuid, float x, float y, float z, float rotation, float alpha) {
            this.uuid = uuid;
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotation = rotation;
            this.alpha = alpha;
        }

        abstract void render(SpriteBatch batch);
    }

    // An image item is rendered via a texture
    private static class ImageItem extends RenderableItem {
        float width, height;
        String assetPath;
        Texture texture;

        ImageItem(String uuid, float x, float y, float z, float rotation, float alpha,
                  float width, float height, String assetPath, Texture texture) {
            super(uuid, x, y, z, rotation, alpha);
            this.width = width;
            this.height = height;
            this.assetPath = assetPath;
            this.texture = texture;
        }

        @Override
        void render(SpriteBatch batch) {
            Color oldColor = batch.getColor();
            batch.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);
            batch.draw(texture, x, y, width / 2, height / 2, width, height, 1, 1, rotation,
                0, 0, texture.getWidth(), texture.getHeight(), false, false);
            batch.setColor(oldColor);
        }
    }

    // A text item is rendered using a BitmapFont
    private static class TextItem extends RenderableItem {
        String text;
        BitmapFont font;
        Color textColor;

        TextItem(String uuid, float x, float y, float z, float rotation, float alpha,
                 String text, BitmapFont font, Color textColor) {
            super(uuid, x, y, z, rotation, alpha);
            this.text = text;
            this.font = font;
            this.textColor = textColor;
        }

        @Override
        void render(SpriteBatch batch) {
            Color oldColor = font.getColor().cpy();
            font.setColor(textColor.r, textColor.g, textColor.b, alpha);
            font.draw(batch, text, x, y);
            font.setColor(oldColor);
        }
    }
}
