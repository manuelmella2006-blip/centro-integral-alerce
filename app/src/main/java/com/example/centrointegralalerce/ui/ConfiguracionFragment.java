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

    // ✅ MAPEO LOCAL DE NOMBRES DE ROLES
    private static final java.util.Map<String, String> NOMBRES_ROLES = new java.util.HashMap<String, String>() {{
        put("admin", "Administrador");
        put("usuario", "Usuario Normal");
        put("invitado", "Invitado");
    }};

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

        // ✅ PRIMERO: Cargar datos inmediatos desde UserSession
        cargarDatosInmediatos();

        // ✅ LUEGO: Actualizar con datos frescos de Firestore
        cargarInfoUsuario();

        setupListeners();

        return view;
    }

    // ✅ NUEVO MÉTODO: Cargar datos al instante desde UserSession
    private void cargarDatosInmediatos() {
        if (auth.getCurrentUser() == null) {
            mostrarDatosPorDefecto();
            return;
        }

        String email = auth.getCurrentUser().getEmail();
        tvUserEmail.setText(email != null ? email : "Sin correo");

        // ✅ Usar UserSession para mostrar datos inmediatos
        String rolId = UserSession.getInstance().getRolId();

        if (rolId != null && !rolId.isEmpty()) {
            mostrarNombreRolInmediato(rolId);
            verificarYMostrarOpciones(); // Mostrar/ocultar opciones inmediatamente
        } else {
            // Si UserSession no tiene rol, intentar cargar desde Firestore después
            chipUserRole.setText("Cargando...");
        }

        // Nombre del usuario podríamos obtenerlo de SharedPreferences si lo guardamos
        tvUserName.setText("Cargando...");
    }

    private void cargarInfoUsuario() {
        if (auth.getCurrentUser() == null) {
            mostrarDatosPorDefecto();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String rolId = documentSnapshot.getString("rolId");

                        // ✅ Actualizar nombre si está disponible
                        if (nombre != null && !nombre.isEmpty()) {
                            tvUserName.setText(nombre);
                        } else {
                            tvUserName.setText("Usuario");
                        }

                        // ✅ Actualizar rol si es diferente al de UserSession
                        if (rolId != null && !rolId.isEmpty()) {
                            String rolActual = UserSession.getInstance().getRolId();
                            if (!rolId.equals(rolActual)) {
                                mostrarNombreRolInmediato(rolId);
                                // También actualizar UserSession si cambió el rol
                                UserSession.getInstance().setRol(rolId, UserSession.getInstance().getPermisos());
                            }
                        }

                    } else {
                        mostrarDatosPorDefecto();
                        AlertManager.showWarningSnackbar(
                                AlertManager.getRootViewSafe(this),
                                "Usuario no registrado en Firestore"
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CONFIGURACION", "Error al cargar datos de Firestore: " + e.getMessage());
                    // No mostramos error para no molestar al usuario, ya tenemos datos de UserSession
                });
    }

    private void mostrarDatosPorDefecto() {
        tvUserName.setText("Usuario no autenticado");
        tvUserEmail.setText("Sin correo");
        chipUserRole.setText("Invitado");
        ocultarOpcionesAdmin();
    }

    // ✅ MÉTODO RÁPIDO: Sin consulta a Firestore
    private void mostrarNombreRolInmediato(String rolId) {
        String nombreDisplay = NOMBRES_ROLES.get(rolId.toLowerCase());

        if (nombreDisplay != null) {
            chipUserRole.setText(nombreDisplay);
        } else {
            chipUserRole.setText(capitalize(rolId));
        }

        // Aplicar color según el rol
        if ("admin".equalsIgnoreCase(rolId)) {
            chipUserRole.setChipBackgroundColorResource(android.R.color.holo_red_light);
        } else {
            chipUserRole.setChipBackgroundColorResource(android.R.color.holo_green_light);
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    // ✅ VERSIÓN REACTIVA
    private void verificarYMostrarOpciones() {
        if (!isAdded() || getActivity() == null) return;

        ocultarOpcionesAdmin();

        if (!UserSession.getInstance().permisosCargados()) {
            Log.w("CONFIGURACION", "⚠️ Permisos no cargados, reintentando...");

            new Handler().postDelayed(() -> {
                if (isAdded() && getActivity() != null) {
                    verificarYMostrarOpciones();
                }
            }, 500); // Reducido a 500ms
            return;
        }

        boolean puedeMantenedores = UserSession.getInstance().puede("gestionar_mantenedores");
        boolean puedeUsuarios = UserSession.getInstance().puede("gestionar_usuarios");

        if (puedeMantenedores) {
            itemMantenedores.setVisibility(View.VISIBLE);
        }

        if (puedeUsuarios) {
            itemGestionarUsuarios.setVisibility(View.VISIBLE);
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