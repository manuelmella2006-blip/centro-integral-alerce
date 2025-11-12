package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
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

    @Override
    public void onResume() {
        super.onResume();
        // ✅ Al volver al fragmento, verificamos si los permisos siguen siendo válidos
        verificarYMostrarOpciones();
    }

    private void cargarInfoUsuario() {
        if (auth.getCurrentUser() == null) {
            tvUserName.setText("Usuario no autenticado");
            tvUserEmail.setText("Sin correo");
            chipUserRole.setText("Invitado");
            ocultarOpcionesAdmin();
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

                        if (rolId != null && !rolId.isEmpty()) {
                            chipUserRole.setText(rolId);

                            // Cambiar color según el rol
                            if ("admin".equalsIgnoreCase(rolId)) {
                                chipUserRole.setChipBackgroundColorResource(android.R.color.holo_red_light);
                            } else {
                                chipUserRole.setChipBackgroundColorResource(android.R.color.holo_green_light);
                            }

                            // ✅ Verificar permisos
                            verificarYMostrarOpciones();

                        } else {
                            chipUserRole.setText("Sin rol asignado");
                            chipUserRole.setChipBackgroundColorResource(android.R.color.darker_gray);
                            ocultarOpcionesAdmin();
                            AlertManager.showWarningToast(requireContext(), "No tienes un rol asignado");
                        }
                    } else {
                        tvUserName.setText("Usuario no encontrado");
                        chipUserRole.setText("Sin rol");
                        chipUserRole.setChipBackgroundColorResource(android.R.color.holo_red_light);
                        ocultarOpcionesAdmin();
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
                    ocultarOpcionesAdmin();
                    AlertManager.showErrorSnackbar(
                            AlertManager.getRootViewSafe(this),
                            "Error: " + e.getMessage()
                    );
                });
    }

    // ✅ NUEVA VERSIÓN REACTIVA
    private void verificarYMostrarOpciones() {
        if (!isAdded() || getActivity() == null) return;

        // Ocultar primero todo
        ocultarOpcionesAdmin();

        // ✅ VERIFICACIÓN CON REINTENTOS
        if (!UserSession.getInstance().permisosCargados()) {
            Log.w("CONFIGURACION", "⚠️ Permisos no cargados, reintentando en 1 segundo...");

            new Handler().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    verificarYMostrarOpciones(); // Reintento
                }
            }, 1000);
            return;
        }

        // ✅ MOSTRAR OPCIONES SEGÚN PERMISOS
        boolean puedeMantenedores = UserSession.getInstance().puede("gestionar_mantenedores");
        boolean puedeUsuarios = UserSession.getInstance().puede("gestionar_usuarios");

        if (puedeMantenedores) {
            itemMantenedores.setVisibility(View.VISIBLE);
            Log.d("CONFIGURACION", "✅ Mostrando Mantenedores - Con permiso");
        }

        if (puedeUsuarios) {
            itemGestionarUsuarios.setVisibility(View.VISIBLE);
            Log.d("CONFIGURACION", "✅ Mostrando Gestión Usuarios - Con permiso");
        }

        Log.d("CONFIGURACION", "🎯 UI Actualizada - Mantenedores: " + puedeMantenedores +
                ", Usuarios: " + puedeUsuarios);
    }

    private void ocultarOpcionesAdmin() {
        itemMantenedores.setVisibility(View.GONE);
        itemGestionarUsuarios.setVisibility(View.GONE);
    }

    private void setupListeners() {
        itemMantenedores.setOnClickListener(v -> {
            if (UserSession.getInstance().puede("gestionar_mantenedores")) {
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
            } else {
                AlertManager.showWarningSnackbar(
                        AlertManager.getRootViewSafe(this),
                        "❌ No tienes permisos para gestionar mantenedores"
                );
            }
        });

        itemGestionarUsuarios.setOnClickListener(v -> {
            if (UserSession.getInstance().puede("gestionar_usuarios")) {
                startActivity(new Intent(requireContext(), RegisterActivity.class));
                AlertManager.showInfoToast(requireContext(), "Abriendo gestión de usuarios...");
            } else {
                AlertManager.showWarningSnackbar(
                        AlertManager.getRootViewSafe(this),
                        "❌ No tienes permisos para gestionar usuarios"
                );
            }
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
