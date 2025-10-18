package com.example.centrointegralalerce.ui;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import java.util.List;
import android.text.TextUtils;

public class WeekCalendarAdapter extends RecyclerView.Adapter<WeekCalendarAdapter.HourViewHolder> {

    private final List<Cita> citas;
    private final List<String> horas;
    private final FragmentManager fragmentManager; // <-- FragmentManager agregado

    public WeekCalendarAdapter(List<Cita> citas, List<String> horas, FragmentManager fragmentManager) {
        this.citas = citas;
        this.horas = horas;
        this.fragmentManager = fragmentManager; // <-- inicializado
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
                    case 0: agregarEventoACelda(holder.cellLun, cita); break;
                    case 1: agregarEventoACelda(holder.cellMar, cita); break;
                    case 2: agregarEventoACelda(holder.cellMie, cita); break;
                    case 3: agregarEventoACelda(holder.cellJue, cita); break;
                    case 4: agregarEventoACelda(holder.cellVie, cita); break;
                    case 5: agregarEventoACelda(holder.cellSab, cita); break;
                    case 6: agregarEventoACelda(holder.cellDom, cita); break;
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

        tvEvent.setText(cita.getActividad());
        tvEvent.setTextSize(10f);
        tvEvent.setTextColor(ContextCompat.getColor(cell.getContext(), R.color.blanco));
        tvEvent.setPadding(8, 6, 8, 6);
        tvEvent.setMaxLines(2);
        tvEvent.setEllipsize(TextUtils.TruncateAt.END);
        tvEvent.setGravity(Gravity.CENTER);

        tvEvent.setBackgroundColor(getColorForTipo(cita.getTipoActividad(), cell));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(2,2,2,2); // pequeño margen entre citas
        tvEvent.setLayoutParams(params);

        // Evento clic usando el FragmentManager pasado al Adapter
        tvEvent.setOnClickListener(v -> {
            CitaDetalleDialog dialog = new CitaDetalleDialog(cita);
            dialog.show(fragmentManager, "detalleCita");
        });

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
