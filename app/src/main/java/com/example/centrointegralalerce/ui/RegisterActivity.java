package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etPassword, etConfirmPassword;
    private Spinner spinnerRol;
    private Button btnRegistrar;
    private TextView tvVolverLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ✅ VERIFICAR PERMISOS CORREGIDO - Solo admin puede crear usuarios
        if (!UserSession.getInstance().puede("crear_usuarios")) {
            Toast.makeText(this, "❌ No tienes permisos para crear usuarios", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerRol = findViewById(R.id.spinnerRol);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvVolverLogin = findViewById(R.id.tvVolverLogin);

        // Configurar listeners
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        tvVolverLogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String rolSeleccionado = spinnerRol.getSelectedItem().toString();

        // ✅ Validaciones (mantener las que ya tienes)
        if (nombre.isEmpty() || nombre.length() < 3) {
            etNombre.setError("El nombre debe tener al menos 3 caracteres");
            etNombre.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo válido");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rolSeleccionado.equals("Seleccionar rol") || rolSeleccionado.isEmpty()) {
            Toast.makeText(this, "Selecciona un rol de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ NUEVO: Mapear nombres de roles a IDs de Firestore
        String rolId;
        String nombreRolFirestore;

        if (rolSeleccionado.equalsIgnoreCase("Administrador")) {
            rolId = "admin";
            nombreRolFirestore = "Administrador"; // Nombre nuevo en Firestore
        } else {
            rolId = "usuario";
            nombreRolFirestore = "Usuario Normal"; // Nombre nuevo en Firestore
        }

        Log.d("REGISTER", "🎯 Guardando - Rol seleccionado: " + rolSeleccionado +
                " | RolId: " + rolId + " | Nombre en Firestore: " + nombreRolFirestore);

        // Deshabilitar botón durante el registro
        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando...");

        // ✅ NUEVO: Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // ✅ IMPORTANTE: Usar el UID de Authentication como document ID en Firestore
                    String userId = authResult.getUser().getUid();
                    Log.d("REGISTER", "✅ Usuario creado en Authentication - UID: " + userId);

                    // ✅ NUEVO: Guardar datos en Firestore usando el UID de Authentication
                    Map<String, Object> datosUsuario = new HashMap<>();
                    datosUsuario.put("nombre", nombre);
                    datosUsuario.put("email", email);
                    datosUsuario.put("rolId", rolId);
                    datosUsuario.put("nombreRol", nombreRolFirestore); // Nuevo campo para mostrar nombre amigable
                    datosUsuario.put("activo", true);
                    datosUsuario.put("fechaCreacion", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    datosUsuario.put("ultimaActualizacion", System.currentTimeMillis());

                    // ✅ USAR EL UID COMO DOCUMENT ID
                    db.collection("usuarios").document(userId)
                            .set(datosUsuario)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("REGISTER", "✅ Datos guardados en Firestore con UID: " + userId);

                                Toast.makeText(RegisterActivity.this,
                                        "¡Usuario " + nombre + " registrado correctamente!",
                                        Toast.LENGTH_SHORT).show();

                                // Limpiar formulario
                                etNombre.setText("");
                                etEmail.setText("");
                                etPassword.setText("");
                                etConfirmPassword.setText("");
                                spinnerRol.setSelection(0);

                                btnRegistrar.setEnabled(true);
                                btnRegistrar.setText("Registrar Usuario");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("REGISTER", "❌ Error al guardar en Firestore: " + e.getMessage());

                                // ✅ IMPORTANTE: Si falla Firestore, eliminar el usuario de Authentication
                                authResult.getUser().delete();

                                btnRegistrar.setEnabled(true);
                                btnRegistrar.setText("Registrar Usuario");
                                Toast.makeText(RegisterActivity.this,
                                        "Error al guardar datos: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnRegistrar.setEnabled(true);
                    btnRegistrar.setText("Registrar Usuario");

                    String errorMessage = e.getMessage();
                    Log.e("REGISTER", "❌ Error en Authentication: " + errorMessage);

                    if (errorMessage != null) {
                        if (errorMessage.contains("email address is already in use")) {
                            Toast.makeText(RegisterActivity.this,
                                    "Este correo ya está registrado",
                                    Toast.LENGTH_LONG).show();
                            etEmail.setError("Email ya registrado");
                        } else if (errorMessage.contains("network error")) {
                            Toast.makeText(RegisterActivity.this,
                                    "Error de conexión. Verifica tu internet",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Error: " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Error desconocido al registrar",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}