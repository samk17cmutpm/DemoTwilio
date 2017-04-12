package com.neo_lab.demotwilio.ui.chatting;

import com.neo_lab.demotwilio.domain.error.APIError;
import com.neo_lab.demotwilio.domain.generator.ServiceGenerator;
import com.neo_lab.demotwilio.domain.response.TokenServer;
import com.neo_lab.demotwilio.domain.services.TokenService;
import com.neo_lab.demotwilio.ui.base.BaseSubscriber;


import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by sam_nguyen on 11/04/2017.
 */

public class ChattingPresenter implements ChattingContract.Presenter {

    private static final String TAG = ChattingPresenter.class.getName();

    private final ChattingContract.View view;

    private TokenService service;

    private CompositeSubscription subscriptions;

    public ChattingPresenter(ChattingContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
        this.service = ServiceGenerator.createService(TokenService.class);
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void start() {

    }

    @Override
    public void requestToken(String deviceId, String userName) {

        Observable<Response<TokenServer>> observable =
                service.getTokenChatting(deviceId, userName);

        subscriptions.add(observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<TokenServer>() {
                    @Override
                    public void handleViewOnRequestSuccess(TokenServer data) {
                        view.onListenerRequestChattingToken(true, "Success", data);
                    }

                    @Override
                    public void handleViewOnRequestError(APIError apiError) {
                        view.onListenerRequestChattingToken(false, apiError.message(), null);

                    }

                    @Override
                    public void handleViewOnConnectSeverError() {

                    }
                })
        );


    }
}
