/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    private PetDbHelper mdbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        mdbHelper=new PetDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        // Create and/or open a database to read from it
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        String[] projection={PetEntry._ID,PetEntry.COLUMN_NAME,PetEntry.COLUMN_GENDER,PetEntry.COLUMN_WEIGHT};

        Cursor cursor=db.query(PetEntry.TABLE_NAME,projection,null,null,null,null,null);

        TextView displayView=(TextView) findViewById(R.id.text_view_pet);
        displayView.setText("Number of rows in pets database table: " + cursor.getCount());

        String id,name,gender,weight;
        try {
            while (cursor.moveToNext()){
                id=cursor.getString(cursor.getColumnIndex(PetEntry._ID));
                name=cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_NAME));
                gender=cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_GENDER));
                weight=cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_WEIGHT));
                displayView.append("\n" + id + "\t" + name + "\t" + gender + "\t" + weight );
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }


    public void insertPet(){
        ContentValues values =new ContentValues();
        values.put(PetEntry.COLUMN_NAME,"zero");
        values.put(PetEntry.COLUMN_BREED,"human");
        values.put(PetEntry.COLUMN_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_WEIGHT,"50");


        SQLiteDatabase db=mdbHelper.getWritableDatabase();
        long id=db.insert(PetEntry.TABLE_NAME,null,values);
        if(id==-1){
            Toast.makeText(this,"Unable to enter data",Toast.LENGTH_SHORT).show();
        }
        displayDatabaseInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
