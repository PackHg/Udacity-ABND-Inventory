package com.oz_heng.apps.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.helper.Utils;
import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;

/**
 * Created by Pack Heng on 12/05/17
 * pack@oz-heng.com
 */

/* TODO: Is EditorActivity completed?
*  Done: Add case to add a new product (when user clicks on + button in CatalogActivity
*  Done: Validate the data from the user input fields
*  TODO: Option to the delete the current product
*  Done: Do we need Menu Option Items for EditorActivity? Yes for saving the product
*  TODO: Take picture of the product
*  TODO: Re-arrange the floating action buttons
*  TODO: Remove input redundant data validation, and remove Warning TextView?
*  TODO: Force EditorActivity UI to be with Portrait mode only?
* */

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // Uses existing loader used by CatalogActivity
    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Content URI for the existing product (null if it's a new product)
    private Uri mCurrentProductUri;

    /* EditText fields to enter the product data */
    EditText mNameET;
    EditText mQuantityET;
    EditText mPriceET;
    TextView mNoImageTV;

    // ImageView to display the product image
    ImageView mImageView;

    TextView mWarningTextView;


    /** Boolean flag which will be true if the user updates part of the product form */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View,
     * implying that they are modifying the view, and we change the
     * mProducrHasChanged boolean to true.
     */
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        /* If the intent doesn't conent a product content URI, then add a product,
         * else edit an existing product */
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_title_add));

            // TODO: invalidate Delete option

        } else {
            setTitle(getString(R.string.editor_title_edit));
            long id = ContentUris.parseId(mCurrentProductUri);

            Toast.makeText(this, "Id: " + id + "\n" +
                    "Uri: " + mCurrentProductUri.toString(), Toast.LENGTH_LONG).show();

            // Initialize a loader to read the product data from the content provider
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mImageView = (ImageView) findViewById(R.id.editor_picture);
        mNoImageTV = (TextView) findViewById(R.id.editor_no_picture_view);
        mNameET = (EditText) findViewById(R.id.editor_name);
        mQuantityET = (EditText) findViewById(R.id.editor_quantity);
        mPriceET = (EditText) findViewById(R.id.editor_price);
        mWarningTextView = (TextView) findViewById(R.id.editor_warning);

//        mNameET.setTextColor(ContextCompat.getColor(this, R.color.editorFieldTextColor));
//        mQuantityET.setTextColor(ContextCompat.getColor(this, R.color.editorFieldTextColor));
//        mPriceET.setTextColor(ContextCompat.getColor(this, R.color.editorFieldTextColor));
//        mWarningTextView.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editor_action_save:
                if (saveProduct()) {
                    finish();       // Exits the activity
                }
                return true;
            case R.id.editor_action_delete:
                deleteProduct();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds a new product or updates an existing product.
     *
     * @return true if the Quantity and Price are correct, else return false.
     */
    private boolean saveProduct() {
        // Get the user inputs
        String nameString = mNameET.getText().toString().trim();
        String quantityString = mQuantityET.getText().toString().trim();
        String priceString = mPriceET.getText().toString().trim();
        // TODO: get the image input from the user

        // If all of the inputs are empty, then return true and without saving.
        if (nameString.isEmpty() && quantityString.isEmpty() && priceString.isEmpty()) {
            return true;
        }

        /* Validate quantity input */
        int quantity = 0;
        boolean isQuantityCorrect = true;

        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, ", saveProduct(): error with parsing Quantity.", e);
            isQuantityCorrect = false;
        } finally {
            if (!isQuantityCorrect || quantity < 0) {
                /* Display warning message to user and set the quantity text with warning color */
                mWarningTextView.setText(getString(R.string.editor_quantity_incorrect));
                mQuantityET.setTextColor(ContextCompat.getColor(this, R.color.editorWarningTextColor));
            }
        }

        /* Validate price input */
        Double price = .0;
        boolean isPriceCorrect = true;

        try {
            price = Double.parseDouble(priceString);
        } catch (NullPointerException | NumberFormatException e) {
            Log.e(LOG_TAG, ", saveProduct(): error with parsing Price. ", e);
            isPriceCorrect = false;
        } finally {
            if (!isPriceCorrect || price < .0) {
                /* Display warning messages to user and set the price text with warning color */
                mWarningTextView.append("\n" + getString(R.string.editor_price_incorrect));
                mPriceET.setTextColor(ContextCompat.getColor(this, R.color.editorWarningTextColor));
            }
        }

        /* If quantity or price is incorred, product data are not saved and return false.
         * Else change the color of the Quantity and Price EditTexts to normal */
        if (!isQuantityCorrect || !isPriceCorrect) {
            return false;
        } else {
            mQuantityET.setTextColor(ContextCompat.getColor(this, R.color.editorFieldTextColor));
            mPriceET.setTextColor(ContextCompat.getColor(this, R.color.editorFieldTextColor));
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);

        if (mCurrentProductUri == null) {
            // Add a new product
            Uri uri = null;
            try {
                uri = getContentResolver().insert(
                        ProductEntry.CONTENT_URI,   // The products content URI
                        values                      // The values to insert
                );
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, ", saveProduct() - when inserting a product: ", e);
            } finally {
                if (uri != null) {
                    long id = ContentUris.parseId(uri);
                    Toast.makeText(this, getString(R.string.save_product_successful_with_id) + id,
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, getString(R.string.save_product_failed),
                            Toast.LENGTH_LONG).show();
                }
            }
        } else {
            // Update an existing product
            int rowsUpdated = 0;
            try {
                rowsUpdated = getContentResolver().update(
                        mCurrentProductUri, // Content URI of the current product to update
                        values,             // Values to update
                        null,               // No selection
                        null                // No selection agrs
                );
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, ", saveProduct() - when updating a product: ", e);
            } finally {
                if (rowsUpdated > 0) {
                    Toast.makeText(this, getString(R.string.update_product_successful),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.update_product_failed),
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        return true;
    }

    /**
     * Deletes an existing product.
     */
    private void deleteProduct() {

        // If the product URI is null, do nothing.
        if (mCurrentProductUri == null) {
            Toast.makeText(this, getString(R.string.delete_no_product_to_delete),
                    Toast.LENGTH_LONG).show();
            return;
        }

        int rowsDeleted = 0;

        try {
            rowsDeleted = getContentResolver().delete(
                    mCurrentProductUri,
                    null,
                    null
            );
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, ", deleteProduct() - when deleting a product: ", e);
            Toast.makeText(this, getString(R.string.delete_product_failed),
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, rowsDeleted + getString(R.string.delete_products_deleted),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(
                this,                   // Context of parent activity
                mCurrentProductUri,    // Content URI of the relevent product
                projection,             // The columns to be included in the resulting cursor
                null,                   // No selection criteria
                null,                   // No selection arguments
                null                    // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            /* Get the relevant column indexes */
            int nameCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageCI = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            /* Get the product data */
            String name = cursor.getString(nameCI);
            int quantity = cursor.getInt(quantityCI);
            double price = cursor.getDouble(priceCI);
            byte[] image = cursor.getBlob(imageCI);

            // Update the fields
            mNameET.setText(name);
            mQuantityET.setText(String.valueOf(quantity));
            mPriceET.setText(String.format("%.2f%n", price));
            if (image == null || image.length == 0) {
                mNoImageTV.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.INVISIBLE);
            } else {
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(Utils.byteArrayToBitmap(image));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameET.setText("");
        mQuantityET.setText("");
        mPriceET.setText("");
        mImageView.setVisibility(View.INVISIBLE);
        mNoImageTV.setVisibility(View.VISIBLE);
    }

}
