package com.oz_heng.apps.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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

    // ImageView to display the product image
    ImageView mImageView;

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

        ConstraintLayout actionButtons = (ConstraintLayout) findViewById(R.id.editor_action_buttons);
        FloatingActionButton deleteFAB = (FloatingActionButton) findViewById(R.id.editor_button_delete);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        /* If the intent doesn't conent a product content URI, then add a product,
         * else edit an existing product */
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_title_add));
            actionButtons.setVisibility(View.INVISIBLE);
        } else {
            setTitle(getString(R.string.editor_title_edit));
            long id = ContentUris.parseId(mCurrentProductUri);

            Toast.makeText(this, "Id: " + id + "\n" +
                    "Uri: " + mCurrentProductUri.toString(), Toast.LENGTH_SHORT).show();

            // Initialize a loader to read the product data from the content provider
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

            deleteFAB.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         showDeleteConfirmationDialog();
                     }
                 }
            );
        }

        mImageView = (ImageView) findViewById(R.id.editor_picture);
        mNameET = (EditText) findViewById(R.id.editor_name);
        mQuantityET = (EditText) findViewById(R.id.editor_quantity);
        mPriceET = (EditText) findViewById(R.id.editor_price);
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
                saveProduct();
                finish();       // Exits the activity
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds a new product or updates an existing product.
     */
    private void saveProduct() {
        // Get the user inputs
        String nameString = mNameET.getText().toString().trim();
        String quantityString = mQuantityET.getText().toString().trim();
        String priceString = mPriceET.getText().toString().trim();
        // TODO: get the image input from the user

        // If all of the inputs are empty, then return true and without saving.
        if (nameString.isEmpty() && quantityString.isEmpty() && priceString.isEmpty()) {
            return;
        }

        int quantity = Integer.parseInt(quantityString);
        Double price = Double.parseDouble(priceString);

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
                    Toast.makeText(this, getString(R.string.editor_save_product_successful_with_id) + id,
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, getString(R.string.editor_save_product_failed),
                            Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Create an AlertDialog.Builder and set the message, and click listeners
     * for the positive and negative buttons on the dialog.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_delete_dialog_msg);
        builder.setPositiveButton(R.string.editor_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.editor_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Deletes an existing product.
     */
    private void deleteProduct() {
        // If the product URI is null, do nothing.
        if (mCurrentProductUri == null) {
            Toast.makeText(this, getString(R.string.editor_delete_no_product_to_delete),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsDeleted = 0;
        boolean isDeleteOK = true;

        try {
            rowsDeleted = getContentResolver().delete(
                    mCurrentProductUri,
                    null,
                    null
            );
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, ", deleteProduct() - when deleting a product: ", e);
            isDeleteOK = false;
        } finally {
            if (isDeleteOK && rowsDeleted > 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_deleted), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
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
    }

}
