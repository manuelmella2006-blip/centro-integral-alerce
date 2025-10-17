package com.example.centrointegralalerce.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import java.util.List;
import android.text.TextUtils;

public class WeekCalendarAdapter extends RecyclerView.Adapter<WeekCalendarAdapter.HourViewHolder> {

    private final List<Cita> citas;
    private final List<String> horas;

    public WeekCalendarAdapter(List<Cita> citas, List<String> horas) {
        this.citas = citas;
        this.horas = horas;
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_row, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        String horaActual = horas.get(position);
        holder.tvHour.setText(horaActual);

        // Limpiar todas las celdas primero
        holder.cellLun.removeAllViews();
        holder.cellMar.removeAllViews();
        holder.cellMie.removeAllViews();
        holder.cellJue.removeAllViews();
        holder.cellVie.removeAllViews();
        holder.cellSab.removeAllViews();
        holder.cellDom.removeAllViews();

        // Buscar citas para esta hora y agregarlas en el día correcto
        for (Cita cita : citas) {
            if (cita.getHora().equals(horaActual)) {
                switch (cita.getDiaSemana()) {
                    case 0: // Lunes
                        agregarEventoACelda(holder.cellLun, cita);
                        break;
                    case 1: // Martes
                        agregarEventoACelda(holder.cellMar, cita);
                        break;
                    case 2: // Miércoles
                        agregarEventoACelda(holder.cellMie, cita);
                        break;
                    case 3: // Jueves
                        agregarEventoACelda(holder.cellJue, cita);
                        break;
                    case 4: // Viernes
                        agregarEventoACelda(holder.cellVie, cita);
                        break;
                    case 5: // Sábado
                        agregarEventoACelda(holder.cellSab, cita);
                        break;
                    case 6: // Domingo
                        agregarEventoACelda(holder.cellDom, cita);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return horas.size();
    }

    private void agregarEventoACelda(LinearLayout cell, Cita cita) {
        TextView tvEvent = new TextView(cell.getContext());

        // Formato mejorado para mostrar en la celda
        String textoEvento = cita.getActividad();
        tvEvent.setText(textoEvento);

        tvEvent.setBackgroundColor(getColorForTipo(cita.getTipoActividad(), cell));
        tvEvent.setTextColor(ContextCompat.getColor(cell.getContext(), R.color.blanco));
        tvEvent.setTextSize(10f);
        tvEvent.setPadding(8, 4, 8, 4);
        tvEvent.setMaxLines(2);
        tvEvent.setEllipsize(TextUtils.TruncateAt.END);
        tvEvent.setGravity(View.TEXT_ALIGNMENT_CENTER);

        // Tooltip con información completa
        String infoCompleta = cita.getHora() + " - " + cita.getActividad() +
                "\nLugar: " + cita.getLugar() +
                "\nTipo: " + cita.getTipoActividad();
        tvEvent.setTooltipText(infoCompleta);

        // Hacer que ocupe toda la celda
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        tvEvent.setLayoutParams(params);

        cell.addView(tvEvent);
    }

    private int getColorForTipo(String tipoActividad, View view) {
        switch (tipoActividad) {
            case "Capacitación": return ContextCompat.getColor(view.getContext(), R.color.verdeSantoTomas);
            case "Taller": return ContextCompat.getColor(view.getContext(), R.color.verdeSecundario);
            case "Charlas": return ContextCompat.getColor(view.getContext(), R.color.verdeClaro);
            case "Atenciones": return ContextCompat.getColor(view.getContext(), R.color.verdeExito);
            case "Operativo": return ContextCompat.getColor(view.getContext(), R.color.amarrilloAdvertencia);
            case "Práctica profesional": return ContextCompat.getColor(view.getContext(), R.color.violetaAcademico);
            case "Diagnóstico": return ContextCompat.getColor(view.getContext(), R.color.naranjaDiagnostico);
            default: return ContextCompat.getColor(view.getContext(), R.color.verdeSantoTomas);
        }
    }

    static class HourViewHolder extends RecyclerView.ViewHolder {
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