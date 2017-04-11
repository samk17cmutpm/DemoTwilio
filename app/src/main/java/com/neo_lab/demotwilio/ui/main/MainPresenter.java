package com.neo_lab.demotwilio.ui.main;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public class MainPresenter implements MainContract.Presenter {

    private static final String TAG = MainPresenter.class.getName();

    private final MainContract.View view;

    public MainPresenter(MainContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
