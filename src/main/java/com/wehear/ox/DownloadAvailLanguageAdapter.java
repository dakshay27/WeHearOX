package com.wehear.ox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wehear.ox.AppModel.DowloadLanguageModel;
import com.wehear.ox.Interfaces.ClickLanguages;

import java.util.List;

public class DownloadAvailLanguageAdapter extends RecyclerView.Adapter<DownloadAvailLanguageAdapter.DownloadAvailLanguageAdapterViewHolder> {
    Activity activity;
    List<DowloadLanguageModel> listLanguage;

    public DownloadAvailLanguageAdapter(Activity activity, List<DowloadLanguageModel> listLanguage) {
        this.activity = activity;
        this.listLanguage = listLanguage;
    }

    @NonNull
    @Override
    public DownloadAvailLanguageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_languages_dowload, parent, false);
        return new DownloadAvailLanguageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadAvailLanguageAdapterViewHolder holder, final int position) {
        final DowloadLanguageModel dowloadLanguageModel=listLanguage.get(position);
        holder.txtLanguageName.setText(dowloadLanguageModel.getLanguageName());

        if(dowloadLanguageModel.isFav()){
            holder.imgFav.setImageResource(R.drawable.ic_baseline_star_24);
        }else{
            holder.imgFav.setImageResource(R.drawable.ic_baseline_star_border_24);
        }
        holder.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dowloadLanguageModel.isFav()) {
                    dowloadLanguageModel.setFav(false);
                    notifyDataSetChanged();
                    ((ClickLanguages) activity).clickToDeleteFav(dowloadLanguageModel.getLanguageName());
                }else{
                    dowloadLanguageModel.setFav(true);
                    notifyDataSetChanged();
                    ((ClickLanguages)activity).clickToInsertFav(dowloadLanguageModel.getLanguageName());
                }
            }
        });
        holder.imgDowload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.custom_dialog_dowload_language);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                TextView cancelbtn2 = dialog.findViewById(R.id.txt_cancel_custom_dailog);
                TextView download=dialog.findViewById(R.id.txt_download_custom_dailog);
                TextView txtLanguage=dialog.findViewById(R.id.txt_langauge_name_custom_dialog);
                txtLanguage.setText(dowloadLanguageModel.getLanguageName());
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        ((ClickLanguages)activity).clickDowloadLanguages(position);
                        dowloadLanguageModel.setFav(true);
                        notifyDataSetChanged();
                        ((ClickLanguages)activity).clickToInsertFav(dowloadLanguageModel.getLanguageName());
                    }
                });
                cancelbtn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return listLanguage.size();
    }

    class DownloadAvailLanguageAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView txtLanguageName;
        ImageView imgFav, imgDowload;

        public DownloadAvailLanguageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLanguageName = itemView.findViewById(R.id.txt_language_name);
            imgFav = itemView.findViewById(R.id.img_language_star);
            imgDowload = itemView.findViewById(R.id.img_language_dowload);
            imgDowload.setImageResource(R.drawable.dowload_arrow);
        }
    }
}
