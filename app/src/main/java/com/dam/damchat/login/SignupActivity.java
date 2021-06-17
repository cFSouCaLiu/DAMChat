package com.dam.damchat.login;
// ma classe à moi (FFf)
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;

//import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dam.damchat.R;
import com.dam.damchat.common.NodesNames;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    /**
     * 1 Variables globales
     **/
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private String name, email, password, confirmPassword;

    private Uri localFileUri, serverFileUri;
    // reMq : la 2é sera un Uri prAndroid, mais en fait pour nous ca sera un URL (car sur serveur sur internet)

    private ImageView ivAvatar;

    /**
     * 5 Ajout de la var FirebaseUser
     **/
    private FirebaseUser firebaseUser;

    /**
     * Ajout de la base RealTime db
     **/
    private DatabaseReference databaseReference;
    /**
     * Ajout de la référence vers le storage
     **/
    private StorageReference fileStorage;

    /**
     * 2 Méthode initUI pour faire le lien entre design et code
     **/
    public void initUI() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivAvatar = findViewById(R.id.ivAvatar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

        /** 3 Appel de la méthode initUI **/
        initUI();

        fileStorage = FirebaseStorage.getInstance().getReference();

    }

    /**
     * 4 Méthode pour la gestion du click sur le bouton SignUp
     **/
    public void btnSignupClick(View v) {
        name = etName.getText().toString().trim();
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        confirmPassword = etConfirmPassword.getText().toString().trim();

        // Si les champs sont vides
        if (name.equals("")) {
            // (si "null" donc  (reMq : pas tout  a fait null stricto sensu ...))
            etName.setError(getString(R.string.enter_name));
        } else if (email.equals("")) {
            etEmail.setError(getString(R.string.enter_email));
        } else if (password.equals("")) {
            etPassword.setError(getString(R.string.enter_password));
        } else if (confirmPassword.equals("")) {
            etConfirmPassword.setError(getString(R.string.confirm_password));
        }
        // Vérification des pattern
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.enter_valid_email));
        } else if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.password_mismatch));
        } else {
            /** Connexion à Firebase **/
            final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            // Création de l'utilisateur dans Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                if (localFileUri != null) {
                                    updateNameAndPhoto();
                                } else {
                                    updateNameOnly();
                                }

//                                Toast.makeText(SignupActivity.this,
//                                        R.string.user_created_successfully,
//                                        Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
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

    /**
     * Méthode pour faire l'enregistrement des informations d'Authentication dans RealTime
     **/
    private void updateNameOnly() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(etName.getText().toString().trim())
                .build();

        firebaseUser.updateProfile(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            String UserId = firebaseUser.getUid();
                            //debut des modifs je 17 juin matin :
                            String userId = firebaseUser.getUid();
                            // connex à Realtime :
                            databaseReference = FirebaseDatabase
                                    .getInstance() //instance de connex à la db
                                    .getReference() // chercher la ref desiree à partir du root de le db
                                    .child(NodesNames.USERS); // la ref en question qui passe comme constante depuis NodesNames
                            // Creat HashMap pr la gest des donnees
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                            hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                            hashMap.put(NodesNames.ONLINE, "true");
                            hashMap.put(NodesNames.AVATAR, "");
                            //envoi des datas vs realtime :
                            databaseReference.child(UserId).setValue(hashMap)
                                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            Toast.makeText(SignupActivity.this,
                                                    R.string.user_created_successfully,
                                                    Toast.LENGTH_SHORT).show();
                                            // Lancement de l'activité suivante
                                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                        }
                                    });


                        } else {
                            // si un pb (on est à la suite du "if (task.isSuccessful()){}" )
                            Toast.makeText(SignupActivity.this, getString(R.string.failed_to_update_user),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });

    }

    // add meth 4 managing avatar ( immg ) :
    public void pickImage(View v) {
        // ici on aura un Intent implicite (pr chercher une immg s le tel)
        //(implicite : à la diff des INtents explicites : qd on va d une activ A vs B )
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); // : qd click s l'immg de l'avatar
            // , une page de rech (browse) d'immg (ds le tel) s'ouvre (si permission (Android 9 refusera sinon))
            startActivityForResult(intent, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
        }
    }

    // aj la meth pr verif si on a la perm ou non :
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this,
                        R.string.access_permission_is_required,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //verifs :
        // le requestcode est-il le bon ?
        if (requestCode == 101) {
            // y a bien une sel d'immg (sinon le resultcode = RESULT_CANCELED
            if (resultCode == RESULT_OK) {
                // Path complet vers l'image sur le terminal
                localFileUri = data.getData();
                //affecter l'Uri à l avatar :
                ivAvatar.setImageURI(localFileUri);
            }
        }
    }

    // add meth pr update de la photo et du contenu iN RealTime db :
    private void updateNameAndPhoto() {
        // Renommer l'image avec l'userId et le type de fichier (ici jpg)
        String strFileName = firebaseUser.getUid() + ".jpg";
        // on place l immg  in le Storage :
        //private StorageReference
        final StorageReference fileRef = fileStorage.child("avatars_user/" + strFileName);
        // Upload vers le storage
        fileRef.putFile(localFileUri)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            //recup l url de l avatar ds le storage :
                            fileRef.getDownloadUrl()
                                    .addOnSuccessListener(SignupActivity.this,
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    serverFileUri = uri;
                                                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(etName.getText().toString().trim())
                                                            .setPhotoUri(serverFileUri)
                                                            .build();

                                                    firebaseUser.updateProfile(request)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        String UserId = firebaseUser.getUid();
                                                                        //debut des modifs je 17 juin matin :
                                                                        String userId = firebaseUser.getUid();
                                                                        // connex à Realtime :
                                                                        databaseReference = FirebaseDatabase
                                                                                .getInstance() //instance de connex à la db
                                                                                .getReference() // chercher la ref desiree à partir du root de le db
                                                                                .child(NodesNames.USERS); // la ref en question qui passe comme constante depuis NodesNames
                                                                        // Creat HashMap pr la gest des donnees
                                                                        HashMap<String, String> hashMap = new HashMap<>();
                                                                        hashMap.put(NodesNames.NAME, etName.getText().toString().trim());
                                                                        hashMap.put(NodesNames.EMAIL, etEmail.getText().toString().trim());
                                                                        hashMap.put(NodesNames.ONLINE, "true");
                                                                        hashMap.put(NodesNames.AVATAR, serverFileUri.getPath());
                                                                        //envoi des datas vs realtime :
                                                                        databaseReference.child(UserId).setValue(hashMap)
                                                                                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                                        Toast.makeText(SignupActivity.this,
                                                                                                R.string.user_created_successfully,
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        // Lancement de l'activité suivante
                                                                                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                                                    }
                                                                                });


                                                                    } else {
                                                                        // S'il y a un problème
                                                                        // si un pb (on est à la suite du "if (task.isSuccessful()){}" )
                                                                        Toast.makeText(SignupActivity.this,
                                                                                getString(R.string.failed_to_update_user) + task.getException(),
                                                                                Toast.LENGTH_LONG).show();
                                                                    }
                                                                }

                                                            });
                                                }
                                            });
                        }
                    }
                });
    }


}

