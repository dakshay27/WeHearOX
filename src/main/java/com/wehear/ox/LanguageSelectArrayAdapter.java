package com.wehear.ox;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.common.io.Resources;

import java.util.HashMap;
import java.util.List;

public class LanguageSelectArrayAdapter extends ArrayAdapter<String> {
    private Context ctx;
    private List<String> languageName;
    private HashMap<String, Boolean> fav;
    private HashMap<String, Boolean> dowload;

    public LanguageSelectArrayAdapter(@NonNull Context context, int resource, List<String> languageName, HashMap<String, Boolean> fav, HashMap<String, Boolean> dowload) {
        super(context, resource, R.id.txt_language_name, languageName);
        this.ctx = context;
        this.languageName = languageName;
        this.fav = fav;
        this.dowload = dowload;
    }


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int pos, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.custom_spinner_layout, parent, false);
       TextView textView = row.findViewById(R.id.txt_language_name);
        textView.setText(languageName.get(pos));
       if(dowload.get(languageName.get(pos)) || fav.get(languageName.get(pos))){
            textView.setTextColor(ContextCompat.getColor(getContext(),R.color.pink));
        }
        return row;
    }
}
