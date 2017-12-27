package com.oz_heng.apps.android.inventory.product;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oz_heng.apps.android.inventory.EditorActivity;
import com.oz_heng.apps.android.inventory.R;
import com.oz_heng.apps.android.inventory.helper.Utils;
import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;

import java.util.Locale;


/**
 * Provides a binding from a data set to views that are displayed within a RecyclerView.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String LOG_TAG = ProductAdapter.class.getSimpleName();

    private Cursor cursor = null;
    private Context context;

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        public final ImageView productImage;
        public final TextView productName, productQuantity, productPrice;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productImage = (ImageView) itemView.findViewById(R.id.list_item_product_image);
            productName = (TextView) itemView.findViewById(R.id.list_item_product_name);
            productQuantity = (TextView) itemView.findViewById(R.id.list_item_product_quantity);
            productPrice = (TextView) itemView.findViewById(R.id.list_item_product_price);

            // Define click listener for the ViewHolder's View.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = getAdapterPosition() + 1;
                    Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                    // Start EditorActivity with the URI as the data field of the intent.
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

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : -1;
    }
}
