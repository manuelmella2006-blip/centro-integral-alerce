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
import com.google.android.material.chip.Chip;
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

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_actividad);

        // ==== BIND DE VISTAS SEGÚN TU XML ====
        // Header
        tvNombre        = findViewById(R.id.tv_nombre);
        chipTipo        = findViewById(R.id.chip_tipo);

        // Info general
        tvPeriodicidad  = findViewById(R.id.tv_periodicidad);
        tvCupo          = findViewById(R.id.tv_cupo);
        tvLugar         = findViewById(R.id.tv_lugar);
        tvFechaHora     = findViewById(R.id.tv_fecha_hora);

        // Participantes
        tvOferentes         = findViewById(R.id.tv_oferentes);
        tvSocioComunitario  = findViewById(R.id.tv_socio_comunitario);
        tvBeneficiarios     = findViewById(R.id.tv_beneficiarios);

        // Archivos
        rvArchivos      = findViewById(R.id.rv_archivos);
        // (no configuramos adapter aquí todavía porque eso depende de tu equipo de datos)

        // ==== TOOLBAR: opcionalmente habilitar back ====
        // como tu XML tiene un MaterialToolbar con id @+id/toolbar, lo enganchamos:
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        db = FirebaseFirestore.getInstance();

        // Obtenemos el ID de la actividad desde el intent
        String actividadId = getIntent().getStringExtra("actividadId");
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

        // Rellenar UI con lo que tenemos
        mostrarActividad(actividad);

        // En paralelo resolvemos nombre humano del oferente
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

        // chip_tipo ← tipoActividadId
        if (chipTipo != null) {
            String tipoTexto = a.getTipoActividadId();
            if (tipoTexto == null || tipoTexto.isEmpty()) {
                tipoTexto = "Sin tipo";
            }
            chipTipo.setText(tipoTexto);
        }

        // ===== INFO GENERAL =====

        // tv_periodicidad
        if (tvPeriodicidad != null) {
            String p = a.getPeriodicidad();
            if (p == null || p.isEmpty()) p = "Sin periodicidad";
            tvPeriodicidad.setText(p);
        }

        // tv_cupo
        if (tvCupo != null) {
            tvCupo.setText(
                    a.getCupo() > 0
                            ? a.getCupo() + " personas"
                            : "Sin cupo"
            );
        }

        // tv_lugar
        if (tvLugar != null) {
            String lugarTexto = a.getLugar();
            if (lugarTexto == null || lugarTexto.isEmpty()) {
                lugarTexto = "Sin lugar";
            }
            tvLugar.setText(lugarTexto);
        }

        // tv_fecha_hora
        if (tvFechaHora != null) {
            String fecha = a.getFecha() != null ? a.getFecha() : "";
            String hora  = a.getHora()  != null ? a.getHora()  : "";
            String fh = (fecha + " - " + hora).trim();

            // limpia el caso " - " (sin datos reales)
            if (fh.equals("-") || fh.equals(" - ") || fh.equals(" -") || fh.equals("- ")) {
                fh = "";
            }

            if (fh.isEmpty()) fh = "Sin fecha/hora";

            tvFechaHora.setText(fh);
        }

        // ===== PARTICIPANTES =====

        // oferente
        if (tvOferentes != null) {
            String oferenteTexto = a.getOferenteId();
            if (oferenteTexto == null || oferenteTexto.isEmpty()) {
                oferenteTexto = "Sin oferente";
            }
            tvOferentes.setText(oferenteTexto);
        }

        // socio comunitario
        if (tvSocioComunitario != null) {
            String socio = a.getSocioComunitarioId();
            if (socio == null || socio.isEmpty()) {
                socio = "Sin socio comunitario";
            }
            tvSocioComunitario.setText(socio);
        }

        // beneficiarios
        if (tvBeneficiarios != null) {
            // ⚠ Tu modelo Actividad NO tiene getBeneficiarios() como lista/string.
            // Podemos usar estado o proyectoId como placeholder temporal
            String beneficiarios = a.getProyectoId();
            if (beneficiarios == null || beneficiarios.isEmpty()) {
                beneficiarios = "No especificado";
            }
            tvBeneficiarios.setText(beneficiarios);
        }

        // ===== ARCHIVOS ADJUNTOS =====
        if (rvArchivos != null) {
            List<String> adjuntos = a.getArchivosAdjuntos();
            // Aquí normalmente setearías un adapter tipo ArchivosAdapter(adjuntos)
            // y mostrarías card_archivos visible solo si hay adjuntos.
            // Como aún no me pasas el adapter, no lo toco para que compile.
        }
    }

    private void loadOferenteNombre(String oferenteId) {
        db.collection("oferentes")
                .document(oferenteId)
                .get()
                .addOnSuccessListener(d -> {
                    if (d.exists()) {
                        String nombre = d.getString("nombre"); // Ajusta al nombre real del campo de Firestore
                        if (nombre != null && !nombre.isEmpty()) {
                            if (tvOferentes != null) {
                                tvOferentes.setText(nombre);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "No se pudo cargar nombre de oferente: " + e.getMessage());
                    // Silencioso; dejamos el ID que ya mostramos
                });
    }
}
