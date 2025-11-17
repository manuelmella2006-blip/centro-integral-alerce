package com.example.centrointegralalerce.ui;
import android.view.View;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReagendarActividadActivity extends AppCompatActivity {

    private static final String TAG = "ReagendarActividad";

    private TextView tvActividadNombre;
    private EditText etMotivo;
    private Button btnNuevaFechaInicio, btnNuevaHoraInicio, btnNuevaFechaTermino, btnNuevaHoraTermino, btnGuardarNuevaFecha;

    private FirebaseFirestore db;
    private String actividadId;
    private Actividad actividadActual;

    // 🆕 NUEVAS VARIABLES
    private String citaId; // ID de la cita específica a reagendar
    private boolean esReagendarCita = false; // Flag para saber si es reagendar cita o actividad

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reagendar_actividad);

        // ✅ Configurar toolbar con botón volver
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 🆕 Obtener datos de la cita (si viene del diálogo)
        citaId = getIntent().getStringExtra("citaId");
        esReagendarCita = citaId != null && !citaId.isEmpty();

        // Obtener datos de la actividad
        actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId == null || actividadId.isEmpty()) {
            AlertManager.showErrorToast(this, "No se recibió la actividad a reagendar");
            finish();
            return;
        }

        // 🆕 Actualizar título según contexto
        if (esReagendarCita) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Reagendar Cita");
            }
        }

        // Resto del código sin cambios...
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        tvActividadNombre = findViewById(R.id.tv_actividad_nombre);
        etMotivo = findViewById(R.id.et_motivo);
        btnNuevaFechaInicio = findViewById(R.id.btn_nueva_fecha_inicio);
        btnNuevaHoraInicio = findViewById(R.id.btn_nueva_hora_inicio);
        btnNuevaFechaTermino = findViewById(R.id.btn_nueva_fecha_termino);
        btnNuevaHoraTermino = findViewById(R.id.btn_nueva_hora_termino);
        btnGuardarNuevaFecha = findViewById(R.id.btn_guardar_nueva_fecha);

        // Configurar pickers
        configurarPickers();

        // Cargar datos de la actividad
        cargarActividad();

        // Configurar botón guardar
        btnGuardarNuevaFecha.setOnClickListener(v -> guardarNuevaFecha());
    }

    // ✅ NUEVO: Manejar botón volver
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarActividad() {
        db.collection("actividades")
                .document(actividadId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        actividadActual = documentSnapshot.toObject(Actividad.class);
                        if (actividadActual != null) {
                            // ✅ NUEVA VALIDACIÓN: Verificar si la actividad está cancelada
                            if ("cancelada".equalsIgnoreCase(actividadActual.getEstado())) {
                                AlertManager.showDestructiveDialog(
                                        this,
                                        "Actividad Cancelada",
                                        "No se puede reagendar una actividad que ha sido cancelada.",
                                        "Aceptar",
                                        new AlertManager.OnConfirmListener() {
                                            @Override
                                            public void onConfirm() {
                                                finish();
                                            }

                                            @Override
                                            public void onCancel() {
                                                finish();
                                            }
                                        }
                                );
                                // Deshabilitar todos los controles
                                deshabilitarControles();
                                return;
                            }

                            mostrarDatosActividad();
                        } else {
                            AlertManager.showErrorToast(this, "Error al cargar datos de la actividad");
                            finish();
                        }
                    } else {
                        AlertManager.showErrorToast(this, "Actividad no encontrada");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    AlertManager.showErrorToast(this, "Error al cargar la actividad");
                    finish();
                });
    }

    // ✅ NUEVO MÉTODO: Deshabilitar controles cuando la actividad está cancelada
    private void deshabilitarControles() {
        etMotivo.setEnabled(false);
        btnNuevaFechaInicio.setEnabled(false);
        btnNuevaHoraInicio.setEnabled(false);
        btnNuevaFechaTermino.setEnabled(false);
        btnNuevaHoraTermino.setEnabled(false);
        btnGuardarNuevaFecha.setEnabled(false);
        btnGuardarNuevaFecha.setVisibility(View.GONE);
    }

    private void mostrarDatosActividad() {
        if (actividadActual != null && tvActividadNombre != null) {
            String nombre = actividadActual.getNombre() != null ?
                    actividadActual.getNombre() : "Sin nombre";
            tvActividadNombre.setText(nombre);

            // Mostrar fechas y horas actuales como referencia
            String fechaInicioActual = actividadActual.getFechaInicio() != null ?
                    actividadActual.getFechaInicio() : "No definida";
            String horaInicioActual = actividadActual.getHoraInicio() != null ?
                    actividadActual.getHoraInicio() : "No definida";
            String fechaTerminoActual = actividadActual.getFechaTermino() != null ?
                    actividadActual.getFechaTermino() : "No definida";
            String horaTerminoActual = actividadActual.getHoraTermino() != null ?
                    actividadActual.getHoraTermino() : "No definida";

            // Establecer texto inicial en los botones con las fechas actuales
            btnNuevaFechaInicio.setText(fechaInicioActual);
            btnNuevaHoraInicio.setText(horaInicioActual);
            btnNuevaFechaTermino.setText(fechaTerminoActual);
            btnNuevaHoraTermino.setText(horaTerminoActual);
        }
    }

    private void configurarPickers() {
        // Pickers para fecha/hora de inicio
        btnNuevaFechaInicio.setOnClickListener(v -> pickDate(btnNuevaFechaInicio));
        btnNuevaHoraInicio.setOnClickListener(v -> pickTime(btnNuevaHoraInicio));

        // Pickers para fecha/hora de término
        btnNuevaFechaTermino.setOnClickListener(v -> pickDate(btnNuevaFechaTermino));
        btnNuevaHoraTermino.setOnClickListener(v -> pickTime(btnNuevaHoraTermino));
    }

    private void pickDate(Button target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            target.setText(fecha);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime(Button target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            target.setText(hora);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void guardarNuevaFecha() {
        String motivo = etMotivo.getText().toString().trim();
        String nuevaFechaInicio = btnNuevaFechaInicio.getText().toString().trim();
        String nuevaHoraInicio = btnNuevaHoraInicio.getText().toString().trim();
        String nuevaFechaTermino = btnNuevaFechaTermino.getText().toString().trim();
        String nuevaHoraTermino = btnNuevaHoraTermino.getText().toString().trim();

        // Validaciones
        if (motivo.isEmpty()) {
            Toast.makeText(this, "Debe ingresar el motivo del cambio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaFechaInicio.isEmpty() || nuevaFechaInicio.equals("Fecha inicio")) {
            Toast.makeText(this, "Debe seleccionar una nueva fecha de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaHoraInicio.isEmpty() || nuevaHoraInicio.equals("Hora inicio")) {
            Toast.makeText(this, "Debe seleccionar una nueva hora de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaFechaTermino.isEmpty() || nuevaFechaTermino.equals("Fecha término")) {
            Toast.makeText(this, "Debe seleccionar una nueva fecha de término", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaHoraTermino.isEmpty() || nuevaHoraTermino.equals("Hora término")) {
            Toast.makeText(this, "Debe seleccionar una nueva hora de término", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar la actividad con el nuevo estado y fechas
        actualizarActividad(nuevaFechaInicio, nuevaHoraInicio, nuevaFechaTermino, nuevaHoraTermino, motivo);
    }

    private void actualizarActividad(String nuevaFechaInicio, String nuevaHoraInicio,
                                     String nuevaFechaTermino, String nuevaHoraTermino, String motivo) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fechaInicio", nuevaFechaInicio);
        updates.put("horaInicio", nuevaHoraInicio);
        updates.put("fechaTermino", nuevaFechaTermino);
        updates.put("horaTermino", nuevaHoraTermino);
        updates.put("estado", "reagendada");
        updates.put("motivoReagendamiento", motivo);
        updates.put("fechaReagendamiento", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        db.collection("actividades")
                .document(actividadId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // 🆕 ACTUALIZAR SOLO LA CITA ESPECÍFICA SI ES REAGENDAR CITA
                    if (esReagendarCita && citaId != null) {
                        actualizarCitaEspecifica(nuevaFechaInicio, nuevaHoraInicio);
                    } else {
                        // Actualizar todas las citas asociadas (comportamiento original)
                        actualizarCitasAsociadas(nuevaFechaInicio, nuevaHoraInicio);
                    }

                    String mensaje = esReagendarCita ?
                            "Cita reagendada correctamente" :
                            "Actividad reagendada correctamente";

                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /**
     * 🆕 Actualiza solo una cita específica en lugar de todas
     */
    private void actualizarCitaEspecifica(String nuevaFecha, String nuevaHora) {
        if (citaId == null || actividadId == null) {
            Log.e(TAG, "❌ No se puede actualizar cita específica - IDs nulos");
            return;
        }

        Map<String, Object> citaUpdates = new HashMap<>();
        citaUpdates.put("fecha", nuevaFecha);
        citaUpdates.put("hora", nuevaHora);
        citaUpdates.put("estado", "reagendada");

        db.collection("actividades")
                .document(actividadId)
                .collection("citas")
                .document(citaId)
                .update(citaUpdates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Cita específica actualizada: " + citaId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al actualizar cita específica: " + e.getMessage());
                });
    }

    private void actualizarCitasAsociadas(String nuevaFecha, String nuevaHora) {
        // Actualizar todas las citas de la subcolección
        db.collection("actividades")
                .document(actividadId)
                .collection("citas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> citaUpdates = new HashMap<>();
                        citaUpdates.put("fecha", nuevaFecha);
                        citaUpdates.put("hora", nuevaHora);

                        document.getReference().update(citaUpdates);
                    }
                    Log.d("Reagendar", "Citas actualizadas correctamente");
                })
                .addOnFailureListener(e -> {
                    Log.e("Reagendar", "Error al actualizar citas: " + e.getMessage());
                });
    }
}