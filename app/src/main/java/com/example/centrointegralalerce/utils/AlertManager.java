package com.example.centrointegralalerce.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.centrointegralalerce.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * Gestor centralizado de alertas para toda la aplicación
 * Compatible con Activity, Fragment y DialogFragment.
 * Incluye: Snackbars, Toasts, Diálogos de confirmación y alertas personalizadas.
 */
public class AlertManager {

    // ============================================
    // SNACKBARS (Feedback visual elegante)
    // ============================================

    public static void showSuccessSnackbar(View view, String mensaje) {
        if (view == null || view.getContext() == null) return;
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.verde_exito));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), android.R.color.white));
        snackbar.show();
    }

    public static void showErrorSnackbar(View view, String mensaje) {
        if (view == null || view.getContext() == null) return;
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.rojo_error));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), android.R.color.white));
        snackbar.show();
    }

    public static void showWarningSnackbar(View view, String mensaje) {
        if (view == null || view.getContext() == null) return;
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.amarillo_advertencia));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), R.color.gris_oscuro));
        snackbar.show();
    }

    public static void showInfoSnackbar(View view, String mensaje) {
        if (view == null || view.getContext() == null) return;
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(ContextCompat.getColor(view.getContext(), R.color.cian_info));
        snackbar.setTextColor(ContextCompat.getColor(view.getContext(), android.R.color.white));
        snackbar.show();
    }

    public static void showSnackbarWithAction(View view, String mensaje, String textoAccion,
                                              View.OnClickListener accion) {
        if (view == null || view.getContext() == null) return;
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setAction(textoAccion, accion);
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.verde_santo_tomas));
        snackbar.show();
    }

    public static void showUndoSnackbar(View view, String mensaje, OnUndoListener listener) {
        if (view == null || view.getContext() == null) return;
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);
        snackbar.setAction("DESHACER", v -> {
            if (listener != null) listener.onUndo();
        });
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.amarillo_advertencia));
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != DISMISS_EVENT_ACTION && listener != null) {
                    listener.onConfirmed();
                }
            }
        });
        snackbar.show();
    }

    // ============================================
    // TOASTS (Mensajes rápidos)
    // ============================================

    public static void showToast(Context context, String mensaje) {
        if (context == null) return;
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String mensaje) {
        if (context == null) return;
        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show();
    }

    public static void showSuccessToast(Context context, String mensaje) {
        if (context == null) return;
        Toast.makeText(context, "✅ " + mensaje, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorToast(Context context, String mensaje) {
        if (context == null) return;
        Toast.makeText(context, "❌ " + mensaje, Toast.LENGTH_SHORT).show();
    }

    public static void showWarningToast(Context context, String mensaje) {
        if (context == null) return;
        Toast.makeText(context, "⚠️ " + mensaje, Toast.LENGTH_SHORT).show();
    }

    public static void showInfoToast(Context context, String mensaje) {
        if (context == null) return;
        Toast.makeText(context, "ℹ️ " + mensaje, Toast.LENGTH_SHORT).show();
    }

    // ============================================
    // DIÁLOGOS (Confirmaciones y alertas)
    // ============================================

    public static void showConfirmDialog(Context context, String titulo, String mensaje,
                                         OnConfirmListener listener) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    if (listener != null) listener.onConfirm();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (listener != null) listener.onCancel();
                })
                .show();
    }

    public static void showDestructiveDialog(Context context, String titulo, String mensaje,
                                             String textoAccion, OnConfirmListener listener) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(textoAccion, (dialog, which) -> {
                    if (listener != null) listener.onConfirm();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (listener != null) listener.onCancel();
                })
                .show();
    }

    public static void showDeleteConfirmDialog(Context context, String itemName,
                                               OnConfirmListener listener) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle("¿Eliminar " + itemName + "?")
                .setMessage("Esta acción no se puede deshacer.")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (listener != null) listener.onConfirm();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    if (listener != null) listener.onCancel();
                })
                .show();
    }

    public static void showInfoDialog(Context context, String titulo, String mensaje) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Entendido", null)
                .show();
    }

    public static void showThreeOptionDialog(Context context, String titulo, String mensaje,
                                             String opcion1, String opcion2, String opcion3,
                                             OnThreeOptionListener listener) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton(opcion1, (dialog, which) -> {
                    if (listener != null) listener.onOption1();
                })
                .setNegativeButton(opcion2, (dialog, which) -> {
                    if (listener != null) listener.onOption2();
                })
                .setNeutralButton(opcion3, (dialog, which) -> {
                    if (listener != null) listener.onOption3();
                })
                .show();
    }

    public static void showListDialog(Context context, String titulo, String[] opciones,
                                      OnItemSelectedListener listener) {
        if (context == null) return;
        new MaterialAlertDialogBuilder(context)
                .setTitle(titulo)
                .setItems(opciones, (dialog, which) -> {
                    if (listener != null) listener.onItemSelected(which, opciones[which]);
                })
                .show();
    }

    public static AlertDialog showLoadingDialog(Context context, String mensaje) {
        if (context == null) return null;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setMessage(mensaje);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    // ============================================
    // INTERFACES DE CALLBACK
    // ============================================

    public interface OnConfirmListener {
        void onConfirm();
        default void onCancel() {}
    }

    public interface OnUndoListener {
        void onUndo();
        void onConfirmed();
    }

    public interface OnThreeOptionListener {
        void onOption1();
        void onOption2();
        void onOption3();
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int position, String item);
    }

    // ============================================
    // HELPERS COMPATIBLES CON FRAGMENTS
    // ============================================

    /**
     * Obtiene la vista raíz de una Activity para Snackbars
     */
    public static View getRootView(Activity activity) {
        if (activity == null) return null;
        return activity.findViewById(android.R.id.content);
    }

    /**
     * Obtiene la vista raíz desde un Fragment de forma segura
     */
    public static View getRootViewSafe(Fragment fragment) {
        if (fragment == null) return null;
        if (fragment.getView() != null) return fragment.getView();
        if (fragment.getActivity() != null)
            return fragment.getActivity().findViewById(android.R.id.content);
        return null;
    }

    /**
     * Muestra un Snackbar desde Activity o Fragment automáticamente
     */
    public static void showSnackbarAuto(Object host, String mensaje, int colorRes) {
        View rootView = null;

        if (host instanceof Activity) {
            rootView = ((Activity) host).findViewById(android.R.id.content);
        } else if (host instanceof Fragment) {
            Fragment f = (Fragment) host;
            if (f.getView() != null) rootView = f.getView();
            else if (f.getActivity() != null)
                rootView = f.getActivity().findViewById(android.R.id.content);
        }

        if (rootView != null && rootView.getContext() != null) {
            Snackbar snackbar = Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG);
            snackbar.setBackgroundTint(ContextCompat.getColor(rootView.getContext(), colorRes));
            snackbar.setTextColor(ContextCompat.getColor(rootView.getContext(), android.R.color.white));
            snackbar.show();
        }
    }
}
