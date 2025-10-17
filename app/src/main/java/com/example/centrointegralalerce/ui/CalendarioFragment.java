package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class CalendarioFragment extends Fragment {

    private RecyclerView rvCalendarWeek;
    private TextView tvCurrentWeek;
    private ImageButton btnPrevWeek, btnNextWeek;
    private FloatingActionButton fabNewActivity;
    private LinearLayout layoutEmptyCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        // ✅ Inicializar vistas
        rvCalendarWeek = view.findViewById(R.id.rv_calendar_week);
        tvCurrentWeek = view.findViewById(R.id.tv_current_week);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        fabNewActivity = view.findViewById(R.id.fab_new_activity);
        layoutEmptyCalendar = view.findViewById(R.id.layout_empty_calendar);

        setupRecyclerView();
        setupListeners();
        loadWeekData();

        return view;
    }

    private void setupRecyclerView() {
        rvCalendarWeek.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO: Conectar adapter cuando esté listo
        // rvCalendarWeek.setAdapter(new CalendarioDayAdapter(new ArrayList<>()));
    }

    private void setupListeners() {
        btnPrevWeek.setOnClickListener(v -> {
            // TODO: Cargar semana anterior
        });

        btnNextWeek.setOnClickListener(v -> {
            // TODO: Cargar semana siguiente
        });

        fabNewActivity.setOnClickListener(v -> {
            // TODO: Navegar a crear actividad
        });
    }

    private void loadWeekData() {
        tvCurrentWeek.setText("14 - 20 Octubre 2024");
        // TODO: Cargar datos reales desde Firebase
    }
}
