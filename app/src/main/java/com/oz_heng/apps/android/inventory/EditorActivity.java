package com.oz_heng.apps.android.inventory;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by Pack Heng on 12/05/17
 * pack@oz-heng.com
 */

public class EditorActivity extends AppCompatActivity {

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProducttUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProducttUri = intent.getData();

        if (mCurrentProducttUri != null) {
            long id = ContentUris.parseId(mCurrentProducttUri);

            Toast.makeText(this, "Id: " + id + "\n" +
                    "Uri: " + mCurrentProducttUri.toString(), Toast.LENGTH_LONG).show();
        }
    }

    // TODO EditorActivity

}
