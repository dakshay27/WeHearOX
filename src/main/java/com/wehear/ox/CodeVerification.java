package com.wehear.ox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import static com.wehear.ox.LoginActivity.PHONENUMBER;

public class CodeVerification extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CodeVerification";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId;
    public static final String NUMBER = "number";
    public static String phoneNumber,phoneNumberRaw;
    private FirebaseAuth mAuth;
    TextView mPhoneNumber, verificationTv;
    EditText mOtp;
    Button mVerify;
    TextView mResend;
        FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();


        setContentView(R.layout.activity_code_verification);
        sharedPreferences=getSharedPreferences("UserDetails",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        findViewById();
//        mPhoneNumber = findViewById(R.id.phoneNumberTv);
        mVerify.setOnClickListener(this);
        mResend.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        Intent login_intent = getIntent();
        phoneNumber = login_intent.getStringExtra(PHONENUMBER);
        phoneNumberRaw = login_intent.getStringExtra("PhoneNumberRaw");


        //mPhoneNumber.setText("+91"+phoneNumber);
            db = FirebaseFirestore.getInstance();


        sendVerificationCode(phoneNumber);
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                 mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                mOtp.setText(code);
                verifyPhoneNumberWithCode(mVerificationId, code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                mVerify.setError("Invalid phone number.");
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(CodeVerification.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
//            Intent to Login Activity
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            // ...
        }
    };

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            Bugfender.d("OTP-Error", e.toString());
        }

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = task.getResult().getUser();
                            String UID = task.getResult().getUser().getUid();
                            Log.d(TAG, "uid-" + UID);
                            db.collection("Users").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            com.wehear.ox.User user=document.toObject(User.class);
                                            if(user.getUserOxId()==null){
                                                Intent intent = new Intent(getApplicationContext(), RegisteredDeviceActivity.class);
                                                startActivity(intent);
                                            }else {
                                                if(user.getUserOxId().equals("00:00:00:00:00:00")){
                                                    editor.putInt("isUsedWeHearOx",-1);
                                                    editor.apply();
                                                }else{
                                                    editor.putInt("isUsedWeHearOx",1);
                                                    editor.apply();
                                                }
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            Intent codeverifyintent = new Intent(CodeVerification.this, UserInfoActivity.class);
                                            codeverifyintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            codeverifyintent.putExtra(NUMBER, phoneNumber);
                                            codeverifyintent.putExtra("PhoneNumberRaw",phoneNumberRaw);
                                            startActivity(codeverifyintent);
                                        }
                                    }

                                }
                            });


                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                mVerify.setError("Invalid code.");
                                mVerify.requestFocus();
                            }
                            Toast.makeText(getApplicationContext(), "Check Phone Number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_verify:
                String code = mOtp.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    mOtp.setError("Enter valid code");
                    mOtp.requestFocus();
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;

            case R.id.btn_resend:
                resendVerificationCode(phoneNumber, mResendToken);
                break;
        }
    }

    private void findViewById() {
        mOtp = findViewById(R.id.et_otp);
        mVerify = findViewById(R.id.btn_verify);
        mResend = findViewById(R.id.btn_resend);
        verificationTv = findViewById(R.id.tv_verification);
    }
}