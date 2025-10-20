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

            // Cargar citas desde Firebase
            // loadCitasFromFirebase(); // ⚠️ Comentado temporalmente
            cargarDatosDePrueba(); // ✅ Datos de prueba

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
     * Carga las citas desde Firebase Firestore con manejo robusto de errores
     */
    private void loadCitasFromFirebase() {
        Log.d(TAG, "=== INICIANDO CARGA DE CITAS ===");

        if (db == null) {
            Log.e(TAG, "❌ FirebaseFirestore es null");
            showError("Error de configuración de Firebase");
            setupViewPager(); // Configurar vacío
            return;
        }

        showLoading(true);

        db.collection("citas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        allCitas.clear();
                        int citasValidas = 0;
                        int citasInvalidas = 0;

                        Log.d(TAG, "Documentos recibidos: " + queryDocumentSnapshots.size());

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                // Intentar parsear la cita
                                Cita cita = document.toObject(Cita.class);

                                // Log detallado del documento
                                Log.d(TAG, "--- Documento: " + document.getId() + " ---");
                                Log.d(TAG, "Datos raw: " + document.getData());

                                if (cita == null) {
                                    Log.e(TAG, "❌ Cita parseada es null - documento: " + document.getId());
                                    citasInvalidas++;
                                    continue;
                                }

                                // Validar cita
                                if (!cita.esValida()) {
                                    Log.w(TAG, "⚠️ Cita inválida: " + cita.toString());
                                    citasInvalidas++;
                                    continue;
                                }

                                // Verificar que el Timestamp se convirtió correctamente
                                if (cita.getFechaHora() == null) {
                                    Log.e(TAG, "❌ Timestamp es null en cita: " + cita.getId());
                                    citasInvalidas++;
                                    continue;
                                }

                                // Verificar que hora y día se calcularon
                                String hora = cita.getHora();
                                int dia = cita.getDiaSemana();
                                Log.d(TAG, "✅ Cita válida: " + cita.getActividad() +
                                        " | Hora: " + hora +
                                        " | Día: " + dia +
                                        " | Timestamp: " + cita.getFechaHora().toDate());

                                if (dia < 0 || dia > 6) {
                                    Log.e(TAG, "❌ Día de semana inválido: " + dia);
                                    citasInvalidas++;
                                    continue;
                                }

                                allCitas.add(cita);
                                citasValidas++;

                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error al parsear documento " + document.getId() + ": " + e.getMessage(), e);
                                citasInvalidas++;
                            }
                        }

                        Log.d(TAG, "=== RESULTADO DE CARGA ===");
                        Log.d(TAG, "Citas válidas: " + citasValidas);
                        Log.d(TAG, "Citas inválidas: " + citasInvalidas);
                        Log.d(TAG, "Total en memoria: " + allCitas.size());

                        // Configurar ViewPager después de cargar las citas
                        setupViewPager();
                        updateWeekLabel(currentWeekStart);
                        checkIfWeekHasCitas(currentWeekStart);

                        showLoading(false);

                        if (allCitas.isEmpty()) {
                            if (citasInvalidas > 0) {
                                showError("Se encontraron " + citasInvalidas + " citas con errores de formato");
                            } else {
                                showInfo("No hay actividades programadas");
                            }
                        } else {
                            showSuccess("Se cargaron " + citasValidas + " actividades");
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error crítico al procesar citas: " + e.getMessage(), e);
                        showError("Error al procesar las actividades");
                        showLoading(false);
                        setupViewPager(); // Configurar vacío
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error en consulta Firebase: " + e.getMessage(), e);
                    showError("Error al cargar actividades: " + e.getMessage());
                    showLoading(false);
                    setupViewPager(); // Aún así configurar el ViewPager vacío
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

    /**
     * DATOS DE PRUEBA - Temporal para testing
     * Genera citas de ejemplo para esta semana
     */
    private void cargarDatosDePrueba() {
        Log.d(TAG, "=== CARGANDO DATOS DE PRUEBA ===");

        showLoading(true);

        allCitas.clear();

        try {
            Calendar cal = Calendar.getInstance();
            com.google.firebase.Timestamp timestamp;

            // LUNES 13 de octubre - 09:00
            cal.set(2025, Calendar.OCTOBER, 13, 9, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita1 = new Cita(
                    "test1",
                    "Reunión de Equipo",
                    "Sala A",
                    "Charlas",
                    timestamp,
                    "test-user"
            );
            allCitas.add(cita1);

            // MARTES 14 de octubre - 10:00
            cal.set(2025, Calendar.OCTOBER, 14, 10, 0, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita2 = new Cita(
                    "test2",
                    "Capacitación Excel",
                    "Laboratorio 1",
                    "Capacitación",
                    timestamp,
                    "test-user"
            );
            allCitas.add(cita2);

            // MIÉRCOLES 15 de octubre - 14:00
            cal.set(2025, Calendar.OCTOBER, 15, 14, 0, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita3 = new Cita(
                    "test3",
                    "Atención Psicológica",
                    "Consultorio 2",
                    "Atenciones",
                    timestamp,
                    "test-user"
            );
            allCitas.add(cita3);

            // JUEVES 16 de octubre - 11:00
            cal.set(2025, Calendar.OCTOBER, 16, 11, 0, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita4 = new Cita(
                    "test4",
                    "Taller de Programación",
                    "Lab Computación",
                    "Taller",
                    timestamp,
                    "test-user"
            );
            allCitas.add(cita4);

            // VIERNES 17 de octubre - 15:00
            cal.set(2025, Calendar.OCTOBER, 17, 15, 0, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita5 = new Cita(
                    "test5",
                    "Operativo de Salud",
                    "Plaza Central",
                    "Operativo",
                    timestamp,
                    "test-user"
            );
            allCitas.add(cita5);

            // SÁBADO 19 de octubre - 16:00 (hora exacta para que se muestre)
            cal.set(2025, Calendar.OCTOBER, 19, 16, 0, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita6 = new Cita(
                    "test6",
                    "Taller de Robótica",
                    "Laboratorio 3",
                    "Taller",
                    timestamp,
                    "abc123uid"
            );
            allCitas.add(cita6);

            // lunes 20 de octubre - 10:00
            cal.set(2025, Calendar.OCTOBER, 20, 10, 0, 0);
            timestamp = new com.google.firebase.Timestamp(cal.getTime());
            Cita cita7 = new Cita(
                    "test7",
                    "Charla Motivacional",
                    "Auditorio",
                    "Charlas",
                    timestamp,
                    "test-user"
            );
            allCitas.add(cita7);

            Log.d(TAG, "=== DATOS DE PRUEBA CREADOS ===");
            Log.d(TAG, "Total citas: " + allCitas.size());

            // Validar cada cita
            for (int i = 0; i < allCitas.size(); i++) {
                Cita cita = allCitas.get(i);
                Log.d(TAG, String.format("Cita %d: %s | Hora: %s | Día: %d | Válida: %s",
                        i + 1,
                        cita.getActividad(),
                        cita.getHora(),
                        cita.getDiaSemana(),
                        cita.esValida()
                ));
            }

            // Configurar ViewPager
            setupViewPager();
            updateWeekLabel(currentWeekStart);
            checkIfWeekHasCitas(currentWeekStart);

            showLoading(false);
            showSuccess("Se cargaron " + allCitas.size() + " actividades de prueba");

        } catch (Exception e) {
            Log.e(TAG, "Error al crear datos de prueba: " + e.getMessage(), e);
            showError("Error al cargar datos de prueba");
            showLoading(false);
            setupViewPager();
        }
    }
}