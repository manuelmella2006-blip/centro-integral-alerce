package com.example.centrointegralalerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.data.UserSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetalleActividadActivity extends AppCompatActivity {

    private static final String TAG = "DetalleActividad";

    // Header
    @Nullable private TextView tvNombre;
    @Nullable private Chip chipTipo;

    // Info general
    @Nullable private TextView tvPeriodicidad;
    @Nullable private TextView tvCupo;
    @Nullable private TextView tvLugar;
    @Nullable private TextView tvFechaHora;

    // Participantes
    @Nullable private TextView tvOferentes;
    @Nullable private TextView tvSocioComunitario;
    @Nullable private TextView tvBeneficiarios;

    // Archivos
    @Nullable private RecyclerView rvArchivos;
    private TextView tvSinArchivos;

    // Botones acción
    @Nullable private MaterialButton btnModificar;
    @Nullable private MaterialButton btnCancelar;
    @Nullable private MaterialButton btnReagendar;
    @Nullable private FloatingActionButton fabAdjuntar;

    private FirebaseFirestore db;
    private String actividadId;
    private Actividad actividadActual;

    // Nuevas variables para archivos
    private ArchivosAdapter archivosAdapter;
    private final List<Map<String, Object>> archivosList = new ArrayList<>();

    // ✅ Nuevo launcher para CancelarActividadActivity
    private final ActivityResultLauncher<Intent> cancelarActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // Recargar los datos de la actividad
                            loadActividad(actividadId);
                            AlertManager.showSuccessSnackbar(
                                    AlertManager.getRootView(this),
                                    "Actividad cancelada correctamente"
                            );
                        }
                    }
            );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_actividad);

        // ==== Toolbar con back (<) ====
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // ==== Bind vistas ====
        tvNombre        = findViewById(R.id.tv_nombre);
        chipTipo        = findViewById(R.id.chip_tipo);
        tvPeriodicidad  = findViewById(R.id.tv_periodicidad);
        tvCupo          = findViewById(R.id.tv_cupo);
        tvLugar         = findViewById(R.id.tv_lugar);
        tvFechaHora     = findViewById(R.id.tv_fecha_hora);
        tvOferentes     = findViewById(R.id.tv_oferentes);
        tvSocioComunitario = findViewById(R.id.tv_socio_comunitario);
        tvBeneficiarios = findViewById(R.id.tv_beneficiarios);
        rvArchivos      = findViewById(R.id.rv_archivos);
        btnModificar    = findViewById(R.id.btn_modificar);
        btnCancelar     = findViewById(R.id.btn_cancelar);
        btnReagendar    = findViewById(R.id.btn_reagendar);
        fabAdjuntar     = findViewById(R.id.fab_adjuntar);

        // ==== Nuevo: Configurar RecyclerView para archivos ====
        tvSinArchivos = findViewById(R.id.tv_sin_archivos);

        if (rvArchivos != null) {
            archivosAdapter = new ArchivosAdapter(archivosList);
            rvArchivos.setLayoutManager(new LinearLayoutManager(this));
            rvArchivos.setAdapter(archivosAdapter);
        }

        db = FirebaseFirestore.getInstance();

        // ==== ID de la actividad ====
        actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId == null || actividadId.isEmpty()) {
            AlertManager.showErrorToast(this, "No se recibió la actividad");
            finish();
            return;
        }

        // ==== Botones acción ====
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                if (actividadActual != null) {
                    Intent intent = new Intent(DetalleActividadActivity.this, ModificarActividadActivity.class);
                    intent.putExtra("actividadId", actividadId);
                    startActivity(intent);
                }
            });
        }

        if (btnReagendar != null) {
            btnReagendar.setOnClickListener(v -> {
                if (actividadActual != null) {
                    Intent intent = new Intent(DetalleActividadActivity.this, ReagendarActividadActivity.class);
                    intent.putExtra("actividadId", actividadId);
                    startActivity(intent);
                }
            });
        }

        if (fabAdjuntar != null) {
            fabAdjuntar.setOnClickListener(v -> {
                if (actividadActual != null) {
                    Intent intent = new Intent(DetalleActividadActivity.this, AdjuntarComunicacionActivity.class);
                    intent.putExtra("actividadId", actividadId);
                    startActivity(intent);
                }
            });
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> abrirCancelarActividad());
        }

        // ==== Cargar datos desde Firestore ====
        loadActividad(actividadId);

        // 🔥 NUEVO: Verificar permisos DESPUÉS de cargar la actividad
        // Esto se hará automáticamente cuando los permisos estén listos
    }

    // ✅ NUEVO MÉTODO DE CONTROL DE PERMISOS
    private void verificarPermisos() {
        UserSession session = UserSession.getInstance();

        Log.d("DETALLE_ACTIVIDAD", "🔍 Iniciando verificación de permisos...");

        // 🔥 NUEVO: Esperar a que los permisos estén cargados
        session.esperarPermisos(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("DETALLE_ACTIVIDAD", "✅ Permisos cargados, actualizando UI");

                        boolean actividadCancelada = actividadActual != null &&
                                "cancelada".equalsIgnoreCase(actividadActual.getEstado());

                        if (btnModificar != null) {
                            boolean puedeModificar = session.puede("modificar_actividades") && !actividadCancelada;
                            btnModificar.setVisibility(puedeModificar ? View.VISIBLE : View.GONE);
                            Log.d("DETALLE_ACTIVIDAD", "🔧 Modificar visible: " + puedeModificar);
                        }

                        if (btnCancelar != null) {
                            boolean puedeCancelar = session.puede("cancelar_actividades") && !actividadCancelada;
                            btnCancelar.setVisibility(puedeCancelar ? View.VISIBLE : View.GONE);
                            Log.d("DETALLE_ACTIVIDAD", "❌ Cancelar visible: " + puedeCancelar);
                        }

                        if (btnReagendar != null) {
                            boolean puedeReagendar = session.puede("reagendar_actividades") && !actividadCancelada;
                            btnReagendar.setVisibility(puedeReagendar ? View.VISIBLE : View.GONE);
                            Log.d("DETALLE_ACTIVIDAD", "📅 Reagendar visible: " + puedeReagendar);
                        }

                        if (fabAdjuntar != null) {
                            boolean puedeAdjuntar = session.puede("adjuntar_comunicaciones") && !actividadCancelada;
                            fabAdjuntar.setVisibility(puedeAdjuntar ? View.VISIBLE : View.GONE);
                            Log.d("DETALLE_ACTIVIDAD", "📎 Adjuntar visible: " + puedeAdjuntar);
                        }

                        Log.d("DETALLE_ACTIVIDAD", "🎯 Verificación completada - Actividad cancelada: " + actividadCancelada);
                    }
                });
            }
        });
    }

    // ✅ Método existente intacto
    private void abrirCancelarActividad() {
        if (actividadActual != null) {
            Intent intent = new Intent(this, CancelarActividadActivity.class);
            intent.putExtra("actividadId", actividadId);
            intent.putExtra("nombreActividad", actividadActual.getNombre());
            cancelarActividadLauncher.launch(intent);
        }
    }

    private void loadActividad(String id) {
        db.collection("actividades")
                .document(id)
                .get()
                .addOnSuccessListener(this::onActividadCargada)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar actividad", e);
                    AlertManager.showErrorSnackbar(AlertManager.getRootView(this),
                            "Error al cargar la actividad. Intenta nuevamente.");
                    finish();
                });
    }

    private void onActividadCargada(DocumentSnapshot doc) {
        if (!doc.exists()) {
            AlertManager.showWarningToast(this, "Actividad no encontrada");
            finish();
            return;
        }

        actividadActual = doc.toObject(Actividad.class);
        if (actividadActual == null) {
            AlertManager.showErrorToast(this, "Datos de actividad inválidos");
            finish();
            return;
        }

        mostrarActividad(actividadActual);
        cargarNombresRelacionados(actividadActual);
        cargarArchivosAdjuntos(); // ✅ NUEVO: Cargar archivos

        AlertManager.showSuccessSnackbar(AlertManager.getRootView(this),
                "Actividad cargada correctamente ✅");
    }

    // ✅ NUEVO MÉTODO: Cargar archivos adjuntos
    private void cargarArchivosAdjuntos() {
        if (actividadId == null || actividadId.isEmpty()) return;

        db.collection("actividades")
                .document(actividadId)
                .collection("comunicaciones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    archivosList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> archivo = doc.getData();
                        archivo.put("id", doc.getId()); // Agregar ID del documento
                        archivosList.add(archivo);
                    }

                    // Actualizar UI
                    actualizarUIArchivos();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar archivos adjuntos", e);
                    actualizarUIArchivos();
                });
    }

    // ✅ NUEVO MÉTODO: Actualizar UI de archivos
    private void actualizarUIArchivos() {
        if (rvArchivos != null && tvSinArchivos != null) {
            if (archivosList.isEmpty()) {
                rvArchivos.setVisibility(View.GONE);
                tvSinArchivos.setVisibility(View.VISIBLE);
            } else {
                rvArchivos.setVisibility(View.VISIBLE);
                tvSinArchivos.setVisibility(View.GONE);
                archivosAdapter.setArchivosList(archivosList);
            }
        }
    }

    private void mostrarActividad(Actividad a) {
        if (tvNombre != null) {
            tvNombre.setText(a.getNombre() != null && !a.getNombre().isEmpty() ? a.getNombre() : "Sin nombre");
        }

        if (chipTipo != null) {
            chipTipo.setText(a.getTipoActividadId() != null && !a.getTipoActividadId().isEmpty()
                    ? a.getTipoActividadId() : "Sin tipo");
        }

        if (tvPeriodicidad != null) {
            String p = a.getPeriodicidad();
            tvPeriodicidad.setText(p != null && !p.isEmpty() ? p : "Sin periodicidad");
        }

        if (tvCupo != null) {
            tvCupo.setText(a.getCupo() > 0 ? a.getCupo() + " personas" : "Sin cupo");
        }

        if (tvLugar != null) {
            tvLugar.setText(a.getLugarId() != null && !a.getLugarId().isEmpty()
                    ? a.getLugarId() : "Sin lugar");
        }

        if (tvFechaHora != null) {
            String fecha = a.getFechaInicio() != null ? a.getFechaInicio() : "";
            String hora = a.getHoraInicio() != null ? a.getHoraInicio() : "";

            String fh = "";
            if (!fecha.isEmpty()) {
                fh = fecha;
            }
            if (!hora.isEmpty()) {
                fh += (fh.isEmpty() ? "" : " - ") + hora;
            }

            tvFechaHora.setText(fh.isEmpty() ? "Sin fecha/hora" : fh);
        }

        if (tvOferentes != null) {
            tvOferentes.setText(a.getOferenteId() != null && !a.getOferenteId().isEmpty()
                    ? "Cargando..." : "Sin oferente");
        }

        if (tvSocioComunitario != null) {
            tvSocioComunitario.setText(a.getSocioComunitarioId() != null && !a.getSocioComunitarioId().isEmpty()
                    ? "Cargando..." : "Sin socio comunitario");
        }

        if (tvBeneficiarios != null) {
            tvBeneficiarios.setText(a.getProyectoId() != null && !a.getProyectoId().isEmpty()
                    ? "Cargando..." : "No especificado");
        }
    }

    private void cargarNombresRelacionados(Actividad actividad) {
        if (actividad.getTipoActividadId() != null && !actividad.getTipoActividadId().isEmpty()) {
            cargarNombreDesdeColeccion("tiposActividad", actividad.getTipoActividadId(),
                    nombre -> {
                        if (chipTipo != null && nombre != null) {
                            chipTipo.setText(nombre);
                        }
                    });
        }

        if (actividad.getLugarId() != null && !actividad.getLugarId().isEmpty()) {
            cargarNombreDesdeColeccion("lugares", actividad.getLugarId(),
                    nombre -> {
                        if (tvLugar != null && nombre != null) {
                            tvLugar.setText(nombre);
                        }
                    });
        }

        if (actividad.getOferenteId() != null && !actividad.getOferenteId().isEmpty()) {
            cargarNombreDesdeColeccion("oferentes", actividad.getOferenteId(),
                    nombre -> {
                        if (tvOferentes != null && nombre != null) {
                            tvOferentes.setText(nombre);
                        }
                    });
        }

        if (actividad.getSocioComunitarioId() != null && !actividad.getSocioComunitarioId().isEmpty()) {
            cargarNombreDesdeColeccion("sociosComunitarios", actividad.getSocioComunitarioId(),
                    nombre -> {
                        if (tvSocioComunitario != null && nombre != null) {
                            tvSocioComunitario.setText(nombre);
                        }
                    });
        }

        if (actividad.getProyectoId() != null && !actividad.getProyectoId().isEmpty()) {
            cargarNombreDesdeColeccion("proyectos", actividad.getProyectoId(),
                    nombre -> {
                        if (tvBeneficiarios != null && nombre != null) {
                            tvBeneficiarios.setText(nombre);
                        }
                    });
        }
    }

    private void cargarNombreDesdeColeccion(String coleccion, String documentoId, NombreCallback callback) {
        db.collection(coleccion)
                .document(documentoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        callback.onNombreCargado(nombre);
                    } else {
                        Log.w(TAG, "Documento no encontrado en " + coleccion + ": " + documentoId);
                        callback.onNombreCargado(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar documento de " + coleccion, e);
                    callback.onNombreCargado(null);
                });
    }

    // Interface para callback de carga de nombres
    private interface NombreCallback {
        void onNombreCargado(String nombre);
    }
}