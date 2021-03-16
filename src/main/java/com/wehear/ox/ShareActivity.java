package com.wehear.ox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShareActivity extends AppCompatActivity {
    TextInputEditText mCode;
    private static final String TAG = "ShareActivity";
    TextInputLayout textInputCustomEndIcon;
    Button shareButton;
    TextView noOfReferraltext;
    TextView referralAmount;
    ImageView refer;
    Button redeembtn;
    ImageView rinfo;
    private static String code;
    private static final int MAX_STEP = 5;
    Button btnNext;
    ViewPager viewPager;
    ShareActivity.MyViewPagerAdapter myViewPagerAdapter;
    static Dialog dialog;
    ImageView bckbtn;
    //    private String about_title_array[] = {
//            "1",
//            "2",
//            "3",
//            "4",
//            "5"
//    };
    private String about_description_array[] = {
            "Use your Wehear OX bonephonesfor atleast 100 minutes to be part of this program.",
            "Join the program and Share your unique code via social-media to your friends and loved ones.",
            "When your friends buy their device, they have to use your promo code at checkout and get 50% off.",
            "At the time of device setup with WeHear OX app, they have to add your referral code",
            "On successful referral it will reflect In your profile and you will get 1000 INR money in account. You can redeem your money."
    };
    private int about_images_array[] = {
            R.drawable.refer1,
            R.drawable.refer2,
            R.drawable.refer3,
            R.drawable.refer4,
            R.drawable.refer5
    };

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private CollectionReference mAllUserRef;
    private FirebaseFirestore mFirebaseFirestore;
    private CollectionReference mReferTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        View _mw = getLayoutInflater().inflate(R.layout.activity_share,null);
        setContentView(_mw);
        bckbtn=findViewById(R.id.btn_back6);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Log.d(TAG, "onCreate: Created");

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        if (isFirstRun) {
            showCustomDialog();
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();

        mCode = findViewById(R.id.et_referCode);
        redeembtn = findViewById(R.id.btn_redeem);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        rinfo = findViewById(R.id.img_rinfo);
        referralAmount=findViewById(R.id.tv_noofcredits);
        //refer = findViewById(R.id.refer);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mAllUserRef = mFirebaseFirestore.collection("Users");
        noOfReferraltext = findViewById(R.id.tv_noOfReferral);
        mAllUserRef.document(mUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            code = documentSnapshot.getString("coderefer");
                            mCode.setText(code);

                            Log.d(TAG, "onSuccess: " + code);
                            mReferTable = mFirebaseFirestore.collection("Referral");
                            mReferTable.document(code).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()){
                                                Long noOfReferrals = (Long) documentSnapshot.get("noOfReferral");
                                                noOfReferraltext.setText(Long.toString(noOfReferrals));
                                                referralAmount.setText(Long.toString(1000*noOfReferrals));
                                            }
                                        }
                                    });
                        }
                    }
                });

        rinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });

        textInputCustomEndIcon = findViewById(R.id.etlayout_custom_end_icon);
        textInputCustomEndIcon.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String string = mCode.getText().toString();
                ClipData myClip = ClipData.newPlainText("text",string);
                clipboard.setPrimaryClip(myClip);
                Toast.makeText(ShareActivity.this, "Copied : " + string  ,Toast.LENGTH_SHORT).show();
            }
        });






        shareButton = findViewById(R.id.btn_material_icon);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = mCode.getText().toString();
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT,"SUBJECT");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey, get your first WeHear OX Bonephones and get 50% discount, use my referral code '"+ code + "' at the time of checkout from: https://www.wehear.in/product-page/wehear-bonephones");
                shareIntent.setType("text/plain");
                startActivity(shareIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.share_info :
                showCustomDialog();
//                startActivity(new Intent(ShareActivity.this, ShareWorksActivity.class));
                break;
            default :
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCustomDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_share_stepper);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        viewPager = (ViewPager) dialog.findViewById(R.id.view_pager);
        btnNext = dialog.findViewById(R.id.btn_next);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        myViewPagerAdapter = new ShareActivity.MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);

        bottomProgressDots(0);

        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < MAX_STEP) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    //finish();
                    dialog.dismiss();
                }
            }
        });

//        ((ImageButton)findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//                dialog.dismiss();
//                Intent i = new Intent(getApplicationContext() , ProfileActivity.class);
//                startActivity(i);
//            }
//        });

//        ((AppCompatButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), ((AppCompatButton) v).getText().toString() + " Clicked", Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void bottomProgressDots(int current_index) {
        LinearLayout dotsLayout = (LinearLayout) dialog.findViewById(R.id.layoutDots);
        ImageView[] dots = new ImageView[MAX_STEP];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            //int width_height = 40;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //params.setMargins(5, 5, 5, 5);
            params.setMargins(5, 0, 5, 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.unselected);
            //dots[i].setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            dotsLayout.addView(dots[i]);
            //dotsLayout.bringToFront();
        }

        if (dots.length > 0) {
            dots[current_index].setImageResource(R.drawable.selected);
            //dots[current_index].setColorFilter(getResources().getColor(R.color.pink), PorterDuff.Mode.SRC_IN);
        }
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            bottomProgressDots(position);

            if (position == about_description_array.length - 1) {
                btnNext.setText(getString(R.string.GOT_IT));
                btnNext.setBackgroundResource(R.drawable.buttongradient);
                btnNext.setTextColor(Color.WHITE);

            } else {
                btnNext.setText(getString(R.string.NEXT));
                btnNext.setBackgroundResource(R.drawable.buttongradient);
                btnNext.setTextColor(Color.WHITE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.share_stepper , container, false);
            ((TextView) view.findViewById(R.id.description)).setText(about_description_array[position]);
            ((ImageView) view.findViewById(R.id.image)).setImageResource(about_images_array[position]);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return about_description_array.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

}