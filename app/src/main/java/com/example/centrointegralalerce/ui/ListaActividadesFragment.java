package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// NUEVOS IMPORTS
import android.app.Activity;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class ListaActividadesFragment extends Fragment {

    private static final String TAG = "ListaActividadesFragment";

    private RecyclerView rvActivitiesList;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabNewActivityList;
    private LinearLayout layoutEmptyList;
    private ProgressBar progressBar;

    // CAMBIO: Usar Actividad en lugar de Cita
    private List<Actividad> actividadesList;
    private List<Actividad> filteredActividadesList;
    private ActividadesListAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Launcher para abrir AgregarActividadActivity y refrescar al volver
    private final ActivityResultLauncher<Intent> crearActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadActivitiesFromFirebase(); // vuelve a consultar y repinta
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_actividades, container, false);

        // Inicializar vistas
        rvActivitiesList = view.findViewById(R.id.rv_activities_list);
        etSearch = view.findViewById(R.id.et_search);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        fabNewActivityList = view.findViewById(R.id.fab_new_activity_list);
        layoutEmptyList = view.findViewById(R.id.layout_empty_list);
        progressBar = view.findViewById(R.id.progress_bar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        actividadesList = new ArrayList<>();
        filteredActividadesList = new ArrayList<>();
        adapter = new ActividadesListAdapter(filteredActividadesList);

        setupRecyclerView();
        setupListeners();
        loadActivitiesFromFirebase();
        checkUserRole();

        return view;
    }

    private void setupRecyclerView() {
        rvActivitiesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActivitiesList.setAdapter(adapter);
    }

    private void setupListeners() {
        // Búsqueda (IME action buscar)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            filterActivities(query);
            return true;
        });

        // Filtros por chips
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterActivitiesByType();
        });

        // FAB nueva actividad -> abre AgregarActividadActivity
        fabNewActivityList.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AgregarActividadActivity.class);
            crearActividadLauncher.launch(intent);
        });
    }

    /**
     * Cargar actividades desde Firebase Firestore - COLECCIÓN CORRECTA
     */
    private void loadActivitiesFromFirebase() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // CAMBIO: Cargar desde la colección "actividades" en lugar de "citas"
        db.collection("actividades")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    actividadesList.clear();
                    filteredActividadesList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Actividad actividad = document.toObject(Actividad.class);
                            if (actividad != null) {
                                // Establecer el ID del documento si es necesario
                                actividadesList.add(actividad);
                                Log.d(TAG, "Actividad cargada: " + actividad.getNombre());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear actividad: " + e.getMessage(), e);
                        }
                    }

                    // Actualizar lista filtrada y UI
                    filteredActividadesList.addAll(actividadesList);
                    adapter.setActividadesList(filteredActividadesList);

                    Log.d(TAG, "Total actividades cargadas: " + actividadesList.size());
                    updateUI();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (actividadesList.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No hay actividades disponibles",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar actividades: " + e.getMessage(), e);
                    Toast.makeText(requireContext(),
                            "Error al cargar actividades: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    updateUI();
                });
    }

    /**
     * Filtrar actividades por búsqueda de texto
     */
    private void filterActivities(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        filteredActividadesList.clear();

        if (q.isEmpty()) {
            filteredActividadesList.addAll(actividadesList);
        } else {
            for (Actividad actividad : actividadesList) {
                String nombre = safe(actividad.getNombre()).toLowerCase(Locale.getDefault());
                String tipo = safe(actividad.getTipoActividadId()).toLowerCase(Locale.getDefault());
                String lugar = safe(actividad.getLugar()).toLowerCase(Locale.getDefault());
                String fecha = safe(actividad.getFecha()).toLowerCase(Locale.getDefault());
                String estado = safe(actividad.getEstado()).toLowerCase(Locale.getDefault());

                if (nombre.contains(q) || tipo.contains(q) || lugar.contains(q) ||
                        fecha.contains(q) || estado.contains(q)) {
                    filteredActividadesList.add(actividad);
                }
            }
        }

        adapter.setActividadesList(filteredActividadesList);
        updateUI();
    }

    /**
     * Filtro por chips - implementación básica
     */
    private void filterActivitiesByType() {
        // Por ahora, mostramos todas las actividades
        filteredActividadesList.clear();
        filteredActividadesList.addAll(actividadesList);
        adapter.setActividadesList(filteredActividadesList);
        updateUI();
    }

    /**
     * Actualizar la UI basándose en la lista filtrada
     */
    private void updateUI() {
        boolean isEmpty = filteredActividadesList.isEmpty();

        if (layoutEmptyList != null) {
            layoutEmptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (rvActivitiesList != null) {
            rvActivitiesList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Verificar rol del usuario para mostrar/ocultar FAB
     */
    private void checkUserRole() {
        if (auth.getCurrentUser() == null) {
            fabNewActivityList.setVisibility(View.GONE);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rolId = documentSnapshot.getString("rolId");

                        // Asumiendo que tienes MainActivity con método isGuest()
                        boolean esInvitado = getActivity() instanceof MainActivity &&
                                ((MainActivity) getActivity()).isGuest();

                        boolean esAdmin = "admin".equalsIgnoreCase(rolId) ||
                                "administrador".equalsIgnoreCase(rolId);

                        fabNewActivityList.setVisibility(
                                (esAdmin && !esInvitado) ? View.VISIBLE : View.GONE
                        );
                    } else {
                        fabNewActivityList.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar rol: " + e.getMessage(), e);
                    fabNewActivityList.setVisibility(View.GONE);
                });
    }

    // Helper de seguridad para strings
    private String safe(String s) {
        return s == null ? "" : s;
    }
}