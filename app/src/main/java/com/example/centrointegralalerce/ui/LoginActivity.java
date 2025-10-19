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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button guestLoginButton;
    private TextView tvRecoverPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar Firebase Auth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        guestLoginButton = findViewById(R.id.guestLoginButton);
        tvRecoverPassword = findViewById(R.id.tvRecoverPassword);

        // Configurar listeners
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

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante el login
        loginButton.setEnabled(false);
        loginButton.setText("Conectando...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Iniciar Sesión");

                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Obtener documento del usuario en Firestore
                        db.collection("usuarios").document(uid)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String rolId = documentSnapshot.getString("rolId");
                                        Toast.makeText(LoginActivity.this, "¡Login exitoso!", Toast.LENGTH_SHORT).show();

                                        Intent intent;
                                        if ("admin".equals(rolId)) {
                                            // Abrir pantalla de administrador
                                            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                                        } else {
                                            // Abrir pantalla de usuario normal
                                            intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                        }

                                        startActivity(intent);
                                        finish();

                                    } else {
                                        Toast.makeText(LoginActivity.this, "No se encontró información del usuario en Firestore.", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Error al leer usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        String errorMsg = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            errorMsg = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void onGuestLoginClick(View view) {
        Toast.makeText(this, "Accediendo como invitado...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(LoginActivity.this, UserMainActivity.class); // invitado como usuario normal
        intent.putExtra("INVITADO", true);
        startActivity(intent);
        finish();
    }
}
