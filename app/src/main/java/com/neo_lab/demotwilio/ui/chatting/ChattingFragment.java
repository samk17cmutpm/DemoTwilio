package com.neo_lab.demotwilio.ui.chatting;


import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.model.Token;
import com.neo_lab.demotwilio.utils.toolbar.ToolbarUtils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ChattingFragment extends Fragment implements ChattingContract.View {

    private ChattingContract.Presenter presenter;

    private View root;

    private Activity activity;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.toolbar_title) TextView tvToolbarTitle;

    public ChattingFragment() {
        // Required empty public constructor
    }

    public static ChattingFragment newInstance() {
        ChattingFragment fragment = new ChattingFragment();
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
        root = inflater.inflate(R.layout.fragment_chatting, container, false);

        ButterKnife.bind(this, root);

        showUI();


        // Get Device Id
        String deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        presenter.requestToken(deviceId, "Nguyen_Van_Sam");

        return root;
    }

    @Override
    public void setPresenter(ChattingContract.Presenter presenter) {

        this.presenter = presenter;

    }

    @Override
    public void showUI() {

        activity = getActivity();

        ToolbarUtils.initialize(toolbar, activity, R.string.message_general_loading, R.drawable.ic_cancel);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
                activity.onBackPressed();
            }
        });

    }

    @Override
    public void onListenerRequestToken(boolean status, String message, Token token) {

    }
}
