package com.example.centrointegralalerce.ui.mantenedores;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OferentesActivity extends AppCompatActivity {

    private static final String TAG = "OferentesActivity";
    private FirestoreRepository repo = new FirestoreRepository();
    private RecyclerView recyclerView;
    private OferentesAdapter adapter;
    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferentes);

        // ✅ Configurar toolbar con botón volver
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerOferentes);
        btnAdd = findViewById(R.id.btnAddOferente);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OferentesAdapter(new ArrayList<>(), new OferentesAdapter.OnItemActionListener() {
            @Override
            public void onEditarClick(OferenteItem item) {
                mostrarDialogoEditar(item);
            }

            @Override
            public void onEliminarClick(String id) {
                confirmarEliminar(id);
            }
        });

        recyclerView.setAdapter(adapter);
        btnAdd.setOnClickListener(v -> mostrarDialogoAgregar());
        cargarOferentes();
    }

    // ✅ Manejar botón volver
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void cargarOferentes() {
        repo.obtenerDocumentos("oferentes", snapshot -> {
            List<OferenteItem> lista = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String id = doc.getId();
                String nombre = doc.getString("nombre");
                if (nombre == null) nombre = "Sin nombre";
                lista.add(new OferenteItem(id, nombre));
            }
            adapter.actualizarDatos(lista);
        });
    }

    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_oferente, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar ventana del diálogo", e);
        }

        EditText etNombreOferente = dialogView.findViewById(R.id.etNombreOferente);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreOferente == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombreOferente.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombreOferente.setError("Por favor ingresa un nombre");
                etNombreOferente.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("activo", true);
            datos.put("fechaCreacion", System.currentTimeMillis());

            repo.crearDocumento("oferentes", datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Oferente creado: " + nombre, Toast.LENGTH_SHORT).show();
                        cargarOferentes();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Guardar");
                    });
        });

        dialog.show();
        etNombreOferente.requestFocus();
    }

    private void mostrarDialogoEditar(OferenteItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_oferente, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar ventana del diálogo", e);
        }

        EditText etNombreOferente = dialogView.findViewById(R.id.etNombreOferente);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreOferente == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        etNombreOferente.setText(item.getNombre());
        etNombreOferente.setSelection(item.getNombre().length());
        btnGuardar.setText("✓ Actualizar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreOferente.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombreOferente.setError("Por favor ingresa un nombre");
                etNombreOferente.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Actualizando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nuevoNombre);
            datos.put("fechaModificacion", System.currentTimeMillis());

            repo.actualizarDocumento("oferentes", item.getId(), datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Oferente actualizado", Toast.LENGTH_SHORT).show();
                        cargarOferentes();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Actualizar");
                    });
        });

        dialog.show();
        etNombreOferente.requestFocus();
    }

    private void confirmarEliminar(String id) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar este oferente?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repo.eliminarDocumento("oferentes", id)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✓ Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarOferentes();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}