package com.example.centrointegralalerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaActividadesFragment extends Fragment {

    private static final String TAG = "ListaActividadesFragment";

    private RecyclerView rvActivitiesList;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private ExtendedFloatingActionButton fabNewActivityList;

    private LinearLayout layoutEmptyList;
    private ProgressBar progressBar;

    private List<Actividad> actividadesList;
    private List<Actividad> filteredActividadesList;
    private ActividadesListAdapter adapter;

    private List<String> actividadIds;
    private List<String> filteredActividadIds;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Launchers
    private final ActivityResultLauncher<Intent> crearActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadActivitiesFromFirebase();
                        }
                    }
            );

    private final ActivityResultLauncher<Intent> cancelarActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            loadActivitiesFromFirebase();
                            AlertManager.showSuccessSnackbar(
                                    AlertManager.getRootView(requireActivity()),
                                    "Actividad cancelada correctamente ✅"
                            );
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
        actividadIds = new ArrayList<>();
        filteredActividadIds = new ArrayList<>();

        adapter = new ActividadesListAdapter(filteredActividadesList);

        setupRecyclerView();
        setupListeners();
        loadActivitiesFromFirebase();
        checkUserRole(); // ✅ ahora usa UserSession

        return view;
    }

    private void setupRecyclerView() {
        rvActivitiesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActivitiesList.setAdapter(adapter);

        adapter.setOnItemClickListener(actividad -> {
            int pos = filteredActividadesList.indexOf(actividad);
            if (pos >= 0 && pos < filteredActividadIds.size()) {
                String actividadId = filteredActividadIds.get(pos);
                Intent intent = new Intent(requireContext(), DetalleActividadActivity.class);
                intent.putExtra("actividadId", actividadId);
                cancelarActividadLauncher.launch(intent);
            } else {
                AlertManager.showWarningToast(requireContext(), "No se pudo obtener el ID de la actividad ❗");
            }
        });
    }

    private void setupListeners() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText() != null
                    ? etSearch.getText().toString().trim()
                    : "";
            filterActivities(query);
            return true;
        });

        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> filterActivitiesByType());

        fabNewActivityList.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AgregarActividadActivity.class);
            crearActividadLauncher.launch(intent);
        });
    }

    private void loadActivitiesFromFirebase() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        db.collection("actividades")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    actividadesList.clear();
                    actividadIds.clear();
                    filteredActividadesList.clear();
                    filteredActividadIds.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Actividad actividad = document.toObject(Actividad.class);
                            if (actividad != null) {
                                actividadesList.add(actividad);
                                actividadIds.add(document.getId());
                                Log.d(TAG, "Actividad cargada: " + actividad.getNombre());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear actividad: " + e.getMessage(), e);
                        }
                    }

                    filteredActividadesList.addAll(actividadesList);
                    filteredActividadIds.addAll(actividadIds);
                    adapter.setActividadesList(filteredActividadesList);

                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    updateUI();

                    View root = AlertManager.getRootView(requireActivity());
                    if (actividadesList.isEmpty()) {
                        AlertManager.showInfoSnackbar(root, "No hay actividades disponibles 📭");
                    } else {
                        AlertManager.showSuccessSnackbar(root, "Actividades cargadas correctamente ✅");
                    }

                    Log.d(TAG, "Total actividades cargadas: " + actividadesList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar actividades: " + e.getMessage(), e);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    updateUI();

                    View root = AlertManager.getRootView(requireActivity());
                    AlertManager.showErrorSnackbar(root, "Error al cargar actividades. Intenta nuevamente ⚠️");
                });
    }

    private void filterActivities(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        filteredActividadesList.clear();
        filteredActividadIds.clear();

        if (q.isEmpty()) {
            filteredActividadesList.addAll(actividadesList);
            filteredActividadIds.addAll(actividadIds);
        } else {
            for (int i = 0; i < actividadesList.size(); i++) {
                Actividad actividad = actividadesList.get(i);

                String nombre = safe(actividad.getNombre()).toLowerCase(Locale.getDefault());
                String tipo = safe(actividad.getTipoActividadNombre()).toLowerCase(Locale.getDefault());
                String lugar = safe(actividad.getLugarNombre()).toLowerCase(Locale.getDefault());
                String fecha = safe(actividad.getFechaInicio()).toLowerCase(Locale.getDefault());
                String estado = safe(actividad.getEstado()).toLowerCase(Locale.getDefault());

                if (nombre.contains(q)
                        || tipo.contains(q)
                        || lugar.contains(q)
                        || fecha.contains(q)
                        || estado.contains(q)) {
                    filteredActividadesList.add(actividad);
                    filteredActividadIds.add(actividadIds.get(i));
                }
            }
        }

        adapter.setActividadesList(filteredActividadesList);
        updateUI();

        if (filteredActividadesList.isEmpty()) {
            AlertManager.showInfoToast(requireContext(), "No se encontraron resultados para \"" + query + "\"");
        }
    }

    private void filterActivitiesByType() {
        filteredActividadesList.clear();
        filteredActividadIds.clear();
        filteredActividadesList.addAll(actividadesList);
        filteredActividadIds.addAll(actividadIds);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateUI() {
        boolean isEmpty = filteredActividadesList.isEmpty();

        if (layoutEmptyList != null) {
            layoutEmptyList.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (rvActivitiesList != null) {
            rvActivitiesList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    // ============================================================
    // ✅ NUEVA VERSIÓN USANDO USERSESSION
    // ============================================================
    private void checkUserRole() {
        UserSession session = UserSession.getInstance();

        if (!session.permisosCargados()) {
            Log.w("LISTA_ACTIVIDADES", "⚠️ Permisos no cargados, reintentando...");
            new Handler().postDelayed(this::checkUserRole, 1000);
            return;
        }

        boolean puedeCrear = session.puede("crear_actividades");
        fabNewActivityList.setVisibility(puedeCrear ? View.VISIBLE : View.GONE);

        Log.d("LISTA_ACTIVIDADES", "🎯 FAB visible: " + puedeCrear + " - Rol: " + session.getRolId());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
