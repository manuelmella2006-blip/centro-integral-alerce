// EditarUsuarioActivity.java
package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditarUsuarioActivity extends AppCompatActivity {

    private static final String TAG = "EditarUsuarioActivity";

    private TextInputEditText etNombre, etEmail;
    private Spinner spinnerRol;
    private Button btnGuardar, btnCancelar;

    private FirebaseFirestore db;
    private String usuarioUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        // Verificar permisos
        if (!UserSession.getInstance().puede("gestionar_usuarios")) {
            AlertManager.showErrorToast(this, "❌ No tienes permisos para editar usuarios");
            finish();
            return;
        }

        initViews();
        setupFirebase();
        cargarDatosUsuario();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar Usuario");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etNombre = findViewById(R.id.etNombre);
        etEmail = findViewById(R.id.etEmail);
        spinnerRol = findViewById(R.id.spinnerRol);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // Obtener datos del intent
        usuarioUid = getIntent().getStringExtra("USUARIO_UID");
        String nombre = getIntent().getStringExtra("USUARIO_NOMBRE");
        String email = getIntent().getStringExtra("USUARIO_EMAIL");
        String rol = getIntent().getStringExtra("USUARIO_ROL");

        etNombre.setText(nombre);
        etEmail.setText(email);

        // Configurar spinner según el rol actual
        if (rol != null) {
            int posicion = "admin".equals(rol) ? 0 : 1;
            spinnerRol.setSelection(posicion);
        }

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void cargarDatosUsuario() {
        // Podrías cargar datos adicionales desde Firestore si es necesario
    }

    private void guardarCambios() {
        String nuevoNombre = etNombre.getText().toString().trim();
        String nuevoRol = spinnerRol.getSelectedItem().toString();

        if (nuevoNombre.isEmpty()) {
            etNombre.setError("El nombre es requerido");
            return;
        }

        // Mapear rol seleccionado a ID de Firestore
        String nuevoRolId = "Administrador".equals(nuevoRol) ? "admin" : "usuario";
        String nombreRolFirestore = "Administrador".equals(nuevoRol) ? "Administrador" : "Usuario Normal";

        Map<String, Object> actualizaciones = new HashMap<>();
        actualizaciones.put("nombre", nuevoNombre);
        actualizaciones.put("rolId", nuevoRolId);
        actualizaciones.put("nombreRol", nombreRolFirestore);
        actualizaciones.put("ultimaActualizacion", System.currentTimeMillis());

        db.collection("usuarios").document(usuarioUid)
                .update(actualizaciones)
                .addOnSuccessListener(aVoid -> {
                    AlertManager.showSuccessToast(this, "✅ Usuario actualizado correctamente");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando usuario: ", e);
                    AlertManager.showErrorToast(this, "❌ Error al actualizar usuario");
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}