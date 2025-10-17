package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.LinearLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import java.util.ArrayList;
import java.util.List;

public class ListaActividadesFragment extends Fragment {

    private RecyclerView rvActivitiesList;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabNewActivityList;
    private LinearLayout layoutEmptyList;
    private List<Cita> activitiesList;

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

        setupRecyclerView();
        setupListeners();
        loadActivities();

        return view;
    }

    private void setupRecyclerView() {
        rvActivitiesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: Conectar adapter cuando esté listo
        // rvActivitiesList.setAdapter(new ActividadesListAdapter(new ArrayList<>()));
    }

    private void setupListeners() {
        // Búsqueda
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            // TODO: Implementar búsqueda
            return false;
        });

        // Filtros por chips
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // TODO: Implementar filtros
        });

        // FAB nueva actividad
        fabNewActivityList.setOnClickListener(v -> {
            // TODO: Navegar a crear actividad
        });
    }

    private void loadActivities() {
        // TODO: Cargar datos reales desde Firebase
        activitiesList = new ArrayList<>();
        // Datos mock temporales
        activitiesList.add(new Cita("Capacitación en Seguridad", "Sala 101", "Lun 9:00", "Capacitación"));
        activitiesList.add(new Cita("Taller de Manualidades", "Sala 202", "Mar 14:00", "Taller"));
        activitiesList.add(new Cita("Charla Salud Mental", "Auditorio", "Mié 11:00", "Charlas"));
    }
}