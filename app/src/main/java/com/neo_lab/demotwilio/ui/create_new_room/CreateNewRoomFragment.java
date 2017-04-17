package com.neo_lab.demotwilio.ui.create_new_room;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.share_preferences_manager.SharedPreferencesManager;
import com.neo_lab.demotwilio.ui.base.BaseKey;
import com.neo_lab.demotwilio.ui.connecting_room.ConnectingRoomActivity;
import com.neo_lab.demotwilio.ui.main.MainActivity;
import com.neo_lab.demotwilio.ui.recording_screen.RecordingScreenActivity;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CreateNewRoomFragment extends Fragment implements CreateNewRoomContract.View {


    private CreateNewRoomContract.Presenter presenter;

    private View root;

    private Activity activity;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.ed_room_name) EditText edRoomName;

    @BindView(R.id.ed_user_name) EditText edUserName;

    public CreateNewRoomFragment() {
        // Required empty public constructor
    }

    public static CreateNewRoomFragment newInstance() {
        CreateNewRoomFragment fragment = new CreateNewRoomFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_create_new_room, container, false);
        ButterKnife.bind(this, root);

        showUI();

        return root;
    }

    @Override
    public void setPresenter(CreateNewRoomContract.Presenter presenter) {

        this.presenter = presenter;

    }

    @Override
    public void showUI() {

        activity = getActivity();

        ToolbarUtils.initialize(toolbar, activity, R.string.app_name);

    }

    @Override
    public boolean validateInputs() {

        edRoomName.setError(null);

        String roomName = this.edRoomName.getText().toString();
        String userName = this.edUserName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(roomName)) {

            this.edRoomName.setError(getString(R.string.error_general_input_empty));

            focusView = this.edRoomName;
            cancel = true;

        }

        if (TextUtils.isEmpty(roomName)) {

            this.edUserName.setError(getString(R.string.error_general_input_empty));

            focusView = this.edUserName;
            cancel = true;

        }

        if (cancel) {
            focusView.requestFocus();
        } else {
        }
        return !cancel;
    }

    @Override
    public void storeNewRoomName(String roomName, String userName) {
        SharedPreferencesManager.getInstance(activity)
                .put(SharedPreferencesManager.Key.NAME_OF_ROOM_CHAT, roomName);
        SharedPreferencesManager.getInstance(activity)
                .put(SharedPreferencesManager.Key.USER_NAME, userName);

    }

    @Override
    public void navigateToMainActivity() {
//        ActivityUtils.startActivity(activity, MainActivity.class);

        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(String.valueOf(BaseKey.IS_RECONNECT_TO_RECORD_VIDEO), false);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.bt_connect)
    public void storeRoomName() {
        if (validateInputs()) {
            storeNewRoomName(edRoomName.getText().toString(), edUserName.getText().toString());
            navigateToMainActivity();
        }

    }

}
