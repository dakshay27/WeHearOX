package com.wehear.ox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.Country;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1001;
    public static final String PHONENUMBER = "phoneNumber";
    public static final String GOOGLE_ACCOUNT = "google_account";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    EditText mPhoneNumber;
    Button mGetOtp;
    TextView hello,signintv,info,or,signinwith;
    FirebaseFirestore db;
    CountryCodePicker ccp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        final Handler handler = new Handler();
        Runnable Run = new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                onWindowFocusChanged(true);
                handler.postDelayed(this,1000);

            }
        };
        handler.postDelayed(Run,1000);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        mAuth = FirebaseAuth.getInstance();
        mPhoneNumber = findViewById(R.id.phoneNumberEt);
        mGetOtp = findViewById(R.id.getOtpBtn);
        hello = findViewById(R.id.hello);
        signintv = findViewById(R.id.signintv);
        info = findViewById(R.id.info);
        signinwith = findViewById(R.id.signinwith);
        or = findViewById(R.id.hello);

        mGetOtp.setOnClickListener(this);

        hello.setText("Hello!");
        db = FirebaseFirestore.getInstance();

        ccp.registerPhoneNumberTextView(mPhoneNumber);

        ccp.setDefaultCountryUsingPhoneCodeAndApply(91);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getOtpBtn:
                String phoneNumber = ccp.getFullNumberWithPlus();

                if (!ccp.isValid()) {
                    mPhoneNumber.setError("Enter a valid mobile");
                    mPhoneNumber.requestFocus();
                    return;
                }

                Intent login_intent = new Intent(getApplicationContext(), CodeVerification.class );
                login_intent.putExtra(PHONENUMBER, phoneNumber);
                login_intent.putExtra("PhoneNumberRaw",mPhoneNumber.getText());
                startActivity(login_intent);
                break;

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

                               );

        }
    }
}
