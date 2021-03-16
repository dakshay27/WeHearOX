package com.wehear.ox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.request.RequestOptions;
import com.wehear.ox.AppModel.ViewPagerModel;

public class PhiFragmentViewPager extends Fragment {

    private ImageView imageView;
    private TextView txtData;

    private ViewPagerModel viewPagerModel;

    public static PhiFragmentViewPager getInstance(ViewPagerModel viewPagerModel) {
        PhiFragmentViewPager fragmentViewPager = new PhiFragmentViewPager();

        if (viewPagerModel != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("model", viewPagerModel);
            fragmentViewPager.setArguments(bundle);
        }
        return fragmentViewPager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            viewPagerModel = getArguments().getParcelable("model");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stapper_phi_1, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtData = view.findViewById(R.id.txt_stapper_data);
        imageView = view.findViewById(R.id.img_stapper_data);
        init();
    }

    private void init() {
        if (viewPagerModel != null) {
            txtData.setText(viewPagerModel.getDisplayText());
            imageView.setImageResource(viewPagerModel.getImage());
        }
    }
}
