package com.example.centrointegralalerce.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

    private String safe(String s) {
        return s == null ? "" : s;
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

            // DEBUG: Marcar cada celda con su número de columna (solo primera fila)

            int citasEncontradas = 0;
            for (Cita cita : citas) {
                if (cita == null) continue;

                String horaCita = cita.getHora();
                if (horaCita == null) continue;

                if (horaCita.equals(horaActual)) {
                    citasEncontradas++;
                    agregarCitaACelda(holder, cita);

                    // 🔥 NUEVO: RESALTAR VISUALMENTE EL ENCABEZADO CORRECTO
                    resaltarEncabezadoDia(cita.getFecha());
                }
            }

            if (citasEncontradas > 0) {
                Log.d(TAG, "Hora " + horaActual + ": " + citasEncontradas + " cita(s) agregada(s)");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en onBindViewHolder posición " + position + ": " + e.getMessage(), e);
        }
    }

    // 🔥 NUEVO MÉTODO: Resaltar el encabezado del día donde está la cita
    private void resaltarEncabezadoDia(Date fechaCita) {
        try {
            if (fechaCita == null) return;

            int diaSemanaIndex = calcularDiaSemanaIndex(fechaCita);
            Log.d(TAG, "🎯 RESALTANDO encabezado para día: " + diaSemanaIndex);

            // Necesitamos acceso a los encabezados - esto requiere modificar el constructor
            // Por ahora, solo log para debug
            Log.d(TAG, "⚠️ Cita en día " + diaSemanaIndex + " pero indicador visual puede estar en otro día");

        } catch (Exception e) {
            Log.e(TAG, "Error resaltando encabezado", e);
        }
    }

    // Método para marcar visualmente cada columna con su número


    @Override
    public int getItemCount() {
        return horas.size();
    }

    private void clearCells(HourViewHolder holder) {
        try {
            // Limpiar SOLO las vistas de cita, no los debug markers
            clearCitasDeCeldas(holder.cellLun);
            clearCitasDeCeldas(holder.cellMar);
            clearCitasDeCeldas(holder.cellMie);
            clearCitasDeCeldas(holder.cellJue);
            clearCitasDeCeldas(holder.cellVie);
            clearCitasDeCeldas(holder.cellSab);
            clearCitasDeCeldas(holder.cellDom);
        } catch (Exception e) {
            Log.e(TAG, "Error al limpiar celdas: " + e.getMessage(), e);
        }
    }
    private void clearCitasDeCeldas(LinearLayout celda) {
        if (celda == null) return;

        // Eliminar solo las vistas que no son debug markers (las que no tienen texto rojo)
        for (int i = celda.getChildCount() - 1; i >= 0; i--) {
            View child = celda.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                // Mantener los debug markers (texto rojo pequeño)
                if (textView.getCurrentTextColor() != Color.RED || textView.getTextSize() > 10f) {
                    celda.removeViewAt(i);
                }
            } else {
                // Eliminar cualquier otra vista (LinearLayout de citas, etc.)
                celda.removeViewAt(i);
            }
        }
    }

    /**
     * AGREGA LA CITA EN LA CELDA CORRECTA + DEBUG VISUAL DE COLUMNAS
     */
    private void agregarCitaACelda(HourViewHolder holder, Cita cita) {
        try {
            if (cita == null || cita.getFecha() == null) {
                Log.e(TAG, "❌ Cita o fecha es null");
                return;
            }

            Calendar cal = Calendar.getInstance();
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

            // Texto principal
            TextView tvTitulo = new TextView(context);
            String titulo = cita.getActividadNombre() != null ? cita.getActividadNombre() : "Actividad";
            if (titulo.length() > 20) titulo = titulo.substring(0, 17) + "...";

            tvTitulo.setText(titulo);
            tvTitulo.setTextSize(10f);
            tvTitulo.setTextColor(Color.WHITE);
            tvTitulo.setTypeface(null, Typeface.BOLD);
            tvTitulo.setGravity(Gravity.CENTER);

            // Texto de fecha para debug
            TextView tvFecha = new TextView(context);
            tvFecha.setText(cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1));
            tvFecha.setTextSize(8f);
            tvFecha.setTextColor(Color.YELLOW);
            tvFecha.setGravity(Gravity.CENTER);
            tvFecha.setTypeface(null, Typeface.BOLD);

            layoutCita.addView(tvTitulo);
            layoutCita.addView(tvFecha);

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
                // NO limpiar aquí - ya se limpió en clearCells()
                celdaDestino.addView(layoutCita);
                Log.d(TAG, "✅ Cita agregada SOLO en columna: " + diaSemanaIndex);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al agregar cita: " + e.getMessage(), e);
        }
    }

    // Método para debug del orden de celdas
    private void debugOrdenCeldas(HourViewHolder holder) {
        try {
            LinearLayout[] columnas = {
                    holder.cellLun, holder.cellMar, holder.cellMie,
                    holder.cellJue, holder.cellVie, holder.cellSab, holder.cellDom
            };

            String[] nombres = {"LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"};

            Log.d(TAG, "=== 🧭 ORDEN DE CELDAS ===");
            for (int i = 0; i < columnas.length; i++) {
                boolean tieneCita = columnas[i].getChildCount() > 0;
                Log.d(TAG, "Celda " + i + " (" + nombres[i] + "): " +
                        (tieneCita ? "CON CITA" : "vacía"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en debugOrdenCeldas", e);
        }
    }

    /**
     * DEBUG VISUAL: Marcar cada columna con su número para identificar el orden REAL
     */
    private void debugColumnas(HourViewHolder holder) {
        try {
            LinearLayout[] columnas = {
                    holder.cellLun, holder.cellMar, holder.cellMie,
                    holder.cellJue, holder.cellVie, holder.cellSab, holder.cellDom
            };

            String[] nombres = {"LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"};

            for (int i = 0; i < columnas.length; i++) {
                if (columnas[i] != null) {
                    TextView debugView = new TextView(context);
                    debugView.setText("COL " + i + "\n" + nombres[i]);
                    debugView.setTextSize(6f); // Más pequeño
                    debugView.setTextColor(Color.RED);
                    debugView.setTypeface(null, Typeface.BOLD);
                    debugView.setGravity(Gravity.CENTER);
                    debugView.setBackgroundColor(Color.parseColor("#10FF0000")); // Fondo semitransparente

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.gravity = Gravity.TOP;
                    debugView.setLayoutParams(params);

                    columnas[i].addView(debugView);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en debugColumnas", e);
        }
    }

    private int calcularDiaSemanaIndex(java.util.Date fecha) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);

            // NO establecer firstDayOfWeek, usar la configuración por defecto
            int dow = cal.get(Calendar.DAY_OF_WEEK);

            // Calendar.DAY_OF_WEEK:
            // 1=DOMINGO, 2=LUNES, 3=MARTES, 4=MIÉRCOLES, 5=JUEVES, 6=VIERNES, 7=SÁBADO
            // Nuestro índice: 0=LUNES, 1=MARTES, 2=MIÉRCOLES, 3=JUEVES, 4=VIERNES, 5=SÁBADO, 6=DOMINGO

            // Mapeo directo:
            // Calendar LUNES(2) -> nuestro 0, MARTES(3)->1, MIÉRCOLES(4)->2, JUEVES(5)->3,
            // VIERNES(6)->4, SÁBADO(7)->5, DOMINGO(1)->6

            switch (dow) {
                case Calendar.MONDAY:     // 2
                    return 0;
                case Calendar.TUESDAY:    // 3
                    return 1;
                case Calendar.WEDNESDAY:  // 4
                    return 2;
                case Calendar.THURSDAY:   // 5
                    return 3;
                case Calendar.FRIDAY:     // 6
                    return 4;
                case Calendar.SATURDAY:   // 7
                    return 5;
                case Calendar.SUNDAY:     // 1
                    return 6;
                default:
                    return -1;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error calculando día de semana para fecha: " + fecha, e);
            return -1;
        }
    }

    // Método auxiliar para debug
    private String getDiaNombre(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "DOMINGO";
            case Calendar.MONDAY: return "LUNES";
            case Calendar.TUESDAY: return "MARTES";
            case Calendar.WEDNESDAY: return "MIÉRCOLES";
            case Calendar.THURSDAY: return "JUEVES";
            case Calendar.FRIDAY: return "VIERNES";
            case Calendar.SATURDAY: return "SÁBADO";
            default: return "DESCONOCIDO";
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
