package com.neo_lab.demotwilio.ui.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;

public class MainActivity extends AppCompatActivity {

    private MainContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainFragment fragment =
                (MainFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (fragment == null) {
            fragment = MainFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), fragment, R.id.contentFrame
            );
        }

        presenter = new MainPresenter(fragment);

    }
}
