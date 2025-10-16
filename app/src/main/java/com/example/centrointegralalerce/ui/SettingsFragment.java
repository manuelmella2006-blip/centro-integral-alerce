package com.example.centrointegralalerce.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.centrointegralalerce.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Button btnLogout = rootView.findViewById(R.id.btnLogout);
        Button btnAbout = rootView.findViewById(R.id.btnAbout);

        btnLogout.setOnClickListener(v ->
                Toast.makeText(getContext(), "Cerrar sesión aún no implementado", Toast.LENGTH_SHORT).show());

        btnAbout.setOnClickListener(v ->
                Toast.makeText(getContext(), "Centro Integral Alerce App\nVersión 1.0", Toast.LENGTH_LONG).show());

        return rootView;
    }
}