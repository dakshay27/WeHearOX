package com.wehear.ox;

import android.app.Activity;
import android.os.Handler;
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

public class DownloadLanguageAdapter extends RecyclerView.Adapter<DownloadLanguageAdapter.DownloadLanguageAdapterViewHolder> {
    Activity activity;
    List<DowloadLanguageModel> dowloadLanString;

    public DownloadLanguageAdapter(Activity activity, List<DowloadLanguageModel> dowloadLanString) {
        this.activity = activity;
        this.dowloadLanString = dowloadLanString;
    }

    @NonNull
    @Override
    public DownloadLanguageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_languages_dowload, parent, false);
        return new DownloadLanguageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadLanguageAdapterViewHolder holder, final int position) {
        final DowloadLanguageModel dowloadLanguageModel = dowloadLanString.get(position);
        holder.txtLanguageName.setText(dowloadLanguageModel.getLanguageName());
        if (dowloadLanguageModel.isProcess()) {
            holder.imgDowload.setImageResource(R.drawable.dowload_pause);
        } else {
            holder.imgDowload.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
        }
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
        if (!dowloadLanguageModel.isProcess()) {
            final int[] click = {1};
            holder.imgDowload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (click[0] == 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (click[0] == 0) {
                                    click[0] = 1;
                                    holder.imgDowload.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
                                }
                            }
                        }, 5000);
                        holder.imgDowload.setImageResource(R.drawable.ic_baseline_delete_24);
                        click[0] = 0;
                    } else if (click[0] == 0) {
                        click[0] = 1;
                        ((ClickLanguages) activity).clickDeleteLanguages(position);
                    }
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return dowloadLanString.size();
    }

    class DownloadLanguageAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView txtLanguageName;
        ImageView imgFav, imgDowload;

        public DownloadLanguageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLanguageName = itemView.findViewById(R.id.txt_language_name);
            imgFav = itemView.findViewById(R.id.img_language_star);
            imgDowload = itemView.findViewById(R.id.img_language_dowload);
            imgDowload.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
        }
    }
}
