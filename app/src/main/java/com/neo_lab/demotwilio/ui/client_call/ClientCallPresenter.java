package com.neo_lab.demotwilio.ui.client_call;

import com.neo_lab.demotwilio.domain.error.APIError;
import com.neo_lab.demotwilio.domain.generator.ServiceGenerator;
import com.neo_lab.demotwilio.domain.response.TokenServer;
import com.neo_lab.demotwilio.domain.services.TokenService;
import com.neo_lab.demotwilio.ui.base.BaseSubscriber;


import retrofit2.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by sam_nguyen on 17/04/2017.
 */

public class ClientCallPresenter implements ClientCallContract.Presenter {

    private static final String TAG = ClientCallPresenter.class.getName();

    private final ClientCallContract.View view;

    private TokenService service;

    private CompositeSubscription subscriptions;

    public ClientCallPresenter(ClientCallContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
        this.service = ServiceGenerator.createService(TokenService.class);
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void start() {

    }

    @Override
    public void requestTokenClientCall(final ClientCallActivity.ClientProfile clientProfile) {

        Observable<Response<TokenServer>> observable = service.getTokenClientCall("https://dev1-dot-calling-dev.appspot.com/tokenClient");

        subscriptions.add(observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<TokenServer>() {
                    @Override
                    public void handleViewOnRequestSuccess(TokenServer data) {

                        view.onListenerRequestClientCall(true, "Request Successfully", data, clientProfile);

                    }

                    @Override
                    public void handleViewOnRequestError(APIError apiError) {
                        view.onListenerRequestClientCall(false, "Request Failed", null, null);
                    }

                    @Override
                    public void handleViewOnConnectSeverError() {

                    }
                })
        );

    }
}
