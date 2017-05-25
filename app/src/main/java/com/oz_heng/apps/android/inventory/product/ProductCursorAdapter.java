package com.oz_heng.apps.android.inventory.product;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oz_heng.apps.android.inventory.R;
import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;
import com.oz_heng.apps.android.inventory.helper.Utils;

/**
 * Created by Pack Heng on 12/05/17
 * pack@oz-heng.com
 */

public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}
     *
     * @param context   The context
     * @param c         The cursor from which to get the data
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c,
                0 /* This flag isn't needed when using {@link android.content.CursorLoader} */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view      Existing view, returned earlier by newView() method.
     * @param context   App context.
     * @param cursor    The cursor from which to get the data. The cursor is already moved to the
     *                  correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find the views
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_product_image);
        TextView nameTextView = (TextView) view.findViewById(R.id.list_item_product_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.list_item_product_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.list_item_product_price);

        // Find the product data to display
        int imageCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int nameCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        String name = cursor.getString(nameCI);
        int quantity = cursor.getInt(quantityCI);
        double price = cursor.getDouble(priceCI);

        // Display the product data

        // Get Blob image, convert it to Bitmap and display
        byte[] imabeBytes = cursor.getBlob(imageCI);
        Bitmap bitmap = Utils.byteArrayToBitmap(imabeBytes);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }

        nameTextView.setText(name);
        quantityTextView.setText(String.valueOf(quantity));
        priceTextView.setText(String.format("%.2f%n", price));
    }
}
