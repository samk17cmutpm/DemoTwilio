package com.neo_lab.demotwilio.ui.create_new_room;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.neo_lab.demotwilio.R;
import com.neo_lab.demotwilio.share_preferences_manager.SharedPreferencesManager;
import com.neo_lab.demotwilio.ui.main.MainActivity;
import com.neo_lab.demotwilio.ui.video_calling_room.VideoCallingRoomActivity;
import com.neo_lab.demotwilio.utils.activity.ActivityUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CreateNewRoomFragment extends Fragment implements CreateNewRoomContract.View {


    private CreateNewRoomContract.Presenter presenter;

    private View root;

    @BindView(R.id.bt_customer) Button btCustomer;

    @BindView(R.id.bt_company) Button btCompany;

    @BindView(R.id.rl_customer) RelativeLayout rlCustomer;

    @BindView(R.id.rl_company) RelativeLayout rlCompnay;

    @BindView(R.id.im_connect_to_room) ImageView imConnectToRoom;

    @BindView(R.id.ed_room_existed) EditText edRoomExisted;

    @BindView(R.id.tv_customer_name_room) TextView tvCustomerNameRoom;

    @BindView(R.id.bt_ok) Button btOk;

    private Activity activity;


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

        tvCustomerNameRoom.setText(presenter.generateRoomNumber());
    }


    @Override
    public void updateUIStatusForTabButton(Button btActive, Button btNotActive) {

        final int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            btActive.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.border_tab_button_active));
            btNotActive.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.border_tab_button_not_active));
        } else {
            btActive.setBackground(ContextCompat.getDrawable(activity, R.drawable.border_tab_button_active));
            btNotActive.setBackground(ContextCompat.getDrawable(activity, R.drawable.border_tab_button_not_active));
        }

    }


    @Override
    public boolean validateInputsForRoomExisted() {

        String roomNumberExisted = edRoomExisted.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(roomNumberExisted)) {
            this.edRoomExisted.setError(getString(R.string.error_general_input_empty));
            focusView = this.edRoomExisted;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
        }
        return !cancel;
    }


    @Override
    public void navigateToVideoCallingActivity() {
        ActivityUtils.startActivity(activity, VideoCallingRoomActivity.class);
    }

    @Override
    public void storeLocalData(SharedPreferencesManager.Key key, String value) {
        SharedPreferencesManager.getInstance(activity).put(key, value);
    }

    @Override
    public void showDialogToEnterUserName() {
        new MaterialDialog.Builder(activity)
                .title(R.string.app_name)
                .content(R.string.user_name_hint)
                .cancelable(false)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .alwaysCallInputCallback()
                .input(R.string.user_connect_name_title, R.string.emtpy, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {

                        if (input.toString().isEmpty()) {

                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

                        } else {

                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);

                            storeLocalData(SharedPreferencesManager.Key.USER_NAME, input.toString());

                        }

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        navigateToVideoCallingActivity();

                    }
                })
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.bt_customer)
    public void onButtonCustomerClick() {

        rlCompnay.setVisibility(View.GONE);
        rlCustomer.setVisibility(View.VISIBLE);

        updateUIStatusForTabButton(btCustomer, btCompany);

        tvCustomerNameRoom.setText(presenter.generateRoomNumber());

    }

    @OnClick(R.id.bt_company)
    public void onButtonCompanyClick() {

        rlCustomer.setVisibility(View.GONE);
        rlCompnay.setVisibility(View.VISIBLE);

        updateUIStatusForTabButton(btCompany, btCustomer);

        edRoomExisted.requestFocus();

    }

    @OnClick(R.id.bt_ok)
    public void onButtonOkClick() {

        if (validateInputsForRoomExisted()) {
            storeLocalData(SharedPreferencesManager.Key.ROOM_NUMBER, edRoomExisted.toString().toString());
            showDialogToEnterUserName();
        }

    }

    @OnClick(R.id.im_connect_to_room)
    public void onImageViewConnectToRoomClick() {

        storeLocalData(SharedPreferencesManager.Key.ROOM_NUMBER, tvCustomerNameRoom.getText().toString());
        showDialogToEnterUserName();

    }


}
