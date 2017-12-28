package com.oz_heng.apps.android.inventory.helper;

/*
 * Created by Pack Heng on 21/05/17
 * pack@oz-heng.com
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.R;
import com.oz_heng.apps.android.inventory.product.ProductContract;

import java.io.ByteArrayOutputStream;

/**
 * Helper methods.
 */
public final class Utils {
    private static final String LOG_TAG = Utils.class.getSimpleName();

    private Utils() {}

    /**
     * Converts an array of bytes productImage to a bitmap.
     * Returns null if the argument is either null or an empty array.
     *
     * @param bytes an Array of bytes
     * @return a Bitmap
     */
    public static @Nullable Bitmap byteArrayToBitmap(@Nullable byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Converts a bitmap productImage to an array of bytes.
     *
     * @param bitmap a Bitmap
     * @return Array of bytes, or null if the bitmap is null or empty
     */
    public static @Nullable byte[] bitmapToByteArray(@Nullable Bitmap bitmap) {
        if (bitmap == null || bitmap.getByteCount() == 0) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * Returns an int from a String.
     *
     * @param s a String
     * @return an int, 0 if the String is empty
     * @throws NumberFormatException if the {@link String} doesn't contain a parsable {@link int}
     */
    public static int stringToInt(String s) throws NumberFormatException {
        if (s.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(s.trim());
    }

    /**
     * Delete all products from the database.
     */
    public static void deleteAllProducts(Context context) {
        int rowsDeleted = 0;
        boolean isDeleteOK = true;

        try {
            rowsDeleted = context.getContentResolver().delete(
                    ProductContract.ProductEntry.CONTENT_URI,
                    null,
                    null);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "deleteAllProducts(): error with deleting all products.", e);
            isDeleteOK = false;
        } finally {
            if (isDeleteOK && rowsDeleted > 0) {
                Toast.makeText(context, rowsDeleted + context.getString(R.string.products_deleted_from_db),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.delete_all_products_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
