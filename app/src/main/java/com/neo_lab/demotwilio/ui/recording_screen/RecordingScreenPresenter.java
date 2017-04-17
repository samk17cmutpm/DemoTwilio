package com.neo_lab.demotwilio.ui.recording_screen;

import com.neo_lab.demotwilio.domain.services.TokenService;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by sam_nguyen on 17/04/2017.
 */

public class RecordingScreenPresenter implements RecordingScreenContract.Presenter{

    private static final String TAG = RecordingScreenPresenter.class.getName();

    private final RecordingScreenContract.View view;

    private TokenService service;

    private CompositeSubscription subscriptions;


    public RecordingScreenPresenter(RecordingScreenContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void requestTokenVideo(String deviceId, String userName) {

    }

    @Override
    public void requestToken(String deviceId, String userName) {

    }
}
