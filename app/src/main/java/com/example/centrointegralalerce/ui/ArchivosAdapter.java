package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Map;

public class ArchivosAdapter extends RecyclerView.Adapter<ArchivosAdapter.ArchivoViewHolder> {

    private List<Map<String, Object>> archivosList;

    public ArchivosAdapter(List<Map<String, Object>> archivosList) {
        this.archivosList = archivosList;
    }

    public void setArchivosList(List<Map<String, Object>> archivosList) {
        this.archivosList = archivosList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArchivoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_archivo, parent, false);
        return new ArchivoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchivoViewHolder holder, int position) {
        Map<String, Object> archivo = archivosList.get(position);
        holder.bind(archivo);
    }

    @Override
    public int getItemCount() {
        return archivosList != null ? archivosList.size() : 0;
    }

    static class ArchivoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNombreArchivo;
        private final TextView tvDescripcion;
        private final MaterialButton btnDescargar;

        public ArchivoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreArchivo = itemView.findViewById(R.id.tv_nombre_archivo);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            btnDescargar = itemView.findViewById(R.id.btn_descargar);
        }

        public void bind(Map<String, Object> archivo) {
            // Nombre del archivo
            String nombre = (String) archivo.get("fileName");
            tvNombreArchivo.setText(nombre != null ? nombre : "Archivo sin nombre");

            // Descripción
            String descripcion = (String) archivo.get("descripcion");
            if (descripcion != null && !descripcion.isEmpty()) {
                tvDescripcion.setText(descripcion);
                tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }

            // Botón para abrir el archivo
            String fileUrl = (String) archivo.get("fileUrl");
            btnDescargar.setOnClickListener(v -> {
                if (fileUrl != null && !fileUrl.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(fileUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Verificar si hay una app que pueda manejar este tipo de archivo
                    if (intent.resolveActivity(itemView.getContext().getPackageManager()) != null) {
                        itemView.getContext().startActivity(intent);
                    } else {
                        // Si no hay app, abrir en el navegador
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl));
                        itemView.getContext().startActivity(browserIntent);
                    }
                }
            });
        }
    }
}