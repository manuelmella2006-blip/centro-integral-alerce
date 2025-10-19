package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListaActividadesFragment extends Fragment {

    private static final String TAG = "ListaActividadesFragment";

    private RecyclerView rvActivitiesList;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabNewActivityList;
    private LinearLayout layoutEmptyList;
    private ProgressBar progressBar;

    private List<Cita> activitiesList;
    private List<Cita> filteredActivitiesList;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

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

        activitiesList = new ArrayList<>();
        filteredActivitiesList = new ArrayList<>();

        setupRecyclerView();
        setupListeners();
        loadActivitiesFromFirebase();
        checkUserRole();

        return view;
    }

    private void setupRecyclerView() {
        rvActivitiesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: Conectar adapter cuando esté listo
        // ActividadesListAdapter adapter = new ActividadesListAdapter(filteredActivitiesList);
        // rvActivitiesList.setAdapter(adapter);
    }

    private void setupListeners() {
        // Búsqueda
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText().toString().trim();
            filterActivities(query);
            return true;
        });

        // Filtros por chips
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // TODO: Implementar filtros por tipo de actividad
            filterActivitiesByType();
        });

        // FAB nueva actividad
        fabNewActivityList.setOnClickListener(v -> {
            // TODO: Navegar a crear actividad
            Toast.makeText(requireContext(), "Crear nueva actividad", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Cargar actividades desde Firebase Firestore
     */
    private void loadActivitiesFromFirebase() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        db.collection("citas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activitiesList.clear();
                    filteredActivitiesList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Cita cita = document.toObject(Cita.class);
                            activitiesList.add(cita);
                            filteredActivitiesList.add(cita);
                            Log.d(TAG, "Actividad cargada: " + cita.getActividad());
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear actividad: " + e.getMessage());
                        }
                    }

                    Log.d(TAG, "Total actividades cargadas: " + activitiesList.size());

                    updateUI();

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (activitiesList.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "No hay actividades disponibles",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar actividades: " + e.getMessage());
                    Toast.makeText(requireContext(),
                            "Error al cargar actividades: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    updateUI();
                });
    }

    /**
     * Filtrar actividades por búsqueda de texto
     */
    private void filterActivities(String query) {
        filteredActivitiesList.clear();

        if (query.isEmpty()) {
            filteredActivitiesList.addAll(activitiesList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Cita cita : activitiesList) {
                if (cita.getActividad().toLowerCase().contains(lowerQuery) ||
                        cita.getLugar().toLowerCase().contains(lowerQuery) ||
                        cita.getTipoActividad().toLowerCase().contains(lowerQuery)) {
                    filteredActivitiesList.add(cita);
                }
            }
        }

        updateUI();
    }

    /**
     * Filtrar actividades por tipo (chips)
     */
    private void filterActivitiesByType() {
        // TODO: Implementar filtro basado en chips seleccionados
        // Por ahora solo muestra todas
        filteredActivitiesList.clear();
        filteredActivitiesList.addAll(activitiesList);
        updateUI();
    }

    /**
     * Actualizar la UI basándose en la lista filtrada
     */
    private void updateUI() {
        boolean isEmpty = filteredActivitiesList.isEmpty();

        if (layoutEmptyList != null) {
            layoutEmptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (rvActivitiesList != null) {
            rvActivitiesList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // TODO: Notificar al adapter cuando esté implementado
        // adapter.notifyDataSetChanged();
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

                        // Verificar si es invitado desde MainActivity
                        MainActivity mainActivity = (MainActivity) getActivity();
                        boolean esInvitado = mainActivity != null && mainActivity.isGuest();

                        // Solo admins pueden crear actividades
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
                    Log.e(TAG, "Error al verificar rol: " + e.getMessage());
                    fabNewActivityList.setVisibility(View.GONE);
                });
    }

    /**
     * Recargar actividades (útil después de crear/editar/eliminar)
     */
    public void reloadActividades() {
        loadActivitiesFromFirebase();
    }
}