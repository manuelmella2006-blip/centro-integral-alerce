package com.example.centrointegralalerce.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import java.util.List;

public class CalendarioAdapter extends RecyclerView.Adapter<CalendarioAdapter.HourViewHolder> {

    private final Context context;
    private final List<String> horas;
    private final List<Cita> citas;
    private final FragmentManager fragmentManager;

    public CalendarioAdapter(Context context, List<String> horas, List<Cita> citas, FragmentManager fragmentManager) {
        this.context = context;
        this.horas = horas;
        this.citas = citas;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public HourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_row, parent, false);
        return new HourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourViewHolder holder, int position) {
        String horaActual = horas.get(position);
        holder.tvHour.setText(horaActual);

        clearCells(holder);

        for (Cita cita : citas) {
            if (cita.getHora().equals(horaActual)) {
                agregarCitaACelda(holder, cita);
            }
        }
    }

    @Override
    public int getItemCount() {
        return horas.size();
    }

    private void clearCells(HourViewHolder holder) {
        holder.cellLun.removeAllViews();
        holder.cellMar.removeAllViews();
        holder.cellMie.removeAllViews();
        holder.cellJue.removeAllViews();
        holder.cellVie.removeAllViews();
        holder.cellSab.removeAllViews();
        holder.cellDom.removeAllViews();
    }

    private void agregarCitaACelda(HourViewHolder holder, Cita cita) {
        TextView tvCita = new TextView(context);
        tvCita.setText(cita.getActividad());
        tvCita.setTextSize(10f);
        tvCita.setTextColor(ContextCompat.getColor(context, R.color.blanco));
        tvCita.setPadding(8, 6, 8, 6);
        tvCita.setBackgroundColor(getColorForTipo(cita.getTipoActividad()));
        tvCita.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvCita.setMaxLines(2);
        tvCita.setTooltipText(cita.getActividad() + "\nLugar: " + cita.getLugar() + "\nHora: " + cita.getHora() + "\nTipo: " + cita.getTipoActividad());

        tvCita.setOnClickListener(v -> {
            CitaDetalleDialog dialog = new CitaDetalleDialog(cita);
            dialog.show(fragmentManager, "detalleCita");
        });

        switch (cita.getDiaSemana()) {
            case 0: holder.cellLun.addView(tvCita); break;
            case 1: holder.cellMar.addView(tvCita); break;
            case 2: holder.cellMie.addView(tvCita); break;
            case 3: holder.cellJue.addView(tvCita); break;
            case 4: holder.cellVie.addView(tvCita); break;
            case 5: holder.cellSab.addView(tvCita); break;
            case 6: holder.cellDom.addView(tvCita); break;
        }
    }

    private int getColorForTipo(String tipo) {
        if (tipo == null) return ContextCompat.getColor(context, R.color.verdeSantoTomas);
        switch (tipo) {
            case "Capacitación": return ContextCompat.getColor(context, R.color.verdeSantoTomas);
            case "Taller": return ContextCompat.getColor(context, R.color.verdeSecundario);
            case "Charlas": return ContextCompat.getColor(context, R.color.verdeClaro);
            case "Atenciones": return ContextCompat.getColor(context, R.color.verdeExito);
            case "Operativo": return ContextCompat.getColor(context, R.color.amarrilloAdvertencia);
            case "Práctica profesional": return ContextCompat.getColor(context, R.color.violetaAcademico);
            case "Diagnóstico": return ContextCompat.getColor(context, R.color.naranjaDiagnostico);
            default: return ContextCompat.getColor(context, R.color.verdeSantoTomas);
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
