package com.example.centrointegralalerce.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.centrointegralalerce.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmailRecover;
    private MaterialButton btnSendRecovery;
    private MaterialButton btnVolverLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        // Configurar toolbar con botón de retroceso
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Referencias UI
        etEmailRecover = findViewById(R.id.etEmailRecover);
        btnSendRecovery = findViewById(R.id.btnSendRecovery);
        btnVolverLogin = findViewById(R.id.btnVolverLogin);

        mAuth = FirebaseAuth.getInstance();

        // Listener para enviar email
        btnSendRecovery.setOnClickListener(this::sendRecoveryEmail);

        // Listener para volver al login
        if (btnVolverLogin != null) {
            btnVolverLogin.setOnClickListener(v -> finish());
        }
    }

    private void sendRecoveryEmail(View v) {
        String email = etEmailRecover.getText().toString().trim();

        if(email.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación básica de formato email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras procesa
        btnSendRecovery.setEnabled(false);
        btnSendRecovery.setText("Enviando...");

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            // Rehabilitar botón
            btnSendRecovery.setEnabled(true);
            btnSendRecovery.setText("Enviar Email de Recuperación");

            if(task.isSuccessful()) {
                Toast.makeText(this, "✅ Email de recuperación enviado. Revisa tu bandeja de entrada.",
                        Toast.LENGTH_LONG).show();
                finish(); // Regresa al login
            } else {
                String errorMsg = task.getException() != null
                        ? task.getException().getMessage()
                        : "Error desconocido";
                Toast.makeText(this, "❌ Error: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}