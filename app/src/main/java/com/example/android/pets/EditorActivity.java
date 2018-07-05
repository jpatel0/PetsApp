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
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mNameEditText;

    private EditText mBreedEditText;

    private EditText mWeightEditText;

    private Spinner mGenderSpinner;

    private int mGender = 0;
    private Uri uri;

    private boolean isPetChanged=false;

    private View.OnTouchListener mTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(!isPetChanged)
                isPetChanged=true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        uri=getIntent().getData();
        if(uri==null)
            setTitle("Add a Pet");
        else {
            setTitle(R.string.editor_activity_title_edit_pet);
            getLoaderManager().initLoader(2,null,this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection=new String[]{PetEntry._ID,PetEntry.COLUMN_NAME,PetEntry.COLUMN_BREED,PetEntry.COLUMN_GENDER,PetEntry.COLUMN_WEIGHT};
        return new CursorLoader(this,uri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() == 0)
            return;
        cursor.moveToNext();
        mNameEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_NAME)));
        mBreedEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_BREED)));
        mWeightEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_WEIGHT)));

        switch (cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_GENDER))) {
            case "Male":
                mGenderSpinner.setSelection(1);
                break;
            case "Female":
                mGenderSpinner.setSelection(2);
                break;
            default:
                mGenderSpinner.setSelection(0);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);

    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }


    private void savePet(){
        String name=mNameEditText.getText().toString().trim();
        String breed=mBreedEditText.getText().toString().trim();
        String gender=mGenderSpinner.getSelectedItem().toString();
        String weight=mWeightEditText.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this,"Name field is empty",Toast.LENGTH_SHORT).show();
        }

        else if(mGenderSpinner.getSelectedItem().equals(getString(R.string.gender_unknown))){
            Toast.makeText(this,"Select a gender",Toast.LENGTH_SHORT).show();
        }

        else {
            if(TextUtils.isEmpty(weight))
                weight="0";
            ContentValues values = new ContentValues();
            values.put(PetEntry.COLUMN_NAME, name);
            values.put(PetEntry.COLUMN_BREED, breed);
            values.put(PetEntry.COLUMN_GENDER, gender);
            values.put(PetEntry.COLUMN_WEIGHT, weight);

            if (uri == null) {
                long id = ContentUris.parseId(getContentResolver().insert(PetEntry.CONTENT_URI, values));
                if (id == -1) {
                    Toast.makeText(this, "error in entering data", Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowUpdated = getContentResolver().update(uri, values, null, null);

            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.action_save:
                savePet();
                return true;

            case R.id.action_delete:
                if(uri!=null)
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if(!isPetChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardListener=new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChanges(discardListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
                finish();
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
        int rowDeleted=getContentResolver().delete(uri,null,null);
        if(rowDeleted<=0){

           Toast.makeText(EditorActivity.this,R.string.editor_delete_pet_failed,Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(EditorActivity.this,R.string.editor_delete_pet_successful,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if(!isPetChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardListener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChanges(discardListener);

    }

    private void showUnsavedChanges(DialogInterface.OnClickListener discardListener){

        AlertDialog.Builder alertBuilder=new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.dialog_message);
        alertBuilder.setPositiveButton(R.string.discard_changes_button,discardListener);
        alertBuilder.setNegativeButton(R.string.keep_editing_button,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface!=null){
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alert=alertBuilder.create();
        alert.show();
    }
}