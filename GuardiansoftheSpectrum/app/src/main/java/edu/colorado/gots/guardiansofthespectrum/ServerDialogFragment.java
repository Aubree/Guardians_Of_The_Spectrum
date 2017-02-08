package edu.colorado.gots.guardiansofthespectrum;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
        View view = inflater.inflate(R.layout.dialog_send, null);
        //set a text changed listener on our IP address to format it
        ((EditText) view.findViewById(R.id.serverIP)).addTextChangedListener(new TextWatcher() {
            private boolean editing = false;
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable input) {
                //bail if this gets called as a result of us editing the string ourselves
                if (editing) {
                    return;
                }
                //mark that we're editing the string
                editing = true;
                //rip out old dots
                StringBuilder newText = new StringBuilder(input.toString().replace(".", ""));
                //add in the dots as appropriate:
                //ip address: xxx.xxx.xxx.xxx
                //index:      0123456789abcde
                if (newText.length() > 3) {
                    newText.insert(3, ".");
                }
                if (newText.length() > 7) {
                    newText.insert(7, ".");
                }
                if (newText.length() > 11) {
                    newText.insert(11, ".");
                }
                //set the editing string to appear in the input
                input.replace(0, input.length(), newText.toString());
                //we've finished editing
                editing = false;
            }
        });

        //set our layout for the dialog box
        builder.setView(view);
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(ServerDialogFragment.this);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(ServerDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
