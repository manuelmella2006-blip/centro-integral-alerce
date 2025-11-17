package com.example.centrointegralalerce.ui;
import android.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdjuntarComunicacionActivity extends AppCompatActivity {

    private MaterialAutoCompleteTextView spActividad;
    private TextInputEditText etDescripcion;
    private MaterialButton btnSeleccionarArchivo, btnGuardar;
    private LinearProgressIndicator progressIndicator;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri archivoUri;
    private String actividadId;
    private final List<String> actividadesNombres = new ArrayList<>();
    private final List<String> actividadesIds = new ArrayList<>();
    private ArrayAdapter<String> actividadesAdapter;

    // Código para selección de archivo
    private static final int PICK_FILE_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjuntar_comunicacion);

        // Configurar toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Bind views
        spActividad = findViewById(R.id.spActividad);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnSeleccionarArchivo = findViewById(R.id.btnSeleccionarArchivo);
        btnGuardar = findViewById(R.id.btnGuardar);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Configurar adaptador para actividades
        actividadesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, actividadesNombres);
        spActividad.setAdapter(actividadesAdapter);

        // Cargar actividades desde Firestore
        cargarActividades();

        // Verificar si viene de DetalleActividad
        actividadId = getIntent().getStringExtra("actividadId");
        if (actividadId != null && !actividadId.isEmpty()) {
            // Si viene de detalle, cargar esa actividad específica
            cargarActividadEspecifica(actividadId);
        }

        // Listeners
        btnSeleccionarArchivo.setOnClickListener(v -> seleccionarArchivo());
        btnGuardar.setOnClickListener(v -> guardarComunicacion());

        // Deshabilitar guardar inicialmente
        btnGuardar.setEnabled(false);
    }

    private void cargarActividades() {
        db.collection("actividades")
                .whereEqualTo("estado", "activa") // ✅ Ya solo carga actividades activas
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    actividadesNombres.clear();
                    actividadesIds.clear();

                    for (var doc : queryDocumentSnapshots) {
                        String nombre = doc.getString("nombre");
                        if (nombre != null) {
                            actividadesNombres.add(nombre);
                            actividadesIds.add(doc.getId());
                        }
                    }
                    actividadesAdapter.notifyDataSetChanged();

                    // Si hay una actividad específica preseleccionada, seleccionarla
                    if (actividadId != null && !actividadId.isEmpty()) {
                        seleccionarActividadPorId(actividadId);
                    }
                })
                .addOnFailureListener(e -> {
                    AlertManager.showErrorToast(this, "Error al cargar actividades");
                });
    }

    private void cargarActividadEspecifica(String id) {
        db.collection("actividades")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String estado = documentSnapshot.getString("estado");

                        // ✅ NUEVA VALIDACIÓN: Verificar si la actividad está cancelada
                        if ("cancelada".equalsIgnoreCase(estado)) {
                            AlertManager.showDestructiveDialog(
                                    this,
                                    "Actividad Cancelada",
                                    "No se pueden adjuntar archivos a una actividad cancelada.",
                                    "Aceptar",
                                    new AlertManager.OnConfirmListener() {
                                        @Override
                                        public void onConfirm() {
                                            finish();
                                        }

                                        @Override
                                        public void onCancel() {
                                            finish();
                                        }
                                    }
                            );
                            deshabilitarControles();
                            return;
                        }

                        if (nombre != null) {
                            // Agregar a las listas si no existe
                            if (!actividadesIds.contains(id)) {
                                actividadesIds.add(id);
                                actividadesNombres.add(nombre);
                                actividadesAdapter.notifyDataSetChanged();
                            }
                            // Seleccionar en el spinner
                            seleccionarActividadPorId(id);
                        }
                    }
                });
    }

    // ✅ NUEVO MÉTODO: Deshabilitar controles cuando la actividad está cancelada
    private void deshabilitarControles() {
        spActividad.setEnabled(false);
        etDescripcion.setEnabled(false);
        btnSeleccionarArchivo.setEnabled(false);
        btnGuardar.setEnabled(false);
        btnGuardar.setVisibility(View.GONE);
    }

    private void seleccionarActividadPorId(String id) {
        int index = actividadesIds.indexOf(id);
        if (index >= 0 && index < actividadesNombres.size()) {
            spActividad.setText(actividadesNombres.get(index), false);
            // Deshabilitar el spinner si viene de detalle
            spActividad.setEnabled(false);
        }
    }

    private void seleccionarArchivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Permitir cualquier tipo de archivo
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Selecciona un archivo"), PICK_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            AlertManager.showErrorToast(this, "No hay una aplicación para seleccionar archivos");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                archivoUri = data.getData();
                String fileName = getFileName(archivoUri);
                btnSeleccionarArchivo.setText(fileName != null ? fileName : "Archivo seleccionado");
                validarFormulario();
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (var cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void validarFormulario() {
        boolean actividadSeleccionada = spActividad.getText() != null &&
                !spActividad.getText().toString().trim().isEmpty();
        boolean archivoSeleccionado = archivoUri != null;

        btnGuardar.setEnabled(actividadSeleccionada && archivoSeleccionado);
    }

    private void guardarComunicacion() {
        String nombreActividad = spActividad.getText().toString().trim();
        int index = actividadesNombres.indexOf(nombreActividad);

        if (index == -1) {
            AlertManager.showErrorToast(this, "Selecciona una actividad válida");
            return;
        }

        String actividadId = actividadesIds.get(index);
        String descripcion = etDescripcion.getText() != null ?
                etDescripcion.getText().toString().trim() : "";

        // Mostrar progreso
        progressIndicator.setVisibility(View.VISIBLE);
        btnGuardar.setEnabled(false);

        // Subir archivo a Firebase Storage
        subirArchivo(actividadId, descripcion);
    }

    private void subirArchivo(String actividadId, String descripcion) {
        String fileName = "comunicaciones/" + actividadId + "/" + UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference().child(fileName);

        fileRef.putFile(archivoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                guardarEnFirestore(actividadId, descripcion, uri.toString(), fileName);
                            })
                            .addOnFailureListener(e -> {
                                progressIndicator.setVisibility(View.GONE);
                                btnGuardar.setEnabled(true);
                                AlertManager.showErrorToast(this, "Error al obtener URL del archivo");
                            });
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    AlertManager.showErrorToast(this, "Error al subir archivo: " + e.getMessage());
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressIndicator.setProgress((int) progress);
                });
    }

    private void guardarEnFirestore(String actividadId, String descripcion, String fileUrl, String fileName) {
        // Crear subcolección dentro de la actividad
        Map<String, Object> comunicacion = new HashMap<>();
        comunicacion.put("descripcion", descripcion);
        comunicacion.put("fileUrl", fileUrl);
        comunicacion.put("fileName", getFileName(archivoUri));
        comunicacion.put("storagePath", fileName);
        comunicacion.put("fechaSubida", System.currentTimeMillis());
        comunicacion.put("tipo", "comunicacion");

        // Guardar en subcolección de la actividad
        db.collection("actividades")
                .document(actividadId)
                .collection("comunicaciones")
                .add(comunicacion)
                .addOnSuccessListener(documentReference -> {
                    progressIndicator.setVisibility(View.GONE);
                    AlertManager.showSuccessToast(this, "Archivo guardado correctamente");
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnGuardar.setEnabled(true);
                    AlertManager.showErrorToast(this, "Error al guardar información: " + e.getMessage());
                    // Log detallado del error
                    Log.e("FirestoreError", "Error: " + e.getMessage(), e);
                });
    }
}