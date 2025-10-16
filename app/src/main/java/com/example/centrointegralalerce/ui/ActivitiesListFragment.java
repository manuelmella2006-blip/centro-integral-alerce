package com.example.centrointegralalerce.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesListFragment extends Fragment {

    private RecyclerView recyclerView;
    private CitasAdapter adapter;
    private List<Cita> activitiesList;

    public ActivitiesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activities_list, container, false);

        recyclerView = rootView.findViewById(R.id.rvActivitiesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cargarDatosMock();
        adapter = new CitasAdapter(activitiesList);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    private void cargarDatosMock() {
        activitiesList = new ArrayList<>();
        activitiesList.add(new Cita("Capacitación en Seguridad", "Sala 101", "Lun 9:00", "Capacitación"));
        activitiesList.add(new Cita("Taller de Manualidades", "Sala 202", "Mar 14:00", "Taller"));
        activitiesList.add(new Cita("Charla Salud Mental", "Auditorio", "Mié 11:00", "Charlas"));
        activitiesList.add(new Cita("Atención Psicológica", "Consultorio 1", "Jue 15:30", "Atenciones"));
        activitiesList.add(new Cita("Operativo Rural", "Comuna 5", "Vie 8:00", "Operativo"));
    }
}