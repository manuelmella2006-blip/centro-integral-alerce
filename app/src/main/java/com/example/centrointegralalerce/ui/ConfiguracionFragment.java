package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfiguracionFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private Chip chipUserRole;
    private LinearLayout itemMantenedores, itemGestionarUsuarios;
    private SwitchMaterial switchNotifications;
    private LinearLayout itemDiasAviso, itemChangePassword, itemLogout, itemAbout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        chipUserRole = view.findViewById(R.id.chip_user_role);
        itemMantenedores = view.findViewById(R.id.item_mantenedores);
        itemGestionarUsuarios = view.findViewById(R.id.item_gestionar_usuarios);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        itemDiasAviso = view.findViewById(R.id.item_dias_aviso);
        itemChangePassword = view.findViewById(R.id.item_change_password);
        itemLogout = view.findViewById(R.id.item_logout);
        itemAbout = view.findViewById(R.id.item_about);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cargarInfoUsuario();
        setupListeners();

        return view;
    }

    private void cargarInfoUsuario() {
        if (auth.getCurrentUser() == null) {
            tvUserName.setText("Usuario no autenticado");
            tvUserEmail.setText("Sin correo");
            chipUserRole.setText("Invitado");
            itemGestionarUsuarios.setVisibility(View.GONE);
            itemMantenedores.setVisibility(View.GONE);
            AlertManager.showWarningToast(requireContext(), "Debes iniciar sesión");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();
        tvUserEmail.setText(email != null ? email : "Sin correo");

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String rolId = documentSnapshot.getString("rolId");

                        tvUserName.setText(nombre != null ? nombre : "Usuario");

                        // ⭐ SIMPLIFICADO: Mostrar directamente el rolId
                        if (rolId != null && !rolId.isEmpty()) {
                            chipUserRole.setText(rolId);
                            chipUserRole.setChipBackgroundColorResource(android.R.color.holo_green_light);

                            // Verificar si es admin (sin consultar otra colección)
                            boolean esAdmin = "Administrador".equalsIgnoreCase(rolId) ||
                                    "admin".equalsIgnoreCase(rolId);

                            itemGestionarUsuarios.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
                            itemMantenedores.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
                        } else {
                            chipUserRole.setText("Sin rol asignado");
                            chipUserRole.setChipBackgroundColorResource(android.R.color.darker_gray);
                            itemGestionarUsuarios.setVisibility(View.GONE);
                            itemMantenedores.setVisibility(View.GONE);
                            AlertManager.showWarningToast(requireContext(), "No tienes un rol asignado");
                        }
                    } else {
                        tvUserName.setText("Usuario no encontrado");
                        chipUserRole.setText("Sin rol");
                        chipUserRole.setChipBackgroundColorResource(android.R.color.holo_red_light);
                        itemGestionarUsuarios.setVisibility(View.GONE);
                        itemMantenedores.setVisibility(View.GONE);
                        AlertManager.showWarningSnackbar(
                                AlertManager.getRootViewSafe(this),
                                "Usuario no registrado en Firestore"
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("Error al cargar");
                    chipUserRole.setText("Error al cargar rol");
                    chipUserRole.setChipBackgroundColorResource(android.R.color.holo_red_light);
                    itemGestionarUsuarios.setVisibility(View.GONE);
                    itemMantenedores.setVisibility(View.GONE);
                    AlertManager.showErrorSnackbar(
                            AlertManager.getRootViewSafe(this),
                            "Error: " + e.getMessage()
                    );
                });
    }

    private void setupListeners() {
        // Navegar a Mantenedores
        itemMantenedores.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                mainActivity.navigateToMantenedores();
                AlertManager.showInfoSnackbar(
                        AlertManager.getRootViewSafe(this),
                        "Abriendo Mantenedores..."
                );
            } else {
                AlertManager.showErrorToast(requireContext(), "Error al abrir Mantenedores");
            }
        });

        itemGestionarUsuarios.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RegisterActivity.class));
            AlertManager.showInfoToast(requireContext(), "Abriendo gestión de usuarios...");
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                AlertManager.showSuccessToast(requireContext(),
                        "Notificaciones " + (isChecked ? "activadas 🔔" : "desactivadas 🔕")));

        itemDiasAviso.setOnClickListener(v ->
                AlertManager.showInfoSnackbar(AlertManager.getRootViewSafe(this),
                        "Funcionalidad 'Días de aviso' aún no disponible"));

        itemChangePassword.setOnClickListener(v ->
                AlertManager.showInfoToast(requireContext(),
                        "Cambio de contraseña - Próximamente"));

        itemLogout.setOnClickListener(v -> showLogoutDialog());

        itemAbout.setOnClickListener(v ->
                AlertManager.showInfoDialog(requireContext(),
                        "Acerca de",
                        "Centro Integral Alerce App\nVersión 1.0\n\nUniversidad Santo Tomás\n2024"));
    }

    private void showLogoutDialog() {
        AlertManager.showDestructiveDialog(
                requireContext(),
                "Cerrar sesión",
                "¿Estás seguro que deseas cerrar sesión?",
                "Cerrar sesión",
                new AlertManager.OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        auth.signOut();
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        requireActivity().finish();
                        AlertManager.showSuccessToast(requireContext(), "Sesión cerrada correctamente");
                    }

                    @Override
                    public void onCancel() {
                        AlertManager.showInfoToast(requireContext(), "Operación cancelada");
                    }
                }
        );
    }
}
