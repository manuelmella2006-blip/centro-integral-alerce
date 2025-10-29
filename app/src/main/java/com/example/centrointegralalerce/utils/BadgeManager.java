package com.example.centrointegralalerce.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.centrointegralalerce.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Gestor de badges (indicadores numéricos) para la aplicación
 * Muestra contadores de actividades pendientes, notificaciones, etc.
 */
public class BadgeManager {

    /**
     * Agrega un badge a un item del BottomNavigationView
     */
    public static BadgeDrawable showBadge(BottomNavigationView bottomNav, int menuItemId, int count) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setNumber(count);
        badge.setBackgroundColor(ContextCompat.getColor(bottomNav.getContext(), R.color.rojo_error));
        badge.setBadgeTextColor(Color.WHITE);
        badge.setMaxCharacterCount(3); // Máximo 999

        if (count > 0) {
            badge.setVisible(true);
        } else {
            badge.setVisible(false);
        }

        return badge;
    }

    /**
     * Actualiza el número de un badge existente
     */
    public static void updateBadge(BottomNavigationView bottomNav, int menuItemId, int newCount) {
        BadgeDrawable badge = bottomNav.getBadge(menuItemId);
        if (badge != null) {
            badge.setNumber(newCount);
            badge.setVisible(newCount > 0);
        } else {
            showBadge(bottomNav, menuItemId, newCount);
        }
    }

    /**
     * Oculta un badge
     */
    public static void hideBadge(BottomNavigationView bottomNav, int menuItemId) {
        BadgeDrawable badge = bottomNav.getBadge(menuItemId);
        if (badge != null) {
            badge.setVisible(false);
        }
    }

    /**
     * Elimina un badge completamente
     */
    public static void removeBadge(BottomNavigationView bottomNav, int menuItemId) {
        bottomNav.removeBadge(menuItemId);
    }

    /**
     * Incrementa el contador de un badge
     */
    public static void incrementBadge(BottomNavigationView bottomNav, int menuItemId) {
        BadgeDrawable badge = bottomNav.getBadge(menuItemId);
        if (badge != null) {
            badge.setNumber(badge.getNumber() + 1);
            badge.setVisible(true);
        } else {
            showBadge(bottomNav, menuItemId, 1);
        }
    }

    /**
     * Decrementa el contador de un badge
     */
    public static void decrementBadge(BottomNavigationView bottomNav, int menuItemId) {
        BadgeDrawable badge = bottomNav.getBadge(menuItemId);
        if (badge != null) {
            int newCount = Math.max(0, badge.getNumber() - 1);
            badge.setNumber(newCount);
            badge.setVisible(newCount > 0);
        }
    }

    /**
     * Badge personalizado para vistas
     */
    public static TextView createCustomBadge(Context context, ViewGroup parent, int count) {
        TextView badge = new TextView(context);
        badge.setText(String.valueOf(count));
        badge.setBackgroundResource(R.drawable.badge_background);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(10);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(8, 4, 8, 4);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.setMargins(0, 8, 8, 0);
        badge.setLayoutParams(params);

        badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

        parent.addView(badge);
        return badge;
    }

    /**
     * Actualiza un badge personalizado
     */
    public static void updateCustomBadge(TextView badge, int count) {
        if (badge != null) {
            badge.setText(String.valueOf(count));
            badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Badge con punto indicador (sin número)
     */
    public static BadgeDrawable showDotBadge(BottomNavigationView bottomNav, int menuItemId) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setBackgroundColor(ContextCompat.getColor(bottomNav.getContext(), R.color.verde_santo_tomas));
        badge.clearNumber();
        badge.setVisible(true);
        return badge;
    }

    /**
     * Badge de actividades pendientes con color específico
     */
    public static BadgeDrawable showActivityBadge(BottomNavigationView bottomNav,
                                                  int menuItemId, int count, String tipo) {
        BadgeDrawable badge = bottomNav.getOrCreateBadge(menuItemId);
        badge.setNumber(count);
        badge.setBadgeTextColor(Color.WHITE);

        // Color según tipo de actividad
        int color = getColorForType(bottomNav.getContext(), tipo);
        badge.setBackgroundColor(color);

        badge.setVisible(count > 0);
        return badge;
    }

    /**
     * Obtiene color según tipo de actividad
     */
    private static int getColorForType(Context context, String tipo) {
        if (tipo == null) return ContextCompat.getColor(context, R.color.gris_medio);

        switch (tipo) {
            case "urgente":
                return ContextCompat.getColor(context, R.color.rojo_error);
            case "importante":
                return ContextCompat.getColor(context, R.color.amarillo_advertencia);
            case "normal":
                return ContextCompat.getColor(context, R.color.cian_info);
            default:
                return ContextCompat.getColor(context, R.color.verde_santo_tomas);
        }
    }

    /**
     * Limpia todos los badges de un BottomNavigationView
     */
    public static void clearAllBadges(BottomNavigationView bottomNav) {
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            int itemId = bottomNav.getMenu().getItem(i).getItemId();
            removeBadge(bottomNav, itemId);
        }
    }
}