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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
        tvVolverLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String rol = spinnerRol.getSelectedItem().toString();

        // Validaciones
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa tu nombre completo");
            etNombre.requestFocus();
            return;
        }

        if (nombre.length() < 3) {
            etNombre.setError("El nombre debe tener al menos 3 caracteres");
            etNombre.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu correo electrónico");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo válido");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingresa una contraseña");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirma tu contraseña");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rol.equals("Seleccionar rol") || rol.isEmpty()) {
            Toast.makeText(this, "Selecciona un rol de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón durante el registro
        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Registrando...");

        // Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String userId = auth.getCurrentUser().getUid();

                        // Guardar datos adicionales en Firestore
                        Map<String, Object> datosUsuario = new HashMap<>();
                        datosUsuario.put("nombre", nombre);
                        datosUsuario.put("email", email);
                        datosUsuario.put("rolId", rol);
                        datosUsuario.put("fechaRegistro", com.google.firebase.firestore.FieldValue.serverTimestamp());

                        db.collection("usuarios").document(userId)

                                .set(datosUsuario)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this,
                                            "¡Usuario registrado correctamente!",
                                            Toast.LENGTH_SHORT).show();

                                    // Volver al login
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    intent.putExtra("EMAIL_REGISTRADO", email);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnRegistrar.setEnabled(true);
                                    btnRegistrar.setText("Registrar Usuario");
                                    Toast.makeText(RegisterActivity.this,
                                            "Error al guardar datos: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Registrar Usuario");

                        String errorMessage = e.getMessage();
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
                    }
                });
    }

    @Override
    public void onBackPressed() {
        // Volver al login al presionar back
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}