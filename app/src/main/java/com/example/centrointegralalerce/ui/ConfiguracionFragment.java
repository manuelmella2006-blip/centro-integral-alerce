package com.example.centrointegralalerce.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.utils.NotificationScheduler;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfiguracionFragment extends Fragment {

    private static final String TAG = "ConfiguracionFragment";

    // -------------------------------------------------------------
    // 🔧 PREFERENCIAS
    // -------------------------------------------------------------
    private static final String PREFS_NAME = "ConfigPrefs";
    private static final String KEY_DIAS_AVISO = "dias_aviso";
    private static final String KEY_NOTIF_ENABLED = "notificaciones_enabled";
    private static final int DEFAULT_DIAS_AVISO = 3;

    private SharedPreferences prefs;

    // UI
    private TextView tvUserName, tvUserEmail, tvDiasAvisoValue;
    private Chip chipUserRole;
    private LinearLayout itemMantenedores, itemGestionarUsuarios;
    private LinearLayout itemDiasAviso, itemChangePassword, itemLogout, itemAbout;
    private SwitchMaterial switchNotifications;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // 🔔 Notificaciones
    private NotificationScheduler notificationScheduler;

    // Nombres legibles de roles
    private static final java.util.Map<String, String> NOMBRES_ROLES =
            new java.util.HashMap<String, String>() {{
                put("admin", "Administrador");
                put("usuario", "Usuario Normal");
                put("invitado", "Invitado");
            }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // SharedPreferences
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 🔔 Inicializar NotificationScheduler
        notificationScheduler = new NotificationScheduler(requireContext());

        // Inicializar UI
        initViews(view);

        // Cargar datos de usuario
        cargarDatosInmediatos();
        cargarInfoUsuario();

        // Cargar preferencias UI
        loadPreferencesUI();

        // Listeners
        setupListeners();

        return view;
    }

    // -------------------------------------------------------------
    // INICIALIZAR VISTAS
    // -------------------------------------------------------------
    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvDiasAvisoValue = view.findViewById(R.id.tv_dias_aviso_value);

        chipUserRole = view.findViewById(R.id.chip_user_role);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        itemMantenedores = view.findViewById(R.id.item_mantenedores);
        itemGestionarUsuarios = view.findViewById(R.id.item_gestionar_usuarios);
        itemDiasAviso = view.findViewById(R.id.item_dias_aviso);
        itemChangePassword = view.findViewById(R.id.item_change_password);
        itemLogout = view.findViewById(R.id.item_logout);
        itemAbout = view.findViewById(R.id.item_about);
    }

    // -------------------------------------------------------------
    // CARGA INMEDIATA (UserSession)
    // -------------------------------------------------------------
    private void cargarDatosInmediatos() {
        if (auth.getCurrentUser() == null) {
            mostrarDatosPorDefecto();
            return;
        }

        tvUserEmail.setText(auth.getCurrentUser().getEmail());

        String rolId = UserSession.getInstance().getRolId();
        if (rolId != null) {
            mostrarNombreRolInmediato(rolId);
            verificarYMostrarOpciones();
        } else {
            chipUserRole.setText("Cargando...");
        }

        tvUserName.setText("Cargando...");
    }

    // -------------------------------------------------------------
    // CARGA DESDE FIRESTORE
    // -------------------------------------------------------------
    private void cargarInfoUsuario() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        mostrarDatosPorDefecto();
                        return;
                    }

                    String nombre = doc.getString("nombre");
                    String rolId = doc.getString("rolId");

                    tvUserName.setText(nombre != null ? nombre : "Usuario");

                    if (rolId != null && !rolId.equals(UserSession.getInstance().getRolId())) {
                        mostrarNombreRolInmediato(rolId);
                    }
                });
    }

    // -------------------------------------------------------------
    // MOSTRAR ROL
    // -------------------------------------------------------------
    private void mostrarNombreRolInmediato(String rolId) {
        String nombre = NOMBRES_ROLES.getOrDefault(rolId, rolId);
        chipUserRole.setText(nombre);

        if (rolId.equalsIgnoreCase("admin")) {
            chipUserRole.setChipBackgroundColorResource(android.R.color.holo_red_light);
        } else {
            chipUserRole.setChipBackgroundColorResource(android.R.color.holo_green_light);
        }
    }

    private void mostrarDatosPorDefecto() {
        tvUserName.setText("Usuario no autenticado");
        tvUserEmail.setText("Sin correo");
        chipUserRole.setText("Invitado");
        ocultarOpcionesAdmin();
    }

    private void verificarYMostrarOpciones() {
        ocultarOpcionesAdmin();

        new Handler().postDelayed(() -> {
            boolean puedeMant = UserSession.getInstance().puede("gestionar_mantenedores");
            boolean puedeUsers = UserSession.getInstance().puede("gestionar_usuarios");

            if (puedeMant) itemMantenedores.setVisibility(View.VISIBLE);
            if (puedeUsers) itemGestionarUsuarios.setVisibility(View.VISIBLE);
        }, 400);
    }

    private void ocultarOpcionesAdmin() {
        itemMantenedores.setVisibility(View.GONE);
        itemGestionarUsuarios.setVisibility(View.GONE);
    }

    // -------------------------------------------------------------
    // CARGAR PREFERENCIAS UI
    // -------------------------------------------------------------
    private void loadPreferencesUI() {
        int diasAviso = prefs.getInt(KEY_DIAS_AVISO, DEFAULT_DIAS_AVISO);
        updateDiasAvisoText(diasAviso);

        boolean notifEnabled = prefs.getBoolean(KEY_NOTIF_ENABLED, true);
        switchNotifications.setChecked(notifEnabled);
    }

    private void updateDiasAvisoText(int dias) {
        if (dias == 1) tvDiasAvisoValue.setText("1 día");
        else if (dias == 7) tvDiasAvisoValue.setText("1 semana");
        else if (dias == 14) tvDiasAvisoValue.setText("2 semanas");
        else tvDiasAvisoValue.setText(dias + " días");
    }

    // -------------------------------------------------------------
    // LISTENERS
    // -------------------------------------------------------------
    private void setupListeners() {

        // 🔔 Activar / desactivar notificaciones
        switchNotifications.setOnCheckedChangeListener((btn, enabled) -> {
            prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply();

            if (enabled) {
                AlertManager.showSuccessToast(requireContext(), "✅ Notificaciones activadas 🔔");
                // Reprogramar todas las notificaciones
                reprogramarNotificaciones();
            } else {
                AlertManager.showInfoToast(requireContext(), "🔕 Notificaciones desactivadas");
                // Cancelar todas las notificaciones programadas
                cancelarTodasLasNotificaciones();
            }

            Log.d(TAG, "🔔 Notificaciones " + (enabled ? "activadas" : "desactivadas"));
        });

        // 📅 Selección de días de aviso
        itemDiasAviso.setOnClickListener(v -> showDiasAvisoDialog());

        itemMantenedores.setOnClickListener(v -> {
            if (UserSession.getInstance().puede("gestionar_mantenedores")) {
                ((MainActivity) requireActivity()).navigateToMantenedores();
            }
        });

        itemGestionarUsuarios.setOnClickListener(v -> {
            if (UserSession.getInstance().puede("gestionar_usuarios")) {
                startActivity(new Intent(requireContext(), RegisterActivity.class));
            }
        });

        // 🔐 Cambiar contraseña
        itemChangePassword.setOnClickListener(v -> {
            AlertManager.showInfoDialog(requireContext(), "Cambiar Contraseña",
                    "Función en desarrollo. Próximamente podrás cambiar tu contraseña desde aquí.");
        });

        // 🚪 CERRAR SESIÓN - CORREGIDO
        itemLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        itemAbout.setOnClickListener(v ->
                AlertManager.showInfoDialog(requireContext(), "Acerca de",
                        "Centro Integral Alerce\nVersión 1.0"));
    }

    // -------------------------------------------------------------
    // 🔐 MÉTODO PARA CERRAR SESIÓN
    // -------------------------------------------------------------
    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "🚪 Cerrando sesión desde ConfiguracionFragment");

        // Cancelar todas las notificaciones programadas
        if (notificationScheduler != null) {
            notificationScheduler.cancelAllNotifications();
        }

        // Limpiar preferencias de notificaciones
        prefs.edit()
                .putBoolean(KEY_NOTIF_ENABLED, false)
                .apply();

        // Cerrar sesión en MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).cerrarSesion();
        } else {
            // Fallback si no se puede acceder a MainActivity
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    // -------------------------------------------------------------
    // 🔔 MÉTODOS DE NOTIFICACIONES
    // -------------------------------------------------------------

    /**
     * Muestra el diálogo para seleccionar días de aviso
     */
    private void showDiasAvisoDialog() {

        int currentDias = prefs.getInt(KEY_DIAS_AVISO, DEFAULT_DIAS_AVISO);

        String[] opciones = {
                "1 día antes",
                "2 días antes",
                "3 días antes",
                "5 días antes",
                "7 días antes (1 semana)",
                "14 días antes (2 semanas)"
        };

        int[] valores = {1, 2, 3, 5, 7, 14};

        int selectedIndex = 2;
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] == currentDias) selectedIndex = i;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("📅 Días de aviso previo")
                .setIcon(R.drawable.ic_notification)
                .setSingleChoiceItems(opciones, selectedIndex, (dialog, which) -> {
                    int nuevosDias = valores[which];

                    // Guardar nueva configuración
                    prefs.edit().putInt(KEY_DIAS_AVISO, nuevosDias).apply();
                    updateDiasAvisoText(nuevosDias);

                    // Mostrar confirmación
                    AlertManager.showSuccessSnackbar(
                            AlertManager.getRootViewSafe(this),
                            "✅ Avisos configurados para " + nuevosDias + " día(s) antes"
                    );

                    // 🔔 Reprogramar todas las notificaciones con la nueva configuración
                    reprogramarNotificaciones();

                    Log.d(TAG, "📅 Días de aviso actualizados a: " + nuevosDias);

                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Reprogramar todas las notificaciones con la configuración actual
     */
    private void reprogramarNotificaciones() {
        if (notificationScheduler == null) {
            Log.w(TAG, "⚠️ NotificationScheduler no inicializado");
            return;
        }

        if (!areNotificationsEnabled(requireContext())) {
            Log.d(TAG, "🔕 Notificaciones deshabilitadas, no se reprograma");
            return;
        }

        Log.d(TAG, "🔄 Reprogramando todas las notificaciones...");

        // Buscar el CalendarioFragment y llamar a su método de reprogramación
        if (getActivity() != null) {
            Fragment calendarioFragment = getActivity().getSupportFragmentManager()
                    .findFragmentByTag("CalendarioFragment");

            if (calendarioFragment instanceof CalendarioFragment) {
                // El CalendarioFragment está en memoria, llamar directamente
                ((CalendarioFragment) calendarioFragment).reprogramarTodasLasNotificaciones();
                Log.d(TAG, "✅ Reprogramación solicitada al CalendarioFragment");
            } else {
                // El CalendarioFragment no está en memoria
                // La configuración ya está guardada, se aplicará cuando se cargue
                Log.d(TAG, "ℹ️ CalendarioFragment no visible, configuración guardada para próxima carga");

                AlertManager.showInfoToast(requireContext(),
                        "Configuración guardada. Se aplicará al cargar el calendario");
            }
        }
    }

    /**
     * Cancelar todas las notificaciones programadas
     */
    private void cancelarTodasLasNotificaciones() {
        if (notificationScheduler == null) {
            Log.w(TAG, "⚠️ NotificationScheduler no inicializado");
            return;
        }

        Log.d(TAG, "🗑️ Cancelando todas las notificaciones...");

        try {
            notificationScheduler.cancelAllNotifications();
            Log.d(TAG, "✅ Todas las notificaciones canceladas");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al cancelar notificaciones", e);
        }
    }

    // -------------------------------------------------------------
    // MÉTODOS ESTÁTICOS (para Workers, alarmas, etc.)
    // -------------------------------------------------------------

    /**
     * Obtiene los días de aviso configurados
     * Se usa desde NotificationScheduler y otros componentes
     */
    public static int getDiasAviso(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DIAS_AVISO, DEFAULT_DIAS_AVISO);
    }

    /**
     * Verifica si las notificaciones están habilitadas
     * Se usa desde NotificationScheduler antes de programar
     */
    public static boolean areNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_NOTIF_ENABLED, true);
    }
}