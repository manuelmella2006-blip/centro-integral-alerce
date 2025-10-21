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

// IMPORTA Cita desde el paquete correcto:
import com.example.centrointegralalerce.data.Cita;
// Si la clase está en data, cambia el import anterior.

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador del calendario con sistema dinámico de filas
 * Solo muestra las horas donde hay citas programadas
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

    /**
     * Debug: Imprime información de todas las citas
     */
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

    private String safe(String s) { return s == null ? "" : s; }

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
                if (cita == null) continue;

                // Validación de hora
                String horaCita = cita.getHora();
                if (horaCita == null) continue;

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
            if (cita == null || cita.getFecha() == null) return;

            // Calcular día de la semana desde la fecha (0=lun ... 6=dom)
            int diaSemana = calcularDiaSemanaIndex(cita.getFecha());
            if (diaSemana < 0 || diaSemana > 6) return;

            // === Contenedor de la cita ===
            LinearLayout layoutCita = new LinearLayout(context);
            layoutCita.setOrientation(LinearLayout.VERTICAL);
            layoutCita.setGravity(android.view.Gravity.CENTER);
            layoutCita.setPadding(16, 16, 16, 16);
            layoutCita.setBackgroundResource(R.drawable.bg_cita_gradient);
            layoutCita.setElevation(5f);
            layoutCita.setClipToOutline(false);

            // === Texto principal (usa lugarId o fecha como fallback) ===
            TextView tvTitulo = new TextView(context);
            String titulo = (cita.getLugarId() != null && !cita.getLugarId().isEmpty())
                    ? cita.getLugarId()
                    : new SimpleDateFormat("dd MMM", new Locale("es", "ES"))
                    .format(cita.getFecha());
            tvTitulo.setText(titulo);
            tvTitulo.setTextSize(13f);
            tvTitulo.setTextColor(Color.WHITE);
            tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitulo.setGravity(android.view.Gravity.CENTER);
            tvTitulo.setShadowLayer(4f, 0f, 2f, Color.parseColor("#66000000"));

            layoutCita.addView(tvTitulo);

            // === LayoutParams ===
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            params.setMargins(4, 3, 4, 3);
            layoutCita.setLayoutParams(params);

            // === Color del degradado por estado ===
            int colorBase = getColorForEstado(cita.getEstado());
            layoutCita.getBackground().setTint(colorBase);

            // === Click: abrir detalle ===
            layoutCita.setOnClickListener(v -> {
                try {
                    CitaDetalleDialog dialog = new CitaDetalleDialog(cita);
                    dialog.show(fragmentManager, "detalleCita");
                } catch (Exception e) {
                    Log.e(TAG, "Error al mostrar detalle: " + e.getMessage(), e);
                }
            });

            // === Añadir a la celda del día ===
            LinearLayout celdaDestino = obtenerCeldaPorDia(holder, diaSemana);
            if (celdaDestino != null) {
                celdaDestino.removeAllViews(); // si deseas múltiples, quita esta línea
                celdaDestino.addView(layoutCita);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al agregar cita: " + e.getMessage(), e);
        }
    }

    // Calcula índice 0..6 a partir de java.util.Date, con lunes como primer día
    private int calcularDiaSemanaIndex(java.util.Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int dow = cal.get(Calendar.DAY_OF_WEEK); // 1=dom..7=sáb
        // Convertir a 0=lun..6=dom
        switch (dow) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            case Calendar.SUNDAY: return 6;
            default: return -1;
        }
    }

    /**
     * Paleta de colores basada en estado
     */
    private int getColorForEstado(String estado) {
        int def = Color.parseColor("#2E7D32"); // Verde por defecto
        if (estado == null || estado.trim().isEmpty()) return def;
        String e = estado.trim().toLowerCase(Locale.getDefault());

        switch (e) {
            case "activa":
            case "activo":
            case "confirmada":
                return Color.parseColor("#2E7D32"); // verde
            case "pendiente":
                return Color.parseColor("#00796B"); // teal
            case "inactiva":
            case "cancelada":
            case "cancelado":
                return Color.parseColor("#E64A19"); // rojo/naranja
            case "reprogramada":
                return Color.parseColor("#5E35B1"); // violeta
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
