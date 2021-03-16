package com.wehear.ox;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.wehear.ox.LoginActivity.GOOGLE_ACCOUNT;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {
                // This method will be executed once the timer is over
                if(mAuth.getCurrentUser()==null)
                {
                    Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
                else
                 {
                     String UID = mAuth.getCurrentUser().getUid();
                     db.collection("Users").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                         @Override
                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                             if(task.isSuccessful())
                             {
                                 DocumentSnapshot document = task.getResult();
                                 if(document.exists())
                                 {

                                     Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                     startActivity(intent);
                                     finish();
                                 }
                                 else
                                 {   Intent google_intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                                     startActivity(google_intent);
                                     finish();
                                 }
                             }

                         }
                     });

                 }


            }
        }, 3000);
    }
}

