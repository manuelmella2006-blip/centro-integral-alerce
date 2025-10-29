package com.example.centrointegralalerce.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgregarActividadActivity extends AppCompatActivity {

    // Text inputs simples
    private android.widget.EditText etNombreActividad, etCupo, etDiasAvisoPrevio;

    // Dropdowns tipo Material (AutoCompleteTextView)
    private MaterialAutoCompleteTextView spLugar, spTipoActividad, spOferente, spSocioComunitario, spProyecto, spPeriodicidad;

    // Botones fecha/hora
    private Button btnFechaInicio, btnHoraInicio, btnFechaTermino, btnHoraTermino;

    // Acciones finales
    private Button btnGuardarActividad, btnCancelarActividad;

    private LinearLayout llDiasSemana;
    private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo;

    private FirebaseFirestore db;

    // Listas de nombres y IDs
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_actividad);

        db = FirebaseFirestore.getInstance();

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
        btnGuardarActividad = findViewById(R.id.btnGuardarActividad);
        btnCancelarActividad = findViewById(R.id.btnCancelarActividad);
        llDiasSemana = findViewById(R.id.llDiasSemana);
        cbLunes = findViewById(R.id.cbLunes);
        cbMartes = findViewById(R.id.cbMartes);
        cbMiercoles = findViewById(R.id.cbMiercoles);
        cbJueves = findViewById(R.id.cbJueves);
        cbViernes = findViewById(R.id.cbViernes);
        cbSabado = findViewById(R.id.cbSabado);
        cbDomingo = findViewById(R.id.cbDomingo);

        setupEmptyAdapters();
        cargarPeriodicidad();
        cargarSpinnersDesdeFirebase();
        configurarPickers();

        btnGuardarActividad.setOnClickListener(v -> guardarActividad());

        btnCancelarActividad.setOnClickListener(v -> {
            AlertManager.showDestructiveDialog(
                    this,
                    "Descartar actividad",
                    "¿Seguro que quieres descartar esta actividad? Perderás los datos no guardados.",
                    "Sí, salir",
                    new AlertManager.OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            AlertManager.showInfoToast(AgregarActividadActivity.this, "Continuas editando la actividad");
                        }
                    }
            );
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
                    } else {
                        AlertManager.showErrorToast(this, "Error cargando " + coleccion);
                    }
                });
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

    private void guardarActividad() {
        String nombre = etNombreActividad.getText().toString().trim();
        String periodicidadTxt = spPeriodicidad.getText().toString().trim();
        String fechaInicio = btnFechaInicio.getText().toString().trim();
        String horaInicio = btnHoraInicio.getText().toString().trim();
        String fechaTermino = btnFechaTermino.getText().toString().trim();
        String horaTermino = btnHoraTermino.getText().toString().trim();

        if (nombre.isEmpty() || fechaInicio.isEmpty() || horaInicio.isEmpty() || fechaTermino.isEmpty() || horaTermino.isEmpty()) {
            AlertManager.showWarningSnackbar(AlertManager.getRootView(this), "Complete todos los campos de fechas y hora ⚠️");
            return;
        }

        int cupo = etCupo.getText().toString().isEmpty() ? 0 : Integer.parseInt(etCupo.getText().toString());
        int diasAviso = etDiasAvisoPrevio.getText().toString().isEmpty() ? 0 : Integer.parseInt(etDiasAvisoPrevio.getText().toString());

        String tipoActividadId = getSelectedId(spTipoActividad, tipoActividadList, tipoActividadIds);
        String oferenteId = getSelectedId(spOferente, oferentesList, oferenteIds);
        String socioId = getSelectedId(spSocioComunitario, sociosList, socioIds);
        String proyectoId = getSelectedId(spProyecto, proyectosList, proyectoIds);
        String lugarId = getSelectedId(spLugar, lugaresList, lugarIds);

        Map<String, Object> actividad = new HashMap<>();
        actividad.put("nombre", nombre);
        actividad.put("tipoActividadId", tipoActividadId);
        actividad.put("periodicidad", periodicidadTxt);
        actividad.put("cupo", cupo);
        actividad.put("proyectoId", proyectoId);
        actividad.put("oferenteId", oferenteId);
        actividad.put("socioComunitarioId", socioId);
        actividad.put("lugarId", lugarId);
        actividad.put("diasAvisoPrevio", diasAviso);
        actividad.put("estado", "activa");
        actividad.put("fechaInicio", fechaInicio);
        actividad.put("horaInicio", horaInicio);
        actividad.put("fechaTermino", fechaTermino);
        actividad.put("horaTermino", horaTermino);

        db.collection("actividades")
                .add(actividad)
                .addOnSuccessListener(docRef -> {
                    AlertManager.showSuccessSnackbar(AlertManager.getRootView(this), "Actividad guardada correctamente ✅");
                    crearCitas(docRef.getId(), periodicidadTxt, fechaInicio, fechaTermino, horaInicio);
                    limpiarCampos();
                    finish();
                })
                .addOnFailureListener(e ->
                        AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "Error al guardar actividad: " + e.getMessage())
                );
    }

    private String getSelectedId(MaterialAutoCompleteTextView view, List<String> nombres, List<String> ids) {
        String texto = view.getText() != null ? view.getText().toString().trim() : "";
        int index = nombres.indexOf(texto);
        if (index >= 0 && index < ids.size()) return ids.get(index);
        return null;
    }

    private void crearCitas(String actividadId, String periodicidad, String fechaInicio, String fechaTermino, String hora) {
        CollectionReference citasRef = db.collection("actividades").document(actividadId).collection("citas");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date inicio = sdf.parse(fechaInicio);
            Date fin = sdf.parse(fechaTermino);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inicio);

            if (periodicidad.equalsIgnoreCase("Puntual")) {
                Map<String, Object> cita = new HashMap<>();
                cita.put("fecha", fechaInicio);
                cita.put("hora", hora);
                cita.put("estado", "programada");
                citasRef.add(cita);
            } else {
                while (!calendar.getTime().after(fin)) {
                    int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);
                    boolean crear;
                    switch (diaSemana) {
                        case Calendar.MONDAY:
                            crear = cbLunes.isChecked();
                            break;
                        case Calendar.TUESDAY:
                            crear = cbMartes.isChecked();
                            break;
                        case Calendar.WEDNESDAY:
                            crear = cbMiercoles.isChecked();
                            break;
                        case Calendar.THURSDAY:
                            crear = cbJueves.isChecked();
                            break;
                        case Calendar.FRIDAY:
                            crear = cbViernes.isChecked();
                            break;
                        case Calendar.SATURDAY:
                            crear = cbSabado.isChecked();
                            break;
                        case Calendar.SUNDAY:
                            crear = cbDomingo.isChecked();
                            break;
                        default:
                            crear = false;
                            break;
                    }

                    if (crear) {
                        Map<String, Object> cita = new HashMap<>();
                        cita.put("fecha", sdf.format(calendar.getTime()));
                        cita.put("hora", hora);
                        cita.put("estado", "programada");
                        citasRef.add(cita);
                    }

                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        } catch (ParseException e) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "Error creando citas: " + e.getMessage());
        }
    }

    private void limpiarCampos() {
        etNombreActividad.setText("");
        etCupo.setText("");
        etDiasAvisoPrevio.setText("");
        spLugar.setText("", false);
        spTipoActividad.setText("", false);
        spOferente.setText("", false);
        spSocioComunitario.setText("", false);
        spProyecto.setText("", false);
        spPeriodicidad.setText("", false);
        btnFechaInicio.setText("Fecha inicio");
        btnHoraInicio.setText("Hora inicio");
        btnFechaTermino.setText("Fecha término");
        btnHoraTermino.setText("Hora término");
        llDiasSemana.setVisibility(View.GONE);
        cbLunes.setChecked(false);
        cbMartes.setChecked(false);
        cbMiercoles.setChecked(false);
        cbJueves.setChecked(false);
        cbViernes.setChecked(false);
        cbSabado.setChecked(false);
        cbDomingo.setChecked(false);
    }
}
