package com.oz_heng.apps.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.helper.Utils;
import com.oz_heng.apps.android.inventory.product.ProductAdapter;
import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;

/*
  TODO: Handle empty view.
  TODO: Option to delete all the products in the database.
  DONE: Fits weill picture in the ImageView.
  TODO: Don't Catch Generic Exception.
  TODO: Complete CatalogActivity
 */

/**
 * Activity listing products that have been saved in the database.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup Add Button to open EditorActivity
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.catalog_plus_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Create recycler view.
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.catalog_recycler_view);
        recyclerView.setHasFixedSize(true);
        // Create an adpater and connect it with the recycler view.
        productAdapter = new ProductAdapter(this);
        recyclerView.setAdapter(productAdapter);
        // Give the recycler view a default layout manager.
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Adding inbuilt divider line.
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));

        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.catalog_action_insert_dummy_pet:
                insertDummyProduct1();
                return true;
            case R.id.catalog_action_delete_all_pets:
                // Todo: delete all pet entries
                insertDummyProduct2();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging
     * purposes only.
     */
    private void insertDummyProduct1() {

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Android");
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 10);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 1.0);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, Utils.bitmapToByteArray(bitmap));

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        if (uri != null) {
            Toast.makeText(this, getString(R.string.editor_save_product_successful_with_id) + id, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_save_product_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging
     * purposes only.
     */
    private void insertDummyProduct2() {

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Android 2");
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 20);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 2.0);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, Utils.bitmapToByteArray(bitmap));

        Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        long id = ContentUris.parseId(uri);

        if (uri != null) {
            Toast.makeText(this, getString(R.string.editor_save_product_successful_with_id) + id, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_save_product_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(
                this,                // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,                 // The columns to include in the resulting cursor
                null,               // No selection criteria
                null,            // No selection arguments
                null                // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update the adapter with new cursor containing updated product data.
        productAdapter.setData(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productAdapter.setData(null);
    }
}
