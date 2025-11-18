package com.example.centrointegralalerce.ui;
import java.util.HashMap;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.services.NotificationChannelManager;
import com.example.centrointegralalerce.utils.FCMTokenManager;
import com.example.centrointegralalerce.utils.BadgeManager;
import com.example.centrointegralalerce.utils.AlertManager;
import com.example.centrointegralalerce.utils.SharedPreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_NOTIFICATION = 123;

    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;
    private boolean esInvitado = false;
    private String rolUsuario = "usuario";

    private FCMTokenManager fcmTokenManager;
    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsManager = new SharedPreferencesManager(this);
        if (!prefsManager.isLoggedIn() || FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        View rootView = AlertManager.getRootView(this);

        // 🔹 Leer si el usuario es invitado o tiene rol asignado
        esInvitado = getIntent().getBooleanExtra("INVITADO", false);
        String rolId = getIntent().getStringExtra("ROL");
        if (rolId != null) {
            rolUsuario = rolId;
        }

        // 🔹 Mensajes según tipo de usuario
        if (esInvitado) {
            AlertManager.showWarningSnackbar(rootView, "Modo invitado - Funcionalidad limitada");
        } else if ("admin".equals(rolUsuario)) {
            AlertManager.showSuccessSnackbar(rootView, "Bienvenido Administrador 👑");
        } else {
            AlertManager.showInfoSnackbar(rootView, "Bienvenido Usuario 👋");
        }

        // 🔹 Inicializar toolbar + bottomNavigation
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setSupportActionBar(toolbar);

        // 🔧 AGREGAR BOTÓN DEBUG PARA PERMISOS
        setupBotonDebug();

        // 🔥 FORZAR RECARGA DE PERMISOS AL INICIAR (solo para pruebas)
        recargarPermisosManual();
        Log.d("MAIN_DEBUG", "🚀 recargarPermisosManual() ejecutado automáticamente");

        // 🔔 Notificaciones
        inicializarNotificaciones();

        // 🔐 Cargar permisos del usuario
        cargarRolYPermisos(rolUsuario);

        // 🔽 Activar navegación inferior
        setupBottomNavigation();

        // 🔴 Badges (número de citas)
        actualizarBadges();
    }

    private void setupBotonDebug() {
        Button btnDebug = findViewById(R.id.btnDebugPermisos);
        if (btnDebug != null) {
            // Cambia a View.VISIBLE cuando quieras debuggear
            btnDebug.setVisibility(View.VISIBLE); // o View.GONE para ocultar

            btnDebug.setOnClickListener(v -> {
                diagnosticarProblemaPermisos();
                Toast.makeText(this, "🔧 Recargando permisos...", Toast.LENGTH_SHORT).show();
            });

            Log.d("MAIN_DEBUG", "✅ Botón debug de permisos configurado");
        }
    }


    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // EN MainActivity.java - REEMPLAZAR el método cargarRolYPermisos
    private void cargarRolYPermisos(String rolId) {
        Log.d("MAIN_DEBUG", "🔍 Iniciando carga de permisos...");
        Log.d("MAIN_DEBUG", "Rol recibido: " + rolId);

        UserSession session = UserSession.getInstance();
        session.debugPermisos();

        // 🔥 NUEVO: Usar el nuevo método de espera
        session.esperarPermisos(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("MAIN_DEBUG", "✅ PERMISOS CONFIRMADOS - Iniciando UI");

                        // 🔥 NUEVO: Ejecutar diagnóstico de permisos
                        verificarEstructuraPermisos();

                        // 🔥 NUEVO: Mostrar confirmación en logs
                        UserSession session = UserSession.getInstance();
                        Log.d("MAIN_DEBUG", "🎯 Permisos finales cargados:");
                        Log.d("MAIN_DEBUG", " - Rol: " + session.getRolId());
                        Log.d("MAIN_DEBUG", " - Crear actividades: " + session.puedeCrearActividades());
                        Log.d("MAIN_DEBUG", " - Modificar actividades: " + session.puedeModificarActividades());
                        Log.d("MAIN_DEBUG", " - Cancelar actividades: " + session.puedeCancelarActividades());
                        Log.d("MAIN_DEBUG", " - Reagendar actividades: " + session.puedeReagendarActividades());
                        Log.d("MAIN_DEBUG", " - Adjuntar archivos: " + session.puedeAdjuntarComunicaciones());

                        cargarFragmentoInicial();
                        actualizarMenuNavigation();
                    }
                });
            }
        });

        // 🔥 NUEVO: Cargar permisos si no están cargados
        if (!session.permisosCargados()) {
            Log.d("MAIN_DEBUG", "🔄 Permisos no cargados, iniciando carga desde Firestore...");
            cargarPermisosDesdeFirestore(rolId != null ? rolId : "usuario");
        } else {
            Log.d("MAIN_DEBUG", "✅ Permisos ya estaban cargados previamente");

            // 🔥 IMPORTANTE: También puedes verificar aquí si quieres
            verificarEstructuraPermisos();
        }
    }

    private void cargarPermisosDesdeFirestore(String rolId) {
        Log.d("MAIN_DEBUG", "🔄 Cargando permisos desde Firestore para rol: " + rolId);

        FirebaseFirestore.getInstance().collection("roles").document(rolId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getData() != null) {
                        Map<String, Object> data = doc.getData();
                        Map<String, Boolean> permisos = new HashMap<>();

                        // 🔥 CORRECCIÓN COMPLETA: Extraer todos los campos booleanos como permisos
                        Log.d("MAIN_DEBUG", "📊 Todos los campos en el documento:");

                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();

                            Log.d("MAIN_DEBUG", "   Campo: " + key + " = " + value + " (tipo: " + (value != null ? value.getClass().getSimpleName() : "null") + ")");

                            // Si es un mapa de permisos, procesarlo
                            if ("permisos".equals(key) && value instanceof Map) {
                                Map<?, ?> permisosMap = (Map<?, ?>) value;
                                Log.d("MAIN_DEBUG", "📊 Procesando mapa de permisos: " + permisosMap.size() + " elementos");

                                for (Map.Entry<?, ?> permisoEntry : permisosMap.entrySet()) {
                                    if (permisoEntry.getKey() instanceof String && permisoEntry.getValue() instanceof Boolean) {
                                        String permisoKey = (String) permisoEntry.getKey();
                                        Boolean permisoValue = (Boolean) permisoEntry.getValue();
                                        permisos.put(permisoKey, permisoValue);
                                        Log.d("MAIN_DEBUG", "   ✅ Permiso: " + permisoKey + ": " + permisoValue);
                                    }
                                }
                            }
                            // Si es un campo booleano directo, también agregarlo como permiso
                            else if (value instanceof Boolean) {
                                permisos.put(key, (Boolean) value);
                                Log.d("MAIN_DEBUG", "   ✅ Campo directo como permiso: " + key + ": " + value);
                            }
                        }

                        // 🔥 VERIFICACIÓN CRÍTICA: Asegurar que tenemos los permisos esenciales
                        verificarPermisosCriticos(permisos, rolId);

                        UserSession.getInstance().setRol(rolId, permisos);
                        Log.d("MAIN_DEBUG", "✅ " + permisos.size() + " permisos cargados desde Firestore");

                    } else {
                        Log.w("MAIN_DEBUG", "⚠️ Rol no encontrado en Firestore: " + rolId);
                        cargarPermisosPorDefecto(rolId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MAIN_DEBUG", "❌ Error al cargar permisos desde Firestore", e);
                    cargarPermisosPorDefecto(rolId);
                });
    }

    // 🔥 NUEVO MÉTODO: Verificar que tenemos los permisos críticos
    private void verificarPermisosCriticos(Map<String, Boolean> permisos, String rolId) {
        String[] permisosCriticos = {
                "modificar_actividades", "cancelar_actividades", "reagendar_actividades",
                "adjuntar_comunicaciones", "crear_actividades", "eliminar_actividades"
        };

        Log.d("MAIN_DEBUG", "🎯 VERIFICACIÓN CRÍTICA DE PERMISOS PARA: " + rolId);

        for (String permiso : permisosCriticos) {
            boolean tienePermiso = permisos.getOrDefault(permiso, false);
            Log.d("MAIN_DEBUG", "   " + permiso + ": " + tienePermiso +
                    " (en mapa: " + permisos.containsKey(permiso) + ")");
        }

        // Si faltan permisos críticos, cargar desde defaults
        if (!permisos.containsKey("modificar_actividades") || !permisos.containsKey("cancelar_actividades")) {
            Log.w("MAIN_DEBUG", "⚠️ Faltan permisos críticos, usando valores por defecto");
            cargarPermisosPorDefecto(rolId);
        }
    }
    private void diagnosticarProblemaPermisos() {
        Log.d("MAIN_DEBUG", "🔍 DIAGNÓSTICO DE PERMISOS");

        // Verificar si ya hay permisos cargados
        UserSession session = UserSession.getInstance();
        if (session.permisosCargados()) {
            Map<String, Boolean> permisosActuales = session.getPermisos();
            Log.d("MAIN_DEBUG", "📊 Permisos actualmente en UserSession: " + permisosActuales.size());
            for (Map.Entry<String, Boolean> entry : permisosActuales.entrySet()) {
                Log.d("MAIN_DEBUG", "   " + entry.getKey() + ": " + entry.getValue());
            }
        } else {
            Log.d("MAIN_DEBUG", "❌ No hay permisos cargados en UserSession");
        }

        // Forzar recarga desde Firestore
        String rolId = getIntent().getStringExtra("ROL");
        if (rolId != null) {
            Log.d("MAIN_DEBUG", "🔄 Forzando recarga de permisos para rol: " + rolId);
            cargarPermisosDesdeFirestore(rolId);
        }
    }

    private void agregarBotonDebug() {
        // Solo para desarrollo - quitar después
        Button btnDebug = new Button(this);
        btnDebug.setText("🔧 Recargar Permisos");
        btnDebug.setOnClickListener(v -> {
            diagnosticarProblemaPermisos();
            Toast.makeText(this, "Recargando permisos...", Toast.LENGTH_SHORT).show();
        });

        // Agregar a la interfaz (puedes ponerlo en un menú o layout temporal)
        // O simplemente usar el logcat para ver los resultados
    }
    private void actualizarMenuNavigation() {
        if (bottomNavigation == null) return;

        Menu menu = bottomNavigation.getMenu();
        MenuItem mantenedoresItem = menu.findItem(R.id.nav_mantenedores);

        if (mantenedoresItem != null) {
            boolean puedeMantenedores = UserSession.getInstance().puede("gestionar_mantenedores");
            mantenedoresItem.setVisible(puedeMantenedores);

            Log.d("MAIN_DEBUG", "🎯 Menu Navigation - Mantenedores visible: " + puedeMantenedores +
                    " | Rol: " + UserSession.getInstance().getRolId());
        }
    }

    // ✅ ACTUALIZADO: Ahora usa tag para el CalendarioFragment
    private void cargarFragmentoInicial() {
        boolean puedeCrear = UserSession.getInstance().puedeCrearActividades();
        boolean puedeEliminar = UserSession.getInstance().puedeEliminarActividades();
        boolean puedeVerTodas = UserSession.getInstance().puedeVerTodasActividades();

        Bundle args = new Bundle();
        args.putBoolean("puedeCrear", puedeCrear);
        args.putBoolean("puedeEliminar", puedeEliminar);
        args.putBoolean("puedeVerTodas", puedeVerTodas);

        CalendarioFragment fragment = new CalendarioFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment, "CalendarioFragment")  // ✅ TAG AGREGADO
                .commit();

        bottomNavigation.setSelectedItemId(R.id.nav_calendar);

        Log.d("MAIN_DEBUG", "🎯 Fragmento inicial cargado con permisos:");
        Log.d("MAIN_DEBUG", " - Crear: " + puedeCrear);
        Log.d("MAIN_DEBUG", " - Eliminar: " + puedeEliminar);
        Log.d("MAIN_DEBUG", " - Ver todas: " + puedeVerTodas);
    }

    private void cargarPermisosPorDefecto(String rolId) {
        Map<String, Boolean> permisosPorDefecto = new HashMap<>();

        if ("admin".equals(rolId)) {
            permisosPorDefecto.put("crear_usuarios", true);
            permisosPorDefecto.put("gestionar_usuarios", true);
            permisosPorDefecto.put("gestionar_mantenedores", true);
            permisosPorDefecto.put("crear_actividades", true);
            permisosPorDefecto.put("modificar_actividades", true);
            permisosPorDefecto.put("cancelar_actividades", true);
            permisosPorDefecto.put("reagendar_actividades", true);
            permisosPorDefecto.put("adjuntar_comunicaciones", true);
            permisosPorDefecto.put("eliminar_actividades", true);
            permisosPorDefecto.put("ver_todas_actividades", true);
        } else {
            // 🔥 PERMISOS COMPLETOS PARA USUARIO NORMAL (igual que en Firebase)
            permisosPorDefecto.put("crear_usuarios", false);
            permisosPorDefecto.put("gestionar_usuarios", false);
            permisosPorDefecto.put("gestionar_mantenedores", false);
            permisosPorDefecto.put("crear_actividades", true);
            permisosPorDefecto.put("modificar_actividades", true);
            permisosPorDefecto.put("cancelar_actividades", true);
            permisosPorDefecto.put("reagendar_actividades", true);
            permisosPorDefecto.put("adjuntar_comunicaciones", true);
            permisosPorDefecto.put("eliminar_actividades", true);
            permisosPorDefecto.put("ver_todas_actividades", true);
        }

        UserSession.getInstance().setRol(rolId, permisosPorDefecto);
        Log.d("MAIN_DEBUG", "✅ Permisos por defecto cargados para: " + rolId);
    }

    private void recargarPermisosManual() {
        UserSession.getInstance().limpiarSesion();
        String rolId = getIntent().getStringExtra("ROL");
        if (rolId != null) {
            cargarPermisosDesdeFirestore(rolId);
        }
    }
    private void verificarEstructuraPermisos() {
        Log.d("MAIN_DEBUG", "🔍 Verificando estructura de permisos en UserSession...");
        UserSession session = UserSession.getInstance();

        if (session.permisosCargados()) {
            Map<String, Boolean> permisos = session.getPermisos();
            if (permisos != null) {
                Log.d("MAIN_DEBUG", "=== ESTRUCTURA DE PERMISOS ACTUAL ===");
                for (Map.Entry<String, Boolean> entry : permisos.entrySet()) {
                    Log.d("MAIN_DEBUG", entry.getKey() + ": " + entry.getValue());
                }
                Log.d("MAIN_DEBUG", "=====================================");
            } else {
                Log.e("MAIN_DEBUG", "❌ permisos es NULL en UserSession");
            }
        } else {
            Log.e("MAIN_DEBUG", "❌ Permisos no cargados en UserSession");
        }
    }

    // ✅ ACTUALIZADO: Ahora usa tag para el CalendarioFragment
    private void cargarFragmentoInicialConPermisos(Map<String, Boolean> permisos) {
        boolean puedeCrear = permisos.getOrDefault("crear_actividades", false);
        boolean puedeEliminar = permisos.getOrDefault("eliminar_actividades", false);
        boolean puedeVerTodas = permisos.getOrDefault("ver_todas_actividades", true);

        Bundle args = new Bundle();
        args.putBoolean("puedeCrear", puedeCrear);
        args.putBoolean("puedeEliminar", puedeEliminar);
        args.putBoolean("puedeVerTodas", puedeVerTodas);

        CalendarioFragment fragment = new CalendarioFragment();
        fragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment, "CalendarioFragment")  // ✅ TAG AGREGADO
                .commit();

        bottomNavigation.setSelectedItemId(R.id.nav_calendar);

        Log.d("MAIN_ACTIVITY", "Permisos cargados - gestionar_usuarios: " +
                UserSession.getInstance().puede("gestionar_usuarios"));
    }

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
                Log.d(TAG, "✅ Permiso de notificaciones ya concedido");
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

        db.collection("actividades")
                .get()
                .addOnSuccessListener(actividadesSnapshot -> {
                    final int totalActividades = actividadesSnapshot.size();
                    final int[] procesadas = {0};
                    final int[] totalCitasNoConfirmadas = {0};
                    final int[] totalCitasHoy = {0};

                    Calendar hoy = Calendar.getInstance();
                    hoy.set(Calendar.HOUR_OF_DAY, 0);
                    hoy.set(Calendar.MINUTE, 0);
                    hoy.set(Calendar.SECOND, 0);
                    hoy.set(Calendar.MILLISECOND, 0);

                    Calendar finDia = (Calendar) hoy.clone();
                    finDia.set(Calendar.HOUR_OF_DAY, 23);
                    finDia.set(Calendar.MINUTE, 59);
                    finDia.set(Calendar.SECOND, 59);
                    finDia.set(Calendar.MILLISECOND, 999);

                    if (totalActividades == 0) {
                        BadgeManager.updateBadge(bottomNavigation, R.id.nav_activities_list, 0);
                        BadgeManager.hideBadge(bottomNavigation, R.id.nav_calendar);
                        return;
                    }

                    for (QueryDocumentSnapshot actividadDoc : actividadesSnapshot) {
                        String actividadId = actividadDoc.getId();

                        db.collection("actividades").document(actividadId)
                                .collection("citas")
                                .get()
                                .addOnSuccessListener(citasSnapshot -> {
                                    for (QueryDocumentSnapshot citaDoc : citasSnapshot) {
                                        String citaUserId = citaDoc.getString("userId");
                                        if (userId.equals(citaUserId)) {
                                            Boolean confirmada = citaDoc.getBoolean("confirmada");
                                            if (confirmada != null && !confirmada) {
                                                totalCitasNoConfirmadas[0]++;
                                            }

                                            String fechaCita = citaDoc.getString("fecha");
                                            if (fechaCita != null) {
                                                try {
                                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));
                                                    Date fecha = sdf.parse(fechaCita);
                                                    if (fecha != null &&
                                                            fecha.getTime() >= hoy.getTimeInMillis() &&
                                                            fecha.getTime() <= finDia.getTimeInMillis()) {
                                                        totalCitasHoy[0]++;
                                                    }
                                                } catch (ParseException e) {
                                                    Log.e(TAG, "Error parseando fecha: " + fechaCita, e);
                                                }
                                            }
                                        }
                                    }

                                    procesadas[0]++;
                                    if (procesadas[0] == totalActividades) {
                                        BadgeManager.updateBadge(bottomNavigation, R.id.nav_activities_list, totalCitasNoConfirmadas[0]);
                                        if (totalCitasHoy[0] > 0) {
                                            BadgeManager.showBadge(bottomNavigation, R.id.nav_calendar, totalCitasHoy[0]);
                                        } else {
                                            BadgeManager.hideBadge(bottomNavigation, R.id.nav_calendar);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al cargar citas de actividad " + actividadId, e);
                                    procesadas[0]++;
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar actividades para badges", e);
                });
    }

    // ✅ ACTUALIZADO: Ahora usa tags para todos los fragments
    private void setupBottomNavigation() {
        actualizarMenuNavigation();

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String fragmentTag = null;  // ✅ NUEVO
            String title = "";
            View rootView = AlertManager.getRootView(this);

            int itemId = item.getItemId();

            if (itemId == R.id.nav_calendar) {
                boolean puedeCrear = UserSession.getInstance().puede("crear_actividades");
                boolean puedeEliminar = UserSession.getInstance().puede("eliminar_actividades");
                boolean puedeVerTodas = UserSession.getInstance().puede("ver_todas_actividades");

                Bundle args = new Bundle();
                args.putBoolean("puedeCrear", puedeCrear);
                args.putBoolean("puedeEliminar", puedeEliminar);
                args.putBoolean("puedeVerTodas", puedeVerTodas);

                CalendarioFragment calendarioFragment = new CalendarioFragment();
                calendarioFragment.setArguments(args);
                selectedFragment = calendarioFragment;
                fragmentTag = "CalendarioFragment";  // ✅ IMPORTANTE
                title = "Calendario";

            } else if (itemId == R.id.nav_activities_list) {
                selectedFragment = new ListaActividadesFragment();
                fragmentTag = "ListaActividadesFragment";  // ✅ NUEVO
                title = "Actividades";

            } else if (itemId == R.id.nav_mantenedores) {
                selectedFragment = new MantenedoresFragment();
                fragmentTag = "MantenedoresFragment";  // ✅ NUEVO
                title = "Mantenedores";

                if (!UserSession.getInstance().puede("gestionar_mantenedores")) {
                    AlertManager.showWarningSnackbar(rootView,
                            "⚠️ Solo los administradores pueden gestionar mantenedores");
                    return false;
                }

            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new ConfiguracionFragment();
                fragmentTag = "ConfiguracionFragment";  // ✅ NUEVO
                title = "Configuración";

                if (!UserSession.getInstance().puede("gestionar_mantenedores")) {
                    AlertManager.showWarningSnackbar(rootView,
                            "⚠️ Configuración avanzada solo para administradores");
                }
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title, fragmentTag);  // ✅ ACTUALIZADO
            }
            return true;
        });
    }

    // ✅ ACTUALIZADO: Ahora acepta el parámetro tag
    private void loadFragment(Fragment fragment, String title, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment, tag)  // ✅ TAG AGREGADO
                .commit();

        if (toolbar != null) toolbar.setTitle(title);

        Log.d(TAG, "🔄 Fragment cargado: " + title + " con tag: " + tag);
    }

    // ✅ ACTUALIZADO: Ahora usa tag
    public void navigateToMantenedores() {
        loadFragment(new MantenedoresFragment(), "Mantenedores", "MantenedoresFragment");
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

    // ✅ CORREGIDO: Método cerrarSesion sin usar clear()
    public void cerrarSesion() {
        View rootView = AlertManager.getRootView(this);
        Log.d(TAG, "🚪 Cerrando sesión desde MainActivity");

        // Limpiar sesión local
        prefsManager.clearSession();

        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut();

        // Cancelar suscripciones FCM
        if (fcmTokenManager != null) {
            fcmTokenManager.eliminarToken();
        }

        AlertManager.showInfoSnackbar(rootView, "Sesión cerrada correctamente 👋");

        // Redirigir al login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBadges();
        actualizarMenuNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity destruida");
    }
}