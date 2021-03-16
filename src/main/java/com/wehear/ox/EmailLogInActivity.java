package com.wehear.ox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailLogInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailLogInActivity";
    private FirebaseAuth mAuth;

    Button mLogIn,mSignUp;
    EditText mEmailLogIn,mPasswordLogIn,mEmailSignUp,mPasswordSignUp;
    TextView mForgetPassword,mSignUpTv,mLogInTv;
    LinearLayout mLogInLayout, mSignUpLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_log_in);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        findViewById();

        mLogIn.setOnClickListener(this);
        mSignUp.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
        mSignUpTv.setOnClickListener(this);
        mLogInTv.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    private void findViewById(){
        mLogIn = findViewById(R.id.btn_login);
        mSignUp = findViewById(R.id.btn_signUp);
        mEmailLogIn = findViewById(R.id.et_email);
        mPasswordLogIn = findViewById(R.id.et_password);
        mEmailSignUp = findViewById(R.id.et_emailSignUp);
        mPasswordSignUp = findViewById(R.id.et_passwordSignUp);
        mForgetPassword = findViewById(R.id.tv_forgotPassword);
        mSignUpTv = findViewById(R.id.tv_signUp);
        mLogInTv = findViewById(R.id.tv_logIn);
        mLogInLayout = findViewById(R.id.linear_logIn);
        mSignUpLayout = findViewById(R.id.linear_signUp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                signIn(mEmailLogIn.getText().toString(),mPasswordLogIn.getText().toString());
                break;
            case R.id.tv_forgotPassword:
                sendPasswordReset(mEmailLogIn.getText().toString());
                break;
            case R.id.tv_signUp:
                mLogInLayout.setVisibility(View.GONE);
                mSignUpLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_signUp:
                createAccount(mEmailSignUp.getText().toString(), mPasswordSignUp.getText().toString());
                break;
            case  R.id.tv_logIn:
                mSignUpLayout.setVisibility(View.GONE);
                mLogInLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateLogInForm()) {
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            mAuth.getCurrentUser();
                            Intent i = new Intent(getApplicationContext(),UserInfoActivity.class);
                            startActivity(i);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed!",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (!task.isSuccessful()) {
//                            mStatusTextView.setText(R.string.auth_failed);
                        }

                    }
                });
    }

    private boolean validateLogInForm() {
        boolean valid = true;

        String email = mEmailLogIn.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailLogIn.setError("Required.");
            valid = false;
        } else {
            mEmailLogIn.setError(null);
        }

        String password = mPasswordLogIn.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordLogIn.setError("Required!");
            valid = false;
        } else if(password.length() < 6){
            mPasswordLogIn.setError("Minimum 6 letters.");
            valid = false;
        } else {
            mPasswordLogIn.setError(null);
        }
        return valid;
    }

    public void sendPasswordReset(String emailAddress) {
        if (!validateEmail()) {
            return;
        }

        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Reset password instructions has sent to your email",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(),
                                    "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateEmail() {
        boolean valid = true;
        String email = mEmailLogIn.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailLogIn.setError("Required.");
            valid = false;
        } else {
            mEmailLogIn.setError(null);
        }
        return valid;
    }

    private void createAccount(String email, String password) {
        if (!validateSignUpForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            mAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), "Account Created",Toast.LENGTH_LONG).show();
//                            Intent i = new Intent(getApplicationContext(), UserInfoActivity.class);
//                            startActivity(i);
                        } else {
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateSignUpForm() {
        boolean valid = true;

        String email = mEmailSignUp.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailSignUp.setError("Required.");
            valid = false;
        } else {
            mEmailSignUp.setError(null);
        }

        String password = mPasswordSignUp.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordSignUp.setError("Required!");
            valid = false;
        } else if(password.length() < 6){
            mPasswordSignUp.setError("Minimum 6 Letters.");
            valid = false;
        } else {
            mPasswordSignUp.setError(null);
        }
        return valid;
    }


}
