package com.example.centrointegralalerce.ui;
import android.view.WindowManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.utils.SharedPreferencesManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView tvRecoverPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Ocultar la vista temporalmente mientras verificamos
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefsManager = new SharedPreferencesManager(this);

        // ✅ VERIFICACIÓN INMEDIATA - ANTES de setContentView
        if (prefsManager.isLoggedIn() && mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect();
            return; // Salir inmediatamente sin cargar el layout
        }

        // ✅ Si NO hay sesión, restaurar flags y cargar vista normal
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.activity_login);
        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ Verificación adicional por si el usuario volvió a esta actividad
        if (prefsManager.isLoggedIn() && mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect();
        }
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        tvRecoverPassword = findViewById(R.id.tvRecoverPassword);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(this::onLoginClick);
        tvRecoverPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecoverPasswordActivity.class);
            startActivity(intent);
        });
    }

    public void onLoginClick(View view) {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

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
                        prefsManager.setLoggedIn(true);
                        prefsManager.saveUserEmail(email);
                        checkUserRoleAndRedirect();
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error al iniciar sesión";
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                "Error: " + errorMsg
                        );
                    }
                });
    }

    // ✅ Método optimizado para redirección
    private void checkUserRoleAndRedirect() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rolId = documentSnapshot.getString("rolId");

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("ROL", rolId != null ? rolId : "usuario");
                        startActivity(intent);
                        finish(); // Importante: cerrar LoginActivity
                    } else {
                        // Si no existe el documento, forzar logout
                        handleInvalidSession();
                    }
                })
                .addOnFailureListener(e -> {
                    // En caso de error, forzar logout
                    handleInvalidSession();
                });
    }

    private void handleInvalidSession() {
        prefsManager.clearSession();
        mAuth.signOut();

        // ✅ Asegurarse de que la vista esté cargada antes de mostrar mensaje
        runOnUiThread(() -> {
            if (loginButton != null) {
                AlertManager.showErrorSnackbar(
                        AlertManager.getRootView(this),
                        "Sesión inválida. Por favor ingresa nuevamente."
                );
            }
        });
    }
}