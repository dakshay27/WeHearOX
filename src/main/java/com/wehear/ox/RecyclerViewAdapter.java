package com.wehear.ox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
public Context mContext;
ArrayList<User> userArrayList;
String currentUserID;

    public RecyclerViewAdapter(Context mContext, ArrayList<User> userArrayList, String currentUserID) {
        this.mContext = mContext;
        this.userArrayList = userArrayList;
        this.currentUserID = currentUserID;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_display_layout,
                parent,
                false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.userPosition.setText(Integer.toString(position+1));

        holder.name.setText(userArrayList.get(position).getFname()+" "+userArrayList.get(position).getLname());
        holder.username.setText(userArrayList.get(position).getUsername());
        holder.Phi.setText(Integer.toString(userArrayList.get(position).getPHI()));
        String gender = userArrayList.get(position).getGender();
        String imageLink = userArrayList.get(position).getImageLink();

        int phi = userArrayList.get(position).getPHI();
        if(gender!=null) {


            if (gender.equals("M")) {
                Glide.with(mContext).load(imageLink).placeholder(R.drawable.ic_male_profile).into(holder.image);
            } else if (gender.equals("F")) {
                Glide.with(mContext).load(imageLink).placeholder(R.drawable.ic_female_profile).into(holder.image);


            } else {
                Glide.with(mContext).load(imageLink).placeholder(R.drawable.ic_profile).into(holder.image);

            }
        }
        else
        {
            Glide.with(mContext).load(imageLink).placeholder(R.drawable.ic_profile).into(holder.image);
        }
        if(phi<100) {
            holder.badge.setImageResource(R.drawable.ic_bronze_badge);
        }
        else if(phi<200)
        {
            holder.badge.setImageResource(R.drawable.ic_silver_badge);
        }
        else if(phi<300)
        {
            holder.badge.setImageResource(R.drawable.ic_gold_badge);
        }
        else if(phi<350)
        {
            holder.badge.setImageResource(R.drawable.ic_diamond_badge);
        }

        if(userArrayList.get(position).getCurrentUserId().equals(currentUserID))
        {
            holder.cardView.setBackgroundResource(R.drawable.rounded_corner_current_user);
            holder.name.setText("You");
            SharedPreferences rankPreference = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = rankPreference.edit();
            editor.putInt("friends_rank",position+1);
            editor.apply();

        }
        else {
            holder.name.setText(userArrayList.get(position).getFname() + " " + userArrayList.get(position).getLname());
            holder.cardView.setBackgroundResource(R.drawable.rounded_corner_user);
        }


    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class MyViewHolder extends  RecyclerView.ViewHolder
{
    public TextView name;
    public TextView Phi;
    public TextView username;
    public CircularImageView image;
    public TextView userPosition;
    public ImageView badge;
    public CardView cardView;


    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.user_full_name);
        Phi = (TextView) itemView.findViewById(R.id.user_phi);
        image =(CircularImageView) itemView.findViewById(R.id.user_profile_image);
        username = (TextView) itemView.findViewById(R.id.user_user_name);
        userPosition = (TextView) itemView.findViewById(R.id.user_position);
        badge = (ImageView) itemView.findViewById(R.id.user_badge);
        cardView=(CardView) itemView.findViewById(R.id.user_card_view);

    }
}




}
