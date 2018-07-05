package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameText=(TextView) view.findViewById(R.id.nameText);
        TextView breedText=(TextView) view.findViewById(R.id.breedText);

        String name=cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_NAME));
        String breed=cursor.getString(cursor.getColumnIndex(PetContract.PetEntry.COLUMN_BREED));

        nameText.setText(name);
        if(TextUtils.isEmpty(breed))
            breed=context.getString(R.string.unknown_breed);
        breedText.setText(breed);
    }
}
