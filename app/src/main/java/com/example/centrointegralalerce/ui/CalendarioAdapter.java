package com.example.centrointegralalerce.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

/**
 * ✅ RESTAURADO: Citas con fecha amarilla abajo
 * ✅ CORREGIDO: Cálculo de día de semana
 */
public class CalendarioAdapter extends RecyclerView.Adapter<CalendarioAdapter.HourViewHolder> {
    private static final String TAG = "CalendarioAdapter";

    private final Context context;
    private final List<String> horas;
    private List<Cita> citas;
    private final FragmentManager fragmentManager;

    public CalendarioAdapter(Context context, List<String> horas, List<Cita> citas, FragmentManager fragmentManager) {
        this.context = context;
        this.horas = horas != null ? horas : new ArrayList<>();
        this.citas = citas != null ? citas : new ArrayList<>();
        this.fragmentManager = fragmentManager;

        Log.d(TAG, "Adapter creado con " + this.horas.size() + " horas y " + this.citas.size() + " citas");
        debugCitas();
    }

    private void debugCitas() {
        if (citas.isEmpty()) {
            Log.w(TAG, "⚠️ Lista de citas está vacía");
            return;
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Log.d(TAG, "=== CITAS RECIBIDAS ===");
        for (int i = 0; i < citas.size(); i++) {
            Cita c = citas.get(i);
            if (c == null) {
                Log.e(TAG, "Cita " + i + " es NULL");
                continue;
            }
            String fechaTxt = c.getFecha() != null ? df.format(c.getFecha()) : "sin fecha";
            Log.d(TAG, String.format(Locale.getDefault(),
                    "Cita %d: fecha=%s | hora=%s | lugarId=%s | estado=%s",
                    i, fechaTxt, safe(c.getHora()), safe(c.getLugarId()), safe(c.getEstado())));
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public void actualizarCitas(List<Cita> nuevasCitas) {
        this.citas = nuevasCitas != null ? nuevasCitas : new ArrayList<>();
        Log.d(TAG, "Citas actualizadas: " + citas.size() + " elementos");
        debugCitas();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_row, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        try {
            String horaActual = horas.get(position);
            holder.tvHour.setText(horaActual);

            clearCells(holder);

            int citasEncontradas = 0;
            for (Cita cita : citas) {
                if (cita == null) continue;

                String horaCita = cita.getHora();
                if (horaCita == null) continue;

                if (horaCita.equals(horaActual)) {
                    citasEncontradas++;
                    agregarCitaACelda(holder, cita);
                }
            }

            if (citasEncontradas > 0) {
                Log.d(TAG, "Hora " + horaActual + ": " + citasEncontradas + " cita(s) agregada(s)");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder posición " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return horas.size();
    }

    private void clearCells(HourViewHolder holder) {
        try {
            clearCeldaCompleta(holder.cellLun);
            clearCeldaCompleta(holder.cellMar);
            clearCeldaCompleta(holder.cellMie);
            clearCeldaCompleta(holder.cellJue);
            clearCeldaCompleta(holder.cellVie);
            clearCeldaCompleta(holder.cellSab);
            clearCeldaCompleta(holder.cellDom);
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar celdas: " + e.getMessage(), e);
        }
    }

    private void clearCeldaCompleta(LinearLayout celda) {
        if (celda != null) {
            celda.removeAllViews();
        }
    }

    /**
     * ✅ RESTAURADO: Cita con TÍTULO VERDE + FECHA AMARILLA
     */
    private void agregarCitaACelda(HourViewHolder holder, Cita cita) {
        try {
            if (cita == null || cita.getFecha() == null) {
                Log.e(TAG, "❌ Cita o fecha es null");
                return;
            }

            Calendar cal = Calendar.getInstance(new Locale("es", "ES"));
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setTime(cita.getFecha());

            int diaSemanaIndex = calcularDiaSemanaIndex(cita.getFecha());

            Log.d(TAG, "🎯 AGREGANDO CITA - Índice: " + diaSemanaIndex + " para fecha: " + cita.getFecha());

            if (diaSemanaIndex < 0 || diaSemanaIndex > 6) {
                Log.e(TAG, "❌ Índice de día fuera de rango: " + diaSemanaIndex);
                return;
            }

            // Crear vista de la cita
            LinearLayout layoutCita = new LinearLayout(context);
            layoutCita.setOrientation(LinearLayout.VERTICAL);
            layoutCita.setGravity(Gravity.CENTER);
            layoutCita.setPadding(12, 8, 12, 8);
            layoutCita.setBackgroundResource(R.drawable.bg_cita_gradient);
            layoutCita.setElevation(4f);

            // ✅ Texto principal (VERDE)
            TextView tvTitulo = new TextView(context);
            String titulo = cita.getActividadNombre() != null ? cita.getActividadNombre() : "Actividad";
            if (titulo.length() > 20) titulo = titulo.substring(0, 17) + "...";

            tvTitulo.setText(titulo);
            tvTitulo.setTextSize(10f);
            tvTitulo.setTextColor(Color.WHITE);
            tvTitulo.setTypeface(null, Typeface.BOLD);
            tvTitulo.setGravity(Gravity.CENTER);

            // ✅ Texto de fecha (AMARILLO) - RESTAURADO
            TextView tvFecha = new TextView(context);
            tvFecha.setText(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1));
            tvFecha.setTextSize(8f);
            tvFecha.setTextColor(Color.YELLOW);
            tvFecha.setGravity(Gravity.CENTER);
            tvFecha.setTypeface(null, Typeface.BOLD);

            layoutCita.addView(tvTitulo);
            layoutCita.addView(tvFecha); // ✅ RESTAURADO

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(2, 2, 2, 2);
            layoutCita.setLayoutParams(params);

            int colorBase = getColorForEstado(cita.getEstado());
            layoutCita.getBackground().setTint(colorBase);

            layoutCita.setOnClickListener(v -> {
                try {
                    CitaDetalleDialog dialog = new CitaDetalleDialog(cita);
                    dialog.show(fragmentManager, "detalleCita");
                } catch (Exception e) {
                    Log.e(TAG, "Error al mostrar detalle: " + e.getMessage(), e);
                }
            });

            LinearLayout celdaDestino = obtenerCeldaPorDia(holder, diaSemanaIndex);
            if (celdaDestino != null) {
                celdaDestino.addView(layoutCita);
                Log.d(TAG, "✅ Cita agregada en columna: " + diaSemanaIndex);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al agregar cita: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ CORREGIDO: Cálculo de día con Calendar.MONDAY como primer día
     */
    private int calcularDiaSemanaIndex(Date fecha) {
        try {
            Calendar cal = Calendar.getInstance(new Locale("es", "ES"));
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setTime(fecha);

            int dow = cal.get(Calendar.DAY_OF_WEEK);

            switch (dow) {
                case Calendar.MONDAY:    return 0;
                case Calendar.TUESDAY:   return 1;
                case Calendar.WEDNESDAY: return 2;
                case Calendar.THURSDAY:  return 3;
                case Calendar.FRIDAY:    return 4;
                case Calendar.SATURDAY:  return 5;
                case Calendar.SUNDAY:    return 6;
                default:                 return -1;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error calculando día de semana para fecha: " + fecha, e);
            return -1;
        }
    }

    private int getColorForEstado(String estado) {
        int def = Color.parseColor("#2E7D32");
        if (estado == null || estado.trim().isEmpty()) return def;
        String e = estado.trim().toLowerCase(Locale.getDefault());
        switch (e) {
            case "activa":
            case "activo":
            case "confirmada":
                return Color.parseColor("#2E7D32");
            case "pendiente":
                return Color.parseColor("#00796B");
            case "inactiva":
            case "cancelada":
            case "cancelado":
                return Color.parseColor("#E64A19");
            case "reprogramada":
                return Color.parseColor("#5E35B1");
            default:
                return def;
        }
    }

    private LinearLayout obtenerCeldaPorDia(HourViewHolder holder, int diaSemana) {
        switch (diaSemana) {
            case 0: return holder.cellLun;
            case 1: return holder.cellMar;
            case 2: return holder.cellMie;
            case 3: return holder.cellJue;
            case 4: return holder.cellVie;
            case 5: return holder.cellSab;
            case 6: return holder.cellDom;
            default:
                Log.e(TAG, "Día de semana fuera de rango: " + diaSemana);
                return null;
        }
    }

    public static class HourViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour;
        LinearLayout cellLun, cellMar, cellMie, cellJue, cellVie, cellSab, cellDom;

        public HourViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            cellLun = itemView.findViewById(R.id.cellLun);
            cellMar = itemView.findViewById(R.id.cellMar);
            cellMie = itemView.findViewById(R.id.cellMie);
            cellJue = itemView.findViewById(R.id.cellJue);
            cellVie = itemView.findViewById(R.id.cellVie);
            cellSab = itemView.findViewById(R.id.cellSab);
            cellDom = itemView.findViewById(R.id.cellDom);

            if (tvHour == null) Log.e("HourViewHolder", "❌ tvHour es null");
            if (cellLun == null) Log.e("HourViewHolder", "❌ cellLun es null");
            if (cellMar == null) Log.e("HourViewHolder", "❌ cellMar es null");
            if (cellMie == null) Log.e("HourViewHolder", "❌ cellMie es null");
            if (cellJue == null) Log.e("HourViewHolder", "❌ cellJue es null");
            if (cellVie == null) Log.e("HourViewHolder", "❌ cellVie es null");
            if (cellSab == null) Log.e("HourViewHolder", "❌ cellSab es null");
            if (cellDom == null) Log.e("HourViewHolder", "❌ cellDom es null");
        }
    }
}