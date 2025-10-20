package com.example.centrointegralalerce.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.centrointegralalerce.R;
import com.example.centrointegralalerce.data.Cita;
import com.example.centrointegralalerce.data.CitaFirebase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarioFragment extends Fragment {

    private static final String TAG = "CalendarioFragment";
    private static final int FIREBASE_TIMEOUT_MS = 10000; // 10 segundos

    private ViewPager2 viewPagerCalendar;
    private TextView tvCurrentWeek;
    private View btnPrevWeek, btnNextWeek;
    private FloatingActionButton fabNewActivity;
    private LinearLayout layoutEmptyCalendar;
    private ProgressBar progressBar;

    private List<Cita> allCitas;
    private Calendar currentWeekStart;
    private CalendarPagerAdapter pagerAdapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

            // Inicializar el calendario en la semana actual
            currentWeekStart = Calendar.getInstance();
            setWeekToMonday(currentWeekStart);

            setupListeners();

            // Verificar autenticación antes de cargar
            verificarAutenticacion();

            // Cargar citas desde Firebase
            loadCitasFromFirebase();

        } catch (Exception e) {
            Log.e(TAG, "Error crítico en onCreateView: " + e.getMessage(), e);
            showError("Error al inicializar el calendario");
        }

        return view;
    }

    /**
     * Inicializa todas las vistas con validación
     */
    private void initializeViews(View view) {
        viewPagerCalendar = view.findViewById(R.id.viewpager_calendar);
        tvCurrentWeek = view.findViewById(R.id.tv_current_week);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        fabNewActivity = view.findViewById(R.id.fab_new_activity);
        layoutEmptyCalendar = view.findViewById(R.id.layout_empty_calendar);
        progressBar = view.findViewById(R.id.progress_bar);

        // Validar vistas críticas
        if (viewPagerCalendar == null) {
            Log.e(TAG, "❌ viewPagerCalendar es null - verifica el ID en el XML");
        }
        if (tvCurrentWeek == null) {
            Log.e(TAG, "❌ tvCurrentWeek es null - verifica el ID en el XML");
        }
    }

    /**
     * Verifica si el usuario está autenticado
     */
    private void verificarAutenticacion() {
        if (mAuth == null) {
            Log.e(TAG, "❌ FirebaseAuth es null");
            showError("Error de configuración de Firebase Auth");
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "❌ Usuario NO autenticado - no debería llegar aquí después del login");
            showError("Debes iniciar sesión para ver las actividades");
            setupViewPager(); // Configurar calendario vacío
            return;
        }

        // Usuario autenticado correctamente
        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        boolean isAnonymous = mAuth.getCurrentUser().isAnonymous();

        Log.d(TAG, "========================================");
        Log.d(TAG, "✅ USUARIO AUTENTICADO");
        Log.d(TAG, "UID: " + uid);
        Log.d(TAG, "Email: " + (email != null ? email : "sin email (anónimo)"));
        Log.d(TAG, "Es anónimo: " + isAnonymous);
        Log.d(TAG, "========================================");
    }

    /**
     * Carga las citas desde Firestore (ruta: /actividades/{actividadId}/citas/{citaId})
     */
    /**
     * Carga las citas desde Firestore, combinando información de actividades.
     * Estructura: /actividades/{actividadId}/citas/{citaId}
     */
    private void loadCitasFromFirebase() {
        Log.d(TAG, "=== CARGANDO ACTIVIDADES Y SUS CITAS ===");

        if (db == null) {
            Log.e(TAG, "❌ FirebaseFirestore es null");
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
                        Log.w(TAG, "⚠️ No hay actividades registradas en Firestore");
                        showInfo("No hay actividades registradas");
                        showLoading(false);
                        setupViewPager();
                        return;
                    }

                    final int[] actividadesProcesadas = {0};
                    final int totalActividades = actividadesSnapshot.size();
                    final int[] totalCitas = {0};

                    for (QueryDocumentSnapshot actividadDoc : actividadesSnapshot) {
                        try {
                            String actividadId = actividadDoc.getId();
                            String actividadNombre = actividadDoc.getString("nombre");
                            String tipoActividadId = actividadDoc.getString("tipoActividadId");
                            String estadoActividad = actividadDoc.getString("estado");

                            Log.d(TAG, "📘 Actividad: " + actividadNombre + " (" + actividadId + ")");

                            db.collection("actividades")
                                    .document(actividadId)
                                    .collection("citas")
                                    .get()
                                    .addOnSuccessListener(citasSnapshot -> {
                                        if (citasSnapshot.isEmpty()) {
                                            Log.d(TAG, "⚠️ Sin citas para: " + actividadNombre);
                                        }

                                        for (QueryDocumentSnapshot citaDoc : citasSnapshot) {
                                            try {
                                                CitaFirebase citaFirebase = citaDoc.toObject(CitaFirebase.class);
                                                if (citaFirebase == null) continue;

                                                citaFirebase.setId(citaDoc.getId());
                                                citaFirebase.setActividadId(actividadId);
                                                citaFirebase.setActividadNombre(
                                                        actividadNombre != null ? actividadNombre : "Sin nombre"
                                                );
                                                citaFirebase.setTipoActividadId(
                                                        tipoActividadId != null ? tipoActividadId : "Desconocido"
                                                );

                                                // Si la actividad está inactiva, marcar la cita también
                                                if (estadoActividad != null && !estadoActividad.equalsIgnoreCase("activa")) {
                                                    citaFirebase.setEstado("inactiva");
                                                }

                                                Cita cita = citaFirebase.toCita();
                                                if (cita != null && cita.esValida()) {
                                                    allCitas.add(cita);
                                                    totalCitas[0]++;
                                                    Log.d(TAG, "✅ Cita añadida: " + cita.getActividad() +
                                                            " | Hora: " + cita.getHora() +
                                                            " | Día: " + cita.getDiaSemana());
                                                } else {
                                                    Log.w(TAG, "⚠️ Cita inválida: " + citaDoc.getId());
                                                }

                                            } catch (Exception e) {
                                                Log.e(TAG, "❌ Error al convertir cita: " + e.getMessage(), e);
                                            }
                                        }

                                        actividadesProcesadas[0]++;
                                        if (actividadesProcesadas[0] == totalActividades) {
                                            Log.d(TAG, "=== CARGA COMPLETA ===");
                                            Log.d(TAG, "Total citas cargadas: " + totalCitas[0]);

                                            setupViewPager();
                                            updateWeekLabel(currentWeekStart);
                                            checkIfWeekHasCitas(currentWeekStart);

                                            showLoading(false);
                                            if (allCitas.isEmpty()) {
                                                showInfo("No hay citas registradas");
                                            } else {
                                                showSuccess("Se cargaron " + totalCitas[0] + " citas de actividades");
                                            }
                                        }

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "❌ Error al cargar citas de " + actividadNombre + ": " + e.getMessage());
                                        actividadesProcesadas[0]++;
                                    });

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error al procesar actividad: " + e.getMessage(), e);
                            actividadesProcesadas[0]++;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error al obtener actividades: " + e.getMessage(), e);
                    showError("Error al cargar actividades");
                    showLoading(false);
                    setupViewPager();
                });
    }

    /**
     * Opcional: Cargar solo las citas del usuario actual
     */
    private void loadCitasFromFirebaseForCurrentUser() {
        if (mAuth == null || mAuth.getCurrentUser() == null) {
            Log.w(TAG, "⚠️ Usuario no autenticado");
            showInfo("Debes iniciar sesión para ver tus actividades");
            setupViewPager();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Cargando citas para usuario: " + userId);

        showLoading(true);

        db.collection("citas")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        allCitas.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Cita cita = document.toObject(Cita.class);
                                if (cita != null && cita.esValida()) {
                                    allCitas.add(cita);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear cita: " + e.getMessage());
                            }
                        }

                        Log.d(TAG, "Citas del usuario cargadas: " + allCitas.size());

                        setupViewPager();
                        updateWeekLabel(currentWeekStart);
                        checkIfWeekHasCitas(currentWeekStart);
                        showLoading(false);

                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar citas del usuario: " + e.getMessage(), e);
                        showError("Error al procesar las actividades");
                        showLoading(false);
                        setupViewPager();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar citas del usuario: " + e.getMessage(), e);
                    showError("Error al cargar tus actividades");
                    showLoading(false);
                    setupViewPager();
                });
    }

    private void setupViewPager() {
        try {
            if (viewPagerCalendar == null) {
                Log.e(TAG, "❌ No se puede configurar ViewPager - vista es null");
                return;
            }

            // Crear el adaptador
            pagerAdapter = new CalendarPagerAdapter(allCitas, getParentFragmentManager(), currentWeekStart);
            viewPagerCalendar.setAdapter(pagerAdapter);

            // Empezar en la posición central (semana actual)
            viewPagerCalendar.setCurrentItem(pagerAdapter.getMiddlePosition(), false);

            // Listener para detectar cambios de página
            viewPagerCalendar.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    try {
                        // Obtener la semana correspondiente a esta posición
                        Calendar weekStart = pagerAdapter.getWeekForPosition(position);
                        currentWeekStart = weekStart;

                        // Actualizar UI
                        updateWeekLabel(weekStart);
                        checkIfWeekHasCitas(weekStart);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al cambiar de página: " + e.getMessage(), e);
                    }
                }
            });

            Log.d(TAG, "✅ ViewPager configurado correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al configurar ViewPager: " + e.getMessage(), e);
            showError("Error al configurar el calendario");
        }
    }

    private void setupListeners() {
        try {
            if (btnPrevWeek != null) {
                btnPrevWeek.setOnClickListener(v -> {
                    try {
                        int currentItem = viewPagerCalendar.getCurrentItem();
                        if (currentItem > 0) {
                            viewPagerCalendar.setCurrentItem(currentItem - 1, true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al navegar a semana anterior: " + e.getMessage(), e);
                    }
                });
            }

            if (btnNextWeek != null) {
                btnNextWeek.setOnClickListener(v -> {
                    try {
                        int currentItem = viewPagerCalendar.getCurrentItem();
                        if (pagerAdapter != null && currentItem < pagerAdapter.getItemCount() - 1) {
                            viewPagerCalendar.setCurrentItem(currentItem + 1, true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al navegar a semana siguiente: " + e.getMessage(), e);
                    }
                });
            }

            if (fabNewActivity != null) {
                fabNewActivity.setOnClickListener(v -> {
                    // TODO: Navegar a crear actividad
                    showInfo("Crear nueva actividad");
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error al configurar listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Ajusta el calendario al lunes de la semana actual
     */
    private void setWeekToMonday(Calendar cal) {
        try {
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setMinimalDaysInFirstWeek(4);

            int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday;

            if (currentDayOfWeek == Calendar.SUNDAY) {
                daysFromMonday = 6;
            } else {
                daysFromMonday = currentDayOfWeek - Calendar.MONDAY;
            }

            cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

        } catch (Exception e) {
            Log.e(TAG, "Error al ajustar calendario a lunes: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el texto que muestra la semana actual
     */
    private void updateWeekLabel(Calendar weekStart) {
        if (tvCurrentWeek == null || weekStart == null) {
            Log.w(TAG, "No se puede actualizar label de semana - vista o calendario null");
            return;
        }

        try {
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", new Locale("es", "ES"));
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
                String startMonthName = monthShortFormat.format(weekStart.getTime());
                String endMonthName = monthFormat.format(weekEnd.getTime());
                labelText = String.format(Locale.getDefault(),
                        "%d de %s - %d de %s %d",
                        startDay, startMonthName, endDay, endMonthName, year);
            } else {
                String monthName = monthFormat.format(weekStart.getTime());
                labelText = String.format(Locale.getDefault(),
                        "%d - %d de %s %d",
                        startDay, endDay, monthName, year);
            }

            tvCurrentWeek.setText(labelText);

        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar label de semana: " + e.getMessage(), e);
            tvCurrentWeek.setText("Semana actual");
        }
    }

    /**
     * Verifica si la semana tiene citas
     */
    private void checkIfWeekHasCitas(Calendar weekStart) {
        if (layoutEmptyCalendar == null || weekStart == null) {
            return;
        }

        try {
            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_MONTH, 6);
            weekEnd.set(Calendar.HOUR_OF_DAY, 23);
            weekEnd.set(Calendar.MINUTE, 59);
            weekEnd.set(Calendar.SECOND, 59);

            boolean hasCitas = false;
            for (Cita cita : allCitas) {
                if (cita == null) continue;

                Calendar citaCal = cita.getFechaHoraCalendar();
                if (citaCal != null && !citaCal.before(weekStart) && !citaCal.after(weekEnd)) {
                    hasCitas = true;
                    break;
                }
            }

            layoutEmptyCalendar.setVisibility(hasCitas ? View.GONE : View.VISIBLE);

        } catch (Exception e) {
            Log.e(TAG, "Error al verificar citas de la semana: " + e.getMessage(), e);
        }
    }

    /**
     * Recargar citas (útil después de crear/editar/eliminar)
     */
    public void reloadCitas() {
        Log.d(TAG, "Recargando citas...");
        loadCitasFromFirebase();
    }

    // ===== MÉTODOS AUXILIARES DE UI =====

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        Log.e(TAG, "ERROR: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showInfo(String message) {
        Log.i(TAG, "INFO: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccess(String message) {
        Log.i(TAG, "SUCCESS: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

}