package com.neo_lab.demotwilio.ui.main;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.share_preferences_manager.SharedPreferencesManager;
import com.neo_lab.demotwilio.ui.chatting.ChattingActivity;
import com.neo_lab.demotwilio.ui.chatting.ChattingFragment;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Member;
import com.twilio.chat.Message;
import com.twilio.chat.StatusListener;
import com.twilio.video.AudioTrack;
import com.twilio.video.CameraCapturer;
import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalMedia;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment extends Fragment implements MainContract.View {

    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;

    private static final int REQUEST_MEDIA_PROJECTION = 100;

    private static final String TAG = "VideoActivity";

    private MainContract.Presenter presenter;

    private View root;

    private Activity activity;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.toolbar_title) TextView tvToolbarTitle;

    @BindView(R.id.rl_video_calling) RelativeLayout rlVideoCalling;

    @BindView(R.id.rl_chatting_calling) RelativeLayout rlChattingCalling;

    @BindView(R.id.messagesRecyclerView) RecyclerView messagesRecyclerView;

    private MessagesAdapter messagesAdapter;

    @BindView(R.id.writeMessageEditText) EditText writeMessageEditText;

    @BindView(R.id.sendChatMessageButton)
    Button sendChatMessageButton;

    private ArrayList<Message> messages;

    private ChatClient chatClient;

    private Channel channel;

    /*
     * Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */
    private String accessToken;

    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room room;

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    @BindView(R.id.primary_video_view) VideoView primaryVideoView;

    @BindView(R.id.thumbnail_video_view) VideoView thumbnailVideoView;
    /*
     * Android application UI elements
     */
    @BindView(R.id.video_status_textview) TextView videoStatusTextView;

    private CameraCapturer cameraCapturer;

    private ScreenCapturer screenCapturer;

    private MenuItem screenCaptureMenuItem;

    private final ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
        @Override
        public void onScreenCaptureError(String errorDescription) {
            Log.e(TAG, "Screen capturer error: " + errorDescription);
            stopScreenCapture();
            Toast.makeText(getActivity(), R.string.screen_capture_error,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFirstFrameAvailable() {
            Log.d(TAG, "First frame from screen capturer available");
        }
    };

    private LocalMedia localMedia;

    private LocalAudioTrack localAudioTrack;

    private LocalVideoTrack localVideoTrack;

//    @BindView(R.id.connect_action_fab) FloatingActionButton connectActionFab;
    @BindView(R.id.switch_camera_action_fab) FloatingActionButton switchCameraActionFab;
    @BindView(R.id.local_video_action_fab) FloatingActionButton localVideoActionFab;
    @BindView(R.id.mute_action_fab) FloatingActionButton muteActionFab;
    private android.support.v7.app.AlertDialog alertDialog;
    private AudioManager audioManager;
    private String participantIdentity;

    private int previousAudioMode;
    private VideoRenderer localVideoView;
    private boolean disconnectedFromOnDestroy;

    public MainFragment() {
        // Required empty public constructor
    }


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Grab menu items for updating later
        screenCaptureMenuItem = menu.findItem(R.id.action_share_screen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_record_screen:
//                return true;
            case R.id.action_share_screen:
                String shareScreen = getString(R.string.share_screen);

                if (item.getTitle().equals(shareScreen)) {
                    if (screenCapturer == null) {
                        requestScreenCapturePermission();
                    } else {
                        startScreenCapture();
                    }
                } else {
                    stopScreenCapture();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        ButterKnife.bind(this, root);

        showUI();

        // Get Device Id
        String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        String userName = SharedPreferencesManager.getInstance(activity).getString(SharedPreferencesManager.Key.USER_NAME);

        // Request Token From Server
        presenter.requestToken(deviceId, userName);

        return root;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {

        this.presenter = presenter;

    }

    @OnClick(R.id.im_cancel_chatting)
    public void cancelChatting() {

        rlVideoCalling.setVisibility(View.VISIBLE);
        rlChattingCalling.setVisibility(View.GONE);

    }

    @Override
    public void showUI() {

        activity = getActivity();

        ToolbarUtils.initialize(toolbar, activity, R.string.app_name, R.drawable.ic_message);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlVideoCalling.setVisibility(View.GONE);
                rlChattingCalling.setVisibility(View.VISIBLE);
            }
        });

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        /*
         * Check camera and microphone permissions. Needed in Android M.
         */
        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        } else {
            createLocalMedia();
            setAccessToken();
        }
        /*
         * Set the initial state of the UI
         */
        intializeUI();

        messages = new ArrayList<>();


        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        // for a chat app, show latest at the bottom
        layoutManager.setStackFromEnd(true);

        messagesRecyclerView.setLayoutManager(layoutManager);

        messagesAdapter = new MessagesAdapter();
        messagesRecyclerView.setAdapter(messagesAdapter);

        sendChatMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (channel != null) {
                    String messageBody = writeMessageEditText.getText().toString();
                    Message message = channel.getMessages().createMessage(messageBody);
                    Log.d(TAG,"Message created");
                    channel.getMessages().sendMessage(message, new StatusListener() {
                        @Override
                        public void onSuccess() {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // need to modify user interface elements on the UI thread
                                    writeMessageEditText.setText("");
                                }
                            });

                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            Log.e(TAG,"Error sending message: " + errorInfo.getErrorText());
                        }
                    });
                }
            }
        });

    }

    @Override
    public void initializeVideoRoom() {

    }

    @Override
    public void initializeCamera() {

    }

    @Override
    public void navigateToChattingRoom() {
        ActivityUtils.startActivity(activity, ChattingActivity.class);
    }

    @Override
    public void onListenerRequestVideoToken(boolean status, String message, Token token) {

        updateStatusRequestVideoToken(status, message);

        if (status) {
            String nameRoom = SharedPreferencesManager.getInstance(activity).getString(SharedPreferencesManager.Key.NAME_OF_ROOM_CHAT);
            connectToRoom(nameRoom, token.getToken());
        }

    }

    @Override
    public void updateStatusRequestVideoToken(boolean status, String message) {

        int color;
        if (status) {
            color = getResources().getColor(R.color.white);
        } else {
            color = getResources().getColor(R.color.colorAccent);
        }

        tvToolbarTitle.setText(message);
        tvToolbarTitle.setTextColor(color);

    }

    @Override
    public void onListenerRequestChattingToken(boolean status, String message, Token token) {
        if (status) {
            createChattingRom(token.getToken());
        }

    }

    @Override
    public void updateStatusRequestChattingToken(boolean status, String message) {

    }

    @Override
    public void createChattingRom(String accessToken) {
        ChatClient.Properties.Builder builder = new ChatClient.Properties.Builder();
        builder.setSynchronizationStrategy(ChatClient.SynchronizationStrategy.ALL);
        ChatClient.Properties props = builder.createProperties();
        ChatClient.create(activity, accessToken, props, chatClientCallbackListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.e(TAG, "cameraAndMicPermissionGranted");
        if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
            boolean cameraAndMicPermissionGranted = true;

            for (int grantResult : grantResults) {
                cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (cameraAndMicPermissionGranted) {

                createLocalMedia();
                setAccessToken();
            } else {
                Toast.makeText(activity,
                        R.string.permissions_needed,
                        Toast.LENGTH_LONG).show();


            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");
        /*
         * If the local video track was removed when the app was put in the background, add it back.
         */
        if (localMedia != null && localVideoTrack == null) {
            localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
            localVideoTrack.addRenderer(localVideoView);
        }
    }

    @Override
    public void onPause() {
        /*
         * Remove the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         *
         * If this local video track is being shared in a Room, participants will be notified
         * that the track has been removed.
         */

        if (localMedia != null && localVideoTrack != null) {
            localMedia.removeVideoTrack(localVideoTrack);
            localVideoTrack = null;
            Log.e(TAG, "onPause");
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

        Log.e(TAG, "onDestroy");
        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != RoomState.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }

        /*
         * Release the local media ensuring any memory allocated to audio or video is freed.
         */
        if (localMedia != null) {

            // This one for capture screen
            if (localVideoTrack != null) {
                localMedia.removeVideoTrack(localVideoTrack);
            }

            // Not this one
            localMedia.release();
            localMedia = null;
        }

        super.onDestroy();
    }

    private boolean checkPermissionForCameraAndMicrophone(){
        int resultCamera = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForCameraAndMicrophone(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(activity,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG).show();
        } else {

            Log.e(TAG, "requestPermissionForCameraAndMicrophone");
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE);
        }
    }

    private void createLocalMedia() {
        localMedia = LocalMedia.create(activity);

        // Share your microphone
        localAudioTrack = localMedia.addAudioTrack(true);

        // Share your camera
        cameraCapturer = new CameraCapturer(activity, CameraCapturer.CameraSource.FRONT_CAMERA);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        primaryVideoView.setMirror(true);
        localVideoTrack.addRenderer(primaryVideoView);
        localVideoView = primaryVideoView;
    }

    private void setAccessToken() {
        // Get Device Id
        String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        String userName = SharedPreferencesManager.getInstance(activity).getString(SharedPreferencesManager.Key.USER_NAME);

        presenter.requestTokenVideo(deviceId, userName);
    }

    private void connectToRoom(String roomName, String accessToken) {

        setAudioFocus(true);
        ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        room = Video.connect(activity, connectOptions, roomListener());
        setDisconnectAction();
    }

    /*
     * The initial state when there is no active conversation.
     */
    private void intializeUI() {
//        connectActionFab.setImageDrawable(ContextCompat.getDrawable(activity,
//                R.drawable.ic_call_white_24px));
//        connectActionFab.hide();
//        connectActionFab.setOnClickListener(connectActionClickListener());
        switchCameraActionFab.show();
        switchCameraActionFab.setOnClickListener(switchCameraClickListener());
        localVideoActionFab.show();
        localVideoActionFab.setOnClickListener(localVideoClickListener());
        muteActionFab.show();
        muteActionFab.setOnClickListener(muteClickListener());
    }

    /*
     * The actions performed during disconnect.
     */
    private void setDisconnectAction() {
//        connectActionFab.setImageDrawable(ContextCompat.getDrawable(activity,
//                R.drawable.ic_call_end_white_24px));
//        connectActionFab.show();
//        connectActionFab.setOnClickListener(disconnectClickListener());
    }

    /*
     * Creates an connect UI dialog
     */
    private void showConnectDialog() {
        EditText roomEditText = new EditText(activity);
        alertDialog = Dialog.createConnectDialog(roomEditText,
                connectClickListener(roomEditText), cancelConnectDialogClickListener(), activity);
        alertDialog.show();
    }

    /*
     * Called when participant joins the room
     */
    private void addParticipant(Participant participant) {
        /*
         * This app only displays video for one additional participant per Room
         */
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            Snackbar.make(thumbnailVideoView,
                    "Multiple participants are not currently support in this UI",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        participantIdentity = participant.getIdentity();
        videoStatusTextView.setText("Participant "+ participantIdentity + " joined");

        /*
         * Add participant renderer
         */
        if (participant.getMedia().getVideoTracks().size() > 0) {
            addParticipantVideo(participant.getMedia().getVideoTracks().get(0));
        }

        /*
         * Start listening for participant media events
         */
        participant.getMedia().setListener(mediaListener());
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private void addParticipantVideo(VideoTrack videoTrack) {
        moveLocalVideoToThumbnailView();
        primaryVideoView.setMirror(false);
        videoTrack.addRenderer(primaryVideoView);
    }

    private void moveLocalVideoToThumbnailView() {
        if (thumbnailVideoView.getVisibility() == View.GONE) {
            thumbnailVideoView.setVisibility(View.VISIBLE);
            localVideoTrack.removeRenderer(primaryVideoView);
            localVideoTrack.addRenderer(thumbnailVideoView);
            localVideoView = thumbnailVideoView;
            thumbnailVideoView.setMirror(cameraCapturer.getCameraSource() ==
                    CameraCapturer.CameraSource.FRONT_CAMERA);
        }
    }

    /*
     * Called when participant leaves the room
     */
    private void removeParticipant(Participant participant) {
        videoStatusTextView.setText("Participant "+participant.getIdentity()+ " left.");
        if (!participant.getIdentity().equals(participantIdentity)) {
            return;
        }

        /*
         * Remove participant renderer
         */
        if (participant.getMedia().getVideoTracks().size() > 0) {
            removeParticipantVideo(participant.getMedia().getVideoTracks().get(0));
        }
        participant.getMedia().setListener(null);
        moveLocalVideoToPrimaryView();
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeRenderer(primaryVideoView);
    }

    private void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            localVideoTrack.removeRenderer(thumbnailVideoView);
            thumbnailVideoView.setVisibility(View.GONE);
            localVideoTrack.addRenderer(primaryVideoView);
            localVideoView = primaryVideoView;
            if (primaryVideoView != null)
                primaryVideoView.setMirror(cameraCapturer.getCameraSource() ==
                    CameraCapturer.CameraSource.BACK_CAMERA);
        }
    }

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                videoStatusTextView.setText("Connected to " + room.getName());

                for (Map.Entry<String, Participant> entry : room.getParticipants().entrySet()) {
                    addParticipant(entry.getValue());
                    break;
                }
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                videoStatusTextView.setText("Failed to connect");
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                videoStatusTextView.setText("Disconnected from " + room.getName());
                MainFragment.this.room = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    setAudioFocus(false);
                    intializeUI();
                    moveLocalVideoToPrimaryView();
                }
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                addParticipant(participant);

            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                removeParticipant(participant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }

    private Media.Listener mediaListener() {
        return new Media.Listener() {

            @Override
            public void onAudioTrackAdded(Media media, AudioTrack audioTrack) {
                videoStatusTextView.setText("onAudioTrackAdded");
            }

            @Override
            public void onAudioTrackRemoved(Media media, AudioTrack audioTrack) {
                videoStatusTextView.setText("onAudioTrackRemoved");
            }

            @Override
            public void onVideoTrackAdded(Media media, VideoTrack videoTrack) {
                videoStatusTextView.setText("onVideoTrackAdded");
                addParticipantVideo(videoTrack);
            }

            @Override
            public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
                Log.e(TAG, "onVideoTrackRemoved");
                videoStatusTextView.setText("onVideoTrackRemoved");
                removeParticipantVideo(videoTrack);
            }

            @Override
            public void onAudioTrackEnabled(Media media, AudioTrack audioTrack) {

            }

            @Override
            public void onAudioTrackDisabled(Media media, AudioTrack audioTrack) {

            }

            @Override
            public void onVideoTrackEnabled(Media media, VideoTrack videoTrack) {

            }

            @Override
            public void onVideoTrackDisabled(Media media, VideoTrack videoTrack) {

            }
        };
    }

    private DialogInterface.OnClickListener connectClickListener(final EditText roomEditText) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Connect to room
                 */
//                connectToRoom(roomEditText.getText().toString());
            }
        };
    }

    private View.OnClickListener disconnectClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Disconnect from room
                 */
                if (room != null) {
                    room.disconnect();
                }
                intializeUI();
            }
        };
    }

    private View.OnClickListener connectActionClickListener() {
        return new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showConnectDialog();
            }
        };
    }

    private DialogInterface.OnClickListener cancelConnectDialogClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intializeUI();
                alertDialog.dismiss();
            }
        };
    }

    private View.OnClickListener switchCameraClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraCapturer != null) {
                    CameraCapturer.CameraSource cameraSource = cameraCapturer.getCameraSource();
                    cameraCapturer.switchCamera();
                    if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                        thumbnailVideoView.setMirror(cameraSource == CameraCapturer.CameraSource.BACK_CAMERA);
                    } else {
                        primaryVideoView.setMirror(cameraSource == CameraCapturer.CameraSource.BACK_CAMERA);
                    }
                }
            }
        };
    }

    private View.OnClickListener localVideoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Enable/disable the local video track
                 */
                if (localVideoTrack != null) {
                    boolean enable = !localVideoTrack.isEnabled();
                    localVideoTrack.enable(enable);
                    int icon;
                    if (enable) {
                        icon = R.drawable.ic_videocam_green_24px;
                        switchCameraActionFab.show();
                    } else {
                        icon = R.drawable.ic_videocam_off_red_24px;
                        switchCameraActionFab.hide();
                    }
                    localVideoActionFab.setImageDrawable(
                            ContextCompat.getDrawable(activity, icon));
                }
            }
        };
    }

    private View.OnClickListener muteClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Enable/disable the local audio track. The results of this operation are
                 * signaled to other Participants in the same Room. When an audio track is
                 * disabled, the audio is muted.
                 */
                if (localAudioTrack != null) {
                    boolean enable = !localAudioTrack.isEnabled();
                    localAudioTrack.enable(enable);
                    int icon = enable ?
                            R.drawable.ic_mic_green_24px : R.drawable.ic_mic_off_red_24px;
                    muteActionFab.setImageDrawable(ContextCompat.getDrawable(
                           activity, icon));
                }
            }
        };
    }



    private void setAudioFocus(boolean focus) {
        if (focus) {
            previousAudioMode = audioManager.getMode();
            // Request audio focus before making any device switch.
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            /*
             * Use MODE_IN_COMMUNICATION as the default audio mode. It is required
             * to be in this mode when playout and/or recording starts for the best
             * possible VoIP performance. Some devices have difficulties with
             * speaker mode if this is not set.
             */
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(previousAudioMode);
            audioManager.abandonAudioFocus(null);
        }
    }

    private void startScreenCapture() {
        localMedia.removeVideoTrack(localVideoTrack);
        localVideoTrack = localMedia.addVideoTrack(true, screenCapturer);
        localVideoTrack.removeRenderer(thumbnailVideoView);
        localVideoTrack.addRenderer(thumbnailVideoView);
        screenCaptureMenuItem.setIcon(R.drawable.ic_stop_screen_share_white_24dp);
        screenCaptureMenuItem.setTitle(R.string.stop_screen_share);
    }

    private void stopScreenCapture() {
        localVideoTrack.removeRenderer(thumbnailVideoView);
        localMedia.removeVideoTrack(localVideoTrack);


        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        localVideoTrack.addRenderer(localVideoView);


        screenCaptureMenuItem.setIcon(R.drawable.ic_share_screen);
        screenCaptureMenuItem.setTitle(R.string.share_screen);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != AppCompatActivity.RESULT_OK) {
                Toast.makeText(activity, R.string.screen_capture_permission_not_granted,
                        Toast.LENGTH_LONG).show();
                return;
            }
            screenCapturer = new ScreenCapturer(activity, resultCode, data, screenCapturerListener);
            startScreenCapture();
        }
    }

    private void requestScreenCapturePermission() {
        Log.d(TAG, "Requesting permission to capture screen");
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // This initiates a prompt dialog for the user to confirm screen projection.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    @Override
    public void onDestroyView() {
        if (room != null) {
            room.disconnect();
        }
        super.onDestroyView();
    }



    private void loadChannels() {
        final String nameRoomChat = SharedPreferencesManager.getInstance(activity).getString(SharedPreferencesManager.Key.NAME_OF_ROOM_CHAT);
        chatClient.getChannels().getChannel(nameRoomChat, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                if (channel != null) {
                    joinChannel(channel);
                } else {
                    chatClient.getChannels().createChannel(nameRoomChat,
                            Channel.ChannelType.PUBLIC, new CallbackListener<Channel>() {
                                @Override
                                public void onSuccess(Channel channel) {
                                    if (channel != null) {
                                        joinChannel(channel);
                                    }
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {
                                    Log.e(TAG,"Error creating channel: " + errorInfo.getErrorText());
                                }
                            });
                }
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG,"Error retrieving channel: " + errorInfo.getErrorText());
            }

        });

    }

    private void joinChannel(final Channel channel) {
        Log.d(TAG, "Joining Channel: " + channel.getUniqueName());
        channel.join(new StatusListener() {
            @Override
            public void onSuccess() {
                MainFragment.this.channel = channel;
                Log.d(TAG, "Joined default channel");
                MainFragment.this.channel.addListener(defaultChannelListener);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG,"Error joining channel: " + errorInfo.getErrorText());
            }
        });
    }

    private CallbackListener<ChatClient> chatClientCallbackListener =
            new CallbackListener<ChatClient>() {
                @Override
                public void onSuccess(ChatClient chatClient) {
                    MainFragment.this.chatClient = chatClient;
                    loadChannels();
                    Log.d(TAG, "Success creating Twilio Chat Client");
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.e(TAG,"Error creating Twilio Chat Client: " + errorInfo.getErrorText());
                }
            };

    private ChannelListener defaultChannelListener = new ChannelListener() {
        @Override
        public void onMessageAdd(final Message message) {
            Log.d(TAG, "Message added");
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // need to modify user interface elements on the UI thread
                    messages.add(message);
                    messagesAdapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public void onMessageChange(Message message) {
            Log.d(TAG, "Message changed: " + message.getMessageBody());
        }

        @Override
        public void onMessageDelete(Message message) {
            Log.d(TAG, "Message deleted");
        }

        @Override
        public void onMemberJoin(Member member) {
            Log.d(TAG, "Member joined: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onMemberChange(Member member) {
            Log.d(TAG, "Member changed: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onMemberDelete(Member member) {
            Log.d(TAG, "Member deleted: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onTypingStarted(Member member) {
            Log.d(TAG, "Started Typing: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onTypingEnded(Member member) {
            Log.d(TAG, "Ended Typing: " + member.getUserInfo().getIdentity());
        }

        @Override
        public void onSynchronizationChange(Channel channel) {

        }
    };

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            public TextView messageTextView;

            public ViewHolder(TextView textView) {
                super(textView);
                messageTextView = textView;
            }
        }

        public MessagesAdapter() {

        }

        @Override
        public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                              int viewType) {
            TextView messageTextView = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_chatting_item, parent, false);
            return new MessagesAdapter.ViewHolder(messageTextView);
        }

        @Override
        public void onBindViewHolder(MessagesAdapter.ViewHolder holder, int position) {
            Message message = messages.get(position);
            String messageText = String.format("%s: %s", message.getAuthor(), message.getMessageBody());
            holder.messageTextView.setText(messageText);

        }

        @Override
        public int getItemCount() {
            return messages.size();
        }
    }


}
