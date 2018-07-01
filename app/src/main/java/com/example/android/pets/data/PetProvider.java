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

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
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
        SQLiteDatabase db=mDbHelper.getWritableDatabase();
        long id=db.insert(PetEntry.TABLE_NAME,null,values);
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
