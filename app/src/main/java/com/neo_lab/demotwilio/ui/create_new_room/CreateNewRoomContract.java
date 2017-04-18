package com.neo_lab.demotwilio.ui.create_new_room;

import android.widget.Button;

import com.neo_lab.demotwilio.ui.base.BasePresenter;
import com.neo_lab.demotwilio.ui.base.BaseView;

/**
 * Created by sam_nguyen on 10/04/2017.
 */

public interface CreateNewRoomContract {

    interface View extends BaseView<Presenter> {

        void showUI();

        void updateUIStatusForTabButton(Button btActive, Button btNotActive);

        boolean validateInputsForRoomExisted();

        void navigateToVideoCallingActivity();

        void storeNewRoomNumber(String roomNumber);

    }

    interface Presenter extends BasePresenter {

        String generateRoomNumber();

    }
}
