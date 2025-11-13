package com.example.centrointegralalerce.ui.mantenedores;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.firebase.FirestoreRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TiposActividadActivity extends AppCompatActivity {

    private static final String TAG = "TiposActividadActivity";
    private FirestoreRepository repo = new FirestoreRepository();
    private RecyclerView recyclerView;
    private TiposActividadAdapter adapter;
    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipos_actividad);

        recyclerView = findViewById(R.id.recyclerTipos);
        btnAdd = findViewById(R.id.btnAddTipo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adaptador con funciones de editar y eliminar
        adapter = new TiposActividadAdapter(new ArrayList<>(), new TiposActividadAdapter.OnItemActionListener() {
            @Override
            public void onEditarClick(TipoActividadItem item) {
                mostrarDialogoEditar(item);
            }

            @Override
            public void onEliminarClick(String id) {
                confirmarEliminar(id);
            }
        });

        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> mostrarDialogoAgregar());

        cargarTipos();
    }

    // Cargar los tipos desde Firestore
    private void cargarTipos() {
        repo.obtenerDocumentos("tiposActividad", snapshot -> {
            List<TipoActividadItem> lista = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String id = doc.getId();
                String nombre = doc.getString("nombre");
                if (nombre == null) nombre = "Sin nombre";
                lista.add(new TipoActividadItem(id, nombre));
            }
            adapter.actualizarDatos(lista);
        });
    }

    // ========================================
    // DIÁLOGO PARA CREAR NUEVO TIPO
    // ========================================
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_tipo, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar ventana del diálogo", e);
        }

        EditText etNombreTipo = dialogView.findViewById(R.id.etNombreTipo);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreTipo == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombreTipo.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombreTipo.setError("Por favor ingresa un nombre");
                etNombreTipo.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("activo", true);
            datos.put("fechaCreacion", System.currentTimeMillis());

            repo.crearDocumento("tiposActividad", datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Tipo creado: " + nombre, Toast.LENGTH_SHORT).show();
                        cargarTipos();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Guardar");
                    });
        });

        dialog.show();
        etNombreTipo.requestFocus();
    }

    // ========================================
    // DIÁLOGO PARA EDITAR TIPO EXISTENTE
    // ========================================
    private void mostrarDialogoEditar(TipoActividadItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_tipo, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar ventana del diálogo", e);
        }

        EditText etNombreTipo = dialogView.findViewById(R.id.etNombreTipo);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreTipo == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pre-llenar con el nombre actual
        etNombreTipo.setText(item.getNombre());
        etNombreTipo.setSelection(item.getNombre().length()); // Cursor al final
        btnGuardar.setText("✓ Actualizar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreTipo.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombreTipo.setError("Por favor ingresa un nombre");
                etNombreTipo.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Actualizando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nuevoNombre);
            datos.put("fechaModificacion", System.currentTimeMillis());

            repo.actualizarDocumento("tiposActividad", item.getId(), datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Tipo actualizado", Toast.LENGTH_SHORT).show();
                        cargarTipos();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Actualizar");
                    });
        });

        dialog.show();
        etNombreTipo.requestFocus();
    }

    // ========================================
    // CONFIRMAR Y ELIMINAR TIPO
    // ========================================
    private void confirmarEliminar(String id) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar este tipo de actividad?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repo.eliminarDocumento("tiposActividad", id)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✓ Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarTipos();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}