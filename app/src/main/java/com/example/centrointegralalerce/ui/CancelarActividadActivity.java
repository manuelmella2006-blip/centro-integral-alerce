package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CancelarActividadActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView spActividad;
    private TextInputEditText etMotivo;
    private MaterialButton btnCancelarActividad;

    private FirebaseFirestore db;

    private final ArrayList<String> actividadesList = new ArrayList<>();
    private final ArrayList<String> actividadIds = new ArrayList<>();
    private ArrayAdapter<String> adapterActividades;

    private String actividadIdPrecargada;
    private String nombreActividadPrecargada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelar_actividad);

        db = FirebaseFirestore.getInstance();

        // Referencias UI
        spActividad = findViewById(R.id.spActividad);
        etMotivo = findViewById(R.id.etMotivo);
        btnCancelarActividad = findViewById(R.id.btnCancelarActividad);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // ✅ Obtener actividad precargada si viene de Detalle o Modificar
        actividadIdPrecargada = getIntent().getStringExtra("actividadId");
        nombreActividadPrecargada = getIntent().getStringExtra("nombreActividad");

        // Verificar permisos
        if (!UserSession.getInstance().puede("cancelar_actividades")) {
            btnCancelarActividad.setEnabled(false);
            btnCancelarActividad.setVisibility(View.GONE);
            Toast.makeText(this, "No tienes permiso para cancelar actividades", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupAdapter();
        cargarActividadesActivas();

        btnCancelarActividad.setOnClickListener(v -> cancelarActividad());
    }

    private void setupAdapter() {
        adapterActividades = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, actividadesList);
        spActividad.setAdapter(adapterActividades);
    }

    private void cargarActividadesActivas() {
        db.collection("actividades")
                .whereEqualTo("estado", "activa")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        actividadesList.clear();
                        actividadIds.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String nombre = doc.getString("nombre");
                            if (nombre != null) {
                                actividadesList.add(nombre);
                                actividadIds.add(doc.getId());
                            }
                        }

                        adapterActividades.notifyDataSetChanged();

                        // ✅ Si viene con actividad precargada, seleccionarla automáticamente
                        if (actividadIdPrecargada != null && nombreActividadPrecargada != null) {
                            int index = actividadesList.indexOf(nombreActividadPrecargada);
                            if (index >= 0) {
                                spActividad.setText(nombreActividadPrecargada, false);
                            }
                        }

                        if (actividadesList.isEmpty()) {
                            Toast.makeText(this, "No hay actividades activas para cancelar", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        AlertManager.showErrorToast(this, "Error cargando actividades");
                    }
                });
    }

    private void cancelarActividad() {
        String actividadSeleccionada = spActividad.getText().toString().trim();
        String motivo = etMotivo.getText().toString().trim();

        // Validaciones
        if (actividadSeleccionada.isEmpty()) {
            Toast.makeText(this, "Seleccione una actividad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (motivo.isEmpty()) {
            Toast.makeText(this, "Ingrese el motivo de la cancelación", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Priorizar actividad precargada si existe
        String actividadId;
        if (actividadIdPrecargada != null && nombreActividadPrecargada != null &&
                nombreActividadPrecargada.equals(actividadSeleccionada)) {
            actividadId = actividadIdPrecargada;
        } else {
            int index = actividadesList.indexOf(actividadSeleccionada);
            if (index < 0 || index >= actividadIds.size()) {
                Toast.makeText(this, "Actividad no válida", Toast.LENGTH_SHORT).show();
                return;
            }
            actividadId = actividadIds.get(index);
        }

        // Mostrar diálogo de confirmación
        AlertManager.showDestructiveDialog(
                this,
                "Confirmar cancelación",
                "¿Está seguro que desea cancelar la actividad '" + actividadSeleccionada + "'? Esta acción no se puede deshacer.",
                "Sí, cancelar",
                new AlertManager.OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        procederConCancelacion(actividadId, motivo, actividadSeleccionada);
                    }

                    @Override
                    public void onCancel() {
                        AlertManager.showInfoToast(CancelarActividadActivity.this, "Cancelación abortada");
                    }
                });
    }

    private void procederConCancelacion(String actividadId, String motivo, String nombreActividad) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "cancelada");
        updates.put("motivoCancelacion", motivo);
        updates.put("fechaCancelacion", new java.util.Date());

        db.collection("actividades")
                .document(actividadId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // 🔥 CORREGIDO: Cancelar TODAS las citas asociadas correctamente
                    cancelarTodasLasCitasAsociadas(actividadId);

                    Toast.makeText(this, "Actividad '" + nombreActividad + "' cancelada correctamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();

                    // 🔥 NUEVO: Enviar resultado para actualizar la UI en otras pantallas
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cancelar la actividad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // 🔥 NUEVO MÉTODO: Cancelar TODAS las citas de la subcolección
    private void cancelarTodasLasCitasAsociadas(String actividadId) {
        db.collection("actividades")
                .document(actividadId)
                .collection("citas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> updatesCita = new HashMap<>();
                        updatesCita.put("estado", "cancelada");
                        doc.getReference().update(updatesCita);
                    }
                    Log.d("CancelarActividad", "✅ " + queryDocumentSnapshots.size() + " citas canceladas");
                })
                .addOnFailureListener(e -> {
                    Log.e("CancelarActividad", "❌ Error al cancelar citas asociadas", e);
                });
    }



    private void limpiarCampos() {
        spActividad.setText("");
        etMotivo.setText("");
    }
}