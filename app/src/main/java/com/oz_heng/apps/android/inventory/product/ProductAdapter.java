package com.oz_heng.apps.android.inventory.product;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.EditorActivity;
import com.oz_heng.apps.android.inventory.R;
import com.oz_heng.apps.android.inventory.helper.Utils;
import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;

import java.util.Locale;

import static android.view.View.NO_ID;
import static com.oz_heng.apps.android.inventory.helper.Utils.updateProduct;


/**
 * Provides a binding from a data set to views that are displayed within a RecyclerView.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String LOG_TAG = ProductAdapter.class.getSimpleName();

    private Cursor cursor = null;
    private Context context;

    class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView productImage;
        final TextView productName, productQuantity, productPrice;
        FloatingActionButton salesButton;

        ProductViewHolder(View itemView) {
            super(itemView);
            productImage = (ImageView) itemView.findViewById(R.id.list_item_product_image);
            productName = (TextView) itemView.findViewById(R.id.list_item_product_name);
            productQuantity = (TextView) itemView.findViewById(R.id.list_item_product_quantity);
            productPrice = (TextView) itemView.findViewById(R.id.list_item_product_price);
            salesButton = (FloatingActionButton) itemView.findViewById(R.id.list_item_button_sale);

            // Setting up click listener for the sales button.
            salesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    long id = ProductAdapter.this.getItemId(position);
                    int quantityCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
                    int quantity = cursor.getInt(quantityCI);

                    if (quantity > 0) {
                        quantity--;

                        // Update the product quanity in the database.
                        Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                        ContentValues values = new ContentValues();
                        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                        updateProduct(context, productUri, values);
                    } else {
                        Toast.makeText(context, context.getString(R.string.product_not_in_stock),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

            // Setting up click listener for the ViewHolder's View.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    long id = ProductAdapter.this.getItemId(position);
                    Toast.makeText(context, context.getString(R.string.item_id) + id, Toast.LENGTH_SHORT).show();

                    // Start EditorActivity with the URI as the data field of the intent.
                    Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                    Intent intent = new Intent(context, EditorActivity.class);
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            });
        }
    }

    public ProductAdapter(Context context) {
        this.context = context;
    }

    /**
     * Set the cursor as the data set of the adapter, and notify the data set has changed.
     * @param cursor includes the data set.
     */
    public void setData(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        if (cursor != null) {
            if (cursor.moveToPosition(position)) {
                int imageCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
                int nameCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
                int quantityCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
                int priceCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

                // Display the product name, quantity and price.
                String name = cursor.getString(nameCI);
                holder.productName.setText(name);
                int quantity = cursor.getInt(quantityCI);
                holder.productQuantity.setText(String.valueOf(quantity));
                double price = cursor.getDouble(priceCI);
                holder.productPrice.setText(String.format(Locale.US,"$%.2f%n", price));

                // Display the product image.
                byte[] imageBytes = cursor.getBlob(imageCI);
                Bitmap bitmap = Utils.byteArrayToBitmap(imageBytes);
                if (bitmap != null) {
                    holder.productImage.setImageBitmap(bitmap);
                }
            } else {
                holder.productName.setText(R.string.error_no_product);
            }
        } else {
            Log.e(LOG_TAG, "onBindViewHolder: Cursor is null.");
        }
        
    }

    /**
     * Return the row id of the item at position.
     * @param position Adapter position to query.
     * @return Id of the item, or null if the associated cursor is null.
     */
    @Override
    public long getItemId(int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            int idCI = cursor.getColumnIndex(ProductEntry._ID);
            return cursor.getLong(idCI);
        }
        return NO_ID;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        // Each item in the data set is represented with a unique identifier of type Long.
        super.setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : -1;
    }
}
