package com.wehear.ox;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.wehear.ox.CodeVerification.NUMBER;
import static com.wehear.ox.CodeVerification.phoneNumberRaw;

//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    TextView txtPrivacy,txtterms;
    CheckBox chkTerms;
    private static final String TAG = "UserInfoActivity";
    private final int PICK_IMAGE_REQUEST = 11;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage mFirebaseStorage;
    String gender;

    private StorageReference mStorageRef;
    CircularImageView mProfileCircularImage;
    EditText mFirstName, mLastName, mMobileNumber, mUserName;
    Button mSaveUserInfo;
    private Uri imageUri;
    private FirebaseFirestore mFirebaseFirestore;
    private DocumentReference mDataRef;
    private CollectionReference mAllUserRef, mReferTable, minDataRef;
    private DatabaseReference mDatabaseRef;
    public boolean v = true;
    private static String code;
    final Calendar myCalendar = Calendar.getInstance();
    TextView birthDate;
    String birthDay;
    String imageDownloadUrl;
    String saveDate;
    CustomProgressBar customProgressBar;
    int checkBirthday = 0;
    int checkMinDataRef = 0;
    private ImageView gender_male, gender_female, gender_others;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };


    private FirebaseDatabase mFirebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        assert mUser != null;
        if (mUser.getUid() != null) {
            Log.d(TAG, "onCreate: " + mUser.getUid());
        } else {
            Log.d(TAG, "onCreate: null");
        }
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        sharedPreferences=getSharedPreferences("UserTermscondition",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

//        mAllUserRef = mFirebaseFirestore.collection("Users");
//        mAllUserRef.document(mUser.getUid()).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        if (documentSnapshot.exists()){
//                            String username = documentSnapshot.getString("username");
//                            if (username != null){
//                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                                startActivity(intent);
//                            }
//                        }
//                    }
//                });

        setContentView(R.layout.activity_user_info);
        mProfileCircularImage = findViewById(R.id.img_profileCricular);
        mFirstName = findViewById(R.id.et_firstName);
        mFirstName.requestFocus();
        mLastName = findViewById(R.id.et_lastName);
        mMobileNumber = findViewById(R.id.et_mobileNumber);
        mUserName = findViewById(R.id.et_userName);
        mSaveUserInfo = findViewById(R.id.btn_saveUserInfo);
        mSaveUserInfo.setOnClickListener(this);
        mProfileCircularImage.setOnClickListener(this);
        gender_male = findViewById(R.id.img_gender_male);
        gender_female = findViewById(R.id.img_gender_female);
        gender_others = findViewById(R.id.img_gender_others);
        txtPrivacy=findViewById(R.id.txt_privacy);
        txtterms=findViewById(R.id.txt_terms);
        txtterms.setOnClickListener(this);
        txtPrivacy.setOnClickListener(this);
        chkTerms=findViewById(R.id.chk_term_privacy);
        gender_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender_male.setBackgroundResource(R.drawable.pink_border);
                gender_male.setImageResource(R.drawable.ic_male_profile);
                gender_female.setImageResource(R.drawable.ic_female_profile_gray);
                gender_others.setImageResource(R.drawable.ic_icon_ionic_ios_transgender_gray);
                gender_female.setBackgroundResource(android.R.color.transparent);
                gender_others.setBackgroundResource(android.R.color.transparent);
                gender = "M";
                Log.d(TAG, "Gender-" + gender);
            }
        });
        gender_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gender_female.setBackgroundResource(R.drawable.pink_border);
                gender_male.setImageResource(R.drawable.ic_male_profile_gray);
                gender_female.setImageResource(R.drawable.ic_female_profile);
                gender_others.setImageResource(R.drawable.ic_icon_ionic_ios_transgender_gray);
                gender_male.setBackgroundResource(android.R.color.transparent);
                gender_others.setBackgroundResource(android.R.color.transparent);
                gender = "F";
                Log.d(TAG, "Gender-" + gender);

            }
        });
        gender_others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gender_others.setBackgroundResource(R.drawable.pink_border);
                gender_male.setImageResource(R.drawable.ic_male_profile_gray);
                gender_female.setImageResource(R.drawable.ic_female_profile_gray);
                gender_others.setImageResource(R.drawable.ic_icon_ionic_ios_transgender);
                gender_male.setBackgroundResource(android.R.color.transparent);
                gender_female.setBackgroundResource(android.R.color.transparent);
                gender = "O";
                Log.d(TAG, "Gender-" + gender);

            }
        });


        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStorage.getReference();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Numbers");
        minDataRef = mFirebaseFirestore.collection("minutes-data");
        mDataRef = mFirebaseFirestore.collection("Users").document(mUser.getUid());
        customProgressBar = new CustomProgressBar(UserInfoActivity.this);


//        mDataRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                try {
//                    if ((task.getResult().get("username") != null) && (task.getResult().get("bday") != null)) {
////                        checkBirthday=1;
//                        Log.d(TAG, "BirthdayPresent");
//                        minDataRef.document(mUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if (task.getResult().exists()) {
//                                    Intent intent = new Intent(getApplicationContext(), UserWeHearsExist.class);
//                                    startActivity(intent);
//                                } else {
//                                    createMinuteUsedData();
//                                }
//                            }
//                        });
//
//                    }
//                } catch (NullPointerException e) {
//                    Log.e(TAG, "onComplete: ", e);
//                }
//            }
//        });


        birthDate = (TextView) findViewById(R.id.tv_birthDay);

        birthDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(UserInfoActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personGivenName = acct.getGivenName();
            mFirstName.setText(personGivenName);
            String personFamilyName = acct.getFamilyName();
            mLastName.setText(personFamilyName);
            Uri personPhoto = acct.getPhotoUrl();
            String photo_link = personPhoto.toString();

            Glide.with(UserInfoActivity.this)
                    .load(photo_link)
                    .into(mProfileCircularImage);
        }

        Intent codeverifyintent = getIntent();
        String number = codeverifyintent.getStringExtra(phoneNumberRaw);

        Log.d(TAG, "onCreate: " + number);
        mMobileNumber.setText(number);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_profileCricular:
                chooseImage();
                break;
            case R.id.btn_saveUserInfo:
                if(chkTerms.isChecked()) {
                    if(mUserName.getText().toString().length()>4) {
                        editor.putInt("terms", 1);
                        editor.apply();
                        uploadImage();
                    }else{
                        Toast.makeText(this, "Username must be greater than 4", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    Toast.makeText(this, "Please accepts our Terms and Conditions!!", Toast.LENGTH_SHORT).show();
//                saveInfo();
//                createReferralTable();
                //generateReferCode();
                break;
            case R.id.txt_privacy:
            case R.id.txt_terms:
                Uri uri = Uri.parse("http://www.wehear.in/privacy-policy/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
        }
    }


//    private void generateReferCode() {
//        String first =  mUserName.getText().toString().toUpperCase().substring(0,4);
//        Random random = new Random();
//        String second = String.format("%04d", random.nextInt(10000));
//        final String code = first + second;
//        Log.d(TAG, "generateReferCode: "+ code);
//        Map<String, Object> coderefer = new HashMap<>();
//        coderefer.put("code", code);
//        mAllUserRef.document(mUser.getUid()).set(coderefer).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Toast.makeText(getApplicationContext(),code,Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            imageUri = UCrop.getOutput(data);
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            File tempCrop = new File(getCacheDir(), "tempImage");
            Uri destinationUri = Uri.fromFile(tempCrop);
            UCrop.of(imageUri, destinationUri)
                    .withAspectRatio(1, 1)
                    .start(this);

            mProfileCircularImage.setImageURI(imageUri);
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            mProfileCircularImage.setImageBitmap(bitmap);
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

        if (imageUri != null) {

            customProgressBar.show();
            final StorageReference ref = mStorageRef.child("images/" + mUser.getUid());
            ref.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    imageDownloadUrl = task.getResult().toString();
                                    Log.i(TAG, imageDownloadUrl);
                                    customProgressBar.dismiss();
                                    saveInfo();

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            customProgressBar.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                        }
                    });

        } else {
            saveInfo();
        }
    }

    private void saveInfo() {
        if (!validateSaveForm()) {
            return;
        }
        customProgressBar.show();

        String fname = mFirstName.getText().toString().trim();
        String lname = mLastName.getText().toString().trim();
        String number = mMobileNumber.getText().toString().trim();
        String username = mUserName.getText().toString().trim();
        String first = mUserName.getText().toString().toUpperCase().substring(0, 4);
        Random random = new Random();
        String second = String.format("%04d", random.nextInt(10000));
        code = first + second;
        Log.d(TAG, "generateReferCode: " + code);


        writeNewUser(fname, lname, number, username, code, birthDay, imageDownloadUrl, gender);


    }


    private void writeNewUser(String fname, String lname, final String number, String username, String coderefer, String bDay, String imageDownloadUrl, String Gender) {

        User user = new User(fname, lname, number, username, coderefer, bDay, imageDownloadUrl, 0, Gender);


        mDataRef = mFirebaseFirestore.collection("Users").document(mUser.getUid());
        mDataRef.set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mDatabaseRef.child(number).setValue(mUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                createReferralTable();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to upload", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void createReferralTable() {
        customProgressBar.show();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mReferTable = mFirebaseFirestore.collection("Referral");
        Map<String, Object> referTable = new HashMap<>();
        referTable.put("uid", mUser.getUid());
        referTable.put("noOfReferral", 0);
        mReferTable.document(code).set(referTable).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                createMinuteUsedData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to create Table", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void createMinuteUsedData() {
        customProgressBar.show();
        Log.d(TAG, "createMinuteUsed");
        final Map<String, Object> minData = new HashMap<>();
        minData.put("minUsedOX", 0);
        minData.put("minUsedWOX", 0);
        minData.put("avgSession", 0);
        minData.put("avgVolume", 0);
        minData.put("PHI", 0);
        minData.put("averageDb", 0);

        Date todayDate = new Date(System.currentTimeMillis());
        SimpleDateFormat day = new SimpleDateFormat("yyyy MM dd");
        saveDate = day.format(todayDate);
        final HashMap<String, Object> Date = new HashMap<>();
        Date.put(saveDate, minData);
        minDataRef.document(mUser.getUid()).set(Date).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                customProgressBar.dismiss();
                Intent intent = new Intent(getApplicationContext(), UserWeHearsExist.class);
                startActivity(intent);
                finish();


            }
        });
    }


    private boolean validateSaveForm() {
        boolean valid = true;

        String fname = mFirstName.getText().toString();
        if (TextUtils.isEmpty(fname)) {
            mFirstName.setError("Required.");
            valid = false;
        } else {
            mFirstName.setError(null);
        }

        if (TextUtils.isEmpty(birthDay)) {
            birthDate.setError("Required.");
            valid = false;
        } else {
            birthDate.setError(null);
        }

        String lname = mLastName.getText().toString();
        if (TextUtils.isEmpty(lname)) {
            mLastName.setError("Required.");
            valid = false;
        } else {
            mLastName.setError(null);
        }

        String number = mMobileNumber.getText().toString();
        if (number.isEmpty() || number.length() < 10) {
            mMobileNumber.setError("Enter a valid mobile");
            valid = false;
        } else {
            mMobileNumber.setError(null);
        }

        String username = mUserName.getText().toString();
        if (username == null || !username.matches("[A-Za-z0-9_]+")) {
            mUserName.setError("Enter Valid Username");
            valid = false;
        } else {
            mUserName.setError(null);
        }

        if (gender == null) {
            Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show();
            valid = false;
        }

//        if (!checkFirebaseForUsername(username)){
//            mUserName.setError("Already exist!");
//            valid = false;
//        }else{
//            mUserName.setError(null);
//        }
        return valid;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        birthDay = sdf.format(myCalendar.getTime());
        birthDate.setText(birthDay);
    }

//    private boolean checkFirebaseForUsername(String username) {
//        Query userNameQuery = mAllUserRef.whereEqualTo("username",username);
//        userNameQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                if (task.isSuccessful()) {
//                    for (DocumentSnapshot document : task.getResult()) {
//                        if (document.exists()) {
//                            v = false;
//                            Log.d(TAG, "username already exists");
//                        } else {
//                            Log.d(TAG, "username does not exists");
//                        }
//                    }
//                } else {
//                    v=true;
//                    Log.d("TAG", "Error getting documents: ", task.getException());
//                }
//            }
//        });
//        return v;
//    }
}
