package com.neo_lab.demotwilio.ui.client_call;

import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.ui.base.BasePresenter;
import com.neo_lab.demotwilio.ui.base.BaseView;

/**
 * Created by sam_nguyen on 17/04/2017.
 */

public interface ClientCallContract {

    interface View extends BaseView<Presenter> {
        void getProperties();

        void showUI();

        void onListenerRequestClientCall(boolean status, String message, Token token, ClientCallActivity.ClientProfile clientProfile);
    }

    interface Presenter extends BasePresenter {
        void requestTokenClientCall(ClientCallActivity.ClientProfile clientProfile);
    }

}
