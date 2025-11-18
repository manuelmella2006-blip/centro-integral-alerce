package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
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

        // ✅ VALIDACIÓN 1: Campos vacíos
        if (email.isEmpty() || password.isEmpty()) {
            AlertManager.showWarningSnackbar(
                    AlertManager.getRootView(this),
                    "Por favor ingresa correo y contraseña"
            );
            return;
        }

        // ✅ VALIDACIÓN 2: Formato de email válido
        if (!isValidEmail(email)) {
            AlertManager.showWarningSnackbar(
                    AlertManager.getRootView(this),
                    "Por favor ingresa un correo electrónico válido"
            );
            return;
        }

        // ✅ VALIDACIÓN 3: Longitud mínima de contraseña
        if (password.length() < 6) {
            AlertManager.showWarningSnackbar(
                    AlertManager.getRootView(this),
                    "La contraseña debe tener al menos 6 caracteres"
            );
            return;
        }

        // ✅ VALIDACIÓN 4: Contraseña no debe contener espacios
        if (password.contains(" ")) {
            AlertManager.showWarningSnackbar(
                    AlertManager.getRootView(this),
                    "La contraseña no puede contener espacios"
            );
            return;
        }

        // ✅ VALIDACIÓN 5: Email no debe exceder longitud máxima
        if (email.length() > 100) {
            AlertManager.showWarningSnackbar(
                    AlertManager.getRootView(this),
                    "El correo electrónico es demasiado largo"
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
                        // ✅ VALIDACIÓN 6: Verificar si el usuario existe y está activo en Firestore
                        verifyUserInFirestore(email);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Error al iniciar sesión";

                        // ✅ VALIDACIÓN 7: Mensajes de error específicos de Firebase Auth
                        String userFriendlyError = getFirebaseAuthErrorMessage(errorMsg);
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                userFriendlyError
                        );
                    }
                });
    }

    // ✅ NUEVO MÉTODO: Validación de formato de email
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // ✅ NUEVO MÉTODO: Verificar usuario en Firestore después de autenticación exitosa
    private void verifyUserInFirestore(String email) {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ VALIDACIÓN 8: Verificar si el usuario está activo
                        Boolean activo = documentSnapshot.getBoolean("activo");
                        if (activo != null && !activo) {
                            // Usuario inactivo, cerrar sesión y mostrar mensaje
                            mAuth.signOut();
                            prefsManager.clearSession();
                            AlertManager.showErrorSnackbar(
                                    AlertManager.getRootView(this),
                                    "Tu cuenta está desactivada. Contacta al administrador."
                            );
                            return;
                        }

                        // ✅ VALIDACIÓN 9: Verificar que tenga rol asignado
                        String rolId = documentSnapshot.getString("rolId");
                        if (rolId == null || rolId.isEmpty()) {
                            // Usuario sin rol asignado
                            mAuth.signOut();
                            prefsManager.clearSession();
                            AlertManager.showErrorSnackbar(
                                    AlertManager.getRootView(this),
                                    "Tu cuenta no tiene permisos asignados. Contacta al administrador."
                            );
                            return;
                        }

                        // ✅ Usuario válido, proceder con login
                        prefsManager.setLoggedIn(true);
                        prefsManager.saveUserEmail(email);
                        checkUserRoleAndRedirect();
                    } else {
                        // ✅ VALIDACIÓN 10: Usuario no existe en Firestore
                        mAuth.signOut();
                        prefsManager.clearSession();
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                "Usuario no encontrado en el sistema. Contacta al administrador."
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    // ✅ VALIDACIÓN 11: Error al consultar Firestore
                    mAuth.signOut();
                    prefsManager.clearSession();
                    AlertManager.showErrorSnackbar(
                            AlertManager.getRootView(this),
                            "Error al verificar usuario. Intenta nuevamente."
                    );
                });
    }

    // ✅ MÉTODO MEJORADO: Mensajes de error amigables para Firebase Auth
    private String getFirebaseAuthErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "Error desconocido al iniciar sesión";
        }

        // Convertir a minúsculas para facilitar la comparación
        String errorLower = errorMessage.toLowerCase();

        if (errorLower.contains("invalid-email")) {
            return "El formato del correo electrónico no es válido";
        } else if (errorLower.contains("user-not-found")) {
            return "No existe una cuenta con este correo electrónico";
        } else if (errorLower.contains("wrong-password")) {
            return "Contraseña incorrecta. Verifica tu contraseña e intenta nuevamente";
        } else if (errorLower.contains("invalid-credential") || errorLower.contains("invalid-password")) {
            return "Contraseña inválida. Verifica tus credenciales";
        } else if (errorLower.contains("network-error") || errorLower.contains("network_error")) {
            return "Error de conexión. Verifica tu conexión a internet";
        } else if (errorLower.contains("too-many-requests")) {
            return "Demasiados intentos fallidos. Tu cuenta ha sido temporalmente bloqueada. Intenta más tarde o restablece tu contraseña";
        } else if (errorLower.contains("user-disabled")) {
            return "Esta cuenta ha sido deshabilitada. Contacta al administrador";
        } else if (errorLower.contains("email-already-in-use")) {
            return "Este correo electrónico ya está registrado";
        } else if (errorLower.contains("weak-password")) {
            return "La contraseña es demasiado débil";
        } else if (errorLower.contains("operation-not-allowed")) {
            return "Operación no permitida. Contacta al administrador";
        } else if (errorLower.contains("requires-recent-login")) {
            return "Esta operación requiere que inicies sesión nuevamente";
        } else {
            // Para errores desconocidos, mostrar un mensaje genérico
            Log.e("LOGIN_ERROR", "Error de Firebase Auth: " + errorMessage);
            return "Error al iniciar sesión. Verifica tus credenciales";
        }
    }

    // ✅ Método existente mejorado
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

    // ✅ Método existente
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

    // ✅ Método existente
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