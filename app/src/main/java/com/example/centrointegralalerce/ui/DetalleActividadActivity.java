package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class DetalleActividadActivity extends AppCompatActivity {

    private static final String TAG = "DetalleActividad";

    private TextView tvNombre, tvTipo, tvCupo, tvLugar, tvFechaHora, tvOferentes;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_actividad);

        tvNombre = findViewById(R.id.tv_nombre);
        tvTipo = findViewById(R.id.tv_tipo);
        tvCupo = findViewById(R.id.tv_cupo);
        tvLugar = findViewById(R.id.tv_lugar);
        tvFechaHora = findViewById(R.id.tv_fecha_hora);
        tvOferentes = findViewById(R.id.tv_oferentes);

        db = FirebaseFirestore.getInstance();

        String actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId == null || actividadId.isEmpty()) {
            Toast.makeText(this, "No se recibió la actividad", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadActividad(actividadId);
    }

    private void loadActividad(String id) {
        db.collection("actividades").document(id)
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

        mostrarActividad(actividad);
        // Resolver nombre del oferente si quieres mostrarlo
        if (actividad.getOferenteId() != null && !actividad.getOferenteId().isEmpty()) {
            loadOferenteNombre(actividad.getOferenteId());
        }
    }

    private void mostrarActividad(Actividad a) {
        tvNombre.setText(a.getNombre() != null ? a.getNombre() : "Sin nombre");
        tvTipo.setText(a.getTipoActividadId() != null ? a.getTipoActividadId() : "Sin tipo");
        tvCupo.setText(String.valueOf(a.getCupo()));
        tvLugar.setText(a.getLugar() != null ? a.getLugar() : "Sin lugar");
        String fh = ((a.getFecha() != null ? a.getFecha() : "") + " " + (a.getHora() != null ? a.getHora() : "")).trim();
        tvFechaHora.setText(fh.isEmpty() ? "Sin fecha/hora" : fh);
        // Muestra el ID del oferente como fallback inmediato
        tvOferentes.setText(a.getOferenteId() != null ? a.getOferenteId() : "Sin oferente");
    }

    private void loadOferenteNombre(String oferenteId) {
        db.collection("oferentes").document(oferenteId)
                .get()
                .addOnSuccessListener(d -> {
                    if (d.exists()) {
                        String nombre = d.getString("nombre"); // ajusta el campo real (p.ej. "nombre")
                        if (nombre != null && !nombre.isEmpty()) {
                            tvOferentes.setText(nombre);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Si falla, dejamos el ID mostrado; no es crítico
                });
    }
}
