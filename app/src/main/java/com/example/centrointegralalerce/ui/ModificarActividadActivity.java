package com.example.centrointegralalerce.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.centrointegralalerce.firebase.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModificarActividadActivity extends AppCompatActivity {

    // Text inputs simples
    private android.widget.EditText etNombreActividad, etCupo, etDiasAvisoPrevio;

    // Dropdowns tipo Material
    private MaterialAutoCompleteTextView spLugar, spTipoActividad, spOferente, spSocioComunitario, spProyecto, spPeriodicidad;

    // Botones fecha/hora
    private Button btnFechaInicio, btnHoraInicio, btnFechaTermino, btnHoraTermino;

    // Acciones finales
    private Button btnGuardarCambios, btnCancelarActividad, btnReagendarActividad;

    private LinearLayout llDiasSemana;
    private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo;

    private FirebaseFirestore db;

    // Listas
    private final ArrayList<String> tipoActividadList = new ArrayList<>();
    private final ArrayList<String> oferentesList = new ArrayList<>();
    private final ArrayList<String> sociosList = new ArrayList<>();
    private final ArrayList<String> proyectosList = new ArrayList<>();
    private final ArrayList<String> lugaresList = new ArrayList<>();
    private final ArrayList<String> periodicidadesList = new ArrayList<>();

    private final ArrayList<String> tipoActividadIds = new ArrayList<>();
    private final ArrayList<String> oferenteIds = new ArrayList<>();
    private final ArrayList<String> socioIds = new ArrayList<>();
    private final ArrayList<String> proyectoIds = new ArrayList<>();
    private final ArrayList<String> lugarIds = new ArrayList<>();

    private ArrayAdapter<String> adapterTipoActividad, adapterOferente, adapterSocio, adapterProyecto, adapterLugar, adapterPeriodicidad;

    private String actividadId;
    private Actividad actividadActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_actividad);

        db = FirebaseFirestore.getInstance();

        // ==== Obtener ID de la actividad ====
        actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId == null || actividadId.isEmpty()) {
            AlertManager.showErrorToast(this, "No se recibió la actividad a modificar");
            finish();
            return;
        }

        // Referencias UI
        etNombreActividad = findViewById(R.id.etNombreActividad);
        etCupo = findViewById(R.id.etCupo);
        etDiasAvisoPrevio = findViewById(R.id.etDiasAvisoPrevio);
        spTipoActividad = findViewById(R.id.spTipoActividad);
        spPeriodicidad = findViewById(R.id.spPeriodicidad);
        spOferente = findViewById(R.id.spOferente);
        spSocioComunitario = findViewById(R.id.spSocioComunitario);
        spProyecto = findViewById(R.id.spProyecto);
        spLugar = findViewById(R.id.spLugar);
        btnFechaInicio = findViewById(R.id.btnFechaInicio);
        btnHoraInicio = findViewById(R.id.btnHoraInicio);
        btnFechaTermino = findViewById(R.id.btnFechaTermino);
        btnHoraTermino = findViewById(R.id.btnHoraTermino);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnCancelarActividad = findViewById(R.id.btnCancelarActividad);
        btnReagendarActividad = findViewById(R.id.btnReagendarActividad);
        llDiasSemana = findViewById(R.id.llDiasSemana);
        cbLunes = findViewById(R.id.cbLunes);
        cbMartes = findViewById(R.id.cbMartes);
        cbMiercoles = findViewById(R.id.cbMiercoles);
        cbJueves = findViewById(R.id.cbJueves);
        cbViernes = findViewById(R.id.cbViernes);
        cbSabado = findViewById(R.id.cbSabado);
        cbDomingo = findViewById(R.id.cbDomingo);

        // ✅ Verificar permisos
        if (!UserSession.getInstance().puede("modificar_actividades")) {
            btnGuardarCambios.setEnabled(false);
            btnGuardarCambios.setVisibility(View.GONE);
            Toast.makeText(this, "No tienes permiso para modificar actividades", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupEmptyAdapters();
        cargarPeriodicidad();
        cargarSpinnersDesdeFirebase();
        configurarPickers();

        // Cargar datos de la actividad
        cargarActividad(actividadId);

        btnGuardarCambios.setOnClickListener(v -> guardarCambios());

        btnCancelarActividad.setOnClickListener(v -> {
            // ✅ Si existe una actividad cargada, abre CancelarActividadActivity
            if (actividadActual != null) {
                Intent intent = new Intent(ModificarActividadActivity.this, CancelarActividadActivity.class);
                intent.putExtra("actividadId", actividadId);
                intent.putExtra("nombreActividad", actividadActual.getNombre());
                startActivity(intent);
            } else {
                // Si no hay actividad cargada, muestra el diálogo de confirmación tradicional
                AlertManager.showDestructiveDialog(
                        this,
                        "Cancelar modificación",
                        "¿Seguro que quieres descartar los cambios? Se perderán las modificaciones no guardadas.",
                        "Sí, salir",
                        new AlertManager.OnConfirmListener() {
                            @Override
                            public void onConfirm() {
                                finish();
                            }

                            @Override
                            public void onCancel() {
                                AlertManager.showInfoToast(ModificarActividadActivity.this, "Continuas editando la actividad");
                            }
                        }
                );
            }
        });

        btnReagendarActividad.setOnClickListener(v -> {
            if (actividadActual != null) {
                Intent intent = new Intent(ModificarActividadActivity.this, ReagendarActividadActivity.class);
                intent.putExtra("actividadId", actividadId);
                startActivity(intent);
            }
        });

    }

    private void setupEmptyAdapters() {
        adapterTipoActividad = makeAdapter(tipoActividadList);
        adapterOferente = makeAdapter(oferentesList);
        adapterSocio = makeAdapter(sociosList);
        adapterProyecto = makeAdapter(proyectosList);
        adapterLugar = makeAdapter(lugaresList);
        adapterPeriodicidad = makeAdapter(periodicidadesList);

        spTipoActividad.setAdapter(adapterTipoActividad);
        spOferente.setAdapter(adapterOferente);
        spSocioComunitario.setAdapter(adapterSocio);
        spProyecto.setAdapter(adapterProyecto);
        spLugar.setAdapter(adapterLugar);
        spPeriodicidad.setAdapter(adapterPeriodicidad);
    }

    private ArrayAdapter<String> makeAdapter(List<String> data) {
        return new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
    }

    private void cargarPeriodicidad() {
        periodicidadesList.clear();
        periodicidadesList.add("Puntual");
        periodicidadesList.add("Periódica");
        adapterPeriodicidad.notifyDataSetChanged();

        spPeriodicidad.setOnItemClickListener((parent, view, position, id) -> {
            String periodicidad = periodicidadesList.get(position);
            llDiasSemana.setVisibility(
                    periodicidad.equalsIgnoreCase("Periódica") ? View.VISIBLE : View.GONE
            );
        });
    }

    private void cargarSpinnersDesdeFirebase() {
        cargarDropdown("tiposActividad", tipoActividadList, tipoActividadIds, adapterTipoActividad);
        cargarDropdown("oferentes", oferentesList, oferenteIds, adapterOferente);
        cargarDropdown("sociosComunitarios", sociosList, socioIds, adapterSocio);
        cargarDropdown("proyectos", proyectosList, proyectoIds, adapterProyecto);
        cargarDropdown("lugares", lugaresList, lugarIds, adapterLugar);
    }

    private void cargarDropdown(String coleccion, List<String> nombres, List<String> ids, ArrayAdapter<String> adapter) {
        db.collection(coleccion)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        nombres.clear();
                        ids.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String nombre = doc.getString("nombre");
                            if (nombre != null) {
                                nombres.add(nombre);
                                ids.add(doc.getId());
                            }
                        }
                        adapter.notifyDataSetChanged();

                        // Una vez cargados los datos, pre-cargar la actividad
                        if (actividadActual != null) {
                            precargarDatosActividad();
                        }
                    } else {
                        AlertManager.showErrorToast(this, "Error cargando " + coleccion);
                    }
                });
    }

    private void cargarActividad(String id) {
        db.collection("actividades")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        actividadActual = documentSnapshot.toObject(Actividad.class);
                        if (actividadActual != null) {
                            // Pre-cargar datos si los spinners ya están listos
                            if (!tipoActividadList.isEmpty()) {
                                precargarDatosActividad();
                            }
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

    private void precargarDatosActividad() {
        if (actividadActual == null) return;

        // Campos básicos
        etNombreActividad.setText(actividadActual.getNombre());
        etCupo.setText(String.valueOf(actividadActual.getCupo()));
        etDiasAvisoPrevio.setText(String.valueOf(actividadActual.getDiasAvisoPrevio()));

        // Fechas y horas
        btnFechaInicio.setText(actividadActual.getFechaInicio());
        btnHoraInicio.setText(actividadActual.getHoraInicio());
        btnFechaTermino.setText(actividadActual.getFechaTermino());
        btnHoraTermino.setText(actividadActual.getHoraTermino());

        // Periodicidad
        if (actividadActual.getPeriodicidad() != null) {
            spPeriodicidad.setText(actividadActual.getPeriodicidad(), false);
            llDiasSemana.setVisibility(
                    actividadActual.getPeriodicidad().equalsIgnoreCase("Periódica") ? View.VISIBLE : View.GONE
            );
        }

        // Seleccionar valores en dropdowns
        seleccionarEnDropdown(spTipoActividad, tipoActividadList, tipoActividadIds, actividadActual.getTipoActividadId());
        seleccionarEnDropdown(spOferente, oferentesList, oferenteIds, actividadActual.getOferenteId());
        seleccionarEnDropdown(spSocioComunitario, sociosList, socioIds, actividadActual.getSocioComunitarioId());
        seleccionarEnDropdown(spProyecto, proyectosList, proyectoIds, actividadActual.getProyectoId());
        seleccionarEnDropdown(spLugar, lugaresList, lugarIds, actividadActual.getLugarId());
    }

    private void seleccionarEnDropdown(MaterialAutoCompleteTextView dropdown, List<String> nombres, List<String> ids, String idBuscado) {
        if (idBuscado != null && !idBuscado.isEmpty()) {
            int index = ids.indexOf(idBuscado);
            if (index >= 0 && index < nombres.size()) {
                dropdown.setText(nombres.get(index), false);
            }
        }
    }

    private void configurarPickers() {
        btnFechaInicio.setOnClickListener(v -> pickDate(btnFechaInicio));
        btnFechaTermino.setOnClickListener(v -> pickDate(btnFechaTermino));
        btnHoraInicio.setOnClickListener(v -> pickTime(btnHoraInicio));
        btnHoraTermino.setOnClickListener(v -> pickTime(btnHoraTermino));
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

    private void guardarCambios() {
        if (actividadActual == null) return;

        String nombre = etNombreActividad.getText().toString().trim();
        String periodicidadTxt = spPeriodicidad.getText().toString().trim();
        String fechaInicio = btnFechaInicio.getText().toString().trim();
        String horaInicio = btnHoraInicio.getText().toString().trim();
        String fechaTermino = btnFechaTermino.getText().toString().trim();
        String horaTermino = btnHoraTermino.getText().toString().trim();

        if (nombre.isEmpty() || fechaInicio.isEmpty() || horaInicio.isEmpty()
                || fechaTermino.isEmpty() || horaTermino.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        int cupo = etCupo.getText().toString().isEmpty()
                ? 0
                : Integer.parseInt(etCupo.getText().toString());
        int diasAviso = etDiasAvisoPrevio.getText().toString().isEmpty()
                ? 0
                : Integer.parseInt(etDiasAvisoPrevio.getText().toString());

        // IDs Firestore
        String tipoActividadId = getSelectedId(spTipoActividad, tipoActividadList, tipoActividadIds);
        String oferenteId = getSelectedId(spOferente, oferentesList, oferenteIds);
        String socioId = getSelectedId(spSocioComunitario, sociosList, socioIds);
        String proyectoId = getSelectedId(spProyecto, proyectosList, proyectoIds);
        String lugarId = getSelectedId(spLugar, lugaresList, lugarIds);

        // Datos actualizados de la actividad
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("tipoActividadId", tipoActividadId);
        updates.put("periodicidad", periodicidadTxt);
        updates.put("cupo", cupo);
        updates.put("proyectoId", proyectoId);
        updates.put("oferenteId", oferenteId);
        updates.put("socioComunitarioId", socioId);
        updates.put("lugarId", lugarId);
        updates.put("diasAvisoPrevio", diasAviso);
        updates.put("fechaInicio", fechaInicio);
        updates.put("horaInicio", horaInicio);
        updates.put("fechaTermino", fechaTermino);
        updates.put("horaTermino", horaTermino);

        // Actualizar en Firestore
        db.collection("actividades")
                .document(actividadId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Actividad actualizada correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String getSelectedId(MaterialAutoCompleteTextView view, List<String> nombres, List<String> ids) {
        String texto = view.getText() != null ? view.getText().toString().trim() : "";
        int index = nombres.indexOf(texto);
        if (index >= 0 && index < ids.size()) return ids.get(index);
        return null;
    }
}