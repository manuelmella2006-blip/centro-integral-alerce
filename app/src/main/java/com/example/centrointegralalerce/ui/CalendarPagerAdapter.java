package com.example.centrointegralalerce.ui;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Adaptador profesional para ViewPager2 con diseño Santo Tomás
 */
public class CalendarPagerAdapter extends RecyclerView.Adapter<CalendarPagerAdapter.WeekViewHolder> {
    private static final String TAG = "CalendarPagerAdapter";

    private static final int TOTAL_WEEKS = 1000;
    private static final int MIDDLE_POSITION = TOTAL_WEEKS / 2;

    private final List<Cita> allCitas;
    private final FragmentManager fragmentManager;
    private final Calendar baseWeekStart;
    private Calendar today; // Día actual para resaltar

    public CalendarPagerAdapter(List<Cita> allCitas, FragmentManager fragmentManager, Calendar currentWeek) {
        this.allCitas = allCitas != null ? allCitas : new ArrayList<>();
        this.fragmentManager = fragmentManager;
        this.baseWeekStart = currentWeek != null ? (Calendar) currentWeek.clone() : Calendar.getInstance();
        normalizeToMonday(this.baseWeekStart);

        // Guardar fecha actual
        this.today = Calendar.getInstance();
        normalizeToMonday(this.today);

        Log.d(TAG, "✅ Adapter creado con " + this.allCitas.size() + " citas");
    }

    private void normalizeToMonday(Calendar cal) {
        try {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);

            int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (currentDayOfWeek == Calendar.SUNDAY) ? 6 : currentDayOfWeek - Calendar.MONDAY;

            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error normalizando calendario", e);
        }
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
        try {
            int weekOffset = position - MIDDLE_POSITION;
            Calendar weekStart = (Calendar) baseWeekStart.clone();
            weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

            holder.bind(weekStart, allCitas, fragmentManager, today);
        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder posición " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return TOTAL_WEEKS;
    }

    public int getMiddlePosition() {
        return MIDDLE_POSITION;
    }

    public Calendar getWeekForPosition(int position) {
        try {
            int weekOffset = position - MIDDLE_POSITION;
            Calendar weekStart = (Calendar) baseWeekStart.clone();
            weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);
            return weekStart;
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo semana para posición " + position, e);
            return (Calendar) baseWeekStart.clone();
        }
    }

    // ============================================
    // VIEW HOLDER CON DISEÑO PROFESIONAL
    // ============================================
    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "WeekViewHolder";

        private final RecyclerView rvCalendarWeek;
        private final LinearLayout[] dayHeaders;
        private final TextView[] dayNames;
        private final TextView[] dayNumbers;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);

            rvCalendarWeek = itemView.findViewById(R.id.rv_calendar_week);
            if (rvCalendarWeek != null) {
                rvCalendarWeek.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            // Inicializar arrays para headers mejorados
            dayHeaders = new LinearLayout[7];
            dayNames = new TextView[7];
            dayNumbers = new TextView[7];

            for (int i = 0; i < 7; i++) {
                // Obtener el LinearLayout contenedor
                int headerId = itemView.getResources().getIdentifier("tv_day_" + i, "id", itemView.getContext().getPackageName());
                dayHeaders[i] = itemView.findViewById(headerId);

                if (dayHeaders[i] != null) {
                    // Obtener los TextViews internos
                    int nameId = itemView.getResources().getIdentifier("tv_day_name_" + i, "id", itemView.getContext().getPackageName());
                    int numberId = itemView.getResources().getIdentifier("tv_day_number_" + i, "id", itemView.getContext().getPackageName());

                    dayNames[i] = itemView.findViewById(nameId);
                    dayNumbers[i] = itemView.findViewById(numberId);
                }
            }
        }

        public void bind(Calendar weekStart, List<Cita> allCitas, FragmentManager fragmentManager, Calendar today) {
            try {
                if (weekStart == null) {
                    Log.e(TAG, "❌ weekStart es null");
                    return;
                }

                // Actualizar headers con indicador de día actual
                updateDayHeadersProfessional(weekStart, today);

                // Filtrar citas de esta semana
                List<Cita> citasSemana = filterCitasForWeek(weekStart, allCitas);
                Log.d(TAG, "📅 Semana: " + weekStart.getTime() + " | Citas: " + citasSemana.size());

                // Obtener horas únicas
                List<String> horasConCitas = getHorasUnicasOrdenadas(citasSemana);

                if (rvCalendarWeek != null) {
                    CalendarioAdapter adapter = new CalendarioAdapter(
                            itemView.getContext(),
                            horasConCitas,
                            citasSemana,
                            fragmentManager
                    );
                    rvCalendarWeek.setAdapter(adapter);
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en bind()", e);
            }
        }

        /**
         * Actualiza los headers con estilo profesional y marca el día actual
         */
        private void updateDayHeadersProfessional(Calendar weekStart, Calendar today) {
            if (weekStart == null) return;

            try {
                String[] diasSemana = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"};
                Calendar cal = (Calendar) weekStart.clone();
                Calendar todayNormalized = (Calendar) today.clone();

                // Normalizar today para comparación
                todayNormalized.set(Calendar.HOUR_OF_DAY, 0);
                todayNormalized.set(Calendar.MINUTE, 0);
                todayNormalized.set(Calendar.SECOND, 0);
                todayNormalized.set(Calendar.MILLISECOND, 0);

                for (int i = 0; i < 7; i++) {
                    if (dayNames[i] == null || dayNumbers[i] == null) continue;

                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                    // Actualizar textos
                    dayNames[i].setText(diasSemana[i]);
                    dayNumbers[i].setText(String.valueOf(dayOfMonth));

                    // Verificar si es el día actual
                    boolean isToday = cal.get(Calendar.YEAR) == todayNormalized.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == todayNormalized.get(Calendar.MONTH) &&
                            cal.get(Calendar.DAY_OF_MONTH) == todayNormalized.get(Calendar.DAY_OF_MONTH);

                    if (isToday) {
                        // Resaltar día actual
                        dayHeaders[i].setBackgroundResource(R.drawable.bg_day_today);
                        dayNames[i].setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blanco));
                        dayNumbers[i].setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blanco));
                        dayNumbers[i].setTypeface(null, Typeface.BOLD);
                    } else {
                        // Estilo normal
                        dayHeaders[i].setBackgroundResource(android.R.color.transparent);
                        dayNames[i].setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gris_medio));
                        dayNumbers[i].setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gris_oscuro));
                        dayNumbers[i].setTypeface(null, Typeface.NORMAL);
                    }

                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error actualizando headers", e);
            }
        }

        private List<Cita> filterCitasForWeek(Calendar weekStart, List<Cita> allCitas) {
            List<Cita> citasSemana = new ArrayList<>();
            if (weekStart == null || allCitas == null) return citasSemana;

            try {
                Calendar weekEnd = (Calendar) weekStart.clone();
                weekEnd.add(Calendar.DAY_OF_MONTH, 6);
                weekEnd.set(Calendar.HOUR_OF_DAY, 23);
                weekEnd.set(Calendar.MINUTE, 59);
                weekEnd.set(Calendar.SECOND, 59);

                for (Cita cita : allCitas) {
                    if (cita == null || cita.getFecha() == null) continue;

                    long t = cita.getFecha().getTime();
                    if (t >= weekStart.getTimeInMillis() && t <= weekEnd.getTimeInMillis()) {
                        citasSemana.add(cita);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error filtrando citas", e);
            }

            return citasSemana;
        }

        private List<String> getHorasUnicasOrdenadas(List<Cita> citas) {
            Set<String> horasSet = new HashSet<>();

            for (Cita cita : citas) {
                if (cita != null && cita.getHora() != null && !cita.getHora().isEmpty()) {
                    horasSet.add(cita.getHora());
                }
            }

            List<String> horasList = new ArrayList<>(horasSet);
            Collections.sort(horasList, (h1, h2) -> {
                try {
                    String[] p1 = h1.split(":");
                    String[] p2 = h2.split(":");
                    int hA = Integer.parseInt(p1[0]);
                    int hB = Integer.parseInt(p2[0]);
                    if (hA != hB) return hA - hB;
                    int mA = p1.length > 1 ? Integer.parseInt(p1[1]) : 0;
                    int mB = p2.length > 1 ? Integer.parseInt(p2[1]) : 0;
                    return mA - mB;
                } catch (Exception e) {
                    return h1.compareTo(h2);
                }
            });

            return horasList;
        }
    }
}