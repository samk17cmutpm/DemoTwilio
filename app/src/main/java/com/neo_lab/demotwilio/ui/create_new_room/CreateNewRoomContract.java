package com.neo_lab.demotwilio.ui.create_new_room;

import com.neo_lab.demotwilio.ui.base.BasePresenter;
import com.neo_lab.demotwilio.ui.base.BaseView;

/**
 * Created by sam_nguyen on 10/04/2017.
 */

public interface CreateNewRoomContract {

    interface View extends BaseView<Presenter> {
        void showUI();
    }

    interface Presenter extends BasePresenter {
        void storeNewRoomName(String roomName);
    }
}
