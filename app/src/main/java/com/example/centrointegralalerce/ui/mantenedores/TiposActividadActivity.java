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
    private View btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipos_actividad);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tipos de Actividad");
        }

        recyclerView = findViewById(R.id.recyclerTipos);
        btnAdd = findViewById(R.id.btnAddTipo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

            Log.d(TAG, "✅ Cargados " + lista.size() + " tipos de actividad");
        });
    }

    // ============================================================
    //     🔵 MÉTODO REEMPLAZADO COMPLETAMENTE — VERSIÓN SIMPLE
    // ============================================================

    /**
     * ✅ VERSIÓN ULTRA SIMPLE CON BUTTON Y EDITTEXT NORMALES
     */
    private void mostrarDialogoAgregar() {
        Log.d(TAG, "🔵 Iniciando mostrarDialogoAgregar()");

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_tipo, null);

        Log.d(TAG, "🔵 Layout inflado");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Log.d(TAG, "🔵 Diálogo creado");

        // Vistas normales: EditText + Button
        EditText etNombreTipo = dialogView.findViewById(R.id.etNombreTipo);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        Log.d(TAG, "🔍 Verificando vistas...");
        Log.d(TAG, "etNombreTipo: " + (etNombreTipo != null ? "OK" : "NULL"));
        Log.d(TAG, "btnCancelar: " + (btnCancelar != null ? "OK" : "NULL"));
        Log.d(TAG, "btnGuardar: " + (btnGuardar != null ? "OK" : "NULL"));

        if (etNombreTipo == null || btnCancelar == null || btnGuardar == null) {
            Log.e(TAG, "❌ ERROR: Alguna vista es null");
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancelar.setOnClickListener(v -> {
            Log.d(TAG, "🔵 Cancelar");
            dialog.dismiss();
        });

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombreTipo.getText().toString().trim();
            Log.d(TAG, "🔵 Guardar presionado - Nombre: " + nombre);

            if (nombre.isEmpty()) {
                etNombreTipo.setError("Ingresa un nombre");
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("activo", true);
            datos.put("fechaCreacion", System.currentTimeMillis());

            Log.d(TAG, "💾 Guardando en Firebase...");

            repo.crearDocumento("tiposActividad", datos)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Guardado exitoso");
                        Toast.makeText(this, "Tipo creado: " + nombre, Toast.LENGTH_SHORT).show();
                        cargarTipos();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error al guardar", e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                    });
        });

        dialog.show();
        Log.d(TAG, "🔵 Diálogo mostrado");

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        etNombreTipo.requestFocus();
    }

    // ============================================================
    //          🟦 VERSIÓN SIMPLE PARA EDITAR
    // ============================================================

    private void mostrarDialogoEditar(TipoActividadItem item) {
        Log.d(TAG, "🔵 Iniciando mostrarDialogoEditar(): " + item.getNombre());

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_agregar_tipo, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText etNombreTipo = dialogView.findViewById(R.id.etNombreTipo);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreTipo == null || btnCancelar == null || btnGuardar == null) {
            Log.e(TAG, "❌ ERROR: Vistas null en diálogo editar");
            Toast.makeText(this, "Error cargando diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        etNombreTipo.setText(item.getNombre());
        etNombreTipo.setSelection(item.getNombre().length());
        btnGuardar.setText("Actualizar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreTipo.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombreTipo.setError("Ingresa un nombre");
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Actualizando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nuevoNombre);
            datos.put("fechaModificacion", System.currentTimeMillis());

            repo.actualizarDocumento("tiposActividad", item.getId(), datos)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Actualización exitosa");
                        Toast.makeText(this, "Tipo actualizado", Toast.LENGTH_SHORT).show();
                        cargarTipos();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error al actualizar", e);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Actualizar");
                    });
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        etNombreTipo.requestFocus();
    }

    /**
     * Confirmación antes de eliminar
     */
    private void confirmarEliminar(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tipo")
                .setMessage("¿Eliminar este tipo de actividad?")
                .setPositiveButton("Eliminar", (d, w) -> {
                    repo.eliminarDocumento("tiposActividad", id)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarTipos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
