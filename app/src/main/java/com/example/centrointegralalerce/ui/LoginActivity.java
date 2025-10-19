package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.centrointegralalerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button guestLoginButton;
    private TextView tvRecoverPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        guestLoginButton = findViewById(R.id.guestLoginButton);
        tvRecoverPassword = findViewById(R.id.tvRecoverPassword);

        loginButton.setOnClickListener(v -> onLoginClick(v));
        guestLoginButton.setOnClickListener(v -> onGuestLoginClick(v));
        tvRecoverPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
            startActivity(intent);
        });
    }

    public void onLoginClick(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Conectando...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Iniciar Sesión");

                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        db.collection("usuarios").document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String rolId = documentSnapshot.getString("rolId");

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("ROL", rolId);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(LoginActivity.this, "No se encontró información del usuario en Firestore.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Error al leer usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace(); // 🔹 Log extra
                                });

                    } else {
                        String errorMsg = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                            task.getException().printStackTrace(); // 🔹 Log extra de la excepción
                        }
                        Toast.makeText(LoginActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void onGuestLoginClick(View view) {
        Toast.makeText(this, "Accediendo como invitado...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("ROL", "usuario"); // invitado tratado como usuario normal
        startActivity(intent);
        finish();
    }
}
