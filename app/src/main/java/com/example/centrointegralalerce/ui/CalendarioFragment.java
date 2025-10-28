package com.example.centrointegralalerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.example.centrointegralalerce.data.CitaFirebase;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment del calendario con diseño profesional Santo Tomás
 */
public class CalendarioFragment extends Fragment {

    private static final String TAG = "CalendarioFragment";

    // Vistas principales
    private ViewPager2 viewPagerCalendar;
    private TextView tvCurrentWeek, tvCurrentYear, tvSubtitleMes;
    private MaterialButton btnPrevWeek, btnNextWeek, btnCreateFirstActivity;
    private ExtendedFloatingActionButton fabNewActivity;
    private View layoutEmptyCalendar; // ⚠️ CAMBIO: De LinearLayout a View
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    // Datos
    private List<Cita> allCitas;
    private Calendar currentWeekStart;
    private CalendarPagerAdapter pagerAdapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Activity Result para crear actividades
    private final ActivityResultLauncher<Intent> crearActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            reloadCitas();
                            showSuccess("✅ Actividad creada exitosamente");
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        try {
            // Inicializar Firebase
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Inicializar vistas
            initializeViews(view);

            allCitas = new ArrayList<>();

            // Configurar calendario
            currentWeekStart = Calendar.getInstance();
            setWeekToMonday(currentWeekStart);

            // Configurar listeners
            setupListeners();

            // Verificar autenticación
            verificarAutenticacion();

            // Cargar citas
            loadCitasFromFirebase();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error crítico en onCreateView", e);
            showError("Error al inicializar el calendario");
        }

        return view;
    }

    private void initializeViews(View view) {
        viewPagerCalendar = view.findViewById(R.id.viewpager_calendar);
        tvCurrentWeek = view.findViewById(R.id.tv_current_week);
        tvCurrentYear = view.findViewById(R.id.tv_current_year);
        tvSubtitleMes = view.findViewById(R.id.tv_subtitle_mes);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        btnCreateFirstActivity = view.findViewById(R.id.btn_create_first_activity);
        fabNewActivity = view.findViewById(R.id.fab_new_activity);

        // ⚠️ CORRECCIÓN: layoutEmptyCalendar es MaterialCardView, NO LinearLayout
        layoutEmptyCalendar = view.findViewById(R.id.layout_empty_calendar);

        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Configurar SwipeRefreshLayout
        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(
                    R.color.verde_santo_tomas,
                    R.color.verde_secundario,
                    R.color.verde_exito
            );
            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.blanco);
        }

        Log.d(TAG, "✅ Vistas inicializadas correctamente");
    }

    private void verificarAutenticacion() {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            Log.e(TAG, "❌ Usuario NO autenticado");
            showError("Debes iniciar sesión para ver las actividades");
            setupViewPager();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        Log.d(TAG, "========================================");
        Log.d(TAG, "✅ USUARIO AUTENTICADO");
        Log.d(TAG, "UID: " + uid);
        Log.d(TAG, "Email: " + (email != null ? email : "sin email"));
        Log.d(TAG, "========================================");
    }

    private void loadCitasFromFirebase() {
        Log.d(TAG, "🔄 Cargando actividades desde Firebase...");

        if (db == null) {
            showError("Error de configuración de Firebase");
            setupViewPager();
            return;
        }

        showLoading(true);
        allCitas.clear();

        db.collection("actividades")
                .get()
                .addOnSuccessListener(actividadesSnapshot -> {
                    if (actividadesSnapshot.isEmpty()) {
                        showLoading(false);
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        setupViewPager();
                        showInfo("No hay actividades registradas");
                        return;
                    }

                    final int totalActividades = actividadesSnapshot.size();
                    final int[] procesadas = {0};

                    for (QueryDocumentSnapshot actividadDoc : actividadesSnapshot) {
                        String actividadId = actividadDoc.getId();
                        String actividadNombre = actividadDoc.getString("nombre");
                        String tipoActividadId = actividadDoc.getString("tipoActividadId");
                        String estadoActividad = actividadDoc.getString("estado");

                        db.collection("actividades").document(actividadId)
                                .collection("citas")
                                .get()
                                .addOnSuccessListener(citasSnapshot -> {
                                    citasSnapshot.forEach(citaDoc -> {
                                        try {
                                            CitaFirebase cf = citaDoc.toObject(CitaFirebase.class);
                                            if (cf == null) return;

                                            cf.setActividadId(actividadId);
                                            cf.setActividadNombre(actividadNombre);
                                            cf.setTipoActividadId(tipoActividadId);
                                            cf.setEstadoActividad(estadoActividad);

                                            Cita c = cf.toCita();
                                            if (c != null) {
                                                allCitas.add(c);
                                            }
                                        } catch (Exception ex) {
                                            Log.e(TAG, "Error mapeando cita", ex);
                                        }
                                    });

                                    procesadas[0]++;
                                    if (procesadas[0] == totalActividades) {
                                        finalizarCargaYActualizarUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error leyendo citas de actividad " + actividadId, e);
                                    procesadas[0]++;
                                    if (procesadas[0] == totalActividades) {
                                        finalizarCargaYActualizarUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error listando actividades", e);
                    showError("Error al cargar actividades");
                    showLoading(false);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    setupViewPager();
                });
    }

    private void finalizarCargaYActualizarUI() {
        Log.d(TAG, "✅ Total citas cargadas: " + allCitas.size());

        setupViewPager();
        updateWeekLabel(currentWeekStart);
        updateMonthSubtitle(currentWeekStart);
        checkIfWeekHasCitas(currentWeekStart);
        showLoading(false);

        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }

        if (allCitas.isEmpty()) {
            showInfo("No hay citas registradas");
        } else {
            showSuccess("✅ " + allCitas.size() + " citas cargadas");
        }
    }

    private void setupViewPager() {
        try {
            if (viewPagerCalendar == null) {
                Log.e(TAG, "❌ ViewPager es null");
                return;
            }

            pagerAdapter = new CalendarPagerAdapter(allCitas, getParentFragmentManager(), currentWeekStart);
            viewPagerCalendar.setAdapter(pagerAdapter);
            viewPagerCalendar.setCurrentItem(pagerAdapter.getMiddlePosition(), false);

            viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    try {
                        Calendar weekStart = pagerAdapter.getWeekForPosition(position);
                        currentWeekStart = weekStart;

                        updateWeekLabel(weekStart);
                        updateMonthSubtitle(weekStart);
                        checkIfWeekHasCitas(weekStart);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al cambiar de página", e);
                    }
                }
            });

            Log.d(TAG, "✅ ViewPager configurado");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando ViewPager", e);
            showError("Error al configurar el calendario");
        }
    }

    private void setupListeners() {
        try {
            // Botón semana anterior
            if (btnPrevWeek != null) {
                btnPrevWeek.setOnClickListener(v -> {
                    try {
                        int currentItem = viewPagerCalendar.getCurrentItem();
                        if (currentItem > 0) {
                            viewPagerCalendar.setCurrentItem(currentItem - 1, true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error navegando a semana anterior", e);
                    }
                });
            }

            // Botón semana siguiente
            if (btnNextWeek != null) {
                btnNextWeek.setOnClickListener(v -> {
                    try {
                        int currentItem = viewPagerCalendar.getCurrentItem();
                        if (pagerAdapter != null && currentItem < pagerAdapter.getItemCount() - 1) {
                            viewPagerCalendar.setCurrentItem(currentItem + 1, true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error navegando a semana siguiente", e);
                    }
                });
            }

            // FAB Nueva actividad
            if (fabNewActivity != null) {
                fabNewActivity.setOnClickListener(v -> openCreateActivity());
            }

            // Botón crear primera actividad (empty state)
            if (btnCreateFirstActivity != null) {
                btnCreateFirstActivity.setOnClickListener(v -> openCreateActivity());
            }

            // Pull to refresh
            if (swipeRefresh != null) {
                swipeRefresh.setOnRefreshListener(() -> {
                    Log.d(TAG, "🔄 Pull to refresh activado");
                    reloadCitas();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error configurando listeners", e);
        }
    }

    /**
     * Abre la actividad para crear nueva actividad
     */
    private void openCreateActivity() {
        try {
            Intent intent = new Intent(requireContext(), AgregarActividadActivity.class);

            long startOfWeekMillis = (currentWeekStart != null)
                    ? currentWeekStart.getTimeInMillis()
                    : System.currentTimeMillis();

            intent.putExtra("startOfWeekMillis", startOfWeekMillis);
            intent.putExtra("suggestedTimeMillis", System.currentTimeMillis());

            crearActividadLauncher.launch(intent);

        } catch (Exception e) {
            Log.e(TAG, "Error abriendo crear actividad", e);
            showError("Error al abrir formulario de actividad");
        }
    }

    /**
     * Ajusta el calendario al lunes de la semana
     */
    private void setWeekToMonday(Calendar cal) {
        try {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);

            int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (currentDayOfWeek == Calendar.SUNDAY) ? 6 : currentDayOfWeek - Calendar.MONDAY;

            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

        } catch (Exception e) {
            Log.e(TAG, "Error ajustando calendario a lunes", e);
        }
    }

    /**
     * Actualiza el label de la semana actual (formato mejorado)
     */
    private void updateWeekLabel(Calendar weekStart) {
        if (tvCurrentWeek == null || weekStart == null) return;

        try {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", new Locale("es", "ES"));
            SimpleDateFormat monthShortFormat = new SimpleDateFormat("MMM", new Locale("es", "ES"));

            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_MONTH, 6);

            int startDay = weekStart.get(Calendar.DAY_OF_MONTH);
            int endDay = weekEnd.get(Calendar.DAY_OF_MONTH);
            int startMonth = weekStart.get(Calendar.MONTH);
            int endMonth = weekEnd.get(Calendar.MONTH);
            int year = weekEnd.get(Calendar.YEAR);

            String labelText;
            if (startMonth != endMonth) {
                String startMonthName = monthShortFormat.format(weekStart.getTime()).toUpperCase();
                String endMonthName = monthShortFormat.format(weekEnd.getTime()).toUpperCase();
                labelText = String.format(Locale.getDefault(),
                        "%d %s - %d %s",
                        startDay, startMonthName, endDay, endMonthName);
            } else {
                String monthName = monthFormat.format(weekStart.getTime()).toUpperCase();
                labelText = String.format(Locale.getDefault(),
                        "%d - %d %s",
                        startDay, endDay, monthName);
            }

            tvCurrentWeek.setText(labelText);

            // Actualizar año
            if (tvCurrentYear != null) {
                tvCurrentYear.setText(String.valueOf(year));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error actualizando label de semana", e);
            tvCurrentWeek.setText("Semana actual");
        }
    }

    /**
     * Actualiza el subtítulo con el mes actual
     */
    private void updateMonthSubtitle(Calendar weekStart) {
        if (tvSubtitleMes == null || weekStart == null) return;

        try {
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
            String subtitle = monthYearFormat.format(weekStart.getTime());
            // Capitalizar primera letra
            subtitle = subtitle.substring(0, 1).toUpperCase() + subtitle.substring(1);
            tvSubtitleMes.setText(subtitle);
        } catch (Exception e) {
            Log.e(TAG, "Error actualizando subtítulo de mes", e);
        }
    }

    /**
     * Verifica si la semana tiene citas
     */
    private void checkIfWeekHasCitas(Calendar weekStart) {
        if (layoutEmptyCalendar == null || weekStart == null) return;

        try {
            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_MONTH, 6);
            weekEnd.set(Calendar.HOUR_OF_DAY, 23);
            weekEnd.set(Calendar.MINUTE, 59);
            weekEnd.set(Calendar.SECOND, 59);

            boolean hasCitas = false;
            for (Cita cita : allCitas) {
                if (cita == null || cita.getFecha() == null) continue;

                long t = cita.getFecha().getTime();
                if (t >= weekStart.getTimeInMillis() && t <= weekEnd.getTimeInMillis()) {
                    hasCitas = true;
                    break;
                }
            }

            layoutEmptyCalendar.setVisibility(hasCitas ? View.GONE : View.VISIBLE);

        } catch (Exception e) {
            Log.e(TAG, "Error verificando citas de la semana", e);
        }
    }

    /**
     * Recarga las citas desde Firebase
     */
    public void reloadCitas() {
        Log.d(TAG, "🔄 Recargando citas...");
        loadCitasFromFirebase();
    }

    // ============================================
    // MÉTODOS AUXILIARES DE UI
    // ============================================

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        Log.e(TAG, "❌ ERROR: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showInfo(String message) {
        Log.i(TAG, "ℹ️ INFO: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccess(String message) {
        Log.i(TAG, "✅ SUCCESS: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar citas al volver al fragment
        if (allCitas != null && pagerAdapter != null) {
            Log.d(TAG, "📱 Fragment resumido - actualizando vista");
        }
    }
}