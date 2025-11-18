package com.example.centrointegralalerce.ui.mantenedores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter para mostrar la lista de lugares en RecyclerView
 */
public class LugaresAdapter extends RecyclerView.Adapter<LugaresAdapter.ViewHolder> {

    private List<LugarItem> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEditarClick(LugarItem item);
        void onEliminarClick(String id);
    }

    public LugaresAdapter(List<LugarItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lugar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LugarItem item = items.get(position);

        holder.tvNombre.setText(item.getNombre());
        holder.tvDescripcion.setText("Ubicación para actividades");

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

    public void actualizarDatos(List<LugarItem> nuevosItems) {
        this.items = nuevosItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDescripcion;
        MaterialButton btnEditar;
        MaterialButton btnEliminar;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreLugar);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}