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

public class ProyectosActivity extends AppCompatActivity {

    private static final String TAG = "ProyectosActivity";
    private FirestoreRepository repo = new FirestoreRepository();
    private RecyclerView recyclerView;
    private ProyectosAdapter adapter;
    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyectos);

        recyclerView = findViewById(R.id.recyclerProyectos);
        btnAdd = findViewById(R.id.btnAddProyecto);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adaptador con funciones de editar y eliminar
        adapter = new ProyectosAdapter(new ArrayList<>(), new ProyectosAdapter.OnItemActionListener() {
            @Override
            public void onEditarClick(ProyectoItem item) {
                mostrarDialogoEditar(item);
            }

            @Override
            public void onEliminarClick(String id) {
                confirmarEliminar(id);
            }
        });

        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> mostrarDialogoAgregar());

        cargarProyectos();
    }

    // Cargar los proyectos desde Firestore
    private void cargarProyectos() {
        repo.obtenerDocumentos("proyectos", snapshot -> {
            List<ProyectoItem> lista = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String id = doc.getId();
                String nombre = doc.getString("nombre");
                String descripcion = doc.getString("descripcion");

                if (nombre == null) nombre = "Sin nombre";
                if (descripcion == null) descripcion = "";

                lista.add(new ProyectoItem(id, nombre, descripcion));
            }
            adapter.actualizarDatos(lista);
        });
    }

    // ========================================
    // DIÁLOGO PARA CREAR NUEVO PROYECTO
    // ========================================
    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_proyecto, null);

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

        EditText etNombreProyecto = dialogView.findViewById(R.id.etNombreProyecto);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreProyecto == null || etDescripcion == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombreProyecto.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombreProyecto.setError("Por favor ingresa un nombre");
                etNombreProyecto.requestFocus();
                return;
            }

            if (descripcion.isEmpty()) {
                etDescripcion.setError("Por favor ingresa una descripción");
                etDescripcion.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("descripcion", descripcion);
            datos.put("activo", true);
            datos.put("fechaCreacion", System.currentTimeMillis());

            repo.crearDocumento("proyectos", datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Proyecto creado: " + nombre, Toast.LENGTH_SHORT).show();
                        cargarProyectos();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Guardar");
                    });
        });

        dialog.show();
        etNombreProyecto.requestFocus();
    }

    // ========================================
    // DIÁLOGO PARA EDITAR PROYECTO EXISTENTE
    // ========================================
    private void mostrarDialogoEditar(ProyectoItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_proyecto, null);

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

        EditText etNombreProyecto = dialogView.findViewById(R.id.etNombreProyecto);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreProyecto == null || etDescripcion == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pre-llenar con los datos actuales
        etNombreProyecto.setText(item.getNombre());
        etNombreProyecto.setSelection(item.getNombre().length());
        etDescripcion.setText(item.getDescripcion());
        btnGuardar.setText("✓ Actualizar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreProyecto.getText().toString().trim();
            String nuevaDescripcion = etDescripcion.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombreProyecto.setError("Por favor ingresa un nombre");
                etNombreProyecto.requestFocus();
                return;
            }

            if (nuevaDescripcion.isEmpty()) {
                etDescripcion.setError("Por favor ingresa una descripción");
                etDescripcion.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Actualizando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nuevoNombre);
            datos.put("descripcion", nuevaDescripcion);
            datos.put("fechaModificacion", System.currentTimeMillis());

            repo.actualizarDocumento("proyectos", item.getId(), datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Proyecto actualizado", Toast.LENGTH_SHORT).show();
                        cargarProyectos();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Actualizar");
                    });
        });

        dialog.show();
        etNombreProyecto.requestFocus();
    }

    // ========================================
    // CONFIRMAR Y ELIMINAR PROYECTO
    // ========================================
    private void confirmarEliminar(String id) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar este proyecto?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repo.eliminarDocumento("proyectos", id)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✓ Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarProyectos();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
