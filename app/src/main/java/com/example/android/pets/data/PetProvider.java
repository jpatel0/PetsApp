package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import com.example.android.pets.data.PetContract.PetEntry;

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDbHelper mDbHelper;
    private static final int PET_CODE=100;
    private static final int PETID_CODE=101;

    private static UriMatcher uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PET,PET_CODE);
        uriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PET+"/#",PETID_CODE);
    }



    @Override
    public boolean onCreate() {
        mDbHelper=new PetDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {

        Cursor cursor;
        int match= uriMatcher.match(uri);

        SQLiteDatabase db=mDbHelper.getReadableDatabase();

        switch (match){

            case PET_CODE:
                cursor=db.query(PetEntry.TABLE_NAME,strings,s,strings1,null,null,s1);
                break;

            case PETID_CODE:

                s=PetEntry._ID+"=?";
                strings1=new String[] { String.valueOf(ContentUris.parseId(uri))};
                cursor=db.query(PetEntry.TABLE_NAME,strings,s,strings1,null,null,s1);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI::" + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(),PetEntry.CONTENT_URI);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PET_CODE:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETID_CODE:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match=uriMatcher.match(uri);

        switch(match){
            case PET_CODE:
                uri=insertPet(uri,contentValues);
                break;

            default:
                throw new IllegalArgumentException("Cannot insert through Uri::"+uri);
        }


        return uri;
    }

    private Uri insertPet(Uri uri,ContentValues values){

        if(values.getAsString(PetEntry.COLUMN_NAME)==null)
            throw new IllegalArgumentException("Pet requires a name");

        if(values.getAsString(PetEntry.COLUMN_GENDER)==null)
            throw new IllegalArgumentException("Pet requires a gender attribute");

        if(values.getAsString(PetEntry.COLUMN_WEIGHT)==null || Integer.parseInt(values.getAsString(PetEntry.COLUMN_WEIGHT))<0)
            throw new IllegalArgumentException("Invalid Weight");

        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        long id=db.insert(PetEntry.TABLE_NAME,null,values);
        if(id>0)
        {
            getContext().getContentResolver().notifyChange(PetEntry.CONTENT_URI,null);
        }
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowDeleted=0;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PET_CODE:
                // Delete all rows that match the selection and selection args
                rowDeleted=database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PETID_CODE:
                // Delete a single row given by the ID in the URI
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowDeleted=database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if(rowDeleted>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        if(values.getAsString(PetEntry.COLUMN_NAME)==null)
            throw new IllegalArgumentException("Pet requires a name");

        if(values.getAsString(PetEntry.COLUMN_GENDER)==null)
            throw new IllegalArgumentException("Pet requires a gender attribute");

        if(values.getAsString(PetEntry.COLUMN_WEIGHT)==null || Integer.parseInt(values.getAsString(PetEntry.COLUMN_WEIGHT))<0)
            throw new IllegalArgumentException("Invalid Weight");

        final int match = uriMatcher.match(uri);
        int rowUpdated=0;
        switch (match) {
            case PET_CODE:
                rowUpdated=updatePet(uri, values, selection, selectionArgs);
                break;
            case PETID_CODE:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowUpdated=updatePet(uri, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
        if (rowUpdated>0)
            getContext().getContentResolver().notifyChange(uri,null);
        return rowUpdated;
    }

    private int updatePet(Uri uri,ContentValues values,String selection,String[] selectionArgs){
        if (values.containsKey(PetEntry.COLUMN_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetEntry.COLUMN_GENDER)) {
            String gender = values.getAsString(PetEntry.COLUMN_GENDER);
            if (gender == null) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetEntry.COLUMN_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = Integer.parseInt(values.getAsString(PetEntry.COLUMN_WEIGHT));
            if (weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        return db.update(PetEntry.TABLE_NAME,values,selection,selectionArgs);
    }
}
