package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView tvRecoverPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias UI
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        tvRecoverPassword = findViewById(R.id.tvRecoverPassword);

        // Eventos
        loginButton.setOnClickListener(this::onLoginClick);
        tvRecoverPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
            startActivity(intent);
        });
    }

    public void onLoginClick(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validación
        if (email.isEmpty() || password.isEmpty()) {
            AlertManager.showWarningSnackbar(
                    AlertManager.getRootView(this),
                    "Por favor ingresa correo y contraseña"
            );
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

                                        AlertManager.showSuccessSnackbar(
                                                AlertManager.getRootView(this),
                                                "Inicio de sesión exitoso ✅"
                                        );

                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("ROL", rolId);
                                        startActivity(intent);
                                        finish();

                                    } else {
                                        AlertManager.showErrorSnackbar(
                                                AlertManager.getRootView(this),
                                                "No se encontró información del usuario en Firestore."
                                        );
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    AlertManager.showErrorSnackbar(
                                            AlertManager.getRootView(this),
                                            "Error al leer usuario: " + e.getMessage()
                                    );
                                });

                    } else {
                        String errorMsg = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                "Error: " + errorMsg
                        );
                    }
                });
    }
}