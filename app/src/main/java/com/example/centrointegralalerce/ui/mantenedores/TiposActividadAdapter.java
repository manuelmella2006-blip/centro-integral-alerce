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

public class TiposActividadAdapter extends RecyclerView.Adapter<TiposActividadAdapter.ViewHolder> {

    private List<TipoActividadItem> items;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEditarClick(TipoActividadItem item);
        void onEliminarClick(String id);
    }

    public TiposActividadAdapter(List<TipoActividadItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tipo_actividad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TipoActividadItem item = items.get(position);

        holder.tvNombre.setText(item.getNombre());
        holder.tvDescripcion.setText("Categoría de actividad");

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

    public void actualizarDatos(List<TipoActividadItem> nuevosItems) {
        this.items = nuevosItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDescripcion;
        MaterialButton btnEditar;  // ✅ CORREGIDO: MaterialButton en lugar de ImageButton
        MaterialButton btnEliminar; // ✅ CORREGIDO: MaterialButton en lugar de ImageButton

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreTipo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}