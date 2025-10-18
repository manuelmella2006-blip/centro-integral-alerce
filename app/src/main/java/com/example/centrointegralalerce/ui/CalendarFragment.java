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
    private WeekCalendarAdapter adapter;
    private List<Cita> listaCitas;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);

        rvWeekSchedule = root.findViewById(R.id.rvWeekSchedule);

        cargarDatosMock();

        List<String> horas = new ArrayList<>();
        for (int i = 8; i <= 18; i++) {
            horas.add(String.format("%02d:00", i));
        }

        //adapter = new WeekCalendarAdapter(listaCitas, horas);
        rvWeekSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWeekSchedule.setAdapter(adapter);

        return root;
    }

    private void cargarDatosMock() {
        listaCitas = new ArrayList<>();
        // Lunes
        listaCitas.add(new Cita("Capacitación Android", "Oficina Central", "Lun 09:00", "Capacitación"));
        // Martes
        listaCitas.add(new Cita("Taller Kotlin", "Sala 2", "Mar 14:00", "Taller"));
        // Miércoles
        listaCitas.add(new Cita("Charla Seguridad", "Auditorio", "Mié 11:00", "Charlas"));
        // Jueves
        listaCitas.add(new Cita("Atención público", "Oficina Atención", "Jue 15:00", "Atenciones"));
        // Viernes
        listaCitas.add(new Cita("Operativo Rural", "Zona Rural", "Vie 08:00", "Operativo"));
        // Sábado
        listaCitas.add(new Cita("Práctica Profesional", "Laboratorio", "Sáb 10:00", "Práctica profesional"));
        // Domingo
        listaCitas.add(new Cita("Diagnóstico sistema", "Oficina Técnica", "Dom 13:00", "Diagnóstico"));
    }
}