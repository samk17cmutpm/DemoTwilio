package com.neo_lab.demotwilio.ui.chatting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;

public class ChattingActivity extends AppCompatActivity {

    private ChattingContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        ChattingFragment fragment =
                (ChattingFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (fragment == null) {
            fragment = ChattingFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), fragment, R.id.contentFrame
            );
        }

        presenter = new ChattingPresenter(fragment);
    }
}
