package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

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

    // Botones acción
    @Nullable private MaterialButton btnModificar;
    @Nullable private MaterialButton btnCancelar;
    @Nullable private MaterialButton btnReagendar;
    @Nullable private FloatingActionButton fabAdjuntar;

    private FirebaseFirestore db;
    private String actividadId;

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
            btnModificar.setOnClickListener(v ->
                    AlertManager.showInfoToast(this, "Modificar actividad — en desarrollo 🛠️"));
        }

        if (btnReagendar != null) {
            btnReagendar.setOnClickListener(v ->
                    AlertManager.showInfoToast(this, "Reagendar actividad — próximamente 📅"));
        }

        if (fabAdjuntar != null) {
            fabAdjuntar.setOnClickListener(v ->
                    AlertManager.showInfoSnackbar(AlertManager.getRootView(this),
                            "Funcionalidad de adjuntar archivos aún no disponible 📎"));
        }

        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v ->
                    AlertManager.showDestructiveDialog(
                            this,
                            "Cancelar actividad",
                            "¿Estás seguro que deseas cancelar esta actividad?",
                            "Cancelar",
                            new AlertManager.OnConfirmListener() {
                                @Override
                                public void onConfirm() {
                                    finish();
                                    AlertManager.showSuccessToast(DetalleActividadActivity.this,
                                            "Actividad cancelada correctamente ✅");
                                }

                                @Override
                                public void onCancel() {
                                    AlertManager.showInfoToast(DetalleActividadActivity.this,
                                            "Acción cancelada");
                                }
                            }));
        }

        // ==== Cargar datos desde Firestore ====
        loadActividad(actividadId);
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

        Actividad actividad = doc.toObject(Actividad.class);
        if (actividad == null) {
            AlertManager.showErrorToast(this, "Datos de actividad inválidos");
            finish();
            return;
        }

        mostrarActividad(actividad);
        AlertManager.showSuccessSnackbar(AlertManager.getRootView(this),
                "Actividad cargada correctamente ✅");

        // Cargar oferente si aplica
        if (actividad.getOferenteId() != null && !actividad.getOferenteId().isEmpty()) {
            loadOferenteNombre(actividad.getOferenteId());
        }
    }

    private void mostrarActividad(Actividad a) {
        // ===== HEADER =====
        if (tvNombre != null) {
            tvNombre.setText(a.getNombre() != null && !a.getNombre().isEmpty() ? a.getNombre() : "Sin nombre");
        }

        if (chipTipo != null) {
            chipTipo.setText(a.getTipoActividadId() != null && !a.getTipoActividadId().isEmpty()
                    ? a.getTipoActividadId() : "Sin tipo");
        }

        // ===== INFO GENERAL =====
        if (tvPeriodicidad != null) {
            String p = a.getPeriodicidad();
            tvPeriodicidad.setText(p != null && !p.isEmpty() ? p : "Sin periodicidad");
        }

        if (tvCupo != null) {
            tvCupo.setText(a.getCupo() > 0 ? a.getCupo() + " personas" : "Sin cupo");
        }

        if (tvLugar != null) {
            tvLugar.setText(a.getLugar() != null && !a.getLugar().isEmpty() ? a.getLugar() : "Sin lugar");
        }

        if (tvFechaHora != null) {
            String fecha = a.getFecha() != null ? a.getFecha() : "";
            String hora = a.getHora() != null ? a.getHora() : "";
            String fh = (fecha + " - " + hora).trim();
            if (fh.equals("-") || fh.isEmpty()) fh = "Sin fecha/hora";
            tvFechaHora.setText(fh);
        }

        // ===== PARTICIPANTES =====
        if (tvOferentes != null) {
            tvOferentes.setText(a.getOferenteId() != null && !a.getOferenteId().isEmpty()
                    ? a.getOferenteId() : "Sin oferente");
        }

        if (tvSocioComunitario != null) {
            tvSocioComunitario.setText(a.getSocioComunitarioId() != null && !a.getSocioComunitarioId().isEmpty()
                    ? a.getSocioComunitarioId() : "Sin socio comunitario");
        }

        if (tvBeneficiarios != null) {
            String beneficiarios = a.getProyectoId();
            tvBeneficiarios.setText(beneficiarios != null && !beneficiarios.isEmpty()
                    ? beneficiarios : "No especificado");
        }

        // ===== ARCHIVOS =====
        if (rvArchivos != null) {
            List<String> adjuntos = a.getArchivosAdjuntos();
            // Aquí iría tu adapter personalizado si ya lo implementaste
        }
    }

    private void loadOferenteNombre(String oferenteId) {
        db.collection("oferentes")
                .document(oferenteId)
                .get()
                .addOnSuccessListener(d -> {
                    if (d.exists()) {
                        String nombre = d.getString("nombre");
                        if (nombre != null && !nombre.isEmpty() && tvOferentes != null) {
                            tvOferentes.setText(nombre);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "No se pudo cargar nombre de oferente: " + e.getMessage());
                    AlertManager.showWarningSnackbar(AlertManager.getRootView(this),
                            "No se pudo cargar el oferente");
                });
    }
}
