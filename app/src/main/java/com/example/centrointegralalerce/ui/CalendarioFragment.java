package com.example.centrointegralalerce.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.example.centrointegralalerce.data.UserSession;
import com.example.centrointegralalerce.utils.AlertManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment del calendario con AlertManager integrado + control de permisos por rol (UserSession)
 */
public class CalendarioFragment extends Fragment {

    private static final String TAG = "CalendarioFragment";

    // Vistas
    private ViewPager2 viewPagerCalendar;
    private TextView tvCurrentWeek, tvCurrentYear, tvSubtitleMes;
    private MaterialButton btnPrevWeek, btnNextWeek, btnCreateFirstActivity;
    private ExtendedFloatingActionButton fabNewActivity;
    private View layoutEmptyCalendar;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;

    // Datos
    private List<Cita> allCitas;
    private Calendar currentWeekStart;
    private CalendarPagerAdapter pagerAdapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Activity Result
    private final ActivityResultLauncher<Intent> crearActividadLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            reloadCitas();
                            AlertManager.showSuccessSnackbar(AlertManager.getRootView(requireActivity()),
                                    "✅ Actividad creada exitosamente");
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario, container, false);

        try {
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            initializeViews(view);

            UserSession session = UserSession.getInstance();
            boolean puedeCrear = session.puede("crear_actividades");
            boolean puedeEliminar = session.puede("eliminar_actividades");

            Log.d(TAG, "🟢 Rol actual: " + session.getRolId());
            Log.d(TAG, "🟩 Permisos cargados: " + session.getPermisos());
            Log.d(TAG, "🔍 puedeCrear = " + puedeCrear + " | puedeEliminar = " + puedeEliminar);

            if (!puedeCrear) {
                if (fabNewActivity != null) fabNewActivity.setVisibility(View.GONE);
                if (btnCreateFirstActivity != null) btnCreateFirstActivity.setVisibility(View.GONE);
                Log.w(TAG, "🚫 Usuario sin permiso para crear actividades");
            }

            if (!puedeEliminar) {
                Log.w(TAG, "🚫 Usuario sin permiso para eliminar actividades");
            }

            allCitas = new ArrayList<>();
            currentWeekStart = Calendar.getInstance();
            setWeekToMonday(currentWeekStart);
            setupListeners();

            verificarAutenticacion();
            loadCitasFromFirebase();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error crítico en onCreateView", e);
            AlertManager.showErrorSnackbar(view, "Error al inicializar el calendario");
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
        layoutEmptyCalendar = view.findViewById(R.id.layout_empty_calendar);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(
                    R.color.verde_santo_tomas,
                    R.color.verde_secundario,
                    R.color.verde_exito
            );
            swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.blanco);
        }
    }

    private void verificarAutenticacion() {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            AlertManager.showWarningSnackbar(AlertManager.getRootView(requireActivity()),
                    "⚠️ Debes iniciar sesión para ver las actividades");
            setupViewPager();
            return;
        }
    }

    private void loadCitasFromFirebase() {
        if (db == null) {
            if (isAdded() && getActivity() != null) {
                AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                        "Error de configuración de Firebase");
                setupViewPager();
            }
            return;
        }

        showLoading(true);
        allCitas.clear();

        db.collection("actividades")
                .whereNotEqualTo("estado", "cancelada") // <-- NUEVO: solo actividades activas
                .get()
                .addOnSuccessListener(actividadesSnapshot -> {
                    if (!isAdded() || getActivity() == null) {
                        showLoading(false);
                        return;
                    }

                    if (actividadesSnapshot.isEmpty()) {
                        showLoading(false);
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        setupViewPager();
                        AlertManager.showInfoSnackbar(AlertManager.getRootView(requireActivity()),
                                "No hay actividades registradas");
                        return;
                    }

                    final int totalActividades = actividadesSnapshot.size();
                    final int[] procesadas = {0};
                    allCitas.clear();

                    for (QueryDocumentSnapshot actividadDoc : actividadesSnapshot) {
                        String actividadId = actividadDoc.getId();

                        String actividadNombre = actividadDoc.getString("nombre");
                        String tipoActividadId = actividadDoc.getString("tipoActividadId");
                        String estadoActividad = actividadDoc.getString("estado");
                        String lugarId = actividadDoc.getString("lugarId");
                        String fechaInicio = actividadDoc.getString("fechaInicio");
                        String fechaTermino = actividadDoc.getString("fechaTermino");
                        String horaInicio = actividadDoc.getString("horaInicio");
                        String horaTermino = actividadDoc.getString("horaTermino");
                        String periodicidad = actividadDoc.getString("periodicidad");
                        String oferenteId = actividadDoc.getString("oferenteId");
                        String proyectoId = actividadDoc.getString("proyectoId");
                        String socioComunitarioId = actividadDoc.getString("socioComunitarioId");
                        Long cupo = actividadDoc.getLong("cupo");
                        Long diasAvisoPrevio = actividadDoc.getLong("diasAvisoPrevio");

                        db.collection("actividades").document(actividadId)
                                .collection("citas")
                                .get()
                                .addOnSuccessListener(citasSnapshot -> {
                                    if (!isAdded() || getActivity() == null) {
                                        return;
                                    }

                                    for (QueryDocumentSnapshot citaDoc : citasSnapshot) {
                                        try {
                                            String citaId = citaDoc.getId();
                                            String estadoCita = citaDoc.getString("estado");
                                            String fechaCita = citaDoc.getString("fecha");
                                            String horaCita = citaDoc.getString("hora");

                                            CitaFirebase cf = new CitaFirebase();
                                            cf.setId(citaId);
                                            cf.setEstado(estadoCita);
                                            cf.setFechaString(fechaCita);
                                            cf.setHora(horaCita);

                                            cf.setActividadId(actividadId);
                                            cf.setActividadNombre(actividadNombre);
                                            cf.setTipoActividadId(tipoActividadId);
                                            cf.setEstadoActividad(estadoActividad);
                                            cf.setLugarId(lugarId);
                                            cf.setFechaInicio(fechaInicio);
                                            cf.setFechaTermino(fechaTermino);
                                            cf.setHoraInicio(horaInicio);
                                            cf.setHoraTermino(horaTermino);
                                            cf.setPeriodicidad(periodicidad);
                                            cf.setOferenteId(oferenteId);
                                            cf.setProyectoId(proyectoId);
                                            cf.setSocioComunitarioId(socioComunitarioId);
                                            cf.setCupo(cupo != null ? cupo.intValue() : 0);
                                            cf.setDiasAvisoPrevio(diasAvisoPrevio != null ? diasAvisoPrevio.intValue() : 0);

                                            Cita c = cf.toCita();
                                            if (c != null) {
                                                allCitas.add(c);
                                                Log.d(TAG, "✅ Cita agregada: " + c.getActividadNombre() + " - " + c.getFecha() + " " + c.getHora());
                                            }
                                        } catch (Exception ex) {
                                            Log.e(TAG, "❌ Error mapeando cita", ex);
                                        }
                                    }

                                    procesadas[0]++;
                                    Log.d(TAG, "📊 Procesadas " + procesadas[0] + "/" + totalActividades + " actividades");

                                    if (procesadas[0] == totalActividades) {
                                        Log.d(TAG, "✅ Carga completada. Total citas: " + allCitas.size());
                                        finalizarCargaYActualizarUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!isAdded() || getActivity() == null) {
                                        return;
                                    }
                                    Log.e(TAG, "❌ Error al cargar citas de actividad " + actividadId, e);
                                    procesadas[0]++;
                                    if (procesadas[0] == totalActividades) {
                                        finalizarCargaYActualizarUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getActivity() == null) {
                        showLoading(false);
                        return;
                    }

                    showLoading(false);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    setupViewPager();
                    AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                            "Error al obtener las actividades: " + e.getMessage());
                    Log.e(TAG, "❌ Error en loadCitasFromFirebase", e);
                });
    }


    private void finalizarCargaYActualizarUI() {
        if (!isAdded() || getActivity() == null) {
            showLoading(false);
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        setupViewPager();

        // 🎯 DEBUG: Verificar datos de citas y calendario antes de actualizar la UI
        debugCitasYCalendario();

        updateWeekLabel(currentWeekStart);
        updateMonthSubtitle(currentWeekStart);
        checkIfWeekHasCitas(currentWeekStart);
        showLoading(false);

        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

        if (allCitas.isEmpty()) {
            AlertManager.showInfoSnackbar(AlertManager.getRootView(requireActivity()),
                    "No hay citas registradas");
        } else {
            AlertManager.showSuccessSnackbar(AlertManager.getRootView(requireActivity()),
                    "✅ " + allCitas.size() + " citas cargadas correctamente");
        }
    }

    private void setupViewPager() {
        try {
            if (!isAdded() || getActivity() == null || viewPagerCalendar == null) {
                return;
            }

            // 🔹 Normalizar la semana al lunes antes de crear el adaptador
            normalizeToMonday(currentWeekStart);

            // 🔹 Inicializar correctamente el adaptador
            pagerAdapter = new CalendarPagerAdapter(allCitas, getChildFragmentManager(), currentWeekStart);
            viewPagerCalendar.setAdapter(pagerAdapter);
            viewPagerCalendar.setCurrentItem(pagerAdapter.getMiddlePosition(), false);

            viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (!isAdded() || getActivity() == null) return;

                    Calendar weekStart = pagerAdapter.getWeekForPosition(position);
                    currentWeekStart = weekStart;
                    updateWeekLabel(weekStart);
                    updateMonthSubtitle(weekStart);
                    checkIfWeekHasCitas(weekStart);
                }
            });

        } catch (Exception e) {
            if (isAdded() && getActivity() != null) {
                AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                        "Error al configurar el calendario");
            }
            Log.e(TAG, "❌ Error en setupViewPager", e);
        }
    }

    private void setupListeners() {
        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                int currentItem = viewPagerCalendar.getCurrentItem();
                if (currentItem > 0)
                    viewPagerCalendar.setCurrentItem(currentItem - 1, true);
            });
        }

        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                int currentItem = viewPagerCalendar.getCurrentItem();
                if (pagerAdapter != null && currentItem < pagerAdapter.getItemCount() - 1)
                    viewPagerCalendar.setCurrentItem(currentItem + 1, true);
            });
        }

        if (fabNewActivity != null) {
            fabNewActivity.setOnClickListener(v -> openCreateActivity());
        }

        if (btnCreateFirstActivity != null) {
            btnCreateFirstActivity.setOnClickListener(v -> openCreateActivity());
        }

        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                AlertManager.showInfoToast(requireContext(), "Recargando citas...");
                reloadCitas();
            });
        }
    }

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
            AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                    "Error al abrir formulario de actividad");
        }
    }

    private void setWeekToMonday(Calendar cal) {
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (currentDayOfWeek == Calendar.SUNDAY) ? 6 : currentDayOfWeek - Calendar.MONDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
    }
    private void normalizeToMonday(Calendar cal) {
        if (cal == null) return;
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        cal.add(Calendar.DAY_OF_MONTH, diff);
    }
    private void debugCitasYCalendario() {
        Log.d(TAG, "=== 🎯 DEBUG CITAS vs CALENDARIO ===");

        if (currentWeekStart == null) {
            Log.e(TAG, "❌ currentWeekStart es null");
            return;
        }

        Calendar weekEnd = (Calendar) currentWeekStart.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm", new Locale("es", "ES"));

        Log.d(TAG, "📅 Semana mostrada: " + sdf.format(currentWeekStart.getTime()) +
                " - " + sdf.format(weekEnd.getTime()));

        Log.d(TAG, "📊 Total de citas cargadas: " + allCitas.size());

        for (int i = 0; i < allCitas.size(); i++) {
            Cita cita = allCitas.get(i);
            if (cita != null && cita.getFecha() != null) {
                String fechaCita = sdf.format(cita.getFecha());
                String actividad = cita.getActividadNombre() != null ?
                        cita.getActividadNombre() : "Sin nombre";
                Log.d(TAG, String.format("Cita %d: %s | %s | %s",
                        i, fechaCita, actividad, cita.getHora()));
            }
        }

        // Verificar qué citas deberían mostrarse en esta semana
        List<Cita> citasEstaSemana = new ArrayList<>();
        for (Cita cita : allCitas) {
            if (cita != null && cita.getFecha() != null) {
                long fechaCita = cita.getFecha().getTime();
                if (fechaCita >= currentWeekStart.getTimeInMillis() &&
                        fechaCita <= weekEnd.getTimeInMillis()) {
                    citasEstaSemana.add(cita);
                }
            }
        }

        Log.d(TAG, "🎯 CITAS QUE DEBERÍAN MOSTRARSE ESTA SEMANA: " + citasEstaSemana.size());
        for (Cita cita : citasEstaSemana) {
            String fechaCita = sdf.format(cita.getFecha());
            Log.d(TAG, "👉 " + fechaCita + " - " + cita.getActividadNombre());
        }
    }
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
            String labelText = (startMonth != endMonth)
                    ? String.format("%d %s - %d %s", startDay, monthShortFormat.format(weekStart.getTime()).toUpperCase(),
                    endDay, monthShortFormat.format(weekEnd.getTime()).toUpperCase())
                    : String.format("%d - %d %s", startDay, endDay, monthFormat.format(weekStart.getTime()).toUpperCase());
            tvCurrentWeek.setText(labelText);
            tvCurrentYear.setText(String.valueOf(year));
        } catch (Exception e) {
            tvCurrentWeek.setText("Semana actual");
        }
    }

    private void updateMonthSubtitle(Calendar weekStart) {
        if (tvSubtitleMes == null || weekStart == null) return;
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        String subtitle = monthYearFormat.format(weekStart.getTime());
        subtitle = subtitle.substring(0, 1).toUpperCase() + subtitle.substring(1);
        tvSubtitleMes.setText(subtitle);
    }

    private void checkIfWeekHasCitas(Calendar weekStart) {
        if (layoutEmptyCalendar == null || weekStart == null) return;
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_MONTH, 6);
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
    }

    public void reloadCitas() {
        Log.d(TAG, "🔄 Recargando citas manualmente...");

        // Forzar destrucción del adapter y limpieza para evitar datos cacheados
        if (pagerAdapter != null) {
            pagerAdapter = null;
        }

        if (viewPagerCalendar != null) {
            viewPagerCalendar.setAdapter(null);
        }

        if (allCitas != null) {
            allCitas.clear();
        } else {
            allCitas = new ArrayList<>();
        }

        loadCitasFromFirebase();
    }


    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPagerCalendar != null) {
            viewPagerCalendar.setAdapter(null);
        }
    }
}
