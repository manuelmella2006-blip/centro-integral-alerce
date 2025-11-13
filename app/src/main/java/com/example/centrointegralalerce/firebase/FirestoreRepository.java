package com.example.centrointegralalerce.firebase;

import android.util.Log;

import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.data.CitaFirebase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

public class FirestoreRepository {

    private static final String TAG = "FirestoreRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ------------------------------------------------------------
    // 🔹 MÉTODOS CRUD GENÉRICOS (para cualquier colección)
    // ------------------------------------------------------------

    /** Crea un documento en la colección indicada */
    public Task<Void> crearDocumento(String coleccion, Map<String, Object> datos) {
        DocumentReference ref = db.collection(coleccion).document();
        return ref.set(datos);
    }

    /** Actualiza un documento existente */
    public Task<Void> actualizarDocumento(String coleccion, String id, Map<String, Object> datos) {
        return db.collection(coleccion).document(id).update(datos);
    }

    /** Elimina un documento por ID */
    public Task<Void> eliminarDocumento(String coleccion, String id) {
        return db.collection(coleccion).document(id).delete();
    }

    /** Obtiene todos los documentos de una colección */
    public void obtenerDocumentos(String coleccion, OnSuccessListener<QuerySnapshot> listener) {
        db.collection(coleccion).get().addOnSuccessListener(listener);
    }

    // ------------------------------------------------------------
    // 🔹 MÉTODO ESPECIAL: Guardar Actividad + Citas (en batch)
    // ------------------------------------------------------------

    /** Guarda una actividad y sus citas usando modelos */
    public Task<Void> guardarActividadConCitas(Actividad actividad, List<CitaFirebase> citas) {
        WriteBatch batch = db.batch();
        DocumentReference actividadRef = db.collection("actividades").document();

        // Guardar actividad principal
        batch.set(actividadRef, actividad);

        // Guardar citas en subcolección
        for (CitaFirebase cita : citas) {
            DocumentReference citaRef = actividadRef.collection("citas").document();
            batch.set(citaRef, cita);
        }

        Log.d(TAG, "Batch preparado para " + citas.size() + " citas (modelo).");
        return batch.commit();
    }

    /** Guarda actividad y citas usando Map<String, Object> */
    public Task<Void> guardarActividadConCitas(Map<String, Object> actividad, List<Map<String, Object>> citas) {
        WriteBatch batch = db.batch();
        DocumentReference actividadRef = db.collection("actividades").document();

        batch.set(actividadRef, actividad);

        for (Map<String, Object> cita : citas) {
            DocumentReference citaRef = actividadRef.collection("citas").document();
            batch.set(citaRef, cita);
        }

        Log.d(TAG, "Batch preparado para " + citas.size() + " citas (Map).");
        return batch.commit();
    }
}

