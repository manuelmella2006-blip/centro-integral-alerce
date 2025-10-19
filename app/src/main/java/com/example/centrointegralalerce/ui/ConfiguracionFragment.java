package com.example.centrointegralalerce.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private void cargarInfoUsuario() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nombre = documentSnapshot.getString("nombre");
                String email = documentSnapshot.getString("email");
                String rol = documentSnapshot.getString("rol");

                tvUserName.setText(nombre);
                tvUserEmail.setText(email);
                chipUserRole.setText(rol);

                MainActivity mainActivity = (MainActivity) getActivity();
                boolean esInvitado = mainActivity != null && mainActivity.isGuest();
                boolean esAdmin = "Administrador".equals(rol);

                // Mostrar solo opciones administrativas si es admin y no invitado
                itemGestionarUsuarios.setVisibility((esAdmin && !esInvitado) ? View.VISIBLE : View.GONE);
                itemMantenedores.setVisibility((esAdmin && !esInvitado) ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupListeners() {
        itemMantenedores.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Mantenedores - Por implementar", Toast.LENGTH_SHORT).show());

        itemGestionarUsuarios.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RegisterActivity.class);
            startActivity(intent);
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(requireContext(),
                        "Notificaciones " + (isChecked ? "activadas" : "desactivadas"),
                        Toast.LENGTH_SHORT).show());

        itemDiasAviso.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Días de aviso - Por implementar", Toast.LENGTH_SHORT).show());

        itemChangePassword.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Cambiar contraseña - Por implementar", Toast.LENGTH_SHORT).show());

        itemLogout.setOnClickListener(v -> showLogoutDialog());

        itemAbout.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Acerca de")
                        .setMessage("Centro Integral Alerce App\nVersión 1.0\n\nUniversidad Santo Tomás\n2024")
                        .setPositiveButton("Aceptar", null)
                        .show());
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> {
                    auth.signOut(); // cerrar sesión en Firebase
                    requireActivity().finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
