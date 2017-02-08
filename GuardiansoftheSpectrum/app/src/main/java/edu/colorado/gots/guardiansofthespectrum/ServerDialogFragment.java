package edu.colorado.gots.guardiansofthespectrum;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

//interface containing functions that we will call when buttons are pressed on our dialog box
public class ServerDialogFragment extends DialogFragment {
    public interface ServerDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private ServerDialogListener listener;

    //called when fragment is attached to a specific context
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ServerDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ServerDialogListener");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //set our layout for the dialog box
        builder.setView(inflater.inflate(R.layout.dialog_send, null))
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ServerDialogFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(ServerDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
