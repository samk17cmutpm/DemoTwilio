package com.neo_lab.demotwilio.ui.create_new_room;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CreateNewRoomFragment extends Fragment implements CreateNewRoomContract.View {

    private CreateNewRoomContract.Presenter presenter;

    private View root;

    private Activity activity;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.ed_room_name) EditText edRoomName;

    public CreateNewRoomFragment() {
        // Required empty public constructor
    }

    public static CreateNewRoomFragment newInstance() {
        CreateNewRoomFragment fragment = new CreateNewRoomFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_create_new_room, container, false);
        ButterKnife.bind(this, root);

        showUI();

        return root;
    }

    @Override
    public void setPresenter(CreateNewRoomContract.Presenter presenter) {

        this.presenter = presenter;

    }

    @Override
    public void showUI() {

        activity = getActivity();

        ToolbarUtils.initialize(toolbar, activity, R.string.app_name);

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.bt_connect)
    public void storeRoomName() {
        presenter.storeNewRoomName(edRoomName.getText().toString());
    }

}
