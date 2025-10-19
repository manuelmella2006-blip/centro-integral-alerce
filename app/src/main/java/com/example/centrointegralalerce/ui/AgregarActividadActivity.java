package com.example.centrointegralalerce.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.example.centrointegralalerce.R;

public class AgregarActividadActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etProyectos, etPeriodicidad, etTiposActividad;
    private TextInputEditText etCupo, etOferentes, etSocioComunitario, etBeneficiarios, etDiasAviso;
    private Button btnAgregarActividad;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_actividad);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        inicializarVistas();

        // Configurar botón
        btnAgregarActividad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarActividad();
            }
        });
    }

    private void inicializarVistas() {
        etNombre = findViewById(R.id.etNombre);
        etProyectos = findViewById(R.id.etProyectos);
        etPeriodicidad = findViewById(R.id.etPeriodicidad);
        etTiposActividad = findViewById(R.id.etTiposActividad);
        etCupo = findViewById(R.id.etCupo);
        etOferentes = findViewById(R.id.etOferentes);
        etSocioComunitario = findViewById(R.id.etSocioComunitario);
        etBeneficiarios = findViewById(R.id.etBeneficiarios);
        etDiasAviso = findViewById(R.id.etDiasAviso);
        btnAgregarActividad = findViewById(R.id.btnAgregarActividad);
    }

    private void agregarActividad() {
        // Obtener valores de los campos
        String nombre = etNombre.getText().toString().trim();
        String proyectos = etProyectos.getText().toString().trim();
        String periodicidad = etPeriodicidad.getText().toString().trim();
        String tiposActividad = etTiposActividad.getText().toString().trim();
        String cupoStr = etCupo.getText().toString().trim();
        String oferentes = etOferentes.getText().toString().trim();
        String socioComunitario = etSocioComunitario.getText().toString().trim();
        String beneficiarios = etBeneficiarios.getText().toString().trim();
        String diasAvisoStr = etDiasAviso.getText().toString().trim();

        // Validar campos obligatorios
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El nombre es obligatorio");
            return;
        }
        if (TextUtils.isEmpty(periodicidad)) {
            etPeriodicidad.setError("La periodicidad es obligatoria");
            return;
        }
        if (TextUtils.isEmpty(cupoStr)) {
            etCupo.setError("El cupo es obligatorio");
            return;
        }

        int cupo = Integer.parseInt(cupoStr);
        int diasAviso = TextUtils.isEmpty(diasAvisoStr) ? 0 : Integer.parseInt(diasAvisoStr);

        // Crear HashMap con los datos
        Map<String, Object> actividad = new HashMap<>();
        actividad.put("nombre", nombre);
        actividad.put("proyectos", proyectos);
        actividad.put("periodicidad", periodicidad);
        actividad.put("tiposActividad", tiposActividad);
        actividad.put("cupo", cupo);
        actividad.put("oferentesActividad", oferentes);
        actividad.put("socioComunitario", socioComunitario);
        actividad.put("beneficiariosServicio", beneficiarios);
        actividad.put("archivosAdjuntos", new ArrayList<String>());
        actividad.put("diasAvisoPrevio", diasAviso);
        actividad.put("fechaCreacion", com.google.firebase.Timestamp.now());

        // Agregar a Firestore
        db.collection("actividades")
                .add(actividad)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AgregarActividadActivity.this,
                            "Actividad agregada con ID: " + documentReference.getId(),
                            Toast.LENGTH_LONG).show();

                    // ✅ NUEVO: Indicar éxito y cerrar Activity
                    setResult(Activity.RESULT_OK);
                    finish(); // Cierra la Activity y regresa al calendario
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AgregarActividadActivity.this,
                            "Error al agregar actividad: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // ✅ NUEVO: Indicar fallo
                    setResult(Activity.RESULT_CANCELED);
                });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etProyectos.setText("");
        etPeriodicidad.setText("");
        etTiposActividad.setText("");
        etCupo.setText("");
        etOferentes.setText("");
        etSocioComunitario.setText("");
        etBeneficiarios.setText("");
        etDiasAviso.setText("");
    }
}
