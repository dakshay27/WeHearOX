package com.wehear.ox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class UserWeHearsExist extends AppCompatActivity {
    private Button btnWithOx, btnWithOutOx;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseFirestore mFirebaseFirestore;
    private static final String TAG = "UserWeHearsExist";
    private String weHearOxMacId = "00:00:00:00:00:00";
    private DocumentReference mDataRef;
    private int usedWeHearOx=0;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        assert mUser != null;
        sharedPreferences=getSharedPreferences("UserDetails",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        editor.putInt("isUsedWeHearOx",usedWeHearOx);
        editor.apply();
        if (mUser.getUid() != null) {
            Log.d(TAG, "onCreate: " + mUser.getUid());
        } else {
            Log.d(TAG, "onCreate: null");
        }
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_user_we_hears_exist);

        btnWithOutOx = findViewById(R.id.btn_not_contains_we_hear);
        btnWithOx = findViewById(R.id.btn_contains_we_hear);

        mDataRef = mFirebaseFirestore.collection("Users").document(mUser.getUid());
        btnWithOutOx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(UserWeHearsExist.this, "Called", Toast.LENGTH_SHORT).show();
                Map<String, Object> data = new HashMap<>();
                data.put("userOxId", weHearOxMacId);
                    mDataRef.set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            usedWeHearOx=-1;
                            editor.putInt("isUsedWeHearOx",usedWeHearOx);
                            editor.apply();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

            }
        });
        btnWithOx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserWeHearsExist.this,AvailableDevicesActivity.class));
            }
        });
    }
}