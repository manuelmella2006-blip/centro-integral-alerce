package com.example.centrointegralalerce.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.utils.CitaDateValidator;
import com.example.centrointegralalerce.utils.CitaValidationDialog;
import com.example.centrointegralalerce.firebase.FirestoreRepository;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgregarActividadActivity extends AppCompatActivity {

    private static final String TAG = "AgregarActividadActivity";

    // Inputs
    private android.widget.EditText etNombreActividad, etCupo, etDiasAvisoPrevio;

    // Dropdowns
    private MaterialAutoCompleteTextView spLugar, spTipoActividad, spOferente, spSocioComunitario, spProyecto, spPeriodicidad;

    // Fechas / horas
    private Button btnFechaInicio, btnHoraInicio, btnFechaTermino, btnHoraTermino;

    // Acciones
    private Button btnGuardarActividad, btnCancelarActividad;

    // Calendario recurrente
    private LinearLayout llDiasSemana;
    private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes, cbSabado, cbDomingo;

    private FirebaseFirestore db;

    private final ArrayList<String> tipoActividadList = new ArrayList<>();
    private final ArrayList<String> tipoActividadIds = new ArrayList<>();
    private final ArrayList<String> oferentesList = new ArrayList<>();
    private final ArrayList<String> oferenteIds = new ArrayList<>();
    private final ArrayList<String> sociosList = new ArrayList<>();
    private final ArrayList<String> socioIds = new ArrayList<>();
    private final ArrayList<String> proyectosList = new ArrayList<>();
    private final ArrayList<String> proyectoIds = new ArrayList<>();
    private final ArrayList<String> lugaresList = new ArrayList<>();
    private final ArrayList<String> lugarIds = new ArrayList<>();
    private final ArrayList<String> periodicidadesList = new ArrayList<>();

    private ArrayAdapter<String> adapterTipoActividad, adapterOferente, adapterSocio, adapterProyecto, adapterLugar, adapterPeriodicidad;

    // ⚠️ Fechas seleccionadas (validadas)
    private Date fechaInicioSeleccionada;
    private Date fechaTerminoSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_actividad);

        db = FirebaseFirestore.getInstance();

        // Referencias
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

        // Permisos
        UserSession session = UserSession.getInstance();

        if (!session.permisosCargados() || !session.puede("crear_actividades")) {
            Toast.makeText(this, "No tienes permiso para crear actividades", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupEmptyAdapters();
        cargarPeriodicidad();
        cargarSpinnersDesdeFirebase();
        configurarPickers();

        btnGuardarActividad.setOnClickListener(v -> validarYGuardarActividad());

        btnCancelarActividad.setOnClickListener(v ->
                AlertManager.showDestructiveDialog(
                        this,
                        "Descartar actividad",
                        "¿Seguro que quieres descartar esta actividad?",
                        "Sí, salir",
                        this::finish
                )
        );
    }

    // ===========================================
    // ⚠️ VALIDACIÓN DE FECHAS CON DIÁLOGOS
    // ===========================================

    /**
     * Actualiza la vista del botón de fecha y muestra información adicional
     */
    private void actualizarVistaFecha(Button target, Date fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
        target.setText(sdf.format(fecha));

        // Mostrar tiempo restante
        String tiempoRestante = CitaDateValidator.getTiempoRestante(fecha);
        Log.d(TAG, "Tiempo restante: " + tiempoRestante);
    }

    /**
     * Selector de fecha con validación integrada
     */
    private void pickDate(Button target) {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {

            Calendar seleccion = Calendar.getInstance();
            seleccion.set(year, month, dayOfMonth, 0, 0, 0);
            seleccion.set(Calendar.MILLISECOND, 0);

            Date fecha = seleccion.getTime();
            boolean esInicio = (target == btnFechaInicio);

            // 1️⃣ Validar fecha pasada
            if (CitaDateValidator.esFechaPasada(fecha)) {
                CitaValidationDialog.mostrarAdvertenciaFechaPasada(
                        this,
                        fecha,
                        new CitaValidationDialog.OnValidacionListener() {
                            @Override
                            public void onConfirmado() {
                                asignarFecha(esInicio, fecha);
                                actualizarVistaFecha(target, fecha);
                            }

                            @Override
                            public void onCancelado() {
                                limpiarFecha(esInicio, target);
                            }
                        }
                );
                return;
            }

            // 2️⃣ Validar fecha muy lejana (+180 días)
            long diasFaltantes = CitaDateValidator.getDiasFaltantes(fecha);
            if (diasFaltantes > 180) {
                CitaValidationDialog.mostrarConfirmacionFechaLejana(
                        this,
                        fecha,
                        new CitaValidationDialog.OnValidacionListener() {
                            @Override
                            public void onConfirmado() {
                                asignarFecha(esInicio, fecha);
                                actualizarVistaFecha(target, fecha);
                            }

                            @Override
                            public void onCancelado() {
                                limpiarFecha(esInicio, target);
                            }
                        }
                );
                return;
            }

            // 3️⃣ Validar relación entre fecha inicio y término
            if (!esInicio && fechaInicioSeleccionada != null) {
                if (fecha.before(fechaInicioSeleccionada)) {
                    AlertManager.showErrorSnackbar(
                            AlertManager.getRootView(this),
                            "❌ La fecha término NO puede ser antes de la fecha inicio"
                    );
                    limpiarFecha(false, target);
                    return;
                }
            }

            // 4️⃣ Si es fecha inicio y ya existe término, validar coherencia
            if (esInicio && fechaTerminoSeleccionada != null) {
                if (fechaTerminoSeleccionada.before(fecha)) {
                    AlertManager.showErrorSnackbar(
                            AlertManager.getRootView(this),
                            "❌ No puedes poner fecha inicio después de fecha término"
                    );
                    limpiarFecha(true, target);
                    return;
                }
            }

            // 5️⃣ Mostrar información según el estado temporal
            Cita temp = new Cita();
            temp.setFecha(fecha);
            CitaDateValidator.EstadoTemporal estado = CitaDateValidator.getEstadoTemporal(temp);

            switch (estado) {
                case HOY:
                    AlertManager.showInfoToast(this, "📍 Esta fecha es HOY");
                    break;
                case PROXIMA_24H:
                    AlertManager.showInfoToast(this, "⏰ Esta fecha es MAÑANA");
                    break;
                case PROXIMA_SEMANA:
                    AlertManager.showSuccessToast(this, "✅ Fecha válida (próxima semana)");
                    break;
                default:
                    AlertManager.showSuccessToast(this, "✅ Fecha válida");
                    break;
            }

            // 6️⃣ Asignar y actualizar vista
            asignarFecha(esInicio, fecha);
            actualizarVistaFecha(target, fecha);

            Log.d(TAG, "Fecha seleccionada: " + fecha + " | Estado: " + estado);

        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Asigna la fecha seleccionada
     */
    private void asignarFecha(boolean esInicio, Date fecha) {
        if (esInicio) {
            fechaInicioSeleccionada = fecha;
            Log.d(TAG, "✅ Fecha inicio asignada: " + fecha);
        } else {
            fechaTerminoSeleccionada = fecha;
            Log.d(TAG, "✅ Fecha término asignada: " + fecha);
        }
    }

    /**
     * Limpia la fecha seleccionada
     */
    private void limpiarFecha(boolean esInicio, Button target) {
        if (esInicio) {
            fechaInicioSeleccionada = null;
            target.setText("Fecha inicio");
            Log.d(TAG, "🗑️ Fecha inicio limpiada");
        } else {
            fechaTerminoSeleccionada = null;
            target.setText("Fecha término");
            Log.d(TAG, "🗑️ Fecha término limpiada");
        }
    }

    // ===========================================
    // CONFIGURACIÓN DE PICKERS
    // ===========================================

    private void configurarPickers() {
        btnFechaInicio.setOnClickListener(v -> pickDate(btnFechaInicio));
        btnFechaTermino.setOnClickListener(v -> pickDate(btnFechaTermino));
        btnHoraInicio.setOnClickListener(v -> pickTime(btnHoraInicio));
        btnHoraTermino.setOnClickListener(v -> pickTime(btnHoraTermino));
    }

    private void pickTime(Button target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            target.setText(hora);
            Log.d(TAG, "Hora seleccionada: " + hora);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    // ===========================================
    // ⚠️ VALIDACIÓN FINAL Y GUARDADO
    // ===========================================

    /**
     * Valida todos los campos antes de guardar
     */
    private void validarYGuardarActividad() {
        Log.d(TAG, "🔍 Iniciando validación de actividad...");

        // Validar nombre
        String nombre = etNombreActividad.getText().toString().trim();
        if (nombre.isEmpty()) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "❌ Ingresa un nombre para la actividad");
            etNombreActividad.requestFocus();
            return;
        }

        // Validar periodicidad
        String periodicidadTxt = spPeriodicidad.getText().toString().trim();
        if (periodicidadTxt.isEmpty()) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "❌ Selecciona una periodicidad");
            spPeriodicidad.requestFocus();
            return;
        }

        // Validar fechas seleccionadas
        if (fechaInicioSeleccionada == null) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "❌ Selecciona una FECHA DE INICIO válida");
            btnFechaInicio.requestFocus();
            return;
        }

        if (fechaTerminoSeleccionada == null) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "❌ Selecciona una FECHA DE TÉRMINO válida");
            btnFechaTermino.requestFocus();
            return;
        }

        // Validar horas
        String horaInicio = btnHoraInicio.getText().toString().trim();
        String horaTermino = btnHoraTermino.getText().toString().trim();

        if (horaInicio.equals("Hora inicio") || horaInicio.isEmpty()) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "❌ Selecciona la HORA DE INICIO");
            btnHoraInicio.requestFocus();
            return;
        }

        if (horaTermino.equals("Hora término") || horaTermino.isEmpty()) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), "❌ Selecciona la HORA DE TÉRMINO");
            btnHoraTermino.requestFocus();
            return;
        }

        // ⚠️ Validación final con CitaDateValidator
        String errorValidacion = CitaDateValidator.validarFechaParaCreacion(
                fechaInicioSeleccionada,
                this
        );

        if (errorValidacion != null) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(this), errorValidacion);
            return;
        }

        // Validar relación inicio - término
        if (fechaTerminoSeleccionada.before(fechaInicioSeleccionada)) {
            AlertManager.showErrorSnackbar(
                    AlertManager.getRootView(this),
                    "❌ Fecha término no puede ser menor a fecha inicio"
            );
            return;
        }

        // Validar días de la semana si es periódica
        if (periodicidadTxt.equalsIgnoreCase("Periódica")) {
            if (!cbLunes.isChecked() && !cbMartes.isChecked() && !cbMiercoles.isChecked() &&
                    !cbJueves.isChecked() && !cbViernes.isChecked() && !cbSabado.isChecked() && !cbDomingo.isChecked()) {
                AlertManager.showErrorSnackbar(AlertManager.getRootView(this),
                        "❌ Selecciona al menos un día de la semana para actividad periódica");
                return;
            }
        }

        Log.d(TAG, "✅ Validación exitosa, procediendo a guardar...");

        // Si todas las validaciones pasaron, guardar
        guardarActividad();
    }

    /**
     * Guarda la actividad en Firebase
     */
    private void guardarActividad() {
        String nombre = etNombreActividad.getText().toString().trim();
        String periodicidadTxt = spPeriodicidad.getText().toString().trim();
        String horaInicio = btnHoraInicio.getText().toString().trim();
        String horaTermino = btnHoraTermino.getText().toString().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaInicioTxt = sdf.format(fechaInicioSeleccionada);
        String fechaTerminoTxt = sdf.format(fechaTerminoSeleccionada);

        int cupo = etCupo.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(etCupo.getText().toString());
        int diasAviso = etDiasAvisoPrevio.getText().toString().isEmpty() ? 0 :
                Integer.parseInt(etDiasAvisoPrevio.getText().toString());

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
        actividad.put("fechaInicio", fechaInicioTxt);
        actividad.put("horaInicio", horaInicio);
        actividad.put("fechaTermino", fechaTerminoTxt);
        actividad.put("horaTermino", horaTermino);

        List<Map<String, Object>> citas = generarCitas(
                periodicidadTxt,
                fechaInicioTxt,
                fechaTerminoTxt,
                horaInicio
        );

        Log.d(TAG, "📝 Guardando actividad con " + citas.size() + " citas");

        FirestoreRepository repo = new FirestoreRepository();
        repo.guardarActividadConCitas(actividad, citas)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Actividad guardada exitosamente");
                    AlertManager.showSuccessToast(this, "✅ Actividad creada correctamente");
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al guardar actividad", e);
                    AlertManager.showErrorSnackbar(AlertManager.getRootView(this),
                            "Error al guardar: " + e.getMessage());
                });
    }

    // ===========================================
    // MÉTODOS AUXILIARES
    // ===========================================

    private String getSelectedId(MaterialAutoCompleteTextView view, List<String> nombres, List<String> ids) {
        String texto = view.getText() != null ? view.getText().toString().trim() : "";
        int index = nombres.indexOf(texto);
        if (index >= 0 && index < ids.size()) return ids.get(index);
        return null;
    }

    private void setupEmptyAdapters() {
        adapterTipoActividad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tipoActividadList);
        adapterOferente = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, oferentesList);
        adapterSocio = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sociosList);
        adapterProyecto = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, proyectosList);
        adapterLugar = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lugaresList);
        adapterPeriodicidad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, periodicidadesList);

        spTipoActividad.setAdapter(adapterTipoActividad);
        spOferente.setAdapter(adapterOferente);
        spSocioComunitario.setAdapter(adapterSocio);
        spProyecto.setAdapter(adapterProyecto);
        spLugar.setAdapter(adapterLugar);
        spPeriodicidad.setAdapter(adapterPeriodicidad);
    }

    private void cargarPeriodicidad() {
        periodicidadesList.add("Puntual");
        periodicidadesList.add("Periódica");

        adapterPeriodicidad.notifyDataSetChanged();

        spPeriodicidad.setOnItemClickListener((p, v, pos, id) ->
                llDiasSemana.setVisibility(
                        periodicidadesList.get(pos).equalsIgnoreCase("Periódica")
                                ? View.VISIBLE : View.GONE
                )
        );
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
                        Log.d(TAG, "✅ Cargados " + nombres.size() + " items de " + coleccion);
                    } else {
                        Log.e(TAG, "❌ Error cargando " + coleccion, task.getException());
                    }
                });
    }

    // ===========================================
    // GENERACIÓN DE CITAS
    // ===========================================

    private List<Map<String, Object>> generarCitas(String periodicidad, String fechaInicio, String fechaTermino, String hora) {
        List<Map<String, Object>> citas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date inicio = sdf.parse(fechaInicio);
            Date fin = sdf.parse(fechaTermino);

            Calendar cal = Calendar.getInstance();
            cal.setTime(inicio);

            if (periodicidad.equalsIgnoreCase("Puntual")) {
                Map<String, Object> cita = new HashMap<>();
                cita.put("fecha", fechaInicio);
                cita.put("hora", hora);
                cita.put("estado", "programada");
                citas.add(cita);
                Log.d(TAG, "📅 Cita puntual creada: " + fechaInicio);

            } else {
                int citasCreadas = 0;
                while (!cal.getTime().after(fin)) {
                    int diaSemana = cal.get(Calendar.DAY_OF_WEEK);
                    boolean crear = false;

                    switch (diaSemana) {
                        case Calendar.MONDAY: crear = cbLunes.isChecked(); break;
                        case Calendar.TUESDAY: crear = cbMartes.isChecked(); break;
                        case Calendar.WEDNESDAY: crear = cbMiercoles.isChecked(); break;
                        case Calendar.THURSDAY: crear = cbJueves.isChecked(); break;
                        case Calendar.FRIDAY: crear = cbViernes.isChecked(); break;
                        case Calendar.SATURDAY: crear = cbSabado.isChecked(); break;
                        case Calendar.SUNDAY: crear = cbDomingo.isChecked(); break;
                    }

                    if (crear) {
                        Map<String, Object> cita = new HashMap<>();
                        cita.put("fecha", sdf.format(cal.getTime()));
                        cita.put("hora", hora);
                        cita.put("estado", "programada");
                        citas.add(cita);
                        citasCreadas++;
                    }

                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                Log.d(TAG, "📅 " + citasCreadas + " citas periódicas creadas");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error generando citas", e);
        }

        return citas;
    }
}