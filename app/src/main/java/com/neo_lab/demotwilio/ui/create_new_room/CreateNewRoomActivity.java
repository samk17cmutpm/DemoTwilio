package com.neo_lab.demotwilio.ui.create_new_room;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.share_preferences_manager.SharedPreferencesManager;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;

public class CreateNewRoomActivity extends AppCompatActivity {

    private CreateNewRoomContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_room);

        CreateNewRoomFragment fragment =
                (CreateNewRoomFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (fragment == null) {
            fragment = CreateNewRoomFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), fragment, R.id.contentFrame
            );
        }

        presenter = new CreateNewRoomPresenter(fragment);
    }
}
