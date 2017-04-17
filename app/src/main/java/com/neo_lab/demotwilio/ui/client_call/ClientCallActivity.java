package com.neo_lab.demotwilio.ui.client_call;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.share_preferences_manager.SharedPreferencesManager;
import com.neo_lab.demotwilio.ui.main.MainActivity;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;
import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClientCallActivity extends AppCompatActivity implements ClientCallContract.View, DeviceListener, ConnectionListener {

    private static final String TAG = "ClientCallActivity";

    private static final int MIC_PERMISSION_REQUEST_CODE = 1;

    private ClientCallContract.Presenter presenter;

    private String userName;

    /*
     * A Device is the primary entry point to Twilio Services
     */
    private Device clientDevice;

    /*
     * A Connection represents a connection between a Device and Twilio Services.
     * Connections are either outgoing or incoming, and not created directly.
     * An outgoing connection is created by Device.connect()
     * An incoming connection are created internally by a Device and hanged to the registered PendingIntent
     */
    private Connection activeConnection;

    private Connection pendingConnection;

    private AudioManager audioManager;

    private int savedAudioMode = AudioManager.MODE_INVALID;

    /*
     * A representation of the current properties of a client token
     */
    protected class ClientProfile {
        private String name;
        private boolean allowOutgoing = true;
        private boolean allowIncoming = true;


        public ClientProfile(String name, boolean allowOutgoing, boolean allowIncoming) {
            this.name = name;
            this.allowOutgoing = allowOutgoing;
            this.allowIncoming = allowIncoming;
        }

        public String getName() {
            return name;
        }

        public boolean isAllowOutgoing() {
            return allowOutgoing;
        }

        public boolean isAllowIncoming() {
            return allowIncoming;
        }
    }

    /*
     * Android application UI elements
     */
    @BindView(R.id.call_action_fab) FloatingActionButton callActionFab;

    @BindView(R.id.mute_action_fab) FloatingActionButton muteActionFab;

    @BindView(R.id.speaker_action_fab) FloatingActionButton speakerActionFab;

    @BindView(R.id.hangup_action_fab) FloatingActionButton hangupActionFab;

    private ClientProfile clientProfile;

    private AlertDialog alertDialog;

    @BindView(R.id.chronometer) Chronometer chronometer;

    @BindView(R.id.call_layout) View callView;

    @BindView(R.id.capability_properties) View capabilityPropertiesView;

    private boolean muteMicrophone;

    private boolean speakerPhone;

    // Application Header

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.toolbar_title) TextView tvToolbarTitle;


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Grab menu items for updating later
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_call);

        ButterKnife.bind(this);

        presenter = new ClientCallPresenter(this);

        getProperties();

        showUI();

        /*
         * Create a default profile (name=jenny, allowOutgoing=true, allowIncoming=true)
         */
        clientProfile = new ClientProfile(userName, true, true);

        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /*
         * Check microphone permissions. Needed in Android M.
         */
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone();
        } else {
            /*
             * Initialize the Twilio Client SDK
             */
            initializeTwilioClientSDK();
        }

        /*
         * Set the initial state of the UI
         */
        setCallAction();
    }

    @Override
    public void setPresenter(ClientCallContract.Presenter presenter) {

    }

    @Override
    public void getProperties() {

        userName = SharedPreferencesManager.getInstance(getApplicationContext()).getString(SharedPreferencesManager.Key.USER_NAME);


    }

    @Override
    public void showUI() {

        ToolbarUtils.initialize(toolbar, this, R.string.client_call, R.drawable.ic_arrow_back_white_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startActivity(ClientCallActivity.this, MainActivity.class, true);
            }
        });

    }

    @Override
    public void onListenerRequestClientCall(boolean status, String message, Token token, ClientProfile clientProfile) {

        if (status) {
            // Update the current Client Profile to represent current properties
            ClientCallActivity.this.clientProfile = clientProfile;

            // Create a Device with the Capability Token
            createDevice(token.getToken());
        } else {

        }

    }

    /*
     * Initialize the Twilio Client SDK
     */
    private void initializeTwilioClientSDK() {

        if (!Twilio.isInitialized()) {
            Twilio.initialize(getApplicationContext(), new Twilio.InitListener() {

                /*
                 * Now that the SDK is initialized we can register using a Capability Token.
                 * A Capability Token is a JSON Web Token (JWT) that specifies how an associated Device
                 * can interact with Twilio services.
                 */
                @Override
                public void onInitialized() {
                    Twilio.setLogLevel(Log.DEBUG);
                    /*
                     * Retrieve the Capability Token from your own web server
                     */
                    retrieveCapabilityToken(clientProfile);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(ClientCallActivity.this, "Failed to initialize the Twilio Client SDK", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /*
     * Create a Device or update the capabilities of the current Device
     */
    private void createDevice(String capabilityToken) {
        try {
            if (clientDevice == null) {
                clientDevice = Twilio.createDevice(capabilityToken, this);

                /*
                 * Providing a PendingIntent to the newly created Device, allowing you to receive incoming calls
                 *
                 *  What you do when you receive the intent depends on the component you set in the Intent.
                 *
                 *  If you're using an Activity, you'll want to override Activity.onNewIntent()
                 *  If you're using a Service, you'll want to override Service.onStartCommand().
                 *  If you're using a BroadcastReceiver, override BroadcastReceiver.onReceive().
                 */

                Intent intent = new Intent(getApplicationContext(), ClientCallActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                clientDevice.setIncomingIntent(pendingIntent);
            } else {
                clientDevice.updateCapabilityToken(capabilityToken);
            }

            TextView clientNameTextView = (TextView) capabilityPropertiesView.findViewById(R.id.client_name_registered_text);
            clientNameTextView.setText("Client Name: " + ClientCallActivity.this.clientProfile.getName());

            TextView outgoingCapabilityTextView = (TextView) capabilityPropertiesView.findViewById(R.id.outgoing_capability_registered_text);
            outgoingCapabilityTextView.setText("Outgoing Capability: " +Boolean.toString(ClientCallActivity.this.clientProfile.isAllowOutgoing()));

            TextView incomingCapabilityTextView = (TextView) capabilityPropertiesView.findViewById(R.id.incoming_capability_registered_text);
            incomingCapabilityTextView.setText("Incoming Capability: " +Boolean.toString(ClientCallActivity.this.clientProfile.isAllowIncoming()));

            TextView libraryVersionTextView = (TextView) capabilityPropertiesView.findViewById(R.id.library_version_text);
            libraryVersionTextView.setText("Library Version: " + Twilio.getVersion());

        } catch (Exception e) {
            Log.e(TAG, "An error has occured updating or creating a Device: \n" + e.toString());
            Toast.makeText(ClientCallActivity.this, "Device error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        if (intent != null) {
            /*
             * Determine if the receiving Intent has an extra for the incoming connection. If so,
             * remove it from the Intent to prevent handling it again next time the Activity is resumed
             */
            Device device = intent.getParcelableExtra(Device.EXTRA_DEVICE);
            Connection incomingConnection = intent.getParcelableExtra(Device.EXTRA_CONNECTION);

            if (incomingConnection == null && device == null) {
                return;
            }
            intent.removeExtra(Device.EXTRA_DEVICE);
            intent.removeExtra(Device.EXTRA_CONNECTION);

            pendingConnection = incomingConnection;
            pendingConnection.setConnectionListener(this);

            showIncomingDialog();
        }
    }

    /*
     * Receive intent for incoming call from Twilio Client Service
     * Android will only call Activity.onNewIntent() if `android:launchMode` is set to `singleTop`.
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /*
     * Request a Capability Token from your public accessible server
     */
    private void retrieveCapabilityToken(final ClientProfile newClientProfile) {
        presenter.requestTokenClientCall(newClientProfile);
    }

    /*
     * Create an outgoing connection
     */
    private void connect(String contact, boolean isPhoneNumber) {
        // Determine if you're calling another client or a phone number
        if (!isPhoneNumber) {
            contact = "client:" + contact.trim();
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("To", contact);

        if (clientDevice != null) {
            // Create an outgoing connection
            activeConnection = clientDevice.connect(params, this);
            setCallUI();
        } else {
            Toast.makeText(ClientCallActivity.this, "No existing device", Toast.LENGTH_SHORT).show();
        }
    }

    /*
     * Disconnect an active connection
     */
    private void disconnect() {
        if (activeConnection != null) {
            activeConnection.disconnect();
            activeConnection = null;
        }
    }

    /*
     * Accept an incoming connection
     */
    private void answer() {
        // Only one connection can exist at time, disconnecting any active connection.
        if( activeConnection != null ){
            activeConnection.disconnect();
        }
        pendingConnection.accept();
        activeConnection = pendingConnection;
        pendingConnection = null;
    }

    /*
     * The initial state when there is no active connection
     */
    private void setCallAction() {
        callActionFab.setOnClickListener(callActionFabClickListener());
        hangupActionFab.setOnClickListener(hangupActionFabClickListener());
        muteActionFab.setOnClickListener(muteMicrophoneFabClickListener());
        speakerActionFab.setOnClickListener(toggleSpeakerPhoneFabClickListener());
    }

    /*
     * Reset UI elements
     */
    private void resetUI() {
        callActionFab.show();
        capabilityPropertiesView.setVisibility(View.VISIBLE);

        hangupActionFab.hide();
        callView.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);

        muteMicrophone = false;
        speakerPhone = false;

        muteActionFab.setImageDrawable(ContextCompat.getDrawable(ClientCallActivity.this, R.drawable.ic_mic_green_24px));
        speakerActionFab.setImageDrawable(ContextCompat.getDrawable(ClientCallActivity.this, R.drawable.ic_speaker_off_black_24dp));

        setAudioFocus(false);
        audioManager.setSpeakerphoneOn(speakerPhone);

        chronometer.stop();
    }

    /*
     * The UI state when there is an active connection
     */
    private void setCallUI() {
        callActionFab.hide();
        capabilityPropertiesView.setVisibility(View.INVISIBLE);
        hangupActionFab.show();
        callView.setVisibility(View.VISIBLE);
        chronometer.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    /*
     * Creates an update token UI dialog
     */
    private void updateClientProfileDialog() {
        alertDialog = Dialog.createRegisterDialog(updateTokenClickListener(), cancelCallClickListener(), clientProfile, this);
        alertDialog.show();
    }

    /*
     * Create an outgoing call UI dialog
     */
    private void showCallDialog() {
        alertDialog = Dialog.createCallDialog(callClickListener(), cancelCallClickListener(), this);
        alertDialog.show();
    }

    /*
     * Creates an incoming call UI dialog
     */
    private void showIncomingDialog() {
        alertDialog = Dialog.createIncomingCallDialog(answerCallClickListener(), cancelCallClickListener(), this);
        alertDialog.show();
    }

    private DialogInterface.OnClickListener answerCallClickListener() {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Accept an incoming call
                 */
                answer();
                setCallUI();
                alertDialog.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener callClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Making an outgoing call
                 */
                EditText contact = (EditText) ((AlertDialog) dialog).findViewById(R.id.contact);
                Spinner spinner = (Spinner) ((AlertDialog) dialog).findViewById(R.id.typeSpinner);
                boolean isPhoneNumber = spinner.getSelectedItemPosition() == 1 ? true : false;

                // Create an outgoing connection
                connect(contact.getText().toString(), isPhoneNumber);
                alertDialog.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener updateTokenClickListener() {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                EditText clientEditText = (EditText) ((AlertDialog) dialog).findViewById(R.id.client_name_edittext);
                String clientName = clientEditText.getText().toString();

                CheckBox outgoingCheckBox = (CheckBox) ((AlertDialog) dialog).findViewById(R.id.outgoing_checkbox);
                boolean allowOutgoing = outgoingCheckBox.isChecked();

                CheckBox incomingCheckBox = (CheckBox) ((AlertDialog) dialog).findViewById(R.id.incoming_checkbox);
                boolean allowIncoming = incomingCheckBox.isChecked();

                ClientProfile newClientProfile = new ClientProfile(clientName, allowOutgoing, allowIncoming);
                alertDialog.dismiss();
                retrieveCapabilityToken(newClientProfile);
            }
        };
    }

    private DialogInterface.OnClickListener cancelCallClickListener() {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (pendingConnection != null) {
                    pendingConnection.reject();
                }
                alertDialog.dismiss();
            }
        };
    }

    private View.OnClickListener muteMicrophoneFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 *  Mute/unmute microphone
                 */
                muteMicrophone = !muteMicrophone;
                if (activeConnection != null) {
                    activeConnection.setMuted(muteMicrophone);
                }
                if (muteMicrophone) {
                    muteActionFab.setImageDrawable(ContextCompat.getDrawable(ClientCallActivity.this, R.drawable.ic_mic_off_red_24px));
                } else {
                    muteActionFab.setImageDrawable(ContextCompat.getDrawable(ClientCallActivity.this, R.drawable.ic_mic_green_24px));
                }
            }
        };
    }

    private View.OnClickListener toggleSpeakerPhoneFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Audio routing to speakerphone or headset
                 */
                speakerPhone = !speakerPhone;

                setAudioFocus(true);
                audioManager.setSpeakerphoneOn(speakerPhone);

                if (speakerPhone) {
                    speakerActionFab.setImageDrawable(ContextCompat.getDrawable(ClientCallActivity.this, R.drawable.ic_speaker_on_black_24dp));
                } else {
                    speakerActionFab.setImageDrawable(ContextCompat.getDrawable(ClientCallActivity.this, R.drawable.ic_speaker_off_black_24dp));
                }
            }
        };
    }

    private View.OnClickListener hangupActionFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetUI();
                disconnect();
            }
        };
    }

    private View.OnClickListener callActionFabClickListener() {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showCallDialog();
            }
        };
    }

    /* Device Listener */
    @Override
    public void onStartListening(Device device) {
        Log.d(TAG, "Device has started listening for incoming connections");
    }

    /* Device Listener */
    @Override
    public void onStopListening(Device device) {
        Log.d(TAG, "Device has stopped listening for incoming connections");
    }

    /* Device Listener */
    @Override
    public void onStopListening(Device device, int errorCode, String error) {
        Log.e(TAG, String.format("Device has encountered an error and has stopped" +
                " listening for incoming connections: %s", error));
    }

    /* Device Listener */
    @Override
    public boolean receivePresenceEvents(Device device) {
        return false;
    }

    /* Device Listener */
    @Override
    public void onPresenceChanged(Device device, PresenceEvent presenceEvent) {
    }

    /* Connection Listener */
    @Override
    public void onConnecting(Connection connection) {
        Log.d(TAG, "Attempting to connect");
    }

    /* Connection Listener */
    @Override
    public void onConnected(Connection connection) {
        Log.d(TAG, "Connected");
    }

    /* Connection Listener */
    @Override
    public void onDisconnected(Connection connection) {
        // Remote participant may have disconnected an incoming call before the local participant was able to respond, rejecting any existing pendingConnections
        if( connection == pendingConnection ) {
            pendingConnection = null;
            alertDialog.dismiss();
        } else if (activeConnection != null && connection != null) {
            if (activeConnection == connection) {
                activeConnection = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetUI();
                    }
                });
            }
            Log.d(TAG, "Disconnect");
        }
    }

    /* Connection Listener */
    @Override
    public void onDisconnected(Connection connection, int errorCode, String error) {
        // A connection other than active connection could have errored out.
        if (activeConnection != null && connection != null) {
            if (activeConnection == connection) {
                activeConnection = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resetUI();
                    }
                });
            }
            Log.e(TAG, String.format("Connection error: %s", error));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_call_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_update_capability:
                updateClientProfileDialog();
                return true;
            default:
                return true;
        }
    }

    private boolean checkPermissionForMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (resultMic == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    "Microphone permissions needed. Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
         * Check if microphone permissions is granted
         */
        if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions.length > 0) {
            boolean granted = true;
            if (granted) {
                /*
                * Initialize the Twilio Client SDK
                */
                initializeTwilioClientSDK();
            } else {
                Toast.makeText(this,
                        "Microphone permissions needed. Please allow in App Settings for additional functionality.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setAudioFocus(boolean setFocus) {
        if (audioManager != null) {
            if (setFocus) {
                savedAudioMode = audioManager.getMode();
                // Request audio focus before making any device switch.
                audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                /*
                 * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
                 * required to be in this mode when playout and/or recording starts for
                 * best possible VoIP performance. Some devices have difficulties with speaker mode
                 * if this is not set.
                 */
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(savedAudioMode);
                audioManager.abandonAudioFocus(null);
            }
        }
    }
}
