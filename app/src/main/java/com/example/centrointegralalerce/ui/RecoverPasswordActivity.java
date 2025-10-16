package com.example.centrointegralalerce.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.centrointegralalerce.R;
import com.google.firebase.auth.FirebaseAuth;

public class RecoverPasswordActivity extends AppCompatActivity {

    private EditText etEmailRecover;
    private Button btnSendRecovery;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        etEmailRecover = findViewById(R.id.etEmailRecover);
        btnSendRecovery = findViewById(R.id.btnSendRecovery);

        mAuth = FirebaseAuth.getInstance();

        btnSendRecovery.setOnClickListener(this::sendRecoveryEmail);
    }

    private void sendRecoveryEmail(View v) {
        String email = etEmailRecover.getText().toString().trim();

        if(email.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa tu correo", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(this, "Email de recuperación enviado", Toast.LENGTH_LONG).show();
                finish(); // Regresa al login
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}