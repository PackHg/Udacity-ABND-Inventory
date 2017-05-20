package com.oz_heng.apps.android.inventory.product;

/**
 * Created by Pack Heng on 12/05/17
 * pack@oz-heng.com
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.oz_heng.apps.android.inventory.product.ProductContract.ProductEntry;

/**
 * Database helper for Inventory app. Manages database creation and version management.
 */
public class ProductDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /** String constants for SQL keywords */
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String DROP_TABLE = "DROP TABLE";
    private static final String IF_EXISTS = " IF EXISTS ";
    private static final String TEXT = " TEXT";
    private static final String INTEGER = " INTEGER";
    private static final String REAL = " REAL";
    private static final String BLOB = " BLOB";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String AUTOINCREMENT = " AUTOINCREMENT";
    private static final String NULL = " NULL";
    private static final String NOT_NULL = " NOT NULL";
    private static final String DEFAULT = " DEFAULT ";
    private static final String COMMA_SEP = ", ";

    /** String constant for the SQL statement to create the products table */
    private static final String SQL_CREATE_ENTRIES =
            CREATE_TABLE + ProductEntry.TABLE_NAME + " (" +
                    ProductEntry._ID + INTEGER + PRIMARY_KEY + AUTOINCREMENT + COMMA_SEP +
                    ProductEntry.COLUMN_PRODUCT_NAME + TEXT + NOT_NULL + COMMA_SEP +
                    ProductEntry.COLUMN_PRODUCT_QUANTITY + INTEGER + NOT_NULL + DEFAULT + "0" + COMMA_SEP +
                    ProductEntry.COLUMN_PRODUCT_PRICE + REAL + NOT_NULL + DEFAULT + "0" + COMMA_SEP +
                    ProductEntry.COLUMN_PRODUCT_IMAGE + BLOB + DEFAULT + NULL + ");";

    /** String constant for the SQL statement to delete the products table */
    private static final String SQL_DELETE_ENTRIES = DROP_TABLE + IF_EXISTS + ProductEntry.TABLE_NAME;


    /**
     * Constructs a new instance of {@link ProductDbHelper}.
     *
     * @param context of the app
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
