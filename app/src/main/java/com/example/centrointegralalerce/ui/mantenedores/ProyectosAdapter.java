package com.example.centrointegralalerce.ui.mantenedores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;

import java.util.List;

public class ProyectosAdapter extends RecyclerView.Adapter<ProyectosAdapter.ViewHolder> {

    private List<ProyectoItem> items;
    private OnItemActionListener listener;

    // Interface para manejar las acciones
    public interface OnItemActionListener {
        void onEditarClick(ProyectoItem item);
        void onEliminarClick(String id);
    }

    public ProyectosAdapter(List<ProyectoItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proyecto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProyectoItem item = items.get(position);

        holder.tvNombre.setText(item.getNombre());

        // Mostrar descripción o mensaje por defecto
        if (item.getDescripcion() != null && !item.getDescripcion().isEmpty()) {
            holder.tvDescripcion.setText(item.getDescripcion());
        } else {
            holder.tvDescripcion.setText("Sin descripción");
        }

        // Click en editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarClick(item);
            }
        });

        // Click en eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(item.getId());
            }
        });

        // Click en toda la card también permite editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditarClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void actualizarDatos(List<ProyectoItem> nuevosItems) {
        this.items = nuevosItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDescripcion;
        ImageButton btnEditar;
        ImageButton btnEliminar;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreProyecto);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}