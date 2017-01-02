package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class LiveActivity extends BaseActivity implements Publisher.PublisherListener,
        Session.SessionListener {

    private static final int MAX_NUM_SUBSCRIBERS = 3;

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, LiveActivity.class);
        return intent;
    }

    @Inject
    User user;

    @BindView(R.id.layoutTop)
    ViewGroup layoutTop;

    @BindView(R.id.layoutBottom)
    ViewGroup layoutBottom;

    // VARIABLES
    private Unbinder unbinder;
    private Session session;
    private Publisher publisher;
    private List<Subscriber> subscriberList = new ArrayList<>();
    private Map<Stream, Subscriber> subscriberStreamList = new HashMap<>();

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();
        initResources();
        initPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session == null) return;

        session.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (session == null) return;

        session.onPause();

        if (isFinishing()) disconnectSession();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        disconnectSession();
        super.onDestroy();
    }

    private void init() {

    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initResources() {

    }

    private void initPermissions() {
        subscriptions.add(RxPermissions.getInstance(LiveActivity.this)
                .request(PermissionUtils.PERMISSIONS_LIVE)
                .subscribe(granted -> {
                    session = new Session(LiveActivity.this, BuildConfig.OPEN_TOK_API_KEY, BuildConfig.OPEN_TOK_SESSION_ID);
                    session.setSessionListener(this);
                    session.connect(BuildConfig.OPEN_TOK_TOKEN);
                }));
    }

    @Override
    public void onConnected(Session session) {
        Timber.d("onConnected: Connected to session " + session.getSessionId());

        publisher = new Publisher(this, "publisher");
        publisher.setPublisherListener(this);
        publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        layoutBottom.addView(publisher.getView());
        this.session.publish(publisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Timber.d("onDisconnected: disconnected from session " + session.getSessionId());
        this.session = null;
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Timber.d("onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());

        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Timber.d("onStreamReceived: New stream " + stream.getStreamId() + " in session " + this.session.getSessionId());

        if (subscriberList.size() + 1 > MAX_NUM_SUBSCRIBERS) {
            Toast.makeText(this, "New subscriber ignored. MAX_NUM_SUBSCRIBERS limit reached.", Toast.LENGTH_LONG).show();
            return;
        }

        final Subscriber subscriber = new Subscriber(this, stream);
        this.session.subscribe(subscriber);
        subscriberList.add(subscriber);
        subscriberStreamList.put(stream, subscriber);

        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        layoutTop.addView(subscriber.getView());
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Timber.d("onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

        Subscriber subscriber = subscriberStreamList.get(stream);
        if (subscriber == null) {
            return;
        }

        subscriberList.remove(subscriber);
        subscriberStreamList.remove(stream);

        layoutTop.removeView(subscriber.getView());
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Timber.d("onStreamCreated: Own stream " + stream.getStreamId() + " created");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Timber.d("onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void disconnectSession() {
        if (session == null) {
            return;
        }

        if (subscriberList.size() > 0) {
            for (Subscriber subscriber : subscriberList) {
                if (subscriber != null) {
                    session.unsubscribe(subscriber);
                    subscriber.destroy();
                }
            }
        }

        if (publisher != null) {
            layoutBottom.removeView(publisher.getView());
            session.unpublish(publisher);
            publisher.destroy();
            publisher = null;
        }

        session.disconnect();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}