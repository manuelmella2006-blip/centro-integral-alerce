package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MantenedoresFragment extends Fragment {

    private LinearLayout cardCrearLugar;
    private LinearLayout cardTiposActividad;
    private LinearLayout cardOferentes;
    private LinearLayout cardSociosComunitarios;
    private LinearLayout cardProyectos;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mantenedores, container, false);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inicializar vistas
        cardCrearLugar = view.findViewById(R.id.card_crear_lugar);
        cardTiposActividad = view.findViewById(R.id.card_tipos_actividad);
        cardOferentes = view.findViewById(R.id.card_oferentes);
        cardSociosComunitarios = view.findViewById(R.id.card_socios_comunitarios);
        cardProyectos = view.findViewById(R.id.card_proyectos);

        // Verificar permisos de administrador
        verificarPermisos();

        // Configurar listeners
        setupListeners();

        return view;
    }

    private void verificarPermisos() {
        // Verificar si el usuario es administrador
        if (auth.getCurrentUser() == null) {
            deshabilitarCards();
            Toast.makeText(requireContext(),
                    "Debes iniciar sesión para acceder a mantenedores",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String rolId = documentSnapshot.getString("rolId");

                        boolean esAdmin = "admin".equalsIgnoreCase(rolId) ||
                                "administrador".equalsIgnoreCase(rolId);

                        if (!esAdmin) {
                            deshabilitarCards();
                            Toast.makeText(requireContext(),
                                    "Solo los administradores pueden acceder a esta sección",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        deshabilitarCards();
                    }
                })
                .addOnFailureListener(e -> {
                    deshabilitarCards();
                    Toast.makeText(requireContext(),
                            "Error al verificar permisos: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deshabilitarCards() {
        cardCrearLugar.setEnabled(false);
        cardTiposActividad.setEnabled(false);
        cardOferentes.setEnabled(false);
        cardSociosComunitarios.setEnabled(false);
        cardProyectos.setEnabled(false);

        cardCrearLugar.setAlpha(0.5f);
        cardTiposActividad.setAlpha(0.5f);
        cardOferentes.setAlpha(0.5f);
        cardSociosComunitarios.setAlpha(0.5f);
        cardProyectos.setAlpha(0.5f);
    }

    private void setupListeners() {
        // Card Crear Lugar
        cardCrearLugar.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "🏢 Crear Lugar - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
            // TODO: Abrir activity/dialog para crear lugar
            // Intent intent = new Intent(requireContext(), CrearLugarActivity.class);
            // startActivity(intent);
        });

        // Card Tipos de Actividad
        cardTiposActividad.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "🏷️ Tipos de Actividad - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
            // TODO: Abrir activity/dialog para gestionar tipos
        });

        // Card Oferentes
        cardOferentes.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "👤 Oferentes - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
            // TODO: Abrir activity/dialog para gestionar oferentes
        });

        // Card Socios Comunitarios
        cardSociosComunitarios.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "🏢 Socios Comunitarios - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
            // TODO: Abrir activity/dialog para gestionar socios
        });

        // Card Proyectos
        cardProyectos.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "📁 Proyectos - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
            // TODO: Abrir activity/dialog para gestionar proyectos
        });
    }
}