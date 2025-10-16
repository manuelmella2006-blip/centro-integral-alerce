package com.example.centrointegralalerce.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import java.util.List;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {

    private final List<Cita> citas;

    public CitasAdapter(List<Cita> citas) {
        this.citas = citas;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citas.get(position);
        holder.tvActividad.setText(cita.getActividad());
        holder.tvLugar.setText(cita.getLugar());
        holder.tvFechaHora.setText(cita.getFechaHora());

        int color = getColorForTipo(cita.getTipoActividad(), holder.itemView);
        holder.tvActividad.setTextColor(color);
    }

    private int getColorForTipo(String tipoActividad, View view) {
        switch (tipoActividad) {
            case "Capacitación":
                return ContextCompat.getColor(view.getContext(), R.color.verdeSantoTomas);
            case "Taller":
                return ContextCompat.getColor(view.getContext(), R.color.verdeSecundario);
            case "Charlas":
                return ContextCompat.getColor(view.getContext(), R.color.verdeClaro);
            case "Atenciones":
                return ContextCompat.getColor(view.getContext(), R.color.verdeExito);
            case "Operativo":
                return ContextCompat.getColor(view.getContext(), R.color.amarrilloAdvertencia);
            case "Práctica profesional":
                return ContextCompat.getColor(view.getContext(), R.color.violetaAcademico);
            case "Diagnóstico":
                return ContextCompat.getColor(view.getContext(), R.color.naranjaDiagnostico);
            default:
                return ContextCompat.getColor(view.getContext(), android.R.color.black);
        }
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView tvActividad, tvLugar, tvFechaHora;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActividad = itemView.findViewById(R.id.tvActividad);
            tvLugar = itemView.findViewById(R.id.tvLugar);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
        }
    }
}