package com.example.centrointegralalerce.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Adapter para mostrar citas en RecyclerView con validación de estado temporal
 */
public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {

    private List<Cita> citas;
    private OnCitaClickListener listener;

    /**
     * Interface para manejar clicks en las citas
     */
    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public CitaAdapter(List<Cita> citas, OnCitaClickListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita_calendar, parent, false);
        return new CitaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citas.get(position);
        holder.bind(cita, listener);
    }

    @Override
    public int getItemCount() {
        return citas != null ? citas.size() : 0;
    }

    /**
     * Actualiza la lista de citas y refresca el RecyclerView
     */
    public void updateCitas(List<Cita> nuevasCitas) {
        this.citas = nuevasCitas;
        notifyDataSetChanged();
    }

    // ===========================================
    // ViewHolder
    // ===========================================

    public static class CitaViewHolder extends RecyclerView.ViewHolder {

        // Vistas
        private TextView tvActividadNombre;
        private TextView tvHora;
        private TextView tvLugar;
        private TextView tvEstadoTemporal;
        private Chip chipEstado;
        private ImageView ivUrgencia;
        private View viewEstadoIndicator;

        public CitaViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeViews();
        }

        private void initializeViews() {
            tvActividadNombre = itemView.findViewById(R.id.tv_actividad_nombre);
            tvHora = itemView.findViewById(R.id.tv_hora);
            tvLugar = itemView.findViewById(R.id.tv_lugar);
            tvEstadoTemporal = itemView.findViewById(R.id.tv_estado_temporal);
            chipEstado = itemView.findViewById(R.id.chip_estado);
            ivUrgencia = itemView.findViewById(R.id.iv_urgencia);
            viewEstadoIndicator = itemView.findViewById(R.id.view_estado_indicator);
        }

        /**
         * Vincula los datos de la cita con las vistas
         */
        public void bind(Cita cita, OnCitaClickListener listener) {
            if (cita == null) return;

            // 1️⃣ Datos básicos
            tvActividadNombre.setText(cita.getActividadNombre() != null ?
                    cita.getActividadNombre() : "Sin nombre");

            tvHora.setText(cita.getHora() != null ?
                    "🕐 " + cita.getHora() : "🕐 Sin hora");

            tvLugar.setText(cita.getLugarId() != null ?
                    "📍 " + cita.getLugarId() : "📍 Sin lugar");

            // 2️⃣ Obtener estado temporal usando CitaDateValidator
            CitaDateValidator.EstadoTemporal estado =
                    CitaDateValidator.getEstadoTemporal(cita);

            // 3️⃣ Actualizar chip con badge text (usando CitaValidationDialog)
            String badgeText = CitaValidationDialog.getBadgeText(cita);
            chipEstado.setText(badgeText);

            // 4️⃣ Actualizar descripción temporal (usando CitaDateValidator)
            String descripcion = CitaDateValidator.getMensajeDescriptivo(cita);
            tvEstadoTemporal.setText(descripcion);

            // 5️⃣ Configurar colores y estilos según el estado
            configurarEstiloSegunEstado(estado);

            // 6️⃣ Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCitaClick(cita);
                }
            });
        }

        /**
         * Configura los estilos visuales según el estado temporal de la cita
         */
        private void configurarEstiloSegunEstado(CitaDateValidator.EstadoTemporal estado) {
            Context context = itemView.getContext();

            switch (estado) {
                case ATRASADA:
                    aplicarEstiloAtrasada(context);
                    break;

                case HOY:
                    aplicarEstiloHoy(context);
                    break;

                case PROXIMA_24H:
                    aplicarEstiloProxima24h(context);
                    break;

                case PROXIMA_SEMANA:
                    aplicarEstiloProximaSemana(context);
                    break;

                case FUTURA:
                    aplicarEstiloFutura(context);
                    break;
            }
        }

        /**
         * Estilo para citas ATRASADAS (⚠️ ROJO)
         */
        private void aplicarEstiloAtrasada(Context context) {
            chipEstado.setChipBackgroundColorResource(android.R.color.holo_red_light);
            chipEstado.setTextColor(context.getColor(android.R.color.white));

            viewEstadoIndicator.setBackgroundColor(
                    context.getColor(android.R.color.holo_red_dark));

            tvEstadoTemporal.setTextColor(
                    context.getColor(android.R.color.holo_red_dark));

            // Mostrar icono de urgencia
            ivUrgencia.setVisibility(View.VISIBLE);
            ivUrgencia.setColorFilter(
                    context.getColor(android.R.color.holo_red_dark));

            // Aplicar transparencia a los textos
            aplicarTransparencia(0.6f);
        }

        /**
         * Estilo para citas de HOY (🟠 NARANJA)
         */
        private void aplicarEstiloHoy(Context context) {
            chipEstado.setChipBackgroundColorResource(android.R.color.holo_orange_light);
            chipEstado.setTextColor(context.getColor(android.R.color.white));

            viewEstadoIndicator.setBackgroundColor(
                    context.getColor(android.R.color.holo_orange_dark));

            tvEstadoTemporal.setTextColor(
                    context.getColor(android.R.color.holo_orange_dark));

            // Mostrar icono de urgencia
            ivUrgencia.setVisibility(View.VISIBLE);
            ivUrgencia.setColorFilter(
                    context.getColor(android.R.color.holo_orange_dark));

            // Sin transparencia
            aplicarTransparencia(1.0f);
        }

        /**
         * Estilo para citas PRÓXIMAS 24H (🟠 NARANJA OSCURO)
         */
        private void aplicarEstiloProxima24h(Context context) {
            chipEstado.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
            chipEstado.setTextColor(context.getColor(android.R.color.white));

            viewEstadoIndicator.setBackgroundColor(
                    context.getColor(android.R.color.holo_orange_light));

            tvEstadoTemporal.setTextColor(
                    context.getColor(android.R.color.holo_orange_dark));

            // Ocultar icono de urgencia
            ivUrgencia.setVisibility(View.GONE);

            // Sin transparencia
            aplicarTransparencia(1.0f);
        }

        /**
         * Estilo para citas PRÓXIMA SEMANA (🔵 AZUL)
         */
        private void aplicarEstiloProximaSemana(Context context) {
            chipEstado.setChipBackgroundColorResource(android.R.color.holo_blue_light);
            chipEstado.setTextColor(context.getColor(android.R.color.white));

            viewEstadoIndicator.setBackgroundColor(
                    context.getColor(android.R.color.holo_blue_dark));

            tvEstadoTemporal.setTextColor(
                    context.getColor(android.R.color.holo_blue_dark));

            // Ocultar icono de urgencia
            ivUrgencia.setVisibility(View.GONE);

            // Sin transparencia
            aplicarTransparencia(1.0f);
        }

        /**
         * Estilo para citas FUTURAS (🟢 VERDE)
         */
        private void aplicarEstiloFutura(Context context) {
            chipEstado.setChipBackgroundColorResource(android.R.color.holo_green_light);
            chipEstado.setTextColor(context.getColor(android.R.color.white));

            viewEstadoIndicator.setBackgroundColor(
                    context.getColor(android.R.color.holo_green_dark));

            tvEstadoTemporal.setTextColor(
                    context.getColor(android.R.color.holo_green_dark));

            // Ocultar icono de urgencia
            ivUrgencia.setVisibility(View.GONE);

            // Sin transparencia
            aplicarTransparencia(1.0f);
        }

        /**
         * Aplica transparencia a los textos principales
         */
        private void aplicarTransparencia(float alpha) {
            tvActividadNombre.setAlpha(alpha);
            tvHora.setAlpha(alpha);
            tvLugar.setAlpha(alpha);
        }
    }
}