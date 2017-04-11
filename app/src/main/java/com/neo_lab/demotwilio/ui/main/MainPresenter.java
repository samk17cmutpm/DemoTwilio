package com.neo_lab.demotwilio.ui.main;

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

public class MainPresenter implements MainContract.Presenter {

    private static final String TAG = MainPresenter.class.getName();

    private final MainContract.View view;

    private TokenService service;

    private CompositeSubscription subscriptions;

    public MainPresenter(MainContract.View view) {
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
                            view.onListenerRequestVideoToken(true, "Success", tokenServerResponse.body());
                        } else {
                            view.onListenerRequestVideoToken(false, "Failed", null);
                        }

                    }
                })
        );

    }
}
