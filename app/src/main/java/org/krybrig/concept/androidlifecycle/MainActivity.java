package org.krybrig.concept.androidlifecycle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String STATE_KEY_PROGRESSBAR = "org.krybrig.state.progressbar";
    private static final String STATE_KEY_BUTTON = "org.krybrig.state.button";
    private static final String STATE_KEY_RESUME_LIVE_DATA = "org.krybrig.state.livedata";
    private static final int MAX_PROGRESSBAR = 10;

    private boolean resume;
    private DataProducer producer;
    private Button button;
    private ProgressBar pgbar;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        producer = new DataProducer();

        button = findViewById(R.id.btCount);
        pgbar = findViewById(R.id.pgbar);
        pgbar.setMax(MAX_PROGRESSBAR);

        if (savedInstanceState != null) {
            int progress = savedInstanceState.getInt(STATE_KEY_PROGRESSBAR, 0);
            pgbar.setProgress(progress);
            button.setEnabled(savedInstanceState.getBoolean(STATE_KEY_BUTTON, true));
            resume = savedInstanceState.getBoolean(STATE_KEY_RESUME_LIVE_DATA, false);

            Log.w("COUNTER", "Load progress: " + pgbar.getProgress() + "; button = " + button.isEnabled() + "; resume = " + resume);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveData();
            }
        });

        if (resume) {
            resumeLiveData();
        }
    }

    @Override
    protected void onStop() {
        button.setOnClickListener(null);

        if (disposable!= null) {
            disposable.dispose();
        }

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_KEY_BUTTON, button.isEnabled());
        outState.putInt(STATE_KEY_PROGRESSBAR, pgbar.getProgress());
        outState.putBoolean(STATE_KEY_RESUME_LIVE_DATA, resume);

        super.onSaveInstanceState(outState);
    }

    private void startLiveData() {
        resume = true;
        button.setEnabled(false);
        pgbar.setProgress(0);

        ValueHolder.getInstance().setObservableSource(producer.retrieve(MAX_PROGRESSBAR));
        resumeLiveData();
    }

    private void resumeLiveData() {
        disposable = ValueHolder.getInstance().getLiveValue()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        button.setEnabled(true);
                        resume = false;
                        Toast.makeText(MainActivity.this, R.string.toast_task_completed, Toast.LENGTH_SHORT).show();
                        Log.w("COUNTER", "Completed");
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        pgbar.setProgress(integer);
                        Log.w("COUNTER", "Live data " + integer);
                    }
                });
    }
}
