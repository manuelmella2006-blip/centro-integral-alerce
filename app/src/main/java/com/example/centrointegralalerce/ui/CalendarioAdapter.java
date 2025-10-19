package com.example.centrointegralalerce.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Debug: Imprime información de todas las citas
     */
    private void debugCitas() {
        if (citas.isEmpty()) {
            Log.w(TAG, "⚠️ Lista de citas está vacía");
            return;
        }

        Log.d(TAG, "=== CITAS RECIBIDAS ===");
        for (int i = 0; i < citas.size(); i++) {
            Cita cita = citas.get(i);
            if (cita == null) {
                Log.e(TAG, "Cita " + i + " es NULL");
                continue;
            }

            Log.d(TAG, String.format("Cita %d: %s | Hora: %s | Día: %d | Tipo: %s | Válida: %s",
                    i,
                    cita.getActividad(),
                    cita.getHora(),
                    cita.getDiaSemana(),
                    cita.getTipoActividad(),
                    cita.esValida()
            ));

            // Alertas específicas
            if (!cita.esValida()) {
                Log.e(TAG, "❌ Cita inválida detectada: " + cita.toString());
            }
            if (cita.getDiaSemana() < 0 || cita.getDiaSemana() > 6) {
                Log.e(TAG, "❌ Día de semana fuera de rango: " + cita.getDiaSemana());
            }
        }
    }

    /**
     * Actualiza la lista de citas y refresca la vista
     */
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
                if (cita == null) {
                    Log.w(TAG, "Cita null encontrada en la lista");
                    continue;
                }

                // Validación robusta de hora
                String horaCita = cita.getHora();
                if (horaCita == null) {
                    Log.w(TAG, "Hora null en cita: " + cita.getActividad());
                    continue;
                }

                // Comparación exacta de hora
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
            holder.cellLun.removeAllViews();
            holder.cellMar.removeAllViews();
            holder.cellMie.removeAllViews();
            holder.cellJue.removeAllViews();
            holder.cellVie.removeAllViews();
            holder.cellSab.removeAllViews();
            holder.cellDom.removeAllViews();
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar celdas: " + e.getMessage(), e);
        }
    }

    private void agregarCitaACelda(HourViewHolder holder, Cita cita) {
        try {
            // Validaciones previas
            if (cita == null) {
                Log.e(TAG, "❌ Intentando agregar cita null");
                return;
            }

            int diaSemana = cita.getDiaSemana();
            if (diaSemana < 0 || diaSemana > 6) {
                Log.e(TAG, "❌ Día de semana inválido: " + diaSemana + " para cita: " + cita.getActividad());
                return;
            }

            // Crear vista de la cita
            TextView tvCita = new TextView(context);
            tvCita.setText(cita.getActividad());
            tvCita.setTextSize(11f);
            tvCita.setTextColor(Color.WHITE);
            tvCita.setPadding(8, 8, 8, 8);
            tvCita.setBackgroundColor(getColorForTipo(cita.getTipoActividad()));
            tvCita.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tvCita.setMaxLines(3);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            tvCita.setLayoutParams(params);

            // Click listener
            tvCita.setOnClickListener(v -> {
                try {
                    CitaDetalleDialog dialog = new CitaDetalleDialog(cita);
                    dialog.show(fragmentManager, "detalleCita");
                } catch (Exception e) {
                    Log.e(TAG, "Error al mostrar diálogo de detalle: " + e.getMessage(), e);
                }
            });

            // Agregar a la celda correspondiente
            LinearLayout celdaDestino = obtenerCeldaPorDia(holder, diaSemana);
            if (celdaDestino != null) {
                celdaDestino.addView(tvCita);
                Log.d(TAG, "✅ Cita agregada: " + cita.getActividad() + " en día " + diaSemana);
            } else {
                Log.e(TAG, "❌ Celda destino es null para día: " + diaSemana);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al agregar cita a celda: " + e.getMessage(), e);
            Log.e(TAG, "Cita problemática: " + (cita != null ? cita.toString() : "null"));
        }
    }

    /**
     * Obtiene la celda correspondiente según el día de la semana
     */
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

    /**
     * Paleta de colores mejorada con manejo de errores
     */
    private int getColorForTipo(String tipo) {
        int colorPorDefecto = Color.parseColor("#2E7D32"); // Verde oscuro

        if (tipo == null || tipo.trim().isEmpty()) {
            Log.w(TAG, "Tipo de actividad null o vacío, usando color por defecto");
            return colorPorDefecto;
        }

        try {
            switch (tipo.trim()) {
                case "Capacitación":
                    return Color.parseColor("#1B5E20");
                case "Taller":
                    return Color.parseColor("#2E7D32");
                case "Charlas":
                    return Color.parseColor("#388E3C");
                case "Atenciones":
                    return Color.parseColor("#00796B");
                case "Operativo":
                    return Color.parseColor("#F57C00");
                case "Práctica profesional":
                    return Color.parseColor("#5E35B1");
                case "Diagnóstico":
                    return Color.parseColor("#E64A19");
                default:
                    Log.d(TAG, "Tipo desconocido: '" + tipo + "', usando color por defecto");
                    return colorPorDefecto;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al parsear color: " + e.getMessage(), e);
            return colorPorDefecto;
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
        }
    }
}