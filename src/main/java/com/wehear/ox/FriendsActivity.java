package com.wehear.ox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static android.Manifest.permission.READ_CONTACTS;

//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.OnDisconnect;
//import com.google.firebase.database.ValueEventListener;

public class FriendsActivity extends AppCompatActivity {
    int hasDataPermission;
    private static final int MAX_STEP = 1;
    static BottomSheetDialog bottonDialog;
    private CustomViewPager viewPager;
    ImageView imgcancel;
    TextView imgNext;
    MyViewPagerAdapter myViewPagerAdapter;
    private static final int REQUEST_CODE_WRITE_DATA = 2;
    private static final String TAG = "FriendsActivity";
    private String[] text = {"Allow contacts access."};
    private int[] image2 = {R.drawable.ic_permission_contact};
    private String[] des = {
            "Wehear OX needs to use your contacts to find out your friends using WeHear OX devices. You can share and compare PHI with your friends.",
    };
    ProgressDialog dialog;
    ArrayList<String> friendUID = new ArrayList<String>();


    RecyclerView recyclerView;
    ArrayList<User> userArrayList = new ArrayList<User>();


    private DatabaseReference numberRef;
    ArrayList<String> contactNum;
    ArrayList<String> Name;
    ImageView bckbtn;

    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference myReference;
    private RecyclerViewAdapter adapter;
    DocumentReference userRef;
    CustomProgressBar customProgressBar;
    Map<Long, User> userTreeMap = new TreeMap<>();
    boolean firstFunctionResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading contacts...");
        dialog.create();
        contactNum = new ArrayList<String>();
        Name = new ArrayList<String>();
        numberRef = FirebaseDatabase.getInstance().getReference().child("Numbers");
        db = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        customProgressBar = new CustomProgressBar(FriendsActivity.this);
        customProgressBar.show();
        bckbtn = findViewById(R.id.btn_back4);
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapter = new RecyclerViewAdapter(FriendsActivity.this, userArrayList, mUser.getUid());
        recyclerView = findViewById(R.id.recycler_view_friends);
        mFirebaseStorage = FirebaseStorage.getInstance();
        myReference = mFirebaseStorage.getReference().child("images").child("Tzt7woTUSuVUyTThs3xuNCBa66G3");
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        friendUID.add(mUser.getUid());
        checkPermissionGrantOrNot();
    }

    void getContactsDetails() {
        Cursor contacts = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME

        );
        Log.i("Count", Integer.toString(contacts.getCount()));


        final HashMap<String, String> map = new HashMap<>();
        if (contacts != null) {
            while (contacts.moveToNext()) {

                String number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        .replace("+91", "").replaceAll(" ", "").replaceAll("[-\\[\\]^/,'*:.!><~@#$%+=?|\"\\\\()]", "");
                //Log.d(TAG, "onCreate: new contact" + map.size());
                contactNum.add(number);
            }


            Log.i(TAG, "onCreate: Total Contacts - " + contactNum.size());

            contacts.close();
        }
        getInfo(mUser.getUid());

        for (String numbers : contactNum) {


            numberRef.child(numbers).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Object UID = dataSnapshot.getValue();
                    if (UID != null) {
                        if (!friendUID.contains(UID.toString())) {
                            Log.d(TAG, "Uid-" + UID.toString());
                            friendUID.add(UID.toString());
                            getInfo(UID.toString());
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("Status", "Not present");
                }

            });
        }
        final Handler handler = new Handler();
        Runnable info = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                handler.postDelayed(this, 1000);

            }
        };
        handler.postDelayed(info, 1000);
    }

    private void getInfo(String UID) {

//        for(String id:friendUID) {
        final String currentUserId = UID;

        userRef = db.collection("Users").document(UID);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Long phi = new Long(0);
                String url;
                int PHI;
                String fname = documentSnapshot.getString("fname");
                String lname = documentSnapshot.getString("lname");
                String username = documentSnapshot.getString("username");
                String gender = documentSnapshot.getString("gender");
                try {
                    phi = (Long) documentSnapshot.get("phi");
                    PHI = phi.intValue();
                } catch (Exception e) {
                    PHI = 0;
                }


                try {
                    url = documentSnapshot.getString("imageLink");
                } catch (Exception e) {
                    url = "";
                }
                User user = new User(fname, lname, username, url, PHI, currentUserId, gender);
                if (!userArrayList.contains(user))
                    userArrayList.add(user);
            }
        });
//        }

        userRef = db.collection("Users").document(mUser.getUid());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                sortArrayList();
            }
        }, 1000);


    }

    void sortArrayList() {
        Comparator cmp = new UserPhiComparator();
        Collections.sort(userArrayList, cmp);
        adapter.notifyDataSetChanged();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setAdapter();
            }
        }, 1500);


    }

    public void setAdapter() {
        customProgressBar.dismiss();
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_WRITE_DATA:
                if (grantResults.length > 0) {

                    boolean readContacts = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (readContacts) {
                        getContactsDetails();
                        bottonDialog.dismiss();

                    } else {
                        checkPermissionGrantOrNot();
                    }
                }
                break;
        }
    }

    void checkSelfPermission() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{READ_CONTACTS}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_CODE_WRITE_DATA);
            }
        } else {
            getContactsDetails();
        }
    }

    private void showCustomDialog() {
        bottonDialog = new BottomSheetDialog(FriendsActivity.this, R.style.BottomSheetDialogTheme);
        View bottomView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_view_pager_permission, (LinearLayout) findViewById(R.id.linear_bottom_sheet));
        viewPager = (CustomViewPager) bottomView.findViewById(R.id.view_pager_permission);
        viewPager.disableScroll(true);
        imgNext = (TextView) bottomView.findViewById(R.id.btn_next_permission);
        imgcancel = (ImageView) bottomView.findViewById(R.id.img_cancel);
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
//        bottomProgressDots(0);
//        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now = viewPager.getCurrentItem();
                if (now == 0) {
                    checkSelfPermission();
                }
            }
        });
        imgcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < MAX_STEP) {
                    viewPager.setCurrentItem(current);
                } else {
                    bottonDialog.dismiss();
                }
            }
        });
        bottonDialog.setContentView(bottomView);
        bottonDialog.setCancelable(false);
        bottonDialog.show();
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.custom_view_pager_permission_intrusction, container, false);
            ((TextView) view.findViewById(R.id.txt_permission_name)).setText(text[position]);
            ((ImageView) view.findViewById(R.id.img_permission_pic)).setImageResource(image2[position]);
            ((TextView) view.findViewById(R.id.txt_permission_dis)).setText(des[position]);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return text.length;
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

    void checkPermissionGrantOrNot() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            showCustomDialog();
        } else {
            getContactsDetails();
        }
    }
}
