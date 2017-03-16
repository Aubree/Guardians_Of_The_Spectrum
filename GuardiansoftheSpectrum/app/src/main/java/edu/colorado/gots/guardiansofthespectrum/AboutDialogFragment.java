package edu.colorado.gots.guardiansofthespectrum;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class AboutDialogFragment extends DialogFragment {
    public interface AboutDialogListener {
        void onDialogNeutralClick(DialogFragment dialog);
    }

    private AboutDialogListener listener;

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AboutDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_about, null);

        builder.setView(view);
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener()
            public void )
    }
}