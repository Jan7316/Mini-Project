package mini.app.orbis;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.util.ArrayList;

/**
 * Created by Jan on 23/10/2016.
 */

public class Cache {

    private static LruCache<String, Bitmap> memoryCache;
    private static ArrayList<String> workingOn;

    private static boolean initialized;

    public static synchronized void init() {
        if(!isInitialized()) {
            // Get max available VM memory, exceeding this amount will throw an
            // OutOfMemory exception. Stored in kilobytes as LruCache takes an
            // int in its constructor.
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;

            memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };

            workingOn = new ArrayList<>();

            initialized = true;
        }
    }

    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        init();
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
            markBitmapAsBeingLoaded(key, false);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    public static boolean isBitmapCached(String key) {
        init();
        return !(memoryCache.get(key) == null);
    }

    public static boolean isBitmapBeingLoaded(String key) {
        init();
        return workingOn.contains(key);
    }

    public static void markBitmapAsBeingLoaded(String key, boolean isBeingLoaded) {
        if(!isBeingLoaded) {
            workingOn.remove(key);
            return;
        }
        if(!workingOn.contains(key)) {
            workingOn.add(key);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

}
