package com.neo_lab.demotwilio.ui.main;

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
    }

    interface Presenter extends BasePresenter {

    }
}
