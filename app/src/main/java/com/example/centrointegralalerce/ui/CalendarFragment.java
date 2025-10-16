package com.example.centrointegralalerce.ui;

import android.os.Bundle;
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

public class CalendarFragment extends Fragment {

    private RecyclerView rvWeekSchedule;
    private CitasAdapter adapter;
    private List<Cita> listaCitas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        rvWeekSchedule = root.findViewById(R.id.rvWeekSchedule);
        rvWeekSchedule.setLayoutManager(new LinearLayoutManager(getContext()));

        cargarDatosMock();

        adapter = new CitasAdapter(listaCitas);
        rvWeekSchedule.setAdapter(adapter);

        return root;
    }

    private void cargarDatosMock() {
        listaCitas = new ArrayList<>();
        listaCitas.add(new Cita("Capacitación en Android", "Oficina Central", "Lun 09:00", "Capacitación"));
        listaCitas.add(new Cita("Taller de Kotlin", "Sala 2", "Mar 14:00", "Taller"));
        listaCitas.add(new Cita("Charla Seguridad", "Auditorio", "Mié 11:00", "Charlas"));
        listaCitas.add(new Cita("Atención a público", "Oficina Atención", "Jue 15:30", "Atenciones"));
        listaCitas.add(new Cita("Operativo Rural", "Zona Rural", "Vie 08:00", "Operativo"));
        listaCitas.add(new Cita("Práctica Profesional", "Laboratorio", "Sáb 10:00", "Práctica profesional"));
        listaCitas.add(new Cita("Diagnóstico de sistema", "Oficina Técnica", "Dom 13:00", "Diagnóstico"));
    }
}