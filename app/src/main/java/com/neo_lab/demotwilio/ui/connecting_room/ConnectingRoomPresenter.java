package com.neo_lab.demotwilio.ui.connecting_room;

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
 * Created by samnguyen on 4/15/17.
 */

public class ConnectingRoomPresenter implements ConnectingRoomContract.Presenter {

    private static final String TAG = ConnectingRoomPresenter.class.getName();

    private final ConnectingRoomContract.View view;

    private TokenService service;

    private CompositeSubscription subscriptions;

    public ConnectingRoomPresenter(ConnectingRoomContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
        this.service = ServiceGenerator.createService(TokenService.class);
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void start() {

    }

    @Override
    public void requestTokenVideo(String deviceId, String userName) {

        Observable<Response<TokenServer>> observable =
                service.getTokenVideo(deviceId, userName);

        subscriptions.add(observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<TokenServer>() {
                    @Override
                    public void handleViewOnRequestSuccess(TokenServer data) {

                        view.onListenerRequestVideoToken(true, "Connected Successfully", data);

                    }

                    @Override
                    public void handleViewOnRequestError(APIError apiError) {
                        view.onListenerRequestVideoToken(false, apiError.message(), null);
                    }

                    @Override
                    public void handleViewOnConnectSeverError() {

                    }
                })
        );

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
