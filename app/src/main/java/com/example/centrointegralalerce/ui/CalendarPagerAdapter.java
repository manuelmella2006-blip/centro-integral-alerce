package com.example.centrointegralalerce.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador para el ViewPager2 que muestra diferentes semanas
 */
public class CalendarPagerAdapter extends RecyclerView.Adapter<CalendarPagerAdapter.WeekViewHolder> {

    private static final int TOTAL_WEEKS = 1000; // Número grande para simular scroll infinito
    private static final int MIDDLE_POSITION = TOTAL_WEEKS / 2; // Posición central

    private final List<Cita> allCitas;
    private final FragmentManager fragmentManager;
    private final Calendar baseWeekStart; // Semana actual como referencia
    private OnWeekChangeListener weekChangeListener;

    public interface OnWeekChangeListener {
        void onWeekChanged(Calendar weekStart);
    }

    public CalendarPagerAdapter(List<Cita> allCitas, FragmentManager fragmentManager, Calendar currentWeek) {
        this.allCitas = allCitas;
        this.fragmentManager = fragmentManager;
        this.baseWeekStart = (Calendar) currentWeek.clone();
    }

    public void setOnWeekChangeListener(OnWeekChangeListener listener) {
        this.weekChangeListener = listener;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_week_page, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        // Calcular el offset desde la posición central
        int weekOffset = position - MIDDLE_POSITION;

        // Calcular la semana correspondiente
        Calendar weekStart = (Calendar) baseWeekStart.clone();
        weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

        // Notificar cambio de semana
        if (weekChangeListener != null) {
            weekChangeListener.onWeekChanged((Calendar) weekStart.clone());
        }

        holder.bind(weekStart, allCitas, fragmentManager);
    }

    @Override
    public int getItemCount() {
        return TOTAL_WEEKS;
    }

    public int getMiddlePosition() {
        return MIDDLE_POSITION;
    }

    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView rvCalendarWeek;
        private final TextView[] dayHeaders;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            rvCalendarWeek = itemView.findViewById(R.id.rv_calendar_week);
            rvCalendarWeek.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            // Referencias a los TextViews de los días
            dayHeaders = new TextView[7];
            dayHeaders[0] = itemView.findViewById(R.id.tv_day_0);
            dayHeaders[1] = itemView.findViewById(R.id.tv_day_1);
            dayHeaders[2] = itemView.findViewById(R.id.tv_day_2);
            dayHeaders[3] = itemView.findViewById(R.id.tv_day_3);
            dayHeaders[4] = itemView.findViewById(R.id.tv_day_4);
            dayHeaders[5] = itemView.findViewById(R.id.tv_day_5);
            dayHeaders[6] = itemView.findViewById(R.id.tv_day_6);
        }

        public void bind(Calendar weekStart, List<Cita> allCitas, FragmentManager fragmentManager) {
            // Actualizar encabezados de días
            updateDayHeaders(weekStart);

            // Generar horas del día
            List<String> horas = new ArrayList<>();
            for (int i = 8; i <= 18; i++) {
                horas.add(String.format(Locale.getDefault(), "%02d:00", i));
            }

            // Filtrar citas de esta semana
            List<Cita> citasSemana = filterCitasForWeek(weekStart, allCitas);

            // Configurar el adaptador del RecyclerView
            CalendarioAdapter adapter = new CalendarioAdapter(
                    itemView.getContext(),
                    horas,
                    citasSemana,
                    fragmentManager
            );
            rvCalendarWeek.setAdapter(adapter);
        }

        private void updateDayHeaders(Calendar weekStart) {
            String[] diasSemana = {"lun", "mar", "mié", "jue", "vie", "sáb", "dom"};
            Calendar cal = (Calendar) weekStart.clone();

            for (int i = 0; i < 7; i++) {
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                dayHeaders[i].setText(diasSemana[i] + " " + dayOfMonth);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        private List<Cita> filterCitasForWeek(Calendar weekStart, List<Cita> allCitas) {
            List<Cita> citasSemana = new ArrayList<>();

            Calendar endCal = (Calendar) weekStart.clone();
            endCal.add(Calendar.DAY_OF_MONTH, 6);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);

            for (Cita cita : allCitas) {
                Calendar citaCal = cita.getFechaHoraCalendar();
                if (citaCal != null) {
                    if (!citaCal.before(weekStart) && !citaCal.after(endCal)) {
                        // Calcular el índice del día (0 = Lunes, 6 = Domingo)
                        long diffInMillis = citaCal.getTimeInMillis() - weekStart.getTimeInMillis();
                        int diaIndex = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                        diaIndex = Math.max(0, Math.min(6, diaIndex));

                        cita.setDiaSemana(diaIndex);
                        citasSemana.add(cita);
                    }
                }
            }

            return citasSemana;
        }
    }
}