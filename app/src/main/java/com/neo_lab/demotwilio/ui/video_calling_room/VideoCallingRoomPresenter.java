package com.neo_lab.demotwilio.ui.video_calling_room;

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
 * Created by sam_nguyen on 18/04/2017.
 */

public class VideoCallingRoomPresenter implements VideoCallingRoomContract.Presenter {

    private static final String TAG = VideoCallingRoomPresenter.class.getName();

    private final VideoCallingRoomContract.View view;

    private TokenService service;

    private CompositeSubscription subscriptions;

    public VideoCallingRoomPresenter(VideoCallingRoomContract.View view) {
        this.view = view;
        this.view.setPresenter(this);
        this.service = ServiceGenerator.createService(TokenService.class);
        this.subscriptions = new CompositeSubscription();
    }

    @Override
    public void start() {

    }

    @Override
    public void requestTokenCallingVideo(String deviceId, String userName) {

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
                        view.onListenerRequestVideoToken(false, "Internet is not available\nCheck and try again", null);
                    }
                })
        );

    }
}
