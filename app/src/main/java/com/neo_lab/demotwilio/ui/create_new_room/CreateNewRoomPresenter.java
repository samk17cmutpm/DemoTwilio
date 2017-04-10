package com.neo_lab.demotwilio.ui.create_new_room;

import android.util.Log;

/**
 * Created by sam_nguyen on 10/04/2017.
 */

public class CreateNewRoomPresenter implements CreateNewRoomContract.Presenter {

    private static final String TAG = CreateNewRoomPresenter.class.getName();

    private final CreateNewRoomContract.View view;

    public CreateNewRoomPresenter(CreateNewRoomContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void storeNewRoomName(String roomName) {
        Log.e(TAG, roomName);
    }
}
