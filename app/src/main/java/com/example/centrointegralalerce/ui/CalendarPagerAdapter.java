package com.example.centrointegralalerce.ui;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
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

    // =============================
    // MÉTODO MEJORADO normalizeToMonday
    // =============================
    private void normalizeToMonday(Calendar cal) {
        try {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Log.d(TAG, "🔧 Semana normalizada a: " + cal.getTime());
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

    // =====================================================
    // VIEWHOLDER
    // =====================================================
    public static class WeekViewHolder extends RecyclerView.ViewHolder {

        private static final String TAG = "WeekViewHolder";

        private final RecyclerView rvCalendarWeek;
        private final TextView[] dayHeaders = new TextView[7];

        private final android.widget.HorizontalScrollView horizontalScrollView;
        private final android.widget.LinearLayout headerWeekDays;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);

            horizontalScrollView = itemView.findViewById(R.id.horizontal_scroll_calendar);
            headerWeekDays = itemView.findViewById(R.id.header_week_days);

            rvCalendarWeek = itemView.findViewById(R.id.rv_calendar_week);
            if (rvCalendarWeek != null) {
                rvCalendarWeek.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }

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

            setupSynchronizedScroll();
        }

        private void setupSynchronizedScroll() {
            if (horizontalScrollView == null || rvCalendarWeek == null) {
                Log.w(TAG, "❌ No se pudo configurar scroll sincronizado - vistas nulas");
                return;
            }

            rvCalendarWeek.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                    horizontalScrollView.scrollTo(scrollX, scrollY));

            horizontalScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) ->
                    rvCalendarWeek.scrollTo(scrollX, scrollY));

            Log.d(TAG, "✅ Scroll sincronizado configurado");
        }

        // =====================================================
        // MÉTODO MEJORADO bind()
        // =====================================================
        public void bind(Calendar weekStart, List<Cita> allCitas, FragmentManager fragmentManager, Calendar todayOriginal) {
            if (weekStart == null) {
                Log.e(TAG, "❌ weekStart es null en bind()");
                return;
            }

            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_MONTH, 6);

            // DEBUG CRÍTICO: Verificar la semana que se está mostrando
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM", Locale.getDefault());
            Log.d(TAG, "🎯 SEMANA COMPLETA:");
            Calendar temp = (Calendar) weekStart.clone();
            for (int i = 0; i < 7; i++) {
                Log.d(TAG, "Día " + i + ": " + sdf.format(temp.getTime()));
                temp.add(Calendar.DAY_OF_MONTH, 1);
            }

            Log.d(TAG, "🎯 Mostrando semana: " + weekStart.getTime() + " a " + weekEnd.getTime());
            Log.d(TAG, "📊 Total de citas disponibles: " + allCitas.size());

            updateDayHeadersSimple(weekStart, todayOriginal);

            List<Cita> citasSemana = filterCitasForWeek(weekStart, allCitas);
            Log.d(TAG, "📅 Semana: " + weekStart.getTime() + " | Citas encontradas: " + citasSemana.size());

            List<String> horasConCitas = getHorasUnicasOrdenadas(citasSemana);
            Log.d(TAG, "⏰ Horas con citas: " + horasConCitas);

            if (rvCalendarWeek != null) {
                CalendarioAdapter adapter = new CalendarioAdapter(
                        itemView.getContext(),
                        horasConCitas,
                        citasSemana,
                        fragmentManager
                );
                rvCalendarWeek.setAdapter(adapter);
            }

            // 🔥 NUEVO: Ajustar el scroll para mostrar el inicio de la semana
            ajustarScrollInicial();
        }

        // =====================================================
        // FILTRADO DE CITAS
        // =====================================================
        private List<Cita> filterCitasForWeek(Calendar weekStart, List<Cita> allCitas) {
            List<Cita> citasSemana = new ArrayList<>();
            if (weekStart == null || allCitas == null) return citasSemana;

            try {
                Calendar weekEnd = (Calendar) weekStart.clone();
                weekEnd.add(Calendar.DAY_OF_MONTH, 6);
                weekEnd.set(Calendar.HOUR_OF_DAY, 23);
                weekEnd.set(Calendar.MINUTE, 59);
                weekEnd.set(Calendar.SECOND, 59);

                Log.d(TAG, "📅 Filtrando semana: " + formatCalendar(weekStart) + " a " + formatCalendar(weekEnd));

                for (Cita cita : allCitas) {
                    if (cita == null || cita.getFecha() == null) continue;

                    // 🔥 NUEVO: Filtrar citas canceladas
                    if ("cancelada".equalsIgnoreCase(cita.getEstado())) {
                        Log.d(TAG, "⛔ Cita cancelada omitida: " + cita.getActividadNombre());
                        continue; // Saltar citas canceladas
                    }

                    // Crear Calendar para la fecha de la cita
                    Calendar citaDate = Calendar.getInstance();
                    citaDate.setTime(cita.getFecha());

                    Calendar citaDateNormalized = (Calendar) citaDate.clone();
                    citaDateNormalized.set(Calendar.HOUR_OF_DAY, 0);
                    citaDateNormalized.set(Calendar.MINUTE, 0);
                    citaDateNormalized.set(Calendar.SECOND, 0);
                    citaDateNormalized.set(Calendar.MILLISECOND, 0);

                    Calendar weekStartNormalized = (Calendar) weekStart.clone();
                    weekStartNormalized.set(Calendar.HOUR_OF_DAY, 0);
                    weekStartNormalized.set(Calendar.MINUTE, 0);
                    weekStartNormalized.set(Calendar.SECOND, 0);
                    weekStartNormalized.set(Calendar.MILLISECOND, 0);

                    Calendar weekEndNormalized = (Calendar) weekEnd.clone();
                    weekEndNormalized.set(Calendar.HOUR_OF_DAY, 23);
                    weekEndNormalized.set(Calendar.MINUTE, 59);
                    weekEndNormalized.set(Calendar.SECOND, 59);
                    weekEndNormalized.set(Calendar.MILLISECOND, 999);

                    boolean inRange = !citaDateNormalized.before(weekStartNormalized)
                            && !citaDateNormalized.after(weekEndNormalized);

                    if (inRange) {
                        citasSemana.add(cita);
                        Log.d(TAG, "✅ Cita agregada: " + cita.getActividadNombre() + " (" + formatCalendar(citaDate) + ")");
                    }
                }

                Log.d(TAG, "🎯 Citas activas encontradas para esta semana: " + citasSemana.size());
            } catch (Exception e) {
                Log.e(TAG, "Error filtrando citas", e);
            }

            return citasSemana;
        }

        private void ajustarScrollInicial() {
            try {
                if (horizontalScrollView != null) {
                    // Esperar un momento para que el layout se renderice completamente
                    horizontalScrollView.postDelayed(() -> {
                        try {
                            // Scroll al inicio (día lunes)
                            horizontalScrollView.scrollTo(0, 0);
                            Log.d(TAG, "🔧 Scroll ajustado al inicio (Lunes)");

                            // DEBUG: Verificar posición actual del scroll
                            horizontalScrollView.post(() -> {
                                int scrollX = horizontalScrollView.getScrollX();
                                Log.d(TAG, "📏 Posición actual del scroll: " + scrollX);
                            });
                        } catch (Exception e) {
                            Log.e(TAG, "Error ajustando scroll", e);
                        }
                    }, 100);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en ajustarScrollInicial", e);
            }
        }
        // Método auxiliar para formatear Calendar
        private String formatCalendar(Calendar cal) {
            if (cal == null) return "null";
            SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(cal.getTime());
        }

        // Método auxiliar para obtener nombre del día
        private String getDiaSemanaNombre(Calendar cal) {
            if (cal == null) return "null";
            String[] dias = {"DOMINGO", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO"};
            return dias[cal.get(Calendar.DAY_OF_WEEK) - 1];
        }

        private Calendar parseFechaString(String fechaStr) {
            if (fechaStr == null || fechaStr.isEmpty()) return null;
            try {
                String[] parts = fechaStr.split("/");
                if (parts.length != 3) return null;

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int year = Integer.parseInt(parts[2]);

                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day, 0, 0, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal;
            } catch (Exception e) {
                Log.e(TAG, "Error parseando fecha: " + fechaStr, e);
                return null;
            }
        }

        // =====================================================
        // ENCABEZADO DE DÍAS
        // =====================================================
        private void updateDayHeadersSimple(Calendar weekStart, Calendar todayOriginal) {
            try {
                // CAMBIO: Quitar los números de los encabezados
                String[] abrevs = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"}; // ← QUITAR (0), (1), etc.

                Calendar cal = (Calendar) weekStart.clone();
                Calendar todayNorm = (Calendar) todayOriginal.clone();
                todayNorm.set(Calendar.HOUR_OF_DAY, 0);
                todayNorm.set(Calendar.MINUTE, 0);
                todayNorm.set(Calendar.SECOND, 0);
                todayNorm.set(Calendar.MILLISECOND, 0);

                Log.d(TAG, "📅 Actualizando encabezados - Semana comienza: " + cal.getTime());
                Log.d(TAG, "📅 HOY es: " + todayNorm.getTime());

                for (int i = 0; i < 7; i++) {
                    TextView header = dayHeaders[i];
                    if (header == null) {
                        Log.w(TAG, "⚠️ Encabezado " + i + " es null");
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                        continue;
                    }

                    String diaSemana = "";
                    switch (cal.get(Calendar.DAY_OF_WEEK)) {
                        case Calendar.MONDAY: diaSemana = "LUNES"; break;
                        case Calendar.TUESDAY: diaSemana = "MARTES"; break;
                        case Calendar.WEDNESDAY: diaSemana = "MIÉRCOLES"; break;
                        case Calendar.THURSDAY: diaSemana = "JUEVES"; break;
                        case Calendar.FRIDAY: diaSemana = "VIERNES"; break;
                        case Calendar.SATURDAY: diaSemana = "SÁBADO"; break;
                        case Calendar.SUNDAY: diaSemana = "DOMINGO"; break;
                    }

                    // CAMBIO: Solo mostrar la abreviatura y el día del mes
                    String label = abrevs[i] + "\n" + cal.get(Calendar.DAY_OF_MONTH);
                    header.setText(label);

                    boolean isToday = cal.get(Calendar.YEAR) == todayNorm.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == todayNorm.get(Calendar.MONTH) &&
                            cal.get(Calendar.DAY_OF_MONTH) == todayNorm.get(Calendar.DAY_OF_MONTH);

                    Log.d(TAG, "📌 Encabezado " + i + " (" + abrevs[i] + ") → " +
                            cal.get(Calendar.DAY_OF_MONTH) + " " + diaSemana +
                            (isToday ? " ← HOY" : ""));

                    if (isToday) {
                        try {
                            header.setBackgroundResource(R.drawable.bg_day_today);
                            Log.d(TAG, "🎯 ENCABEZADO MARCADO COMO HOY: " + i + " - " + diaSemana);
                        } catch (Exception e) {
                            Log.w(TAG, "⚠ bg_day_today faltante");
                        }
                        header.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blanco));
                        header.setTypeface(null, Typeface.BOLD);
                    } else {
                        header.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                        header.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.gris_oscuro));
                        header.setTypeface(null, Typeface.NORMAL);
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en updateDayHeadersSimple()", e);
            }
        }

        // =====================================================
        // HORAS ÚNICAS ORDENADAS
        // =====================================================
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
                    int mA = (p1.length > 1) ? Integer.parseInt(p1[1]) : 0;
                    int mB = (p2.length > 1) ? Integer.parseInt(p2[1]) : 0;
                    return mA - mB;
                } catch (Exception e) {
                    return h1.compareTo(h2);
                }
            });
            return horasList;
        }
    }
}
