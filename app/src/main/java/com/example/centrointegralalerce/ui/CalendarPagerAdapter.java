package com.example.centrointegralalerce.ui;

import android.util.Log;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Adaptador para el ViewPager2 que muestra diferentes semanas del calendario
 * CON SISTEMA DINÁMICO: Solo muestra las horas donde hay citas
 */
public class CalendarPagerAdapter extends RecyclerView.Adapter<CalendarPagerAdapter.WeekViewHolder> {
    private static final String TAG = "CalendarPagerAdapter";

    private static final int TOTAL_WEEKS = 1000; // Simular scroll infinito
    private static final int MIDDLE_POSITION = TOTAL_WEEKS / 2;

    private final List<Cita> allCitas;
    private final FragmentManager fragmentManager;
    private final Calendar baseWeekStart; // Semana de referencia (siempre lunes)

    public CalendarPagerAdapter(List<Cita> allCitas, FragmentManager fragmentManager, Calendar currentWeek) {
        this.allCitas = allCitas != null ? allCitas : new ArrayList<>();
        this.fragmentManager = fragmentManager;

        // Clonar y asegurar que empiece en lunes a las 00:00:00
        this.baseWeekStart = currentWeek != null ? (Calendar) currentWeek.clone() : Calendar.getInstance();
        normalizeToMonday(this.baseWeekStart);

        Log.d(TAG, "Adapter creado con " + this.allCitas.size() + " citas totales");
        Log.d(TAG, "Semana base: " + this.baseWeekStart.getTime());
    }

    /**
     * Normaliza un Calendar para que apunte al lunes de su semana a las 00:00:00
     */
    private void normalizeToMonday(Calendar cal) {
        try {
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

        } catch (Exception e) {
            Log.e(TAG, "Error al normalizar calendario: " + e.getMessage(), e);
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
            // Calcular el offset desde la posición central
            int weekOffset = position - MIDDLE_POSITION;

            // Clonar la semana base y sumar semanas completas
            Calendar weekStart = (Calendar) baseWeekStart.clone();
            weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);

            Log.d(TAG, "Mostrando semana offset " + weekOffset + ": " + weekStart.getTime());

            // Enlazar datos de esa semana
            holder.bind(weekStart, allCitas, fragmentManager);

        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder posición " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return TOTAL_WEEKS;
    }

    public int getMiddlePosition() {
        return MIDDLE_POSITION;
    }

    /**
     * Calcula la semana correspondiente a una posición dada
     */
    public Calendar getWeekForPosition(int position) {
        try {
            int weekOffset = position - MIDDLE_POSITION;
            Calendar weekStart = (Calendar) baseWeekStart.clone();
            weekStart.add(Calendar.WEEK_OF_YEAR, weekOffset);
            return weekStart;
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener semana para posición " + position + ": " + e.getMessage(), e);
            return (Calendar) baseWeekStart.clone();
        }
    }

    // ------------------------------------------------------
    // ViewHolder: representa una semana completa
    // ------------------------------------------------------
    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        private static final String TAG = "WeekViewHolder";

        private final RecyclerView rvCalendarWeek;
        private final TextView[] dayHeaders;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            rvCalendarWeek = itemView.findViewById(R.id.rv_calendar_week);

            if (rvCalendarWeek != null) {
                rvCalendarWeek.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            } else {
                Log.e(TAG, "❌ rv_calendar_week es null - verifica el XML");
            }

            // Encabezados (lunes-domingo)
            dayHeaders = new TextView[7];
            dayHeaders[0] = itemView.findViewById(R.id.tv_day_0);
            dayHeaders[1] = itemView.findViewById(R.id.tv_day_1);
            dayHeaders[2] = itemView.findViewById(R.id.tv_day_2);
            dayHeaders[3] = itemView.findViewById(R.id.tv_day_3);
            dayHeaders[4] = itemView.findViewById(R.id.tv_day_4);
            dayHeaders[5] = itemView.findViewById(R.id.tv_day_5);
            dayHeaders[6] = itemView.findViewById(R.id.tv_day_6);

            // Validar que todos los headers existen
            for (int i = 0; i < dayHeaders.length; i++) {
                if (dayHeaders[i] == null) {
                    Log.e(TAG, "❌ tv_day_" + i + " es null - verifica el XML");
                }
            }
        }

        public void bind(Calendar weekStart, List<Cita> allCitas, FragmentManager fragmentManager) {
            try {
                if (weekStart == null) {
                    Log.e(TAG, "❌ weekStart es null en bind()");
                    return;
                }

                Log.d(TAG, "=== BINDING SEMANA: " + weekStart.getTime() + " ===");

                updateDayHeaders(weekStart);

                // Filtrar citas de esta semana
                List<Cita> citasSemana = filterCitasForWeek(weekStart, allCitas);

                Log.d(TAG, "Citas filtradas para esta semana: " + citasSemana.size());

                // 🔹 SISTEMA DINÁMICO: Extraer solo las horas que tienen citas (ordenadas)
                List<String> horasConCitas = getHorasUnicasOrdenadas(citasSemana);

                Log.d(TAG, "Horas únicas con citas: " + horasConCitas.size());
                for (String hora : horasConCitas) {
                    Log.d(TAG, "  - " + hora);
                }

                if (rvCalendarWeek == null) {
                    Log.e(TAG, "❌ No se puede configurar adapter - RecyclerView es null");
                    return;
                }

                // Configurar adaptador interno CON HORAS DINÁMICAS
                CalendarioAdapter adapter = new CalendarioAdapter(
                        itemView.getContext(),
                        horasConCitas, // 🔹 Solo las horas con citas
                        citasSemana,
                        fragmentManager
                );
                rvCalendarWeek.setAdapter(adapter);

                Log.d(TAG, "✅ Adapter configurado con " + citasSemana.size() + " citas en " + horasConCitas.size() + " filas");

            } catch (Exception e) {
                Log.e(TAG, "❌ Error en bind(): " + e.getMessage(), e);
            }
        }

        /**
         * Actualiza los encabezados con el formato "lun 14"
         */
        private void updateDayHeaders(Calendar weekStart) {
            if (weekStart == null) {
                Log.e(TAG, "No se pueden actualizar headers - weekStart es null");
                return;
            }

            try {
                String[] diasSemana = {"lun", "mar", "mié", "jue", "vie", "sáb", "dom"};
                Calendar cal = (Calendar) weekStart.clone();

                for (int i = 0; i < 7; i++) {
                    if (dayHeaders[i] == null) {
                        Log.w(TAG, "Header " + i + " es null, saltando");
                        continue;
                    }

                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    String headerText = diasSemana[i] + " " + dayOfMonth;
                    dayHeaders[i].setText(headerText);

                    Log.d(TAG, "Header " + i + ": " + headerText);

                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al actualizar headers: " + e.getMessage(), e);
            }
        }

        /**
         * Filtra las citas que pertenecen a esta semana con validación robusta
         */
        private List<Cita> filterCitasForWeek(Calendar weekStart, List<Cita> allCitas) {
            List<Cita> citasSemana = new ArrayList<>();

            if (weekStart == null) {
                Log.e(TAG, "❌ weekStart es null en filterCitasForWeek");
                return citasSemana;
            }

            if (allCitas == null || allCitas.isEmpty()) {
                Log.d(TAG, "No hay citas para filtrar");
                return citasSemana;
            }

            try {
                Calendar semanaFin = (Calendar) weekStart.clone();
                semanaFin.add(Calendar.DAY_OF_MONTH, 6);
                semanaFin.set(Calendar.HOUR_OF_DAY, 23);
                semanaFin.set(Calendar.MINUTE, 59);
                semanaFin.set(Calendar.SECOND, 59);

                Log.d(TAG, "Filtrando citas entre:");
                Log.d(TAG, "  Inicio: " + weekStart.getTime());
                Log.d(TAG, "  Fin: " + semanaFin.getTime());

                int citasProcesadas = 0;
                int citasIncluidas = 0;
                int citasExcluidas = 0;

                for (Cita cita : allCitas) {
                    citasProcesadas++;

                    if (cita == null) {
                        Log.w(TAG, "Cita null encontrada");
                        continue;
                    }

                    Calendar citaCal = cita.getFechaHoraCalendar();

                    if (citaCal == null) {
                        Log.w(TAG, "Timestamp null en cita: " + cita.getActividad());
                        citasExcluidas++;
                        continue;
                    }

                    // Verificar si la cita está en el rango
                    boolean enRango = !citaCal.before(weekStart) && !citaCal.after(semanaFin);

                    if (enRango) {
                        // Calcular índice del día (0=Lun, 6=Dom)
                        long diffInMillis = citaCal.getTimeInMillis() - weekStart.getTimeInMillis();
                        int diaIndex = (int) (diffInMillis / (1000 * 60 * 60 * 24));
                        diaIndex = Math.max(0, Math.min(6, diaIndex));

                        // IMPORTANTE: Actualizar el día de la semana
                        cita.setDiaSemana(diaIndex);

                        citasSemana.add(cita);
                        citasIncluidas++;

                        Log.d(TAG, "✅ Incluida: " + cita.getActividad() +
                                " | Fecha: " + citaCal.getTime() +
                                " | Día: " + diaIndex +
                                " | Hora: " + cita.getHora());
                    } else {
                        citasExcluidas++;
                        Log.d(TAG, "⏭️ Excluida (fuera de rango): " + cita.getActividad() +
                                " | Fecha: " + citaCal.getTime());
                    }
                }

                Log.d(TAG, "=== RESULTADO FILTRADO ===");
                Log.d(TAG, "Procesadas: " + citasProcesadas);
                Log.d(TAG, "Incluidas: " + citasIncluidas);
                Log.d(TAG, "Excluidas: " + citasExcluidas);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error al filtrar citas: " + e.getMessage(), e);
            }

            return citasSemana;
        }

        /**
         * 🔹 NUEVO: Extrae las horas únicas donde hay citas y las ordena cronológicamente
         */
        private List<String> getHorasUnicasOrdenadas(List<Cita> citas) {
            Set<String> horasSet = new HashSet<>();

            Log.d(TAG, "🔍 Extrayendo horas únicas de " + citas.size() + " citas");

            for (Cita cita : citas) {
                if (cita == null) continue;

                String hora = cita.getHora();
                if (hora != null && !hora.isEmpty()) {
                    horasSet.add(hora);
                    Log.d(TAG, "  + Hora encontrada: " + hora + " de " + cita.getActividad());
                } else {
                    Log.w(TAG, "  ⚠️ Cita sin hora: " + cita.getActividad());
                }
            }

            List<String> horasList = new ArrayList<>(horasSet);

            // Ordenar las horas cronológicamente
            Collections.sort(horasList, (h1, h2) -> {
                try {
                    // Parsear "HH:mm"
                    String[] parts1 = h1.split(":");
                    String[] parts2 = h2.split(":");

                    int hour1 = Integer.parseInt(parts1[0]);
                    int hour2 = Integer.parseInt(parts2[0]);

                    // Comparar horas
                    if (hour1 != hour2) {
                        return hour1 - hour2;
                    }

                    // Si las horas son iguales, comparar minutos
                    int min1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : 0;
                    int min2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : 0;
                    return min1 - min2;

                } catch (Exception e) {
                    Log.e(TAG, "Error al ordenar horas: " + h1 + " vs " + h2, e);
                    return h1.compareTo(h2); // Fallback a ordenamiento alfabético
                }
            });

            Log.d(TAG, "✅ Horas ordenadas: " + horasList);

            return horasList;
        }
    }
}