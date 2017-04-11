package com.neo_lab.demotwilio.ui.main;

import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.ui.base.BasePresenter;
import com.neo_lab.demotwilio.ui.base.BaseView;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public interface MainContract {

    interface View extends BaseView<Presenter> {

        void showUI();

        void initializeVideoRoom();

        void initializeCamera();

        void navigateToChattingRoom();

        void onListenerRequestVideoToken(boolean status, String message, Token token);

        void updateStatusRequestVideoToken(boolean status, String message);
    }

    interface Presenter extends BasePresenter {

        void requestTokenVideo(String deviceId, String userName);

    }
}
