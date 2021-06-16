package com.dam.damchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dam.damchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etMail, etPassword;
    private String email, password;

    // meth init pr start :
    public void initUI() {
        etMail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_login);
        // appel de la meth d init d UI :
        initUI();

    }

    // meth pr gest clic s bt login :
    public void btnLoginClick(View v) {
        email = etMail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        if (email.equals("")) { // (si "null" donc  (reMq : pas tout  a fait null stricto sensu ...))
            etMail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else {
            // connex a Firebase :
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //RIEN
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        getString(R.string.login_failed) + task.getException(),
                                        Toast.LENGTH_SHORT).show();

                            }
                        }


                    });


        }
    }

    // meth pr bt signup :
    public  void  tvSignUpClick(View v){
        startActivity(new Intent( LoginActivity.this, SignupActivity.class));
    }

    // meth pr bt reset pwd : :
    public  void  tvResetpasswordClick(View v){
        startActivity(new Intent( LoginActivity.this,ResetPasswordActivity.class));
    }


}