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

import android.app.AlertDialog;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    PetCursorAdapter cursorAdapter;

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

        View emptyView =findViewById(R.id.empty_view);
        ListView listView=(ListView) findViewById(R.id.listView);
        listView.setEmptyView(emptyView);

        cursorAdapter=new PetCursorAdapter(this,null);
        listView.setAdapter(cursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View item, int position, long id) {
                Intent editIntent=new Intent();
                editIntent.setClass(getApplicationContext(),EditorActivity.class);
                editIntent.setData(ContentUris.withAppendedId(PetEntry.CONTENT_URI,id));
                startActivity(editIntent);
            }
        });
        getLoaderManager().initLoader(1,null,this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {PetEntry._ID, PetEntry.COLUMN_NAME, PetEntry.COLUMN_BREED};
        return new CursorLoader(this,PetEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    public void insertPet(){
        ContentValues values =new ContentValues();
        values.put(PetEntry.COLUMN_NAME,"jze");
        values.put(PetEntry.COLUMN_BREED,"human");
        values.put(PetEntry.COLUMN_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_WEIGHT,"50");


        Uri insertUri=getContentResolver().insert(PetEntry.CONTENT_URI,values);
        long id=ContentUris.parseId(insertUri);
        if(id==-1){
            Toast.makeText(this,"Unable to enter data",Toast.LENGTH_SHORT).show();
        }
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

        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;

            case R.id.action_delete_all_entries:
                if(cursorAdapter.isEmpty()){
                    Toast.makeText(this,"No pets found to be deleted",Toast.LENGTH_SHORT).show();
                    return true;
                }
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_all_pets_title);
        builder.setMessage(R.string.delete_all_pets_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        // TODO: Implement this method
        int rowDeleted=getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
        if(rowDeleted<=0){

            Toast.makeText(this,R.string.editor_delete_pet_failed,Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this,R.string.editor_delete_pet_successful,Toast.LENGTH_LONG).show();
    }


}
