package iop.org.iop_contributors_app.utils;

import android.graphics.Bitmap;

/**
 * Created by mati on 24/11/16.
 */

public class Cache {

    private static Bitmap qrLittleBitmapCache;
    private static Bitmap qrBigBitmapCache;

    public static Bitmap getQrLittleBitmapCache() {
        return qrLittleBitmapCache;
    }

    public static void setQrLittleBitmapCache(Bitmap qrLittleBitmapCache) {
        Cache.qrLittleBitmapCache = qrLittleBitmapCache;
    }


    public static Bitmap getQrBigBitmapCache() {
        return qrBigBitmapCache;
    }

    public static void setQrBigBitmapCache(Bitmap qrBigBitmapCache) {
        Cache.qrBigBitmapCache = qrBigBitmapCache;
    }
}
