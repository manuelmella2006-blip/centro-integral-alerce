package com.example.centrointegralalerce.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.services.NotificationChannelManager;
import com.example.centrointegralalerce.utils.FCMTokenManager;
import com.example.centrointegralalerce.utils.BadgeManager;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_NOTIFICATION = 123;

    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;
    private boolean esInvitado = false;
    private String rolUsuario = "usuario"; // por defecto

    // 🔹 Gestor de tokens FCM
    private FCMTokenManager fcmTokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View rootView = AlertManager.getRootView(this);

        // Obtener datos del Login
        esInvitado = getIntent().getBooleanExtra("INVITADO", false);
        String rolId = getIntent().getStringExtra("ROL");
        if (rolId != null) {
            rolUsuario = rolId;
        }

        // Mensaje según rol
        if (esInvitado) {
            AlertManager.showWarningSnackbar(rootView, "Modo invitado - Funcionalidad limitada");
        } else if ("admin".equals(rolUsuario)) {
            AlertManager.showSuccessSnackbar(rootView, "Bienvenido Administrador 👑");
        } else {
            AlertManager.showInfoSnackbar(rootView, "Bienvenido Usuario 👋");
        }

        // Inicializar vistas
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Configurar toolbar
        setSupportActionBar(toolbar);

        // 🔹 Inicializar sistema de notificaciones
        inicializarNotificaciones();

        // 🔹 Actualizar badges al iniciar
        actualizarBadges();

        // 🔹 Cargar permisos del rol desde Firestore y abrir Calendario
        if (savedInstanceState == null) {
            cargarRolYPermisos(rolId);
        }

        // Configurar bottom navigation
        setupBottomNavigation();
    }

    // ✅ Nuevo método: cargar permisos del rol desde Firestore
    private void cargarRolYPermisos(String rolId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (rolId == null) {
            Log.w(TAG, "⚠️ No se recibió rolId, cargando calendario por defecto");
            loadFragment(new CalendarioFragment(), "Calendario");
            bottomNavigation.setSelectedItemId(R.id.nav_calendar);
            return;
        }

        db.collection("roles").document(rolId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> permisos = (Map<String, Object>) documentSnapshot.get("permisos");

                        boolean puedeCrear = (boolean) permisos.getOrDefault("crear_actividades", false);
                        boolean puedeEliminar = (boolean) permisos.getOrDefault("eliminar_actividades", false);
                        boolean puedeVerTodas = (boolean) permisos.getOrDefault("ver_todas_actividades", true);

                        // 🔥 Pasamos los permisos al fragment del calendario
                        Bundle args = new Bundle();
                        args.putBoolean("puedeCrear", puedeCrear);
                        args.putBoolean("puedeEliminar", puedeEliminar);
                        args.putBoolean("puedeVerTodas", puedeVerTodas);

                        CalendarioFragment fragment = new CalendarioFragment();
                        fragment.setArguments(args);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .commit();

                        bottomNavigation.setSelectedItemId(R.id.nav_calendar);

                        Log.d(TAG, "✅ Permisos cargados correctamente para rol: " + rolId);
                    } else {
                        AlertManager.showErrorSnackbar(
                                AlertManager.getRootView(this),
                                "No se encontró el rol en Firestore."
                        );
                        // Cargar fragment por defecto
                        loadFragment(new CalendarioFragment(), "Calendario");
                        bottomNavigation.setSelectedItemId(R.id.nav_calendar);
                    }
                })
                .addOnFailureListener(e -> {
                    AlertManager.showErrorSnackbar(
                            AlertManager.getRootView(this),
                            "Error al cargar rol: " + e.getMessage()
                    );
                    // Cargar fragment por defecto
                    loadFragment(new CalendarioFragment(), "Calendario");
                    bottomNavigation.setSelectedItemId(R.id.nav_calendar);
                });
    }

    // 🔹 Inicializar sistema de notificaciones push
    private void inicializarNotificaciones() {
        Log.d(TAG, "🔔 Inicializando sistema de notificaciones");

        NotificationChannelManager.createNotificationChannels(this);
        Log.d(TAG, "✅ Canales de notificación creados");

        verificarPermisoNotificaciones();

        fcmTokenManager = new FCMTokenManager(this);
        fcmTokenManager.obtenerYRegistrarToken();

        suscribirseATemasSegunRol();
        mostrarTokenEnLogcat();
    }

    private void verificarPermisoNotificaciones() {
        View rootView = AlertManager.getRootView(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "⚠️ Solicitando permiso de notificaciones");
                AlertManager.showWarningSnackbar(rootView, "Se requiere permiso para mostrar notificaciones");

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_NOTIFICATION);
            } else {
                AlertManager.showInfoSnackbar(rootView, "✅ Permiso de notificaciones ya concedido");
            }
        } else {
            Log.d(TAG, "✅ Android < 13: No requiere permiso explícito");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        View rootView = AlertManager.getRootView(this);

        if (requestCode == PERMISSION_REQUEST_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permiso concedido");
                AlertManager.showSuccessSnackbar(rootView, "✅ Notificaciones activadas correctamente");

                if (fcmTokenManager != null) {
                    fcmTokenManager.obtenerYRegistrarToken();
                }
            } else {
                Log.w(TAG, "⚠️ Permiso denegado");
                AlertManager.showErrorSnackbar(rootView, "⚠️ No recibirás notificaciones. Puedes activarlas en Configuración.");
            }
        }
    }

    private void suscribirseATemasSegunRol() {
        View rootView = AlertManager.getRootView(this);

        if (fcmTokenManager == null) {
            Log.w(TAG, "⚠️ FCMTokenManager no inicializado");
            return;
        }

        fcmTokenManager.suscribirseATema("todos");

        if (esInvitado) {
            fcmTokenManager.suscribirseATema("invitados");
            AlertManager.showInfoSnackbar(rootView, "🔔 Notificaciones activas para invitados");

        } else if ("admin".equals(rolUsuario)) {
            fcmTokenManager.suscribirseATema("admin");
            fcmTokenManager.suscribirseATema("talleres");
            fcmTokenManager.suscribirseATema("charlas");
            fcmTokenManager.suscribirseATema("atenciones");
            AlertManager.showInfoSnackbar(rootView, "🔔 Notificaciones de administrador activas");

        } else {
            fcmTokenManager.suscribirseATema("usuarios");
            fcmTokenManager.suscribirseATema("talleres");
            fcmTokenManager.suscribirseATema("charlas");
            AlertManager.showInfoSnackbar(rootView, "🔔 Notificaciones de usuario activas");
        }
    }

    private void mostrarTokenEnLogcat() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "========================================");
                        Log.d(TAG, "🔑 TOKEN FCM PARA TESTING:");
                        Log.d(TAG, token);
                        Log.d(TAG, "========================================");
                    } else {
                        Log.e(TAG, "❌ Error al obtener token FCM", task.getException());
                    }
                });
    }

    private void actualizarBadges() {
        if (esInvitado) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.w(TAG, "⚠️ Usuario no autenticado, no se pueden cargar badges");
            return;
        }

        db.collection("citas")
                .whereEqualTo("userId", userId)
                .whereEqualTo("confirmada", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    BadgeManager.updateBadge(bottomNavigation, R.id.nav_activities_list, count);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al contar actividades", e));

        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);

        Calendar finDia = (Calendar) hoy.clone();
        finDia.set(Calendar.HOUR_OF_DAY, 23);
        finDia.set(Calendar.MINUTE, 59);
        finDia.set(Calendar.SECOND, 59);

        db.collection("citas")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fechaHora", new com.google.firebase.Timestamp(hoy.getTime()))
                .whereLessThanOrEqualTo("fechaHora", new com.google.firebase.Timestamp(finDia.getTime()))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    if (count > 0) {
                        BadgeManager.showBadge(bottomNavigation, R.id.nav_calendar, count);
                    } else {
                        BadgeManager.hideBadge(bottomNavigation, R.id.nav_calendar);
                    }
                });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            View rootView = AlertManager.getRootView(this);

            int itemId = item.getItemId();

            if (itemId == R.id.nav_calendar) {
                selectedFragment = new CalendarioFragment();
                title = "Calendario";

            } else if (itemId == R.id.nav_activities_list) {
                selectedFragment = new ListaActividadesFragment();
                title = "Actividades";

            } else if (itemId == R.id.nav_mantenedores) {
                selectedFragment = new MantenedoresFragment();
                title = "Mantenedores";

                if (!"admin".equals(rolUsuario)) {
                    AlertManager.showWarningSnackbar(rootView,
                            "⚠️ Solo los administradores pueden gestionar mantenedores");
                }

            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new ConfiguracionFragment();
                title = "Configuración";

                if (!"admin".equals(rolUsuario)) {
                    AlertManager.showWarningSnackbar(rootView,
                            "⚠️ Configuración avanzada solo para administradores");
                }
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        if (toolbar != null) toolbar.setTitle(title);
    }

    public void navigateToMantenedores() {
        loadFragment(new MantenedoresFragment(), "Mantenedores");
        bottomNavigation.setSelectedItemId(R.id.nav_mantenedores);
    }

    public boolean isGuest() {
        return esInvitado;
    }

    public boolean isAdmin() {
        return "admin".equals(rolUsuario);
    }

    public String getRolUsuario() {
        return rolUsuario;
    }

    public void cerrarSesion() {
        View rootView = AlertManager.getRootView(this);
        Log.d(TAG, "Cerrando sesión y limpiando tokens FCM");

        if (fcmTokenManager != null) {
            fcmTokenManager.eliminarToken();
        }

        AlertManager.showInfoSnackbar(rootView, "Sesión cerrada correctamente 👋");
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBadges();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destruida");
    }
}
