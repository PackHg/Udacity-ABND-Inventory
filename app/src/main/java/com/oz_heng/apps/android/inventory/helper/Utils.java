package com.oz_heng.apps.android.inventory.helper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.R;

import java.io.ByteArrayOutputStream;

/**
 * Helper methods.
 */
public final class Utils {
    private static final String LOG_TAG = Utils.class.getSimpleName();

    /**
     * Private constructor preventing this class being instantiated.
     */
    private Utils() {}

    /**
     * Converts an array of bytes of an image to a bitmap.
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
     * Converts a bitmap of an image to an array of bytes.
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
     * Insert a product into the database.
     *
     * @param context the application context.
     * @param contentUri the content Uri.
     * @param values values of the product to insert.
     * @return the product Uri.
     */
    public static Uri insertProduct(Context context, Uri contentUri, ContentValues values) {
        Uri uri = null;

        try {
            uri = context.getContentResolver().insert(
                contentUri,     // The products content URI
                values          // The values to insert
            );
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "insertProduct(): error with inserting the product.", e);
        } finally {
            if (uri != null) {
                long id = ContentUris.parseId(uri);
                Toast.makeText(context, context.getString(R.string.save_product_successful_with_id) + id,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.save_product_failed),
                        Toast.LENGTH_LONG).show();
            }
        }

        return uri;
    }

    /**
     * Update the product corresponding to the product Uri.
     *
     * @param context the application context.
     * @param productUri the product Uri,
     * @param values to update.
     * @return number of rows updated.
     */

    public static int updateProduct(Context context, Uri productUri, ContentValues values) {
        int rowsUpdated = 0;
        boolean isUpdateOK = true;

        try {
            rowsUpdated = context.getContentResolver().update(
                    productUri,         // Content URI of the current product to update
                    values,             // Values to update
                    null,         // No selection
                    null     // No selection args
            );
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "updateProduct(): error with updating the product.", e);
            isUpdateOK = false;
        } finally {
            if (isUpdateOK && rowsUpdated > 0) {
                Toast.makeText(context, context.getString(R.string.update_product_successful), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.update_product_failed),
                        Toast.LENGTH_LONG).show();
            }
        }

        return rowsUpdated;
    }

    /**
     * Delete an existing product.
     */
    public static int deleteProduct(Context context, Uri productUri) {
        // If the product URI is null, do nothing.
        if (productUri == null) {
            Toast.makeText(context, context.getString(R.string.no_product_to_delete),
                    Toast.LENGTH_LONG).show();
            return 0;
        }

        int rowsDeleted = 0;
        boolean isDeleteOK = true;

        try {
            rowsDeleted = context.getContentResolver().delete(
                    productUri,
                    null,
                    null
            );
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, ", deleteProduct() - when deleting a product: ", e);
            isDeleteOK = false;
        } finally {
            if (isDeleteOK && rowsDeleted > 0) {
                Toast.makeText(context, context.getString(R.string.delete_product_successful),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.delete_product_failed),
                        Toast.LENGTH_LONG).show();
            }
        }

        return rowsDeleted;
    }

    /**
     * Delete all products from the database.
     *
     * @param context the application context.
     * @param contentUri the content Uri.
     * @return the number of rows deleted.
     */
    public static int deleteAllProducts(Context context, Uri contentUri) {
        int rowsDeleted = 0;
        boolean isDeleteOK = true;

        try {
            rowsDeleted = context.getContentResolver().delete(
                    contentUri,
                    null,
                    null);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "deleteAllProducts(): error with deleting all products.", e);
            isDeleteOK = false;
        } finally {
            if (isDeleteOK && rowsDeleted > 0) {
                Toast.makeText(context, rowsDeleted + context.getString(R.string.products_deleted_from_db),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.delete_all_products_failed),
                        Toast.LENGTH_LONG).show();
            }
        }

        return rowsDeleted;
    }
}
