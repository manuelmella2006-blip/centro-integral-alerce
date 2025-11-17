package com.example.centrointegralalerce.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Actividad;

import java.util.List;
import android.graphics.Color;
import android.graphics.Typeface;

public class ActividadesListAdapter extends RecyclerView.Adapter<ActividadesListAdapter.ActividadViewHolder> {

    private List<Actividad> actividadesList;

    // Listener de clic
    public interface OnItemClickListener {
        void onItemClick(Actividad actividad);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ActividadesListAdapter(List<Actividad> actividadesList) {
        this.actividadesList = actividadesList;
    }

    public void setActividadesList(List<Actividad> actividadesList) {
        this.actividadesList = actividadesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_card, parent, false);
        return new ActividadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        Actividad actividad = actividadesList.get(position);
        holder.bind(actividad);

        // Clic: captura la actividad ya vinculada
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(actividad);
            }
        });
    }

    @Override
    public int getItemCount() {
        return actividadesList != null ? actividadesList.size() : 0;
    }

    static class ActividadViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombre, tvTipo, tvFechaHora, tvLugar, tvEstado;

        public ActividadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_activity_name);
            tvTipo = itemView.findViewById(R.id.tv_activity_type);
            tvFechaHora = itemView.findViewById(R.id.tv_activity_datetime);
            tvLugar = itemView.findViewById(R.id.tv_activity_location);
            tvEstado = itemView.findViewById(R.id.tv_activity_status);
        }

        public void bind(Actividad actividad) {
            // Nombre
            tvNombre.setText(actividad.getNombre() != null ? actividad.getNombre() : "Sin nombre");

            // ✅ NUEVO: Aplicar estilo diferente para actividades canceladas
            if ("cancelada".equalsIgnoreCase(actividad.getEstado())) {
                // Cambiar colores para indicar estado cancelado
                tvNombre.setTextColor(Color.GRAY);
                itemView.setAlpha(0.6f);
            } else {
                tvNombre.setTextColor(Color.BLACK);
                itemView.setAlpha(1.0f);
            }

            // Tipo: ahora muestra el nombre si está disponible
            String tipoDisplay;
            if (actividad.getTipoActividadNombre() != null && !actividad.getTipoActividadNombre().isEmpty()) {
                tipoDisplay = "Tipo: " + actividad.getTipoActividadNombre();
            } else if (actividad.getTipoActividadId() != null) {
                tipoDisplay = "Tipo: " + actividad.getTipoActividadId();
            } else {
                tipoDisplay = "Sin tipo";
            }
            tvTipo.setText(tipoDisplay);

            // ⭐ Fecha y hora - ACTUALIZADO para usar fechaInicio y horaInicio
            String fechaHora = "";
            if (actividad.getFechaInicio() != null && !actividad.getFechaInicio().isEmpty()) {
                fechaHora = actividad.getFechaInicio();
            }
            if (actividad.getHoraInicio() != null && !actividad.getHoraInicio().isEmpty()) {
                if (!fechaHora.isEmpty()) {
                    fechaHora += " - " + actividad.getHoraInicio();
                } else {
                    fechaHora = actividad.getHoraInicio();
                }
            }
            tvFechaHora.setText(fechaHora.isEmpty() ? "Sin fecha/hora" : fechaHora);

            // ⭐ Lugar - ACTUALIZADO para usar lugarNombre
            String lugarDisplay;
            if (actividad.getLugarNombre() != null && !actividad.getLugarNombre().isEmpty()) {
                lugarDisplay = actividad.getLugarNombre();
            } else if (actividad.getLugarId() != null) {
                lugarDisplay = "Lugar: " + actividad.getLugarId();
            } else {
                lugarDisplay = "Sin lugar";
            }
            tvLugar.setText(lugarDisplay);

            // Estado - destacar si está cancelada
            String estadoDisplay = actividad.getEstado() != null ? actividad.getEstado() : "Sin estado";
            tvEstado.setText(estadoDisplay);

            if ("cancelada".equalsIgnoreCase(actividad.getEstado())) {
                tvEstado.setTextColor(Color.RED);
                tvEstado.setTypeface(tvEstado.getTypeface(), Typeface.BOLD);
            } else {
                tvEstado.setTextColor(Color.BLACK);
                tvEstado.setTypeface(Typeface.DEFAULT);
            }
        }
    }
}