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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
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
    private MaterialAutoCompleteTextView spLugar;
    private MaterialAutoCompleteTextView spTipoActividad;
    private MaterialAutoCompleteTextView spOferente;
    private MaterialAutoCompleteTextView spSocioComunitario;
    private MaterialAutoCompleteTextView spProyecto;
    private MaterialAutoCompleteTextView spPeriodicidad;

    // Botones fecha/hora
    private Button btnFechaInicio, btnHoraInicio, btnFechaTermino, btnHoraTermino;

    // Acciones finales
    private Button btnGuardarActividad;
    private Button btnCancelarActividad;

    private LinearLayout llDiasSemana;
    private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo;

    private FirebaseFirestore db;

    // Listas de nombres mostrados en cada dropdown
    private final ArrayList<String> tipoActividadList = new ArrayList<>();
    private final ArrayList<String> oferentesList = new ArrayList<>();
    private final ArrayList<String> sociosList = new ArrayList<>();
    private final ArrayList<String> proyectosList = new ArrayList<>();
    private final ArrayList<String> lugaresList = new ArrayList<>();
    private final ArrayList<String> periodicidadesList = new ArrayList<>();

    // Listas paralelas de IDs reales en Firestore
    private final ArrayList<String> tipoActividadIds = new ArrayList<>();
    private final ArrayList<String> oferenteIds = new ArrayList<>();
    private final ArrayList<String> socioIds = new ArrayList<>();
    private final ArrayList<String> proyectoIds = new ArrayList<>();
    private final ArrayList<String> lugarIds = new ArrayList<>();

    // Adaptadores para los dropdowns
    private ArrayAdapter<String> adapterTipoActividad;
    private ArrayAdapter<String> adapterOferente;
    private ArrayAdapter<String> adapterSocio;
    private ArrayAdapter<String> adapterProyecto;
    private ArrayAdapter<String> adapterLugar;
    private ArrayAdapter<String> adapterPeriodicidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_actividad);

        db = FirebaseFirestore.getInstance();

        // Text inputs libres
        etNombreActividad = findViewById(R.id.etNombreActividad);
        etCupo = findViewById(R.id.etCupo);
        etDiasAvisoPrevio = findViewById(R.id.etDiasAvisoPrevio);

        // Dropdowns Material
        spTipoActividad = findViewById(R.id.spTipoActividad);
        spPeriodicidad = findViewById(R.id.spPeriodicidad);
        spOferente = findViewById(R.id.spOferente);
        spSocioComunitario = findViewById(R.id.spSocioComunitario);
        spProyecto = findViewById(R.id.spProyecto);
        spLugar = findViewById(R.id.spLugar);

        // Botones fecha / hora
        btnFechaInicio = findViewById(R.id.btnFechaInicio);
        btnHoraInicio = findViewById(R.id.btnHoraInicio);
        btnFechaTermino = findViewById(R.id.btnFechaTermino);
        btnHoraTermino = findViewById(R.id.btnHoraTermino);

        // Botones finales
        btnGuardarActividad = findViewById(R.id.btnGuardarActividad);
        btnCancelarActividad = findViewById(R.id.btnCancelarActividad);

        // CheckBox días
        llDiasSemana = findViewById(R.id.llDiasSemana);
        cbLunes = findViewById(R.id.cbLunes);
        cbMartes = findViewById(R.id.cbMartes);
        cbMiercoles = findViewById(R.id.cbMiercoles);
        cbJueves = findViewById(R.id.cbJueves);
        cbViernes = findViewById(R.id.cbViernes);
        cbSabado = findViewById(R.id.cbSabado);
        cbDomingo = findViewById(R.id.cbDomingo);

        // Adapters vacíos iniciales
        setupEmptyAdapters();

        // Llenar Periodicidad (local)
        cargarPeriodicidad();

        // Llenar los demás dropdowns desde Firestore
        cargarSpinnersDesdeFirebase();

        // Date/Time pickers para los botones
        configurarPickers();

        // Guardar en Firestore
        btnGuardarActividad.setOnClickListener(v -> guardarActividad());

        // Cancelar: mostrar confirmación antes de descartar
        btnCancelarActividad.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Descartar actividad")
                    .setMessage("¿Seguro que quieres descartar esta actividad? Perderás los datos no guardados.")
                    .setPositiveButton("Sí, salir", (dialog, which) -> finish())
                    .setNegativeButton("Seguir editando", null)
                    .show();
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

    /**
     * Periodicidad es local (Puntual / Periódica)
     */
    private void cargarPeriodicidad() {
        periodicidadesList.clear();
        periodicidadesList.add("Puntual");
        periodicidadesList.add("Periódica");

        adapterPeriodicidad.notifyDataSetChanged();

        // Mostrar/ocultar la fila de días según elección
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

    /**
     * Llena un dropdown Material con datos desde Firestore
     */
    private void cargarDropdown(
            String coleccion,
            List<String> listaNombres,
            List<String> listaIds,
            ArrayAdapter<String> adapter
    ) {

        db.collection(coleccion)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaNombres.clear();
                        listaIds.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String nombre = doc.getString("nombre");
                            if (nombre != null) {
                                listaNombres.add(nombre);
                                listaIds.add(doc.getId());
                            }
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error cargando " + coleccion, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Configura listeners para abrir DatePicker / TimePicker
     * y setear el texto en los botones.
     */
    private void configurarPickers() {
        btnFechaInicio.setOnClickListener(v -> pickDate(btnFechaInicio));
        btnFechaTermino.setOnClickListener(v -> pickDate(btnFechaTermino));

        btnHoraInicio.setOnClickListener(v -> pickTime(btnHoraInicio));
        btnHoraTermino.setOnClickListener(v -> pickTime(btnHoraTermino));
    }

    private void pickDate(Button target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            dayOfMonth, month + 1, year);
                    target.setText(fecha);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTime(Button target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    String hora = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    target.setText(hora);
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void guardarActividad() {
        String nombre = etNombreActividad.getText().toString().trim();
        String periodicidadTxt = spPeriodicidad.getText().toString().trim();
        String fechaInicio = btnFechaInicio.getText().toString().trim();
        String horaInicio = btnHoraInicio.getText().toString().trim();
        String fechaTermino = btnFechaTermino.getText().toString().trim();
        String horaTermino = btnHoraTermino.getText().toString().trim();

        if (nombre.isEmpty() ||
                fechaInicio.isEmpty() ||
                horaInicio.isEmpty() ||
                fechaTermino.isEmpty() ||
                horaTermino.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos de fechas y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        // cupo / diasAvisoPrevio
        int cupo = etCupo.getText().toString().isEmpty()
                ? 0
                : Integer.parseInt(etCupo.getText().toString());

        int diasAviso = etDiasAvisoPrevio.getText().toString().isEmpty()
                ? 0
                : Integer.parseInt(etDiasAvisoPrevio.getText().toString());

        // Mapear selección visible -> id Firestore
        String tipoActividadId = getSelectedId(spTipoActividad, tipoActividadList, tipoActividadIds);
        String oferenteId = getSelectedId(spOferente, oferentesList, oferenteIds);
        String socioId = getSelectedId(spSocioComunitario, sociosList, socioIds);
        String proyectoId = getSelectedId(spProyecto, proyectosList, proyectoIds); // opcional
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
                    Toast.makeText(this, "Actividad guardada", Toast.LENGTH_SHORT).show();
                    crearCitas(
                            docRef.getId(),
                            periodicidadTxt,
                            fechaInicio,
                            fechaTermino,
                            horaInicio
                    );
                    limpiarCampos();
                    finish(); // después de guardar, volvemos atrás
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Error al guardar: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    /**
     * Dado un dropdown y sus listas paralelas, devuelve el ID elegido en Firestore.
     */
    private String getSelectedId(MaterialAutoCompleteTextView view,
                                 List<String> nombres,
                                 List<String> ids) {
        String texto = view.getText() != null ? view.getText().toString().trim() : "";
        int index = nombres.indexOf(texto);
        if (index >= 0 && index < ids.size()) {
            return ids.get(index);
        }
        return null;
    }

    private void crearCitas(String actividadId,
                            String periodicidad,
                            String fechaInicio,
                            String fechaTermino,
                            String hora) {

        CollectionReference citasRef = db
                .collection("actividades")
                .document(actividadId)
                .collection("citas");

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
                // Periódica: generar citas en los días marcados
                while (!calendar.getTime().after(fin)) {
                    int diaSemana = calendar.get(Calendar.DAY_OF_WEEK); // 1=Dom, 2=Lun...
                    boolean crear = false;
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
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "Error creando citas: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
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
