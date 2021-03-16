package com.wehear.ox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.wehear.ox.RecyleAdapter.ProfileAdapter;
import com.wehear.ox.RecyleAdapter.ProfileAdapterModel;

import java.util.ArrayList;
import java.util.List;

public class ProfileSetting extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<ProfileAdapterModel> profileAdapterModels;
    private ProfileAdapter profileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);
        recyclerView=findViewById(R.id.recycler_all_setting);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        profileAdapterModels=new ArrayList<>();
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_icon_ionic_ios_add,"Register new Wehear OX"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_run_background,"Run in background"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_padlock,"Privacy Policy"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_surface1,"Product Warranty"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_icon_ionic_ios_help_circle_outline,"Help"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_ox_icon,"My Wehear OX"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_icon_feather_info,"About"));
        profileAdapterModels.add(new ProfileAdapterModel(R.drawable.ic_profile_setting_icon_material_system_update_alt,"Check For Update"));
        profileAdapter=new ProfileAdapter(this,profileAdapterModels);
        recyclerView.setAdapter(profileAdapter);
    }
}