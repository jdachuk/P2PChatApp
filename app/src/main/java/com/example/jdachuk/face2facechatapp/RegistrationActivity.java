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

import com.example.jdachuk.face2facechatapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText mName, mEmail, mPassword;
    private Button mRegBtn, mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initialize();

        checkAuth();

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString();
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    registerNewUser(name, email, password);
                }
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void registerNewUser(final String name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            setUserData(name);
                        }else {
                            if(task.getException() != null)
                            Toast.makeText(RegistrationActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void setUserData(String name) {
        if(mAuth.getCurrentUser() != null) {
            String userUID = mAuth.getCurrentUser().getUid();
            String device_token = FirebaseInstanceId.getInstance().getToken();
            String status = getString(R.string.status_placeholder);
            User user = new User(name, "default", "default", status, device_token);

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(userUID).setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                sendToMain();
                            } else {
                                if(task.getException() != null)
                                    Toast.makeText(RegistrationActivity.this,
                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();

        mName = findViewById(R.id.reg_name);
        mEmail = findViewById(R.id.reg_email);
        mPassword = findViewById(R.id.reg_password);
        mRegBtn = findViewById(R.id.reg_button);
        mBackBtn = findViewById(R.id.reg_back_button);
    }
}
