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
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;
import com.oz_heng.apps.android.inventory.product.ProductCursorAdapter;
import com.oz_heng.apps.android.inventory.helper.Utils;

/**
 * Created by Pack Heng on 12/05/17
 * pack@oz-heng.com
 */

/*
  TODO: Option to delete all the products in the database
  DONE: Fits weill picture in the ImageView
  TODO: Don't Catch Generic Exception
 */

public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;
    private ProductCursorAdapter mProductCursorAdapter;

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

        /* Attach the cursor adapter to the ListView */
        ListView productlistView = (ListView) findViewById(R.id.catalog_list_view);
        mProductCursorAdapter = new ProductCursorAdapter(
                this,
                null    // There is no product data yet (until the loader finishes) so pass
                        // in null for the Cursor
        );
        productlistView.setAdapter(mProductCursorAdapter);

        /* Find and set empty view on the ListView, so that it only shows when the list has 0 items */
        View emptyView = findViewById(R.id.catalog_empty_view);
        productlistView.setEmptyView(emptyView);

        /* Setup the list item click listener*/
        productlistView.setOnItemClickListener(
          new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                      long id) {
                  Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                  Toast.makeText(CatalogActivity.this, "Id: " + id + "\n" +
                          "Uri: " + uri.toString(), Toast.LENGTH_SHORT).show();

                  /* Start {@link EditorActivity} with the URI as the data field of
                    the intent */
                  Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                  intent.setData(uri);
                  startActivity(intent);
              }
          }
        );

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
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Android 1");
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
                this,                       // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,                 // The columns to include in the resulting cursor
                null,                       // No selection criteria
                null,                       // No selection arguments
                null                        // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Update {@link ProductCursorAdapter} with new cursor containing updated product data
        mProductCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mProductCursorAdapter.swapCursor(null);
    }

    // TODO: Complete CatalogActivity
}
