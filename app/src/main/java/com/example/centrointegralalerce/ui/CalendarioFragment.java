package com.example.centrointegralalerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioFragment extends Fragment {

    private static final int REQUEST_ADD_ACTIVITY = 1;

    private ViewPager2 viewPagerCalendar;
    private TextView tvCurrentWeek;
    private MaterialButton btnPrevWeek, btnNextWeek;
    private FloatingActionButton fabNewActivity;
    private LinearLayout layoutEmptyCalendar;

    private List<Cita> allCitas;
    private Calendar currentWeekStart;
    private CalendarPagerAdapter pagerAdapter;
    private boolean isUserScrolling = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        // Vincular vistas
        viewPagerCalendar = view.findViewById(R.id.viewpager_calendar);
        tvCurrentWeek = view.findViewById(R.id.tv_current_week);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        fabNewActivity = view.findViewById(R.id.fab_new_activity);
        layoutEmptyCalendar = view.findViewById(R.id.layout_empty_calendar);

        // Lista de citas de ejemplo
        allCitas = new ArrayList<>();
        allCitas.add(new Cita("1", "Taller de Robótica", "Laboratorio 3", "Lun 09:00", "Taller"));
        allCitas.add(new Cita("2", "Charla Salud Mental", "Sala 2", "Mié 11:00", "Charlas"));
        allCitas.add(new Cita("3", "Atención Comunitaria", "Sede Central", "Vie 10:00", "Atenciones"));

        // Inicializar el calendario en la semana actual
        currentWeekStart = Calendar.getInstance();
        setWeekToMonday(currentWeekStart);

        setupViewPager();
        setupListeners();
        updateWeekLabel();

        return view;
    }

    private void setupViewPager() {
        // Crear el adaptador
        pagerAdapter = new CalendarPagerAdapter(allCitas, getParentFragmentManager(), currentWeekStart);

        // Configurar listener para actualizar el label cuando cambia la semana
        pagerAdapter.setOnWeekChangeListener(weekStart -> {
            if (isUserScrolling) {
                currentWeekStart = (Calendar) weekStart.clone();
                updateWeekLabel();
                checkIfWeekHasCitas();
            }
        });

        viewPagerCalendar.setAdapter(pagerAdapter);

        // Empezar en la posición central (semana actual)
        viewPagerCalendar.setCurrentItem(pagerAdapter.getMiddlePosition(), false);

        // Listener para detectar cambios de página
        viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                isUserScrolling = (state == ViewPager2.SCROLL_STATE_DRAGGING ||
                        state == ViewPager2.SCROLL_STATE_SETTLING);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int weekOffset = position - pagerAdapter.getMiddlePosition();
                currentWeekStart = Calendar.getInstance();
                setWeekToMonday(currentWeekStart);
                currentWeekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

                updateWeekLabel();
                checkIfWeekHasCitas();
            }
        });
    }

    private void setupListeners() {
        btnPrevWeek.setOnClickListener(v -> {
            int currentItem = viewPagerCalendar.getCurrentItem();
            if (currentItem > 0) {
                viewPagerCalendar.setCurrentItem(currentItem - 1, true);
            }
        });

        btnNextWeek.setOnClickListener(v -> {
            int currentItem = viewPagerCalendar.getCurrentItem();
            if (currentItem < pagerAdapter.getItemCount() - 1) {
                viewPagerCalendar.setCurrentItem(currentItem + 1, true);
            }
        });

        // ✅ NUEVO: Configurar FAB para abrir AgregarActividadActivity
        fabNewActivity.setOnClickListener(v -> {
            abrirAgregarActividad();
        });
    }

    /**
     * ✅ NUEVO: Abre la Activity para agregar actividad
     */
    private void abrirAgregarActividad() {
        Intent intent = new Intent(getActivity(), AgregarActividadActivity.class);
        startActivityForResult(intent, REQUEST_ADD_ACTIVITY);
    }

    /**
     * ✅ NUEVO: Recibe el resultado cuando se agrega una actividad
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ACTIVITY && resultCode == Activity.RESULT_OK) {
            // La actividad se agregó exitosamente
            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Actividad agregada exitosamente",
                        Toast.LENGTH_SHORT).show();
            }
            // Aquí puedes recargar las actividades desde Firestore
            cargarActividadesDesdeFirestore();
        }
    }

    /**
     * ✅ NUEVO: Método para recargar actividades desde Firestore
     */
    private void cargarActividadesDesdeFirestore() {
        // TODO: Implementar lectura de Firestore
        // Por ahora solo mostramos un mensaje
        if (getContext() != null) {
            Toast.makeText(getContext(),
                    "Recargando actividades...",
                    Toast.LENGTH_SHORT).show();
        }

        // Actualizar vista
        checkIfWeekHasCitas();
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }
    }

    private void setWeekToMonday(Calendar cal) {
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4);

        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday;

        if (currentDayOfWeek == Calendar.SUNDAY) {
            daysFromMonday = 6;
        } else {
            daysFromMonday = currentDayOfWeek - Calendar.MONDAY;
        }

        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void updateWeekLabel() {
        try {
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", new Locale("es", "ES"));
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

            Calendar endCal = (Calendar) currentWeekStart.clone();
            endCal.add(Calendar.DAY_OF_MONTH, 6);

            String startDay = dayFormat.format(currentWeekStart.getTime());
            String endDay = dayFormat.format(endCal.getTime());
            String monthYear = monthYearFormat.format(currentWeekStart.getTime());

            tvCurrentWeek.setText(startDay + " - " + endDay + " de " + monthYear);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkIfWeekHasCitas() {
        Calendar endCal = (Calendar) currentWeekStart.clone();
        endCal.add(Calendar.DAY_OF_MONTH, 6);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);

        boolean hasCitas = false;
        for (Cita cita : allCitas) {
            Calendar citaCal = cita.getFechaHoraCalendar();
            if (citaCal != null && !citaCal.before(currentWeekStart) && !citaCal.after(endCal)) {
                hasCitas = true;
                break;
            }
        }

        if (layoutEmptyCalendar != null) {
            layoutEmptyCalendar.setVisibility(hasCitas ? View.GONE : View.VISIBLE);
        }
    }
}
