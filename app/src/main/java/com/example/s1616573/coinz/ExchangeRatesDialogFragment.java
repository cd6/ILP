package com.example.s1616573.coinz;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import java.util.HashMap;

public class ExchangeRatesDialogFragment extends DialogFragment {


    public ExchangeRatesDialogFragment() {
        super();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.rates_popup, null))
                // Add action buttons
                .setPositiveButton(
                        "Yes",
                        (dialog, id) -> {
                            dialog.cancel();
                        });
        return builder.create();
    }
}
