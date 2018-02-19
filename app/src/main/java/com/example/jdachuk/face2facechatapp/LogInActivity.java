package com.example.jdachuk.face2facechatapp;

/**
 * Created by jdachuk on 17.02.18.
 */

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText mEmail, mPassword;
    private Button mLogInBtn, mRegBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        initialize();

        checkAuth();

        mLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    logInUser(email, password);
                }
            }
        });

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registrationIntent = new Intent(LogInActivity.this, RegistrationActivity.class);
                startActivity(registrationIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void checkAuth() {
        if(mAuth.getCurrentUser() != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void logInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            if(mAuth.getCurrentUser() != null) {
                                String userUID = mAuth.getCurrentUser().getUid();
                                String device_token = FirebaseInstanceId.getInstance().getToken();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put(userUID + "/device_token", device_token);

                                FirebaseDatabase.getInstance().getReference()
                                        .child("Users").updateChildren(userMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                sendToMain();
                                            }
                                        });
                            }
                        }else {
                            if(task.getException() != null)
                                Toast.makeText(LogInActivity.this,
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.log_in_email);
        mPassword = findViewById(R.id.log_in_password);
        mLogInBtn = findViewById(R.id.log_in_button);
        mRegBtn = findViewById(R.id.log_in_reg_button);
    }
}
