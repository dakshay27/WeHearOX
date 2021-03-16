package com.wehear.ox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static  Long PHI = 0L ;
    private final int PICK_IMAGE_REQUEST = 11;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private FirebaseStorage mFirebaseStorage;

    private StorageReference myReference;
    private Uri imageUri;
    TextView name,username,minutesTv,phirank,globalrank,global,friendsrank,friends,listfriends,referandearn;
    ImageView friendslistimage,share_button;
    CircularImageView mChangeImage;
    LinearLayout mShare,mFriends;
    Button logout;
    CustomProgressBar customProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(getApplicationContext());

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();
        customProgressBar= new CustomProgressBar(this);
        phirank = findViewById(R.id.tv_phirank);
        globalrank = findViewById(R.id.tv_globalrank);
        global = findViewById(R.id.tv_global);
        friendsrank = findViewById(R.id.tv_friendsrank);
        friends = findViewById(R.id.tv_friends);
        listfriends = findViewById(R.id.tv_listfriends);
        referandearn = findViewById(R.id.tv_referandearn);
        mChangeImage = findViewById(R.id.img_change);
        friendslistimage = findViewById(R.id.img_friendslist);
        share_button = findViewById(R.id.img_share_button);
        name = findViewById(R.id.tv_name);
        username = findViewById(R.id.tv_username);
        logout =  findViewById(R.id.btn_logout);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.profile);
        bottomNavigationView.setItemIconTintList(null);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.features :
                        startActivity(new Intent(getApplicationContext(),FeaturesActivity.class));
                        overridePendingTransition(0,0);

                    case R.id.ox :
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);

                    case R.id.phi :
                        startActivity(new Intent(getApplicationContext(),phiActivity.class));
                        overridePendingTransition(0,0);

                    case R.id.profile :
                        return true;
                }

                return false;
            }
        });

        mFirebaseStorage = FirebaseStorage.getInstance();
        myReference = mFirebaseStorage.getReference().child("images").child(mUser.getUid());

        final SharedPreferences minutes = getSharedPreferences("minutes", 0);
        final int usedMinute = minutes.getInt("mWithOx",0);

        Log.d(TAG, "onCreate: "+ usedMinute);

        String i = Integer.toString(usedMinute);


        logout = findViewById(R.id.btn_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(ProfileActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("LOGOUT")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAuth.signOut();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        mShare = findViewById(R.id.linear_share);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext() , ShareActivity.class);
                startActivity(i);
            }
        });

        mFriends = findViewById(R.id.linear_friends);
        mFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(getApplicationContext() , FriendsActivity.class);
                startActivity(in);
            }
        });

        fetchingDetails();
    }

    private String getRank() {

        final int[] currentUser_phi = new int[1];
        final int[] temp = {0};
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int[] counter = new int[101];
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData().get("phi"));
                                if (mUser.getUid() == document.getId()) {
                                    currentUser_phi[0] = Math.round(Float.parseFloat(String.valueOf(document.getData().get("phi"))));
                                }
                                if(document.getData().get("phi")!= null){

                                    int user_phi = Math.round(Float.parseFloat(String.valueOf(document.getData().get("phi"))));

                                    if(user_phi<=0) {
                                        counter[0]++;
                                    }
                                    else if (user_phi>=100){
                                        counter[100]++;
                                    }else {
                                        counter[user_phi]++;
                                    }

                                }
                            }

                            if (currentUser_phi[0]==-1){
                                currentUser_phi[0]=0;
                            }

                            for (int i=currentUser_phi[0];i<counter.length;i++){
                                if (i==currentUser_phi[0]){
                                    continue;
                                }else {
                                    temp[0] += counter[i];
                                    Log.d(TAG, "counter"+i+": "+counter[i]);
                                }
                            }
                            Log.d(TAG, "temp: "+temp[0]);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        if (temp[0]==0){
            temp[0]= 1;
        }

        globalrank.setText(String.valueOf(temp[0]));
        return String.valueOf(temp[0]);
    }


    private void fetchingDetails() {

        customProgressBar.show();

        DocumentReference dataRef = db.collection("Users").document(mUser.getUid());
        dataRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String fname = documentSnapshot.getString("fname");
                            String lname = documentSnapshot.getString("lname");
                            String uname = documentSnapshot.getString("username");
                            String imageLink = documentSnapshot.getString("imageLink");
                            Log.d(TAG,"Link-"+imageLink);

                            PHI = documentSnapshot.getLong("PHI");
                            Log.d(TAG,"PHI-" + PHI);
//                            name.setText(fname + " " +lname);
//                            username.setText("@"+ uname);



                        }else{
                            Toast.makeText(getApplicationContext(),"No data exits",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(),"Unable to fetch the Details.",Toast.LENGTH_SHORT).show();
                    }
                });
     dataRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
         @Override
         public void onSuccess(DocumentSnapshot documentSnapshot) {
             String imageLink = documentSnapshot.getString("imageLink");
             Log.d(TAG,"Link-"+imageLink);
         }
     });

//        myReference.getDownloadUrl().addOnSuccessListener(
//                new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Log.d(TAG, "onSuccess: " + uri);
//                        imageUri = uri;
//                        Glide.with(getApplicationContext()).load(uri).placeholder(R.drawable.ic_person).into(mChangeImage);
////                Uri newuri = uri.parse(uri.toString());
////                mProfileImage.setImageURI(newuri);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(getApplicationContext(),"Unable to fetch the Details.",Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "onFailure:" );
//            }
//        });
        fetchRank();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            imageUri = UCrop.getOutput(data);
            uploadImage();
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            File tempCrop = new File(getCacheDir(), "tempImage");
            Uri destinationUri = Uri.fromFile(tempCrop);
            UCrop.of(imageUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .start(this);
            mChangeImage.setImageURI(imageUri);
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            mChangeImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (requestCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadImage() {

        if(imageUri != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();


            //StorageReference ref = mStorageRef.child("images/"+ mUser.getUid());
            myReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void fetchRank()
    {
        final int[] count = {0};
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference dataRef = db.collection("Users");
        dataRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for(DocumentSnapshot snapshot:queryDocumentSnapshots.getDocuments())
                {
                    try {
                        {

                            if(PHI<(Long) snapshot.get("PHI"))
                            {
                                count[0]++;
                            }
                        }

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }


            }
        }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                globalrank.setText(Integer.toString(count[0]+1));
                customProgressBar.dismiss();
                db.terminate();
            }
        });


    }

}