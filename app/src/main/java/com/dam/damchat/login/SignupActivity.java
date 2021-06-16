package com.dam.damchat.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.dam.damchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import org.jetbrains.annotations.NotNull;

public class SignupActivity extends AppCompatActivity {

    // 1. var glob :
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private String name, email, password, confirmPassword;

    // 5. ajout de la var Firebase
    private FirebaseUser firebaseUser;

    //6. ajout de la database realtime db :
    private DatabaseReference databaseReference;

    //meth initUI pr lien entre design et code :
    public void initUI() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.et_ConfirmPassword);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        initUI();
    }

    //meth pr gest cl s bt sgnup :
    public void btnSignupClick(View v) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.equals("")) { // (si "null" donc  (reMq : pas tout  a fait null stricto sensu ...))
            etName.setError(getString(R.string.enter_name));
        } else if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        }
        //verif des patytern :
        else if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
         {
            etEmail.setError("Enter valid email");
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Pwd mismatch !");
        } else {
            // connex a fbase
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            //creer user ds l authentication :
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                Toast.makeText(SignupActivity.this,
                                        R.string.user_created_successfully,
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));

                            } else {
                                Toast.makeText(SignupActivity.this,
                                        getString(R.string.signup_failed) + task.getException(),

                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })

            //ou bien, meth alternative :
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull @NotNull Exception e) {
//                    Toast.makeText(SignupActivity.this,
//                            getString(R.string.signup_failed) + e.getMessage(),
//                            Toast.LENGTH_SHORT).show();
            //  }
            //})

            ;
        }




    }

    private  void  updateNameOnly(){
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){

                        }
                    }
                });

    }




}

