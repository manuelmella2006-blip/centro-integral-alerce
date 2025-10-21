package com.example.centrointegralalerce.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AgregarActividadActivity extends AppCompatActivity {

    private EditText etNombreActividad, etCupo, etDiasAvisoPrevio, etLugar;
    private EditText etFechaInicio, etHoraInicio, etFechaTermino, etHoraTermino;
    private Spinner spTipoActividad, spOferente, spSocioComunitario, spProyecto, spPeriodicidad;
    private Button btnGuardarActividad;

    private LinearLayout llDiasSemana;
    private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo;

    private FirebaseFirestore db;

    private ArrayList<String> tipoActividadList = new ArrayList<>();
    private ArrayList<String> oferentesList = new ArrayList<>();
    private ArrayList<String> sociosList = new ArrayList<>();
    private ArrayList<String> proyectosList = new ArrayList<>();

    private ArrayList<String> tipoActividadIds = new ArrayList<>();
    private ArrayList<String> oferenteIds = new ArrayList<>();
    private ArrayList<String> socioIds = new ArrayList<>();
    private ArrayList<String> proyectoIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_actividad);

        db = FirebaseFirestore.getInstance();

        // EditText
        etNombreActividad = findViewById(R.id.etNombreActividad);
        etCupo = findViewById(R.id.etCupo);
        etDiasAvisoPrevio = findViewById(R.id.etDiasAvisoPrevio);
        etLugar = findViewById(R.id.etLugar);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etHoraInicio = findViewById(R.id.etHoraInicio);
        etFechaTermino = findViewById(R.id.etFechaTermino);
        etHoraTermino = findViewById(R.id.etHoraTermino);

        // Spinner
        spTipoActividad = findViewById(R.id.spTipoActividad);
        spOferente = findViewById(R.id.spOferente);
        spSocioComunitario = findViewById(R.id.spSocioComunitario);
        spProyecto = findViewById(R.id.spProyecto);
        spPeriodicidad = findViewById(R.id.spPeriodicidad);

        // Botón
        btnGuardarActividad = findViewById(R.id.btnGuardarActividad);

        // CheckBox días
        llDiasSemana = findViewById(R.id.llDiasSemana);
        cbLunes = findViewById(R.id.cbLunes);
        cbMartes = findViewById(R.id.cbMartes);
        cbMiercoles = findViewById(R.id.cbMiercoles);
        cbJueves = findViewById(R.id.cbJueves);
        cbViernes = findViewById(R.id.cbViernes);
        cbSabado = findViewById(R.id.cbSabado);
        cbDomingo = findViewById(R.id.cbDomingo);

        cargarPeriodicidad();
        cargarSpinnersDesdeFirebase();
        configurarPickers();

        btnGuardarActividad.setOnClickListener(v -> guardarActividad());
    }

    private void cargarPeriodicidad() {
        ArrayList<String> periodicidades = new ArrayList<>();
        periodicidades.add("Puntual");
        periodicidades.add("Periódica");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, periodicidades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPeriodicidad.setAdapter(adapter);

        spPeriodicidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String periodicidad = spPeriodicidad.getSelectedItem().toString();
                llDiasSemana.setVisibility(periodicidad.equalsIgnoreCase("Periódica") ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void cargarSpinnersDesdeFirebase() {
        cargarSpinner("tiposActividad", tipoActividadList, tipoActividadIds, spTipoActividad);
        cargarSpinner("oferentes", oferentesList, oferenteIds, spOferente);
        cargarSpinner("sociosComunitarios", sociosList, socioIds, spSocioComunitario);
        cargarSpinner("proyectos", proyectosList, proyectoIds, spProyecto);
    }

    private void cargarSpinner(String coleccion, ArrayList<String> listaNombres, ArrayList<String> listaIds, Spinner spinner) {
        db.collection(coleccion).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaNombres.clear();
                listaIds.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String nombre = doc.getString("nombre");
                    listaNombres.add(nombre != null ? nombre : "(sin nombre)");
                    listaIds.add(doc.getId());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, listaNombres);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error cargando " + coleccion, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarPickers() {
        View.OnClickListener dateListener = v -> mostrarDatePicker((EditText)v);
        View.OnClickListener timeListener = v -> mostrarTimePicker((EditText)v);

        etFechaInicio.setOnClickListener(dateListener);
        etFechaTermino.setOnClickListener(dateListener);

        etHoraInicio.setOnClickListener(timeListener);
        etHoraTermino.setOnClickListener(timeListener);
    }

    private void mostrarDatePicker(EditText et) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            et.setText(String.format("%02d/%02d/%d", dayOfMonth, month+1, year));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarTimePicker(EditText et) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            et.setText(String.format("%02d:%02d", hour, minute));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void guardarActividad() {
        String nombre = etNombreActividad.getText().toString().trim();
        String periodicidad = spPeriodicidad.getSelectedItem().toString();
        String tipoActividadId = tipoActividadIds.get(spTipoActividad.getSelectedItemPosition());
        String oferenteId = oferenteIds.get(spOferente.getSelectedItemPosition());
        String socioId = socioIds.get(spSocioComunitario.getSelectedItemPosition());
        String proyectoId = proyectoIds.size() > 0 ? proyectoIds.get(spProyecto.getSelectedItemPosition()) : null;
        String lugar = etLugar.getText().toString().trim();
        String fechaInicio = etFechaInicio.getText().toString().trim();
        String horaInicio = etHoraInicio.getText().toString().trim();
        String fechaTermino = etFechaTermino.getText().toString().trim();
        String horaTermino = etHoraTermino.getText().toString().trim();

        if (nombre.isEmpty() || fechaInicio.isEmpty() || horaInicio.isEmpty() || fechaTermino.isEmpty() || horaTermino.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos de fechas y hora", Toast.LENGTH_SHORT).show();
            return;
        }

        int cupo = etCupo.getText().toString().isEmpty() ? 0 : Integer.parseInt(etCupo.getText().toString());
        int diasAviso = etDiasAvisoPrevio.getText().toString().isEmpty() ? 0 : Integer.parseInt(etDiasAvisoPrevio.getText().toString());

        Map<String, Object> actividad = new HashMap<>();
        actividad.put("nombre", nombre);
        actividad.put("tipoActividadId", tipoActividadId);
        actividad.put("periodicidad", periodicidad);
        actividad.put("cupo", cupo);
        actividad.put("proyectoId", proyectoId);
        actividad.put("oferenteId", oferenteId);
        actividad.put("socioComunitarioId", socioId);
        actividad.put("lugar", lugar);
        actividad.put("diasAvisoPrevio", diasAviso);
        actividad.put("estado", "activa");
        actividad.put("fechaInicio", fechaInicio);
        actividad.put("horaInicio", horaInicio);
        actividad.put("fechaTermino", fechaTermino);
        actividad.put("horaTermino", horaTermino);

        db.collection("actividades").add(actividad)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Actividad guardada", Toast.LENGTH_SHORT).show();
                    crearCitas(docRef.getId(), periodicidad, fechaInicio, fechaTermino, horaInicio);
                    limpiarCampos();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void crearCitas(String actividadId, String periodicidad, String fechaInicio, String fechaTermino, String hora) {
        CollectionReference citasRef = db.collection("actividades").document(actividadId).collection("citas");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date inicio = sdf.parse(fechaInicio);
            Date fin = sdf.parse(fechaTermino);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(inicio);

            if (periodicidad.equalsIgnoreCase("Puntual")) {
                Map<String,Object> cita = new HashMap<>();
                cita.put("fecha", fechaInicio);
                cita.put("hora", hora);
                cita.put("estado","programada");
                citasRef.add(cita);
            } else {
                // Periódica: crear cita en cada día seleccionado entre fechaInicio y fechaTermino
                while(!calendar.getTime().after(fin)){
                    int diaSemana = calendar.get(Calendar.DAY_OF_WEEK); // 1=Domingo ... 7=Sábado
                    boolean crear = false;
                    switch(diaSemana){
                        case Calendar.MONDAY: crear = cbLunes.isChecked(); break;
                        case Calendar.TUESDAY: crear = cbMartes.isChecked(); break;
                        case Calendar.WEDNESDAY: crear = cbMiercoles.isChecked(); break;
                        case Calendar.THURSDAY: crear = cbJueves.isChecked(); break;
                        case Calendar.FRIDAY: crear = cbViernes.isChecked(); break;
                        case Calendar.SATURDAY: crear = cbSabado.isChecked(); break;
                        case Calendar.SUNDAY: crear = cbDomingo.isChecked(); break;
                    }
                    if(crear){
                        Map<String,Object> cita = new HashMap<>();
                        cita.put("fecha", sdf.format(calendar.getTime()));
                        cita.put("hora", hora);
                        cita.put("estado","programada");
                        citasRef.add(cita);
                    }
                    calendar.add(Calendar.DAY_OF_MONTH,1);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creando citas: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void limpiarCampos() {
        etNombreActividad.setText("");
        etCupo.setText("");
        etDiasAvisoPrevio.setText("");
        etLugar.setText("");
        etFechaInicio.setText("");
        etHoraInicio.setText("");
        etFechaTermino.setText("");
        etHoraTermino.setText("");

        spTipoActividad.setSelection(0);
        spOferente.setSelection(0);
        spSocioComunitario.setSelection(0);
        spProyecto.setSelection(0);
        spPeriodicidad.setSelection(0);

        cbLunes.setChecked(false); cbMartes.setChecked(false); cbMiercoles.setChecked(false);
        cbJueves.setChecked(false); cbViernes.setChecked(false); cbSabado.setChecked(false);
        cbDomingo.setChecked(false);
    }
}
