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

public class SociosComunitariosActivity extends AppCompatActivity {

    private static final String TAG = "SociosComunitariosActivity";
    private FirestoreRepository repo = new FirestoreRepository();
    private RecyclerView recyclerView;
    private SociosComunitariosAdapter adapter;
    private FloatingActionButton btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socios_comunitarios);

        // ✅ Configurar toolbar con botón volver
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerSocios);
        btnAdd = findViewById(R.id.btnAddSocio);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SociosComunitariosAdapter(new ArrayList<>(), new SociosComunitariosAdapter.OnItemActionListener() {
            @Override
            public void onEditarClick(SocioComunitarioItem item) {
                mostrarDialogoEditar(item);
            }

            @Override
            public void onEliminarClick(String id) {
                confirmarEliminar(id);
            }
        });

        recyclerView.setAdapter(adapter);
        btnAdd.setOnClickListener(v -> mostrarDialogoAgregar());
        cargarSocios();
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

    private void cargarSocios() {
        repo.obtenerDocumentos("sociosComunitarios", snapshot -> {
            List<SocioComunitarioItem> lista = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                String id = doc.getId();
                String nombre = doc.getString("nombre");
                String beneficiarios = doc.getString("beneficiarios");

                if (nombre == null) nombre = "Sin nombre";
                if (beneficiarios == null) beneficiarios = "";

                lista.add(new SocioComunitarioItem(id, nombre, beneficiarios));
            }
            adapter.actualizarDatos(lista);
        });
    }

    private void mostrarDialogoAgregar() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_socio, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar ventana del diálogo", e);
        }

        EditText etNombreSocio = dialogView.findViewById(R.id.etNombreSocio);
        EditText etBeneficiarios = dialogView.findViewById(R.id.etBeneficiarios);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreSocio == null || etBeneficiarios == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombreSocio.getText().toString().trim();
            String beneficiarios = etBeneficiarios.getText().toString().trim();

            if (nombre.isEmpty()) {
                etNombreSocio.setError("Por favor ingresa un nombre");
                etNombreSocio.requestFocus();
                return;
            }

            if (beneficiarios.isEmpty()) {
                etBeneficiarios.setError("Por favor ingresa los beneficiarios");
                etBeneficiarios.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("beneficiarios", beneficiarios);
            datos.put("activo", true);
            datos.put("fechaCreacion", System.currentTimeMillis());

            repo.crearDocumento("sociosComunitarios", datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Socio comunitario creado: " + nombre, Toast.LENGTH_SHORT).show();
                        cargarSocios();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Guardar");
                    });
        });

        dialog.show();
        etNombreSocio.requestFocus();
    }

    private void mostrarDialogoEditar(SocioComunitarioItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_socio, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        try {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al configurar ventana del diálogo", e);
        }

        EditText etNombreSocio = dialogView.findViewById(R.id.etNombreSocio);
        EditText etBeneficiarios = dialogView.findViewById(R.id.etBeneficiarios);
        Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        Button btnGuardar = dialogView.findViewById(R.id.btnGuardar);

        if (etNombreSocio == null || etBeneficiarios == null || btnCancelar == null || btnGuardar == null) {
            Toast.makeText(this, "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            return;
        }

        etNombreSocio.setText(item.getNombre());
        etNombreSocio.setSelection(item.getNombre().length());
        etBeneficiarios.setText(item.getBeneficiarios());
        btnGuardar.setText("✓ Actualizar");

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreSocio.getText().toString().trim();
            String nuevosBeneficiarios = etBeneficiarios.getText().toString().trim();

            if (nuevoNombre.isEmpty()) {
                etNombreSocio.setError("Por favor ingresa un nombre");
                etNombreSocio.requestFocus();
                return;
            }

            if (nuevosBeneficiarios.isEmpty()) {
                etBeneficiarios.setError("Por favor ingresa los beneficiarios");
                etBeneficiarios.requestFocus();
                return;
            }

            btnGuardar.setEnabled(false);
            btnGuardar.setText("Actualizando...");

            Map<String, Object> datos = new HashMap<>();
            datos.put("nombre", nuevoNombre);
            datos.put("beneficiarios", nuevosBeneficiarios);
            datos.put("fechaModificacion", System.currentTimeMillis());

            repo.actualizarDocumento("sociosComunitarios", item.getId(), datos)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "✓ Socio comunitario actualizado", Toast.LENGTH_SHORT).show();
                        cargarSocios();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("✓ Actualizar");
                    });
        });

        dialog.show();
        etNombreSocio.requestFocus();
    }

    private void confirmarEliminar(String id) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Confirmar eliminación")
                .setMessage("¿Estás seguro de eliminar este socio comunitario?\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    repo.eliminarDocumento("sociosComunitarios", id)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "✓ Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                cargarSocios();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}