package com.wehear.ox;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileActivity";
    private static  Long PHI = 0L ;
    private final int PICK_IMAGE_REQUEST = 11;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore db;
    private FirebaseStorage mFirebaseStorage;
    SharedPreferences rankPreference;

    private StorageReference myReference;
    private Uri imageUri;
    TextView name,username,minutesTv,phirank,globalrank,global,friendsrank,friends,listfriends,referandearn;
    ImageView friendslistimage,share_button;
    CircularImageView mChangeImage;
    LinearLayout mShare,mFriends;
    Button logout,settings;
    CustomProgressBar customProgressBar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       try {
           View view = inflater.inflate(R.layout.activity_profile, container, false);
           Bugfender.init(getContext(), "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
           Bugfender.enableCrashReporting();

           mAuth = FirebaseAuth.getInstance();
           db = FirebaseFirestore.getInstance();

           mUser = mAuth.getCurrentUser();
           phirank = view.findViewById(R.id.tv_phirank);
           globalrank = view.findViewById(R.id.tv_globalrank);
           global = view.findViewById(R.id.tv_global);
           friendsrank = view.findViewById(R.id.tv_friendsrank);
           friends = view.findViewById(R.id.tv_friends);
           listfriends = view.findViewById(R.id.tv_listfriends);
           referandearn = view.findViewById(R.id.tv_referandearn);
           mChangeImage = view.findViewById(R.id.img_change);
           friendslistimage = view.findViewById(R.id.img_friendslist);
           share_button = view.findViewById(R.id.img_share_button);
           name = view.findViewById(R.id.tv_name);
           username = view.findViewById(R.id.tv_username);

           rankPreference = PreferenceManager.getDefaultSharedPreferences(getContext());


           mFirebaseStorage = FirebaseStorage.getInstance();
           myReference = mFirebaseStorage.getReference().child("images").child(mUser.getUid());

           final SharedPreferences minutes = getContext().getSharedPreferences("minutes", 0);
           final int usedMinute = minutes.getInt("mWithOx", 0);

           Log.d(TAG, "onCreate: " + usedMinute);
           minutesTv = view.findViewById(R.id.tv_earAge_text);

           String i = Integer.toString(usedMinute);

          getRank();

           logout = view.findViewById(R.id.btn_logout);
           settings=view.findViewById(R.id.btn_setting);
           settings.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   startActivity(new Intent(getContext(), ProfileSetting.class));
               }
           });
           logout.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   new AlertDialog.Builder(getContext())
                           .setIcon(android.R.drawable.ic_dialog_alert)
                           .setTitle("LOGOUT")
                           .setMessage("Are you sure you want to logout?")
                           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialog, int which) {
                                   mAuth.signOut();
                                   startActivity(new Intent(getContext(), LoginActivity.class));
                                   getActivity().finish();
                               }
                           })
                           .setNegativeButton("No", null)
                           .show();



               }
           });
           mShare = view.findViewById(R.id.linear_share);
           mShare.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent i = new Intent(getContext(), ShareActivity.class);
                   startActivity(i);
               }
           });

           mFriends = view.findViewById(R.id.linear_friends);
           mFriends.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent in = new Intent(getContext(), FriendsActivity.class);
                   startActivity(in);
               }
           });


           fetchingDetails();
           return view;
       }catch (Exception e)
       {
           Log.e(TAG, "onCreateView", e);
           throw e;
       }
    }

    private void getRank() {

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
                            if (temp[0]==0){
                                temp[0]= 1;
                            }
                            globalrank.setText(String.valueOf(temp[0]));
                            Log.d(TAG, "temp: "+temp[0]);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void fetchingDetails() {


        DocumentReference dataRef = db.collection("Users").document(mUser.getUid());
        dataRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            String fname = documentSnapshot.getString("fname");
                            String lname = documentSnapshot.getString("lname");
                            String uname = documentSnapshot.getString("username");
                            String gender;
                            try {
                                 gender = documentSnapshot.getString("gender");
                            }catch (Exception e)
                            {
                                gender = documentSnapshot.getString("gender");

                            }



                            String imageLink;
                            try {
                                imageLink = documentSnapshot.getString("imageLink");

                                if(gender!=null) {
                                    if (gender.equals("M")) {
                                        Glide.with(getContext()).load(imageLink).dontAnimate().transform(new CircleCrop()).centerCrop().placeholder(R.drawable.ic_male_profile).into(mChangeImage);
                                    } else if (gender.equals("F")) {
                                        Glide.with(getContext()).load(imageLink).dontAnimate().transform(new CircleCrop()).placeholder(R.drawable.ic_female_profile).into(mChangeImage);
                                    } else {
                                        Glide.with(getContext()).load(imageLink).dontAnimate().transform(new CircleCrop()).placeholder(R.drawable.ic_profile).into(mChangeImage);
                                    }
                                }
                                else
                                {
                                    Glide.with(getContext()).load(imageLink).transform(new CircleCrop()).fitCenter().dontAnimate().placeholder(R.drawable.ic_profile).into(mChangeImage);

                                }

                            }catch(Exception e)
                            {


                            }
                            PHI = documentSnapshot.getLong("phi");
                            name.setText(fname + " " +lname);
                            username.setText("@"+ uname);
                            int friendsRank = rankPreference.getInt("friends_rank", -1);
                            int globalRank = rankPreference.getInt("global_rank", -1);

                            if (friendsRank == -1) {
                                friendsrank.setText("--");
                            } else {
                                friendsrank.setText(Integer.toString(friendsRank));
                            }

                            if (globalRank == -1) {
                                fetchRank();
                            } else {
//                                globalrank.setText(Integer.toString(globalRank));
                                Log.d(TAG, "RANK" + globalRank);
                            }





                        }else{
                            Toast.makeText(getContext(),"No data exits",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(),"Unable to fetch the Details.",Toast.LENGTH_SHORT).show();
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
    }
    private void fetchRank()
    {
        final int[] count = {0};
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference dataRef = firebaseFirestore.collection("Users");
        dataRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for(DocumentSnapshot snapshot:queryDocumentSnapshots.getDocuments())
                {
                    try {
                        {

                            if(PHI<(Long) snapshot.get("phi"))
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
                SharedPreferences.Editor editor = rankPreference.edit();
                editor.putInt("global_rank",count[0]+1);
                editor.apply();

//                globalrank.setText(Integer.toString(count[0]+1));

            }
        });


    }


}
