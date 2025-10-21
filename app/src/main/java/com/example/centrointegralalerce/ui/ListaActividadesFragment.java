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

// IMPORTA Cita desde el paquete REAL donde la declaraste.
// Si tu clase está en "model", usa el import de model:
import com.example.centrointegralalerce.data.Cita;
// Si la dejaste en "data", cambia la línea anterior por:
// import com.example.centrointegralalerce.data.Cita;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private List<Cita> activitiesList;
    private List<Cita> filteredActivitiesList;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Launcher para abrir AgregarActividadActivity y refrescar al volver
    private final ActivityResultLauncher<Intent> crearActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            reloadActividades(); // vuelve a consultar y repinta
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
        progressBar = view.findViewById(R.id.progress_bar); // si es null, se ignora más abajo

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
        // Búsqueda (IME action buscar)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            filterActivities(query);
            return true;
        });

        // Filtros por chips (implementación futura)
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterActivitiesByType();
        });

        // FAB nueva actividad -> abre AgregarActividadActivity
        fabNewActivityList.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AgregarActividadActivity.class);
            // Extra opcional: timestamp sugerido para precargar el formulario
            intent.putExtra("suggestedTimeMillis", System.currentTimeMillis());
            crearActividadLauncher.launch(intent); // espera RESULT_OK para refrescar
        });
    }

    /**
     * Cargar actividades (citas) desde Firebase Firestore
     */
    private void loadActivitiesFromFirebase() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        db.collection("citas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activitiesList.clear();
                    filteredActivitiesList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Cita cita = document.toObject(Cita.class);
                            if (cita != null) {
                                activitiesList.add(cita);
                                filteredActivitiesList.add(cita);
                                Log.d(TAG, "Cita cargada: fecha=" + safeFecha(cita.getFecha())
                                        + ", hora=" + safe(cita.getHora())
                                        + ", lugarId=" + safe(cita.getLugarId())
                                        + ", estado=" + safe(cita.getEstado()));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error al parsear cita: " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "Total citas cargadas: " + activitiesList.size());
                    updateUI();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (activitiesList.isEmpty()) {
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
     * Filtrar actividades por búsqueda de texto usando los campos reales de Cita:
     * fecha (como dd/MM/yyyy), hora, estado y lugarId.
     */
    private void filterActivities(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        filteredActivitiesList.clear();

        if (q.isEmpty()) {
            filteredActivitiesList.addAll(activitiesList);
        } else {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            for (Cita cita : activitiesList) {
                String fechaTxt = cita.getFecha() != null ? df.format(cita.getFecha()).toLowerCase(Locale.getDefault()) : "";
                String horaTxt = safe(cita.getHora()).toLowerCase(Locale.getDefault());
                String estadoTxt = safe(cita.getEstado()).toLowerCase(Locale.getDefault());
                String lugarTxt = safe(cita.getLugarId()).toLowerCase(Locale.getDefault());

                if (fechaTxt.contains(q) || horaTxt.contains(q) || estadoTxt.contains(q) || lugarTxt.contains(q)) {
                    filteredActivitiesList.add(cita);
                }
            }
        }

        updateUI();
        // Si ya tienes adapter: adapter.submitList(new ArrayList<>(filteredActivitiesList));
    }

    /**
     * Filtro por chips (placeholder): ahora mismo no limita nada.
     */
    private void filterActivitiesByType() {
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

                        MainActivity mainActivity = (MainActivity) getActivity();
                        boolean esInvitado = mainActivity != null && mainActivity.isGuest();

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

    /**
     * Recargar actividades (útil después de crear/editar/eliminar)
     */
    public void reloadActividades() {
        loadActivitiesFromFirebase();
    }

    // Helpers de seguridad para logs/texto
    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeFecha(Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
    }
}
