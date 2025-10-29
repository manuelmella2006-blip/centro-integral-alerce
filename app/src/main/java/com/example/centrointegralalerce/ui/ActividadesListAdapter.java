// ActividadesListAdapter.java
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

            // Tipo: muestra por ahora el ID del tipo
            String tipoDisplay = actividad.getTipoActividadId() != null
                    ? "Tipo: " + actividad.getTipoActividadId()
                    : "Sin tipo";
            tvTipo.setText(tipoDisplay);

            // Fecha y hora simples (según tu POJO actual)
            String fechaHora = "";
            if (actividad.getFecha() != null) {
                fechaHora = "Fecha: " + actividad.getFecha();
            }
            if (actividad.getHora() != null) {
                fechaHora += (fechaHora.isEmpty() ? "" : " ") + actividad.getHora();
            }
            tvFechaHora.setText(fechaHora.isEmpty() ? "Sin fecha/hora" : fechaHora);

            // Lugar (tu POJO expone lugar como String)
            String lugarDisplay = actividad.getLugar() != null
                    ? "Lugar: " + actividad.getLugar()
                    : "Sin lugar";
            tvLugar.setText(lugarDisplay);

            // Estado
            tvEstado.setText(actividad.getEstado() != null
                    ? "Estado: " + actividad.getEstado()
                    : "Sin estado");
        }
    }
}
