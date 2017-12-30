package com.oz_heng.apps.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.helper.Utils;
import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;

import static com.oz_heng.apps.android.inventory.helper.Utils.deleteProduct;
import static com.oz_heng.apps.android.inventory.helper.Utils.insertProduct;
import static com.oz_heng.apps.android.inventory.helper.Utils.updateProduct;

/**
 * Editor of an existing or new product.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // Uses existing loader used by CatalogActivity
    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Content URI for the existing product (null if it's a new product)
    private Uri currentProductUri;

    /* EditText fields for entering the product data */
    EditText nameET;
    EditText mQuantityET;
    EditText mPriceET;

    // ImageView to display the product image
    ImageView mImageView;

    // Image that may be taken by a camera app
    Bitmap mImageBitmap = null;

    /** Boolean flag which will be true if the user updates part of the product form */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View,
     * implying that they are modifying the view, and we change the
     * mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    // Action buttons
    FloatingActionButton mFABMinus;
    FloatingActionButton mFABPlus;
    FloatingActionButton mFABOrder;
    FloatingActionButton mFABDelete;
    FloatingActionButton mFABTakePhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentProductUri = intent.getData();

        mFABMinus = (FloatingActionButton) findViewById(R.id.editor_button_minus);
        mFABPlus = (FloatingActionButton) findViewById(R.id.editor_button_plus);
        mFABOrder = (FloatingActionButton) findViewById(R.id.editor_button_order);
        mFABDelete = (FloatingActionButton) findViewById(R.id.editor_button_delete);
        mFABTakePhoto = (FloatingActionButton) findViewById(R.id.editor_button_take_photo);

        /* If the intent doesn't content a product content URI, then add a product,
         * else edit an existing product */
        if (currentProductUri == null) {
            // Add a new product

            setTitle(getString(R.string.editor_title_add));

            // Hide non applicable action buttons
            mFABMinus.setVisibility(View.INVISIBLE);
            mFABPlus.setVisibility(View.INVISIBLE);
            mFABOrder.setVisibility(View.INVISIBLE);
            mFABDelete.setVisibility(View.INVISIBLE);
            mFABTakePhoto.setVisibility(View.INVISIBLE);
         }
         else {
            // Edit an existing product

            setTitle(getString(R.string.editor_title_edit));

            // Initialize a loader to read the product data from the content provider
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);

            // Set OnClickListener on action buttons to trigger corresponding action
            mFABPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductHasChanged = true;
                    showIncreaseQuantityDialog();
                }
            });
            mFABMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductHasChanged = true;
                    showDecreaseQuantityDialog();
                }
            });
            mFABOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductHasChanged = true;
                    showOrderDialog();
                }
            });
            mFABDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });
            mFABTakePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProductHasChanged = true;
                    dispatchTakePictureIntent();
                }
            });
        }

        mImageView = (ImageView) findViewById(R.id.editor_picture);
        mImageView.setVisibility(View.VISIBLE);
        nameET = (EditText) findViewById(R.id.editor_name);
        mQuantityET = (EditText) findViewById(R.id.editor_quantity);
        mPriceET = (EditText) findViewById(R.id.editor_price);

        // Take a picture if user clicks on mImageView
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProductHasChanged = true;
                dispatchTakePictureIntent();
            }
        });

        // Setup OnTouchListners should the user touch and modify the input fields
        nameET.setOnTouchListener(mOnTouchListener);
        mQuantityET.setOnTouchListener(mOnTouchListener);
        mPriceET.setOnTouchListener(mOnTouchListener);
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
                 // If the product information have been saved, exit this activity
                 if (saveProduct()) {
                     finish();
                 }
                return true;

            case android.R.id.home:
                /* If the product hasn't been modified, continue navigating up
                   parent activity.
                 */
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise ask user to confitm discarding their changes on the product
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                /* Navigate up to parent activity when user clicks
                                   "Discard" button
                                 */
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return  true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        /* Otherwise if there are unsaved changes, setup a dialog to warn the user.
           Create a click listener to handle the user confirming that changes should be
           discarded.
         */
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }};

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Adds a new product or updates an existing product.
     *
     * @return true if the product has been saved, else returns false.
     */
    private boolean saveProduct() {

        /* If the user hasn't touched nor modified fhe fields, there's nothing to save and
         * return true.
         */
        if (!mProductHasChanged) {
            return true;
        }

        // Get the user inputs
        String nameString = nameET.getText().toString().trim();
        String quantityString = mQuantityET.getText().toString().trim();
        String priceString = mPriceET.getText().toString().trim();

        /* if some of the field inputs are empty, show Toast warning message to the user
         * and return false */
        if (mImageBitmap == null || nameString.isEmpty() || quantityString.isEmpty()
                || priceString.isEmpty()) {
            Toast.makeText(this, getString(R.string.editor_warning_empty_fields), Toast.LENGTH_LONG).show();
            return false;
        }

        // Flag whether the product has been saved or not
        boolean isProductSaved = false;

        int quantity = Integer.parseInt(quantityString);
        Double price = Double.parseDouble(priceString);

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        if (mImageBitmap != null) {
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE,
                    Utils.bitmapToByteArray(mImageBitmap));
        }

        if (currentProductUri == null) {
            // Add a new product
            isProductSaved = insertProduct(this, ProductEntry.CONTENT_URI, values)
                    != null;
        } else {
            // Update an existing product
            int rowsUpdated = updateProduct(this, currentProductUri, values);
            isProductSaved = rowsUpdated > 0;
        }

        return isProductSaved;
    }

    /**
     * Show an {@link AlertDialog} asking the user to confirm the product deletion.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct(EditorActivity.this, currentProductUri);
                // Close the activity
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
     * Shows a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_discard_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_keep_editing,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Dialog asking the user for a number to imcrease the productQuantity by.
     * If input is empty, number is taken as a 0.
     *
     */
    private void showIncreaseQuantityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setMessage(getString(R.string.editor_increase_quantity_dialog_msg));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        int inputQuantity = Utils.stringToInt(text);
                        int quantity = Utils.stringToInt(mQuantityET.getText().toString());
                        quantity += inputQuantity;
                        mQuantityET.setText(String.valueOf(quantity));
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

   private void showDecreaseQuantityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setMessage(getString(R.string.editor_decrease_quantity_dialog_msg));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        int inputQuantity = Utils.stringToInt(text);
                        int quantity = Utils.stringToInt(mQuantityET.getText().toString());
                        if (inputQuantity <= quantity) {
                            quantity -= inputQuantity;
                            mQuantityET.setText(String.valueOf(quantity));
                        } else {
                            Toast.makeText(EditorActivity.this,
                                EditorActivity.this.getString(R.string.editor_decrease_quantity_is_nok_msg,
                                quantity), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showOrderDialog() {
        String[] choice = {"Sms", "Email"};
        AlertDialog.Builder builder = new  AlertDialog.Builder(this);
        builder.setTitle("Order by");
        builder.setItems(choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    switch (i) {
                        case 0:
                            smsOrderProduct();
                            break;
                        case 1:
                            emailOrderProduct();
                            break;
                    }
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * To compose a sms for ordering the current product
     */
    private void smsOrderProduct() {
        String name = nameET.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.editor_warning_empty_name_field),
                    Toast.LENGTH_LONG).show();
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getString(R.string.editor_order_intro_text)).append("\n\n")
                .append(getString(R.string.product_name)).append(" ").append(name).append("\n")
                .append(getString(R.string.product_quantity)).append(" ");


        String quantity = mQuantityET.getText().toString().trim();
        if (!quantity.isEmpty()) {
            stringBuilder.append(quantity);
        }

        stringBuilder.append("\n").append(getString(R.string.product_price)).append(" ");

        String price = mPriceET.getText().toString().trim();
        if (!price.isEmpty()) {
            stringBuilder.append(price);
        }

        stringBuilder.append("\n\n");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("sms_body", stringBuilder.toString());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void emailOrderProduct() {
        String name = nameET.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.editor_warning_empty_name_field),
                    Toast.LENGTH_LONG).show();
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getString(R.string.editor_order_intro_text)).append("\n\n")
                .append(getString(R.string.product_name)).append(" ").append(name).append("\n")
                .append(getString(R.string.product_quantity)).append(" ");


        String quantity = mQuantityET.getText().toString().trim();
        if (!quantity.isEmpty()) {
            stringBuilder.append(quantity);
        }

        stringBuilder.append("\n").append(getString(R.string.product_price)).append(" ");

        String price = mPriceET.getText().toString().trim();
        if (!price.isEmpty()) {
            stringBuilder.append(price);
        }

        stringBuilder.append("\n\n");

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.editor_order_subject_text));
        intent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
                currentProductUri,    // Content URI of the relevent product
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
            nameET.setText(name);
            mQuantityET.setText(String.valueOf(quantity));
            mPriceET.setText(String.format("%.2f%n", price));

            /* If the existing product has a photo, display its photo.
             * If not, display a add photo camera productImage.
             */
            if (image != null && image.length != 0) {
                mImageBitmap = Utils.byteArrayToBitmap(image);
                mImageView.setImageBitmap(mImageBitmap);
            } else {
                Bitmap addPhotoBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_add_a_photo_gray);
                mImageView.setImageBitmap(addPhotoBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameET.setText("");
        mQuantityET.setText("");
        mPriceET.setText("");
        mImageView.setVisibility(View.INVISIBLE);
    }

    /**
     * Invokes an intent to capture a photo.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Gets a photo thumbnail
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mImageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(mImageBitmap);
        }
    }
}
