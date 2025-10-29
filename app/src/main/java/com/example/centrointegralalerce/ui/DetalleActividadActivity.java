package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
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
            toolbar.setNavigationOnClickListener(v -> {
                // simplemente volvemos a la lista anterior
                finish();
            });
        }

        // ==== Bind vistas de datos ====
        tvNombre        = findViewById(R.id.tv_nombre);
        chipTipo        = findViewById(R.id.chip_tipo);

        tvPeriodicidad  = findViewById(R.id.tv_periodicidad);
        tvCupo          = findViewById(R.id.tv_cupo);
        tvLugar         = findViewById(R.id.tv_lugar);
        tvFechaHora     = findViewById(R.id.tv_fecha_hora);

        tvOferentes         = findViewById(R.id.tv_oferentes);
        tvSocioComunitario  = findViewById(R.id.tv_socio_comunitario);
        tvBeneficiarios     = findViewById(R.id.tv_beneficiarios);

        rvArchivos      = findViewById(R.id.rv_archivos);

        // ==== Bind botones inferiores / fab ====
        btnModificar = findViewById(R.id.btn_modificar);
        btnCancelar = findViewById(R.id.btn_cancelar);
        btnReagendar = findViewById(R.id.btn_reagendar);
        fabAdjuntar = findViewById(R.id.fab_adjuntar);

        // Modificar (placeholder)
        if (btnModificar != null) {
            btnModificar.setOnClickListener(v -> {
                Toast.makeText(this, "Modificar (pendiente implementar)", Toast.LENGTH_SHORT).show();
                // acá iría lógica para editar la actividad
            });
        }

        // Reagendar (placeholder)
        if (btnReagendar != null) {
            btnReagendar.setOnClickListener(v -> {
                Toast.makeText(this, "Reagendar (pendiente implementar)", Toast.LENGTH_SHORT).show();
                // acá abrirías selector nueva fecha/hora
            });
        }

        // Adjuntar archivo (placeholder)
        if (fabAdjuntar != null) {
            fabAdjuntar.setOnClickListener(v -> {
                Toast.makeText(this, "Adjuntar archivo (pendiente implementar)", Toast.LENGTH_SHORT).show();
                // acá lanzas picker de archivos
            });
        }

        // Cancelar ABAJO (botón rojo): ahora SOLO vuelve a la lista, sin diálogo
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(v -> {
                finish(); // volvemos directamente
            });
        }

        // ==== Firestore ====
        db = FirebaseFirestore.getInstance();

        // ID de la actividad
        actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId == null || actividadId.isEmpty()) {
            Toast.makeText(this, "No se recibió la actividad", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadActividad(actividadId);
    }

    private void loadActividad(String id) {
        db.collection("actividades")
                .document(id)
                .get()
                .addOnSuccessListener(this::onActividadCargada)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar actividad: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al cargar actividad", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onActividadCargada(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Actividad no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Actividad actividad = doc.toObject(Actividad.class);
        if (actividad == null) {
            Toast.makeText(this, "Datos de actividad inválidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pintar UI con los datos cargados
        mostrarActividad(actividad);

        // Cargar nombre humano del oferente en segundo plano
        if (actividad.getOferenteId() != null && !actividad.getOferenteId().isEmpty()) {
            loadOferenteNombre(actividad.getOferenteId());
        }
    }

    private void mostrarActividad(Actividad a) {
        // ===== HEADER =====
        if (tvNombre != null) {
            String nombreTexto = a.getNombre();
            if (nombreTexto == null || nombreTexto.isEmpty()) {
                nombreTexto = "Sin nombre";
            }
            tvNombre.setText(nombreTexto);
        }

        if (chipTipo != null) {
            String tipoTexto = a.getTipoActividadId();
            if (tipoTexto == null || tipoTexto.isEmpty()) {
                tipoTexto = "Sin tipo";
            }
            chipTipo.setText(tipoTexto);
        }

        // ===== INFO GENERAL =====
        if (tvPeriodicidad != null) {
            String p = a.getPeriodicidad();
            if (p == null || p.isEmpty()) p = "Sin periodicidad";
            tvPeriodicidad.setText(p);
        }

        if (tvCupo != null) {
            tvCupo.setText(
                    a.getCupo() > 0
                            ? a.getCupo() + " personas"
                            : "Sin cupo"
            );
        }

        if (tvLugar != null) {
            String lugarTexto = a.getLugar();
            if (lugarTexto == null || lugarTexto.isEmpty()) {
                lugarTexto = "Sin lugar";
            }
            tvLugar.setText(lugarTexto);
        }

        if (tvFechaHora != null) {
            String fecha = a.getFecha() != null ? a.getFecha() : "";
            String hora  = a.getHora()  != null ? a.getHora()  : "";
            String fh = (fecha + " - " + hora).trim();

            if (fh.equals("-") || fh.equals(" - ") || fh.equals(" -") || fh.equals("- ")) {
                fh = "";
            }
            if (fh.isEmpty()) fh = "Sin fecha/hora";

            tvFechaHora.setText(fh);
        }

        // ===== PARTICIPANTES =====
        if (tvOferentes != null) {
            String oferenteTexto = a.getOferenteId();
            if (oferenteTexto == null || oferenteTexto.isEmpty()) {
                oferenteTexto = "Sin oferente";
            }
            tvOferentes.setText(oferenteTexto);
        }

        if (tvSocioComunitario != null) {
            String socio = a.getSocioComunitarioId();
            if (socio == null || socio.isEmpty()) {
                socio = "Sin socio comunitario";
            }
            tvSocioComunitario.setText(socio);
        }

        if (tvBeneficiarios != null) {
            // placeholder: usamos proyectoId como "beneficiarios"
            String beneficiarios = a.getProyectoId();
            if (beneficiarios == null || beneficiarios.isEmpty()) {
                beneficiarios = "No especificado";
            }
            tvBeneficiarios.setText(beneficiarios);
        }

        // ===== ARCHIVOS ADJUNTOS =====
        if (rvArchivos != null) {
            List<String> adjuntos = a.getArchivosAdjuntos();
            // acá iría tu adapter si ya lo tienes
        }
    }

    private void loadOferenteNombre(String oferenteId) {
        db.collection("oferentes")
                .document(oferenteId)
                .get()
                .addOnSuccessListener(d -> {
                    if (d.exists()) {
                        String nombre = d.getString("nombre"); // ajustar si el campo se llama distinto
                        if (nombre != null && !nombre.isEmpty() && tvOferentes != null) {
                            tvOferentes.setText(nombre);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "No se pudo cargar nombre de oferente: " + e.getMessage());
                    // dejamos el ID que ya estaba mostrado
                });
    }
}
