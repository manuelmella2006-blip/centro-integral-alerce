package com.example.centrointegralalerce.data;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class FirestoreRepository {

    private static final String TAG = "FirestoreRepository";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Guarda una actividad con sus citas en una sola operación Batch.
     */
    public Task<Void> guardarActividadConCitas(Actividad actividad, List<CitaFirebase> citas) {
        WriteBatch batch = db.batch();

        // 🔹 Referencia a la nueva actividad
        DocumentReference actividadRef = db.collection("actividades").document();
        batch.set(actividadRef, actividad);

        // 🔹 Añadir citas dentro de la subcolección
        for (CitaFirebase cita : citas) {
            DocumentReference citaRef = actividadRef.collection("citas").document();
            batch.set(citaRef, cita);
        }

        Log.d(TAG, "Batch preparado para " + citas.size() + " citas.");
        return batch.commit();
    }
}

