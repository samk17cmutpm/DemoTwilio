package com.neo_lab.demotwilio.ui.chatting;

import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.ui.base.BasePresenter;
import com.neo_lab.demotwilio.ui.base.BaseView;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public interface ChattingContract {

    interface View extends BaseView<Presenter> {

        void showUI();

        void onListenerRequestChattingToken(boolean status, String message, Token token);

        void updateStatusRequestChattingToken(boolean status, String message);

        void createChattingRom(String accessToken);
    }

    interface Presenter extends BasePresenter {

        void requestToken(String deviceId, String userName);

    }
}
