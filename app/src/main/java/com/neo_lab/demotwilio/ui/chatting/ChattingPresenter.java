package com.neo_lab.demotwilio.ui.chatting;

import com.neo_lab.demotwilio.domain.generator.ServiceGenerator;
import com.neo_lab.demotwilio.domain.response.TokenServer;
import com.neo_lab.demotwilio.domain.services.TokenService;


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
                .subscribe(new Subscriber<Response<TokenServer>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Response<TokenServer> tokenServerResponse) {

                        if (tokenServerResponse.isSuccessful()) {
                            view.onListenerRequestToken(true, "Success", tokenServerResponse.body());
                        } else {
                            view.onListenerRequestToken(false, "Failed", null);
                        }

                    }
                })
        );


    }
}
