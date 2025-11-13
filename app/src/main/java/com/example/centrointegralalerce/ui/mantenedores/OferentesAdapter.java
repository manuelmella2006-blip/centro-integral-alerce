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

public class OferentesAdapter extends RecyclerView.Adapter<OferentesAdapter.ViewHolder> {

    private List<OferenteItem> items;
    private OnItemActionListener listener;

    // Interface para manejar las acciones
    public interface OnItemActionListener {
        void onEditarClick(OferenteItem item);
        void onEliminarClick(String id);
    }

    public OferentesAdapter(List<OferenteItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_oferente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OferenteItem item = items.get(position);

        holder.tvNombre.setText(item.getNombre());
        holder.tvDescripcion.setText("Toca para editar");

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

    public void actualizarDatos(List<OferenteItem> nuevosItems) {
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
            tvNombre = itemView.findViewById(R.id.tvNombreOferente);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}
