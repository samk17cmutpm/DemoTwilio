package com.neo_lab.demotwilio.ui.connecting_room;

import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.ui.base.BasePresenter;
import com.neo_lab.demotwilio.ui.base.BaseView;

/**
 * Created by samnguyen on 4/15/17.
 */

public interface ConnectingRoomContract {

    interface View extends BaseView<Presenter> {

        void showUI();

        void initializeVideoRoom();

        void initializeRecordVideo();

        void initializeCaptureScreen();

        void initializeCamera();

        void initializeChattingRoom();

        void onListenerRequestVideoToken(boolean status, String message, Token token);

        void updateStatusRequestVideoToken(boolean status, String message);

        void onListenerRequestChattingToken(boolean status, String message, Token token);

        void updateStatusRequestChattingToken(boolean status, String message);

        void createChattingRom(String accessToken);

    }


    interface Presenter extends BasePresenter {

        void requestTokenVideo(String deviceId, String userName);

        void requestToken(String deviceId, String userName);

    }
}
