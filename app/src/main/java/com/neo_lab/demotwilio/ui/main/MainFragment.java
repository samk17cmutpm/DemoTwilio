package com.neo_lab.demotwilio.ui.main;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.ui.chatting.ChattingActivity;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainFragment extends Fragment implements MainContract.View {

    private MainContract.Presenter presenter;

    private View root;

    private Activity activity;

    @BindView(R.id.toolbar) Toolbar toolbar;

    public MainFragment() {
        // Required empty public constructor
    }


    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record_screen:
                return true;
            case R.id.action_share_screen:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        ButterKnife.bind(this, root);

        showUI();

        return root;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {

        this.presenter = presenter;

    }

    @Override
    public void showUI() {

        activity = getActivity();

        ToolbarUtils.initialize(toolbar, activity, R.string.app_name, R.drawable.ic_message);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                navigateToChattingRoom();

            }
        });

    }

    @Override
    public void initializeVideoRoom() {

    }

    @Override
    public void initializeCamera() {

    }

    @Override
    public void navigateToChattingRoom() {
        ActivityUtils.startActivity(activity, ChattingActivity.class);
    }
}
