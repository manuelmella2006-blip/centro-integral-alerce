package com.example.centrointegralalerce.ui.mantenedores;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.firebase.FirestoreRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity para gestionar lugares (crear, editar, eliminar, listar)
 */
public class LugaresActivity extends AppCompatActivity {

    private static final String TAG = "LugaresActivity";
    private FirestoreRepository repo = new FirestoreRepository();
    private RecyclerView recyclerView;
    private LugaresAdapter adapter;
    private FloatingActionButton btnAdd;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lugares);

        // Configurar toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lugares");
        }

        // Referencias a vistas
        recyclerView = findViewById(R.id.recyclerLugares);
        btnAdd = findViewById(R.id.btnAddLugar);
        emptyState = findViewById(R.id.emptyStateLugares);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LugaresAdapter(new ArrayList<>(), new LugaresAdapter.OnItemActionListener() {
            @Override
            public void onEditarClick(LugarItem item) {
                mostrarDialogoEditar(item);
            }

            @Override
            public void onEliminarClick(String id) {
                confirmarEliminar(id);
            }
        });

        recyclerView.setAdapter(adapter);

        // Configurar FAB
        btnAdd.setOnClickListener(v -> mostrarDialogoAgregar());

        // Cargar datos
        cargarLugares();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Carga todos los lugares desde Firebase
     */
    private void cargarLugares() {
        Log.d(TAG, "🔄 Cargando lugares...");

        repo.obtenerDocumentos("lugares", snapshot -> {
            List<LugarItem> lista = new ArrayList<>();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String id = doc.getId();
                String nombre = doc.getString("nombre");

                if (nombre == null || nombre.isEmpty()) {
                    nombre = "Sin nombre";
                }

                lista.add(new LugarItem(id, nombre));
            }

            adapter.actualizarDatos(lista);

            // Mostrar/ocultar empty state
            if (lista.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "✅ Cargados " + lista.size() + " lugares");
        });
    }

    /**
     * Muestra el diálogo para agregar un nuevo lugar
     */
    private void mostrarDialogoAgregar() {
        Log.d(TAG, "🔵 Abriendo diálogo para agregar lugar");

        // Inflar layout
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_lugar, null);

        // Crear diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Obtener vistas
        EditText etNombreLugar = dialogView.findViewById(R.id.etNombreLugar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        // Validar vistas
        if (etNombreLugar == null || btnCancelar == null || btnGuardar == null) {
            Log.e(TAG, "❌ Error: No se encontraron las vistas del diálogo");
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configurar botones
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombreLugar.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombreLugar.setError("Ingresa un nombre");
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("activo", true);
            datos.put("fechaCreacion", System.currentTimeMillis());

            Log.d(TAG, "💾 Guardando lugar: " + nombre);

            repo.crearDocumento("lugares", datos)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Lugar creado exitosamente");
                        Toast.makeText(this, "✅ Lugar creado: " + nombre,
                                Toast.LENGTH_SHORT).show();
                        cargarLugares();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error al crear lugar", e);
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                    });
        });

        // Mostrar diálogo
        dialog.show();

        // Configurar ventana
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        etNombreLugar.requestFocus();
    }

    /**
     * Muestra el diálogo para editar un lugar existente
     */
    private void mostrarDialogoEditar(LugarItem item) {
        Log.d(TAG, "🔵 Abriendo diálogo para editar: " + item.getNombre());

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_lugar, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText etNombreLugar = dialogView.findViewById(R.id.etNombreLugar);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreLugar == null || btnCancelar == null || btnGuardar == null) {
            Log.e(TAG, "❌ Error: No se encontraron las vistas del diálogo");
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pre-cargar datos
        etNombreLugar.setText(item.getNombre());
        etNombreLugar.setSelection(item.getNombre().length());
        btnGuardar.setText("Actualizar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreLugar.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombreLugar.setError("Ingresa un nombre");
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Actualizando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nuevoNombre);
            datos.put("fechaModificacion", System.currentTimeMillis());

            Log.d(TAG, "💾 Actualizando lugar: " + nuevoNombre);

            repo.actualizarDocumento("lugares", item.getId(), datos)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Lugar actualizado exitosamente");
                        Toast.makeText(this, "✅ Lugar actualizado",
                                Toast.LENGTH_SHORT).show();
                        cargarLugares();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error al actualizar lugar", e);
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Actualizar");
                    });
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        etNombreLugar.requestFocus();
    }

    /**
     * Confirma antes de eliminar un lugar
     */
    private void confirmarEliminar(String id) {
        Log.d(TAG, "⚠️ Confirmando eliminación de lugar: " + id);

        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar este lugar?\n\n" +
                        "Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d(TAG, "💥 Eliminando lugar: " + id);

                    repo.eliminarDocumento("lugares", id)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "✅ Lugar eliminado exitosamente");
                                Toast.makeText(this, "✅ Eliminado correctamente",
                                        Toast.LENGTH_SHORT).show();
                                cargarLugares();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "❌ Error al eliminar lugar", e);
                                Toast.makeText(this, "Error al eliminar: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Log.d(TAG, "❌ Eliminación cancelada");
                })
                .show();
    }
}