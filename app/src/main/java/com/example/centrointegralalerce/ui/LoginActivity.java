package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.utils.SharedPreferencesManager;
import com.example.centrointegralalerce.data.UserSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

        // ✅ Evitar interacción mientras verificamos sesión
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefsManager = new SharedPreferencesManager(this);

        // ✅ Si ya hay sesión iniciada
        if (prefsManager.isLoggedIn() && mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect();
            return;
        }

        // ✅ Mostrar la vista si no hay sesión
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setContentView(R.layout.activity_login);
        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ Verificación extra al volver
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

    // ✅ NUEVA VERSIÓN del método
    private void checkUserRoleAndRedirect() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rolId = documentSnapshot.getString("rolId");
                        cargarPermisosYRedirigir(rolId != null ? rolId : "usuario");
                    } else {
                        handleInvalidSession();
                    }
                })
                .addOnFailureListener(e -> handleInvalidSession());
    }

    // ✅ NUEVO: Cargar permisos del rol antes de entrar al MainActivity
    private void cargarPermisosYRedirigir(String rolId) {
        FirebaseFirestore.getInstance().collection("roles").document(rolId)
                .get()
                .addOnSuccessListener(rolDoc -> {
                    if (rolDoc.exists()) {
                        Map<String, Object> data = rolDoc.getData();
                        Map<String, Boolean> permisos = new HashMap<>();

                        if (data != null && data.containsKey("permisos")) {
                            Object permisosObj = data.get("permisos");
                            if (permisosObj instanceof Map) {
                                Map<String, Object> permisosMap = (Map<String, Object>) permisosObj;
                                for (String key : permisosMap.keySet()) {
                                    Object value = permisosMap.get(key);
                                    if (value instanceof Boolean) {
                                        permisos.put(key, (Boolean) value);
                                    }
                                }
                            }
                        }

                        // ✅ Guardar en UserSession antes de redirigir
                        UserSession.getInstance().setRol(rolId, permisos);

                        Log.d("LOGIN_DEBUG", "✅ Permisos cargados: " + permisos.size() + " permisos");

                        // ✅ VERIFICACIÓN EXTRA: Esperar a que UserSession esté realmente cargado
                        new Handler().postDelayed(() -> {
                            if (UserSession.getInstance().permisosCargados()) {
                                Log.d("LOGIN_DEBUG", "✅ UserSession confirmado cargado, redirigiendo...");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("ROL", rolId);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e("LOGIN_DEBUG", "❌ UserSession NO cargado después de espera");
                                cargarPermisosPorDefectoYRedirigir(rolId);
                            }
                        }, 500); // Pequeño delay para asegurar carga

                    } else {
                        cargarPermisosPorDefectoYRedirigir(rolId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LOGIN_DEBUG", "Error cargando permisos, usando defaults", e);
                    cargarPermisosPorDefectoYRedirigir(rolId);
                });
    }

    // ✅ NUEVO: Permisos por defecto si falla la carga
    private void cargarPermisosPorDefectoYRedirigir(String rolId) {
        Map<String, Boolean> permisosPorDefecto = new HashMap<>();

        if ("admin".equals(rolId)) {
            permisosPorDefecto.put("crear_usuarios", true);
            permisosPorDefecto.put("gestionar_usuarios", true);
            permisosPorDefecto.put("gestionar_mantenedores", true);
            permisosPorDefecto.put("crear_actividades", true);
            permisosPorDefecto.put("eliminar_actividades", true);
            permisosPorDefecto.put("ver_todas_actividades", true);
        } else {
            permisosPorDefecto.put("crear_usuarios", false);
            permisosPorDefecto.put("gestionar_usuarios", false);
            permisosPorDefecto.put("gestionar_mantenedores", false);
            permisosPorDefecto.put("crear_actividades", true);
            permisosPorDefecto.put("eliminar_actividades", false);
            permisosPorDefecto.put("ver_todas_actividades", true);
        }

        UserSession.getInstance().setRol(rolId, permisosPorDefecto);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("ROL", rolId);
        startActivity(intent);
        finish();
    }

    private void handleInvalidSession() {
        prefsManager.clearSession();
        mAuth.signOut();

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
