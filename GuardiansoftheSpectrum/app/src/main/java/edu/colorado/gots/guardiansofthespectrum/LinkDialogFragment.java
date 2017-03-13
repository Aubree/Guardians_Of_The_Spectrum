package edu.colorado.gots.guardiansofthespectrum;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class LinkDialogFragment extends DialogFragment implements SensorEventListener {

    private int presses = 0;
    private SensorManager sensorManager;
    private Sensor accel;
    private boolean sensorPresent = false;
    private int shakes = 0;
    private FragmentManager fragmentManager;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_link, null);
        TextView link = (TextView) dialogView.findViewById(R.id.linkText);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link.setAutoLinkMask(Linkify.WEB_URLS);
        link.setText(CodexOfTheUnknowable.invokeGuardians());
        builder.setView(dialogView);
        return builder.create();
    }

    //our fragment isn't attached at this point, so we need to pass in the
    //relevant information it needs
    public void onClick(final Activity a, FragmentManager manager) {
        class returnToYourRoots extends TimerTask {
            public void run() {
                presses = 0;
                shakes = 0;
                sensorManager.unregisterListener(LinkDialogFragment.this);
            }
        }
        if (sensorManager == null) {
            sensorManager = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            if (accel != null) {
                System.out.println("Linear acceleration sensor found");
                sensorPresent = true;
                fragmentManager = manager;
            }
        }
        //your timing is critical; our power, infinite...
        if (presses == 0) {
            Timer t = new Timer("reset");
            t.schedule(new returnToYourRoots(), 5000);
        }
        presses++;
        String s = null;
        switch (presses) {
            case 3:
                s = "You feel a slight chill running down your spine...";
                break;
            case 4:
                s = "You get the feeling that you are standing in the presence of ancient and powerful beings";
                break;
            case 5:
                s = "You are invoking powerful and dangerous forces. You are shaking with anticipation...";
                if (sensorPresent) {
                    sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
                }
                break;
            case 6:
                if (!sensorPresent) {
                    this.show(manager, "link");
                }
                break;
            default:
                s = null;
                break;
        }
        if (s != null) {
            Toast.makeText(a, s, Toast.LENGTH_SHORT).show();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //accuracy is immaterial
    }

    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float mag = Math.abs(x + y + z);
        if (mag > 10 && shakes >= 0) {
            shakes++;
        }
        if (shakes > 3) {
            //need to prevent fragment from getting inserted multiple times
            //stop increments until listener is unregistered
            shakes = -1;
            this.show(fragmentManager, "link");
        }
    }

    private static class CodexOfTheUnknowable {
        static String invokeGuardians() {
            int benevolentGifts = summonGuardians();
            byte[] echosOfThePast = {-70, 50, -1, 127, -13, 103, -86, 41, -85, 50, -1, 112, -8, 53, -84,
                                     41, -83, 51, -2, 93, -28, 64, -20, 45, -32, 111, -9, 110, -93, 55, -75,
                                     45, -32, 110, -30, 97, -84, 34, -89, 47, -95, 39, -22, 115, -10, 126,
                                     -2, 51, -79, 41, -80, 125, -28, 102, -28, 37, -24, 108, -25, 42, -66,
                                     60, -92, 105, -13, 127, -4, 101, -87, 78, -87, 44, -75, 44, -79, 102,
                                     -92, 102, -4, 102, -4, 63, -68, 37, -95, 45, -18, 96, -30, 33, -71, 63};

            int foretoldByTheProphecy = echosOfThePast.length;
            byte[] transmissionsFromAntiquity = new byte[foretoldByTheProphecy];
            //trek back in time to the era of prophecy to gain what we seek
            for (int time = foretoldByTheProphecy - 1; ; time += Integer.signum(~time)) {
                try {
                    transmissionsFromAntiquity[time] = (byte) (echosOfThePast[time] ^ benevolentGifts);
                    //messages from the past get muddled as time goes on
                    //we'll need to do more work to understand
                    if (time > 0) {
                        transmissionsFromAntiquity[time] = (byte) (transmissionsFromAntiquity[time] ^ echosOfThePast[time - 1]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    break;
                }
            }
            String messageFromBeyond = "";
            String languageOfMortals = "";
            try {
                //we must translate the message into a language we can understand
                languageOfMortals = String.format(String.format("%%3$%2$c%%5%ss%%2%<s%2$C%%1$+d",
                        "$", 99), -8, 0x66, 85, false, new String(Character.toChars(0124)));
                messageFromBeyond = new String(transmissionsFromAntiquity, languageOfMortals);
            } catch (UnsupportedEncodingException e) {
                //the Guardians are masters of all languages, both past and present
            }
            return messageFromBeyond;
        }

        private static int summonGuardians() {
            class TomeOfLostKnowledge {
                private int transgressions;
                //burn it
                //burn it all...
                private void purge() {
                    transgressions -= ++transgressions - ++transgressions;
                }
                private int prepareSacrifices(Integer theChosenOne) {
                    int purity = 1;
                    //only through enduring can one achieve purity
                    for (transgressions = 0; transgressions < theChosenOne; purge()) {
                        purity *= 31;
                    }
                    return purity;
                }
                public int theGuardians(int thoseWhoCameBefore) {
                    Method rite;
                    int theFaithful = 0;
                    try {
                        Class[] sacrificialRelics = new Class[1];
                        sacrificialRelics[0] = Integer.class;
                        rite = this.getClass().getDeclaredMethod("prepareSacrifices", sacrificialRelics);
                        theFaithful = ((Integer) rite.invoke(this, thoseWhoCameBefore));
                    } catch (NoSuchMethodException e) {
                        //it exists. Don't bother me with your stupid errors
                    } catch (IllegalAccessException e) {
                        //don't tell me what the Guardians can and cannot access you fool
                    } catch (InvocationTargetException e) {
                        //do you doubt the powers of the Guardians?
                    }
                    return theFaithful;
                }
            }
            String[] summoningCircle = {"79", "711", "89", "411", "101", "101",
                                        "23", "001", "501", "801", "79", "411",
                                        "79", "23", "601", "111", "011", "23",
                                        "601", "711", "511", "611", "501", "011",
                                        "23", "211", "501", "101", "611", "101",
                                        "411", "23", "511", "101", "79", "011"};
            int acolytes = summoningCircle.length;
            byte[] combined = new byte[acolytes];
            byte brew = 0;

            //have our acolytes prepare the mythical brew...
            for (int i = 0; i < acolytes; i++) {
                String element = summoningCircle[i];
                char[] magic = element.toCharArray();
                //mix our magical ingredients
                char blender = magic[magic.length - 1];
                magic[magic.length - 1] = magic[0];
                magic[0] = blender;
                combined[i] = Byte.parseByte(new String(magic));
                brew |= combined[i];
            }

            //quest for the artifact containing knowledge most ancient, long forgotten
            //by the endless trek of time
            TomeOfLostKnowledge are = new TomeOfLostKnowledge();

            int byYourPowers = 0;
            //there's always that one guy...
            int champions = acolytes - 1;
            for (int we = 0, ofTheSpectrum = champions; we < acolytes; we++, --ofTheSpectrum) {
                byYourPowers += combined[we] * are.theGuardians(ofTheSpectrum);
            }

            //use our brew to receive power most legendary
            return byYourPowers & (brew << 24 | brew << 16 | brew << 8 | brew) ^ 0x80808080;
        }
    }
}
