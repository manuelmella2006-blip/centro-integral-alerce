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
import com.example.centrointegralalerce.data.Cita;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReagendarCitaActivity extends AppCompatActivity {

    private static final String TAG = "ReagendarCita";

    private TextView tvCitaInfo;
    private EditText etMotivo;
    private Button btnNuevaFecha, btnNuevaHora, btnGuardar;

    private FirebaseFirestore db;
    private String actividadId;
    private String citaId;
    private String fechaActual;
    private String horaActual;
    private String actividadNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reagendar_cita);

        // Configurar toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reagendar Cita");
        }

        // Obtener datos de la cita
        actividadId = getIntent().getStringExtra("actividadId");
        citaId = getIntent().getStringExtra("citaId");

        if (actividadId == null || citaId == null) {
            AlertManager.showErrorToast(this, "No se recibieron los datos de la cita");
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        tvCitaInfo = findViewById(R.id.tv_cita_info);
        etMotivo = findViewById(R.id.et_motivo);
        btnNuevaFecha = findViewById(R.id.btn_nueva_fecha);
        btnNuevaHora = findViewById(R.id.btn_nueva_hora);
        btnGuardar = findViewById(R.id.btn_guardar);

        // Configurar pickers
        configurarPickers();

        // Cargar datos de la cita
        cargarCita();

        // Configurar botón guardar
        btnGuardar.setOnClickListener(v -> guardarNuevaFecha());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarCita() {
        db.collection("actividades")
                .document(actividadId)
                .collection("citas")
                .document(citaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 🆕 MANEJO MANUAL DE LOS DATOS PARA EVITAR PROBLEMAS DE DESERIALIZACIÓN
                        fechaActual = documentSnapshot.getString("fecha");
                        horaActual = documentSnapshot.getString("hora");
                        actividadNombre = documentSnapshot.getString("actividadNombre");

                        String estadoCita = documentSnapshot.getString("estado");

                        if (fechaActual == null) fechaActual = "Sin fecha";
                        if (horaActual == null) horaActual = "Sin hora";
                        if (actividadNombre == null) actividadNombre = "Sin nombre";

                        // ✅ NUEVA VALIDACIÓN: Verificar si la cita está cancelada
                        if ("cancelada".equalsIgnoreCase(estadoCita)) {
                            AlertManager.showDestructiveDialog(
                                    this,
                                    "Cita Cancelada",
                                    "No se puede reagendar una cita que ha sido cancelada.",
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
                            deshabilitarControles();
                            return;
                        }

                        mostrarDatosCita();
                    } else {
                        AlertManager.showErrorToast(this, "Cita no encontrada");
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al cargar la cita: " + e.getMessage());
                    AlertManager.showErrorToast(this, "Error al cargar la cita");
                    finish();
                });
    }

    // ✅ NUEVO MÉTODO: Deshabilitar controles cuando la cita está cancelada
    private void deshabilitarControles() {
        etMotivo.setEnabled(false);
        btnNuevaFecha.setEnabled(false);
        btnNuevaHora.setEnabled(false);
        btnGuardar.setEnabled(false);
        btnGuardar.setVisibility(View.GONE);
    }

    private void mostrarDatosCita() {
        if (tvCitaInfo != null) {
            String info = "Cita: " + actividadNombre +
                    "\nFecha actual: " + fechaActual +
                    "\nHora actual: " + horaActual;
            tvCitaInfo.setText(info);

            // Establecer texto inicial en los botones con las fechas actuales
            if (!fechaActual.equals("Sin fecha")) {
                btnNuevaFecha.setText(fechaActual);
            } else {
                btnNuevaFecha.setText("Seleccionar fecha");
            }

            if (!horaActual.equals("Sin hora")) {
                btnNuevaHora.setText(horaActual);
            } else {
                btnNuevaHora.setText("Seleccionar hora");
            }
        }
    }

    private void configurarPickers() {
        btnNuevaFecha.setOnClickListener(v -> pickDate());
        btnNuevaHora.setOnClickListener(v -> pickTime());
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();

        // 🆕 Intentar parsear la fecha actual para mostrar en el picker
        if (!fechaActual.equals("Sin fecha")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                java.util.Date date = sdf.parse(fechaActual);
                if (date != null) {
                    Calendar currentDate = Calendar.getInstance();
                    currentDate.setTime(date);
                    c = currentDate;
                }
            } catch (ParseException e) {
                Log.w(TAG, "No se pudo parsear la fecha actual: " + fechaActual);
            }
        }

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            btnNuevaFecha.setText(fecha);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();

        // 🆕 Intentar parsear la hora actual para mostrar en el picker
        if (!horaActual.equals("Sin hora")) {
            try {
                String[] timeParts = horaActual.split(":");
                if (timeParts.length == 2) {
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, minute);
                }
            } catch (Exception e) {
                Log.w(TAG, "No se pudo parsear la hora actual: " + horaActual);
            }
        }

        new TimePickerDialog(this, (view, hour, minute) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            btnNuevaHora.setText(hora);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void guardarNuevaFecha() {
        String motivo = etMotivo.getText().toString().trim();
        String nuevaFecha = btnNuevaFecha.getText().toString().trim();
        String nuevaHora = btnNuevaHora.getText().toString().trim();

        // Validaciones
        if (motivo.isEmpty()) {
            Toast.makeText(this, "Debe ingresar el motivo del cambio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaFecha.isEmpty() || nuevaFecha.equals("Seleccionar fecha")) {
            Toast.makeText(this, "Debe seleccionar una nueva fecha", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nuevaHora.isEmpty() || nuevaHora.equals("Seleccionar hora")) {
            Toast.makeText(this, "Debe seleccionar una nueva hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // Actualizar solo la cita específica
        actualizarCita(nuevaFecha, nuevaHora, motivo);
    }

    private void actualizarCita(String nuevaFecha, String nuevaHora, String motivo) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fecha", nuevaFecha);  // 🆕 Guardar como String
        updates.put("hora", nuevaHora);
        updates.put("estado", "reagendada");
        updates.put("motivoReagendamiento", motivo);
        updates.put("fechaReagendamiento", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        db.collection("actividades")
                .document(actividadId)
                .collection("citas")
                .document(citaId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Cita reagendada correctamente: " + citaId);
                    Toast.makeText(this, "Cita reagendada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al actualizar la cita: " + e.getMessage());
                    Toast.makeText(this, "Error al actualizar la cita: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}