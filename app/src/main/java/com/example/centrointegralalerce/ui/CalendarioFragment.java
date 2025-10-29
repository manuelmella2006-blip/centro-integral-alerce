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
import com.example.centrointegralalerce.utils.AlertManager; // ✅ Import AlertManager
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
 * Fragment del calendario con AlertManager integrado
 */
public class CalendarioFragment extends Fragment {

    private static final String TAG = "CalendarioFragment";

    // Vistas principales
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

    // Activity Result para crear actividades
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
            AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                    "Error de configuración de Firebase");
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
                        AlertManager.showInfoSnackbar(AlertManager.getRootView(requireActivity()),
                                "No hay actividades registradas");
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
                                    procesadas[0]++;
                                    if (procesadas[0] == totalActividades) {
                                        finalizarCargaYActualizarUI();
                                    }
                                    AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                                            "Error al cargar citas de algunas actividades");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    setupViewPager();
                    AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                            "Error al obtener las actividades");
                });
    }

    private void finalizarCargaYActualizarUI() {
        setupViewPager();
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
            if (viewPagerCalendar == null) return;

            pagerAdapter = new CalendarPagerAdapter(allCitas, getParentFragmentManager(), currentWeekStart);
            viewPagerCalendar.setAdapter(pagerAdapter);
            viewPagerCalendar.setCurrentItem(pagerAdapter.getMiddlePosition(), false);

            viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    Calendar weekStart = pagerAdapter.getWeekForPosition(position);
                    currentWeekStart = weekStart;
                    updateWeekLabel(weekStart);
                    updateMonthSubtitle(weekStart);
                    checkIfWeekHasCitas(weekStart);
                }
            });

        } catch (Exception e) {
            AlertManager.showErrorSnackbar(AlertManager.getRootView(requireActivity()),
                    "Error al configurar el calendario");
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
        loadCitasFromFirebase();
    }

    private void showLoading(boolean show) {
        if (progressBar != null)
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}