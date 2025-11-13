package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.ui.mantenedores.TiposActividadActivity;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.ui.CrearLugarActivity;
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

        // ✅ Verificar permisos con UserSession
        verificarPermisos();

        // Configurar listeners
        setupListeners();

        return view;
    }

    // ============================================================
    // ✅ NUEVA VERSIÓN USANDO USERSESSION
    // ============================================================
    private void verificarPermisos() {
        UserSession session = UserSession.getInstance();

        if (!session.permisosCargados()) {
            Log.w("MANTENEDORES", "⚠️ Permisos no cargados, reintentando...");
            new Handler().postDelayed(this::verificarPermisos, 1000);
            return;
        }

        boolean puedeGestionar = session.puede("gestionar_mantenedores");

        if (!puedeGestionar) {
            deshabilitarCards();
            Toast.makeText(requireContext(),
                    "❌ Solo los administradores pueden acceder a mantenedores",
                    Toast.LENGTH_LONG).show();
            Log.d("MANTENEDORES", "🚫 Usuario sin permiso para gestionar mantenedores. Rol: " + session.getRolId());
        } else {
            Log.d("MANTENEDORES", "✅ Usuario con permiso para gestionar mantenedores. Rol: " + session.getRolId());
        }
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
            Intent intent = new Intent(requireContext(), CrearLugarActivity.class);
            startActivity(intent);
        });

        // Card Tipos de Actividad
        cardTiposActividad.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TiposActividadActivity.class);
            startActivity(intent);
        });



        // Card Oferentes
        cardOferentes.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "👤 Oferentes - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
        });

        // Card Socios Comunitarios
        cardSociosComunitarios.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "🏢 Socios Comunitarios - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
        });

        // Card Proyectos
        cardProyectos.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "📁 Proyectos - Próximamente disponible",
                    Toast.LENGTH_SHORT).show();
        });
    }
}
