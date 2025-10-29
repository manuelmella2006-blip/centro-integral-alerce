package com.example.centrointegralalerce.ui;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adaptador para ViewPager2 que muestra una semana desplazable horizontalmente.
 * Se alinea con layout_week_page.xml actual (encabezado simple con tv_day_0 ... tv_day_6).
 */
public class CalendarPagerAdapter extends RecyclerView.Adapter<CalendarPagerAdapter.WeekViewHolder> {

    private static final String TAG = "CalendarPagerAdapter";

    private static final int TOTAL_WEEKS = 1000;
    private static final int MIDDLE_POSITION = TOTAL_WEEKS / 2;

    private final List<Cita> allCitas;
    private final FragmentManager fragmentManager;
    private final Calendar baseWeekStart;
    private final Calendar today; // día actual real

    public CalendarPagerAdapter(List<Cita> allCitas,
                                FragmentManager fragmentManager,
                                Calendar currentWeek) {

        this.allCitas = (allCitas != null) ? allCitas : new ArrayList<>();
        this.fragmentManager = fragmentManager;

        // Semana base (normalizada a Lunes)
        this.baseWeekStart = (currentWeek != null)
                ? (Calendar) currentWeek.clone()
                : Calendar.getInstance();
        normalizeToMonday(this.baseWeekStart);

        // Hoy (sin tocarlo a lunes, lo usamos para resaltar)
        this.today = Calendar.getInstance();

        Log.d(TAG, "✅ Adapter creado con " + this.allCitas.size() + " citas");
    }

    private void normalizeToMonday(Calendar cal) {
        try {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);

            int dow = cal.get(Calendar.DAY_OF_WEEK);
            int offsetFromMonday = (dow == Calendar.SUNDAY) ? 6 : dow - Calendar.MONDAY;

            cal.add(Calendar.DAY_OF_MONTH, -offsetFromMonday);
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

    // =========================
    // ViewHolder
    // =========================
    public static class WeekViewHolder extends RecyclerView.ViewHolder {

        private static final String TAG = "WeekViewHolder";

        private final RecyclerView rvCalendarWeek;

        // Ahora: solo 1 TextView por día (Lun, Mar, Mié...) según tu layout actual.
        private final TextView[] dayHeaders = new TextView[7];

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);

            rvCalendarWeek = itemView.findViewById(R.id.rv_calendar_week);
            if (rvCalendarWeek != null) {
                rvCalendarWeek.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

            // Obtener referencias tv_day_0 ... tv_day_6
            for (int i = 0; i < 7; i++) {
                int headerId = itemView.getResources().getIdentifier(
                        "tv_day_" + i,
                        "id",
                        itemView.getContext().getPackageName()
                );

                TextView tv = itemView.findViewById(headerId);
                dayHeaders[i] = tv;

                if (tv == null) {
                    Log.w(TAG, "⚠ No encontré tv_day_" + i + " en layout_week_page.xml");
                }
            }
        }

        public void bind(Calendar weekStart,
                         List<Cita> allCitas,
                         FragmentManager fragmentManager,
                         Calendar todayOriginal) {

            if (weekStart == null) {
                Log.e(TAG, "❌ weekStart es null en bind()");
                return;
            }

            // 1. Actualizar encabezados (LUN 28, MAR 29, ...)
            updateDayHeadersSimple(weekStart, todayOriginal);

            // 2. Filtrar citas de esa semana
            List<Cita> citasSemana = filterCitasForWeek(weekStart, allCitas);
            Log.d(TAG, "📅 Semana: " + weekStart.getTime() + " | Citas: " + citasSemana.size());

            // 3. Obtener horas únicas ordenadas
            List<String> horasConCitas = getHorasUnicasOrdenadas(citasSemana);

            // 4. Cargar adaptador interno de la lista de horas/citas
            if (rvCalendarWeek != null) {
                CalendarioAdapter adapter = new CalendarioAdapter(
                        itemView.getContext(),
                        horasConCitas,
                        citasSemana,
                        fragmentManager
                );
                rvCalendarWeek.setAdapter(adapter);
            }
        }

        /**
         * Versión adaptada a tu XML actual:
         * - Cada columna de día es UN TextView (tv_day_i).
         * - Le ponemos "LUN 28", "MAR 29", etc.
         * - Si es HOY, lo resaltamos en verde (bg_day_today + texto blanco).
         */
        private void updateDayHeadersSimple(Calendar weekStart, Calendar todayOriginal) {
            try {
                String[] abrevs = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"};

                Calendar cal = (Calendar) weekStart.clone();

                // Normalizamos "today" para comparación solo de fecha (año/mes/día)
                Calendar todayNorm = (Calendar) todayOriginal.clone();
                todayNorm.set(Calendar.HOUR_OF_DAY, 0);
                todayNorm.set(Calendar.MINUTE, 0);
                todayNorm.set(Calendar.SECOND, 0);
                todayNorm.set(Calendar.MILLISECOND, 0);

                for (int i = 0; i < 7; i++) {

                    TextView header = dayHeaders[i];
                    if (header == null) {
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                        continue;
                    }

                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                    // Texto del header: "LUN 28", etc.
                    String label = abrevs[i] + " " + dayOfMonth;
                    header.setText(label);

                    // ¿Es hoy?
                    boolean isToday =
                            cal.get(Calendar.YEAR) == todayNorm.get(Calendar.YEAR) &&
                                    cal.get(Calendar.MONTH) == todayNorm.get(Calendar.MONTH) &&
                                    cal.get(Calendar.DAY_OF_MONTH) == todayNorm.get(Calendar.DAY_OF_MONTH);

                    if (isToday) {
                        // Fondo verde personalizado (tu drawable bg_day_today)
                        try {
                            header.setBackgroundResource(R.drawable.bg_day_today);
                        } catch (Exception e) {
                            // Si no existe bg_day_today, no revienta, solo loguea
                            Log.w(TAG, "⚠ bg_day_today faltante, usando fallback transparente");
                            header.setBackgroundColor(
                                    ContextCompat.getColor(itemView.getContext(), android.R.color.transparent)
                            );
                        }

                        header.setTextColor(ContextCompat.getColor(
                                itemView.getContext(),
                                R.color.blanco
                        ));
                        header.setTypeface(null, Typeface.BOLD);

                    } else {
                        // Estado normal
                        header.setBackgroundColor(
                                ContextCompat.getColor(itemView.getContext(), android.R.color.transparent)
                        );
                        header.setTextColor(ContextCompat.getColor(
                                itemView.getContext(),
                                R.color.gris_oscuro
                        ));
                        header.setTypeface(null, Typeface.NORMAL);
                    }

                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error en updateDayHeadersSimple()", e);
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

                long startMs = weekStart.getTimeInMillis();
                long endMs = weekEnd.getTimeInMillis();

                for (Cita cita : allCitas) {
                    if (cita == null || cita.getFecha() == null) continue;

                    long t = cita.getFecha().getTime();
                    if (t >= startMs && t <= endMs) {
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
