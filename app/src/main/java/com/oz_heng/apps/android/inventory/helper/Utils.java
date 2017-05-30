package com.oz_heng.apps.android.inventory.helper;

/*
 * Created by Pack Heng on 21/05/17
 * pack@oz-heng.com
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;

/**
 * Helper methods
 */
public final class Utils {

    private Utils() {}

    /**
     * Returns an array of bytes image to a bitmap.
     * Returns null if the argument is either null or an empty array.
     *
     * @param bytes Array of bytes
     * @return      Bitmap
     */
    public static @Nullable Bitmap byteArrayToBitmap(@Nullable byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     *
     * @param bitmap
     * @return
     */
    public static @Nullable byte[] bitmapToByteArray(@Nullable Bitmap bitmap) {
        if (bitmap == null || bitmap.getByteCount() == 0) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }
}
