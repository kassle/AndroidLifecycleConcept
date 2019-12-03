package org.krybrig.concept.androidlifecycle;

import android.app.ProgressDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final String STATE_KEY_PROGRESS = "org.krybrig.state.progress";
    private static final String STATE_KEY_BUTTON = "org.krybrig.state.btStartCount";
    private static final String STATE_KEY_RESUME_LIVE_DATA = "org.krybrig.state.livedata";
    private static final String STATE_KEY_PROGRESSBAR_AS_DIALOG = "org.krybrig.state.progressAsDialog";
    private static final String STATE_KEY_CHECKBOX_DIALOG = "org.krybrig.state.checkbox";
    private static final int MAX_PROGRESSBAR = 10;

    private boolean resume;

    private DataProducer producer;
    private Button btStartCount;
    private Button btDialogLifecycle;
    private CheckBox cbProgressAsDialog;

    private ProgressBar pgbar;
    private ProgressDialog pgdialog;
    private Disposable disposable;
    private int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("KASSLE", "Main activity onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        producer = new DataProducer();

        btStartCount = findViewById(R.id.btCount);
        btDialogLifecycle = findViewById(R.id.btDialogLifecycle);
        cbProgressAsDialog = findViewById(R.id.cbProgressDialog);

        pgbar = findViewById(R.id.pgbar);
        pgbar.setMax(MAX_PROGRESSBAR);

        pgdialog = new ProgressDialog(this);
        pgdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pgdialog.setCancelable(false);
        pgdialog.setCanceledOnTouchOutside(false);
        pgdialog.setMax(MAX_PROGRESSBAR);

        if (savedInstanceState != null) {
            restoreViewState(savedInstanceState);
        }

        Log.e("KASSLE", "Main activity onCreated");
    }

    private void restoreViewState(Bundle savedInstanceState) {
        boolean asDialog = savedInstanceState.getBoolean(STATE_KEY_PROGRESSBAR_AS_DIALOG, false);
        cbProgressAsDialog.setChecked(asDialog);
        cbProgressAsDialog.setEnabled(savedInstanceState.getBoolean(STATE_KEY_CHECKBOX_DIALOG, true));
        btStartCount.setEnabled(savedInstanceState.getBoolean(STATE_KEY_BUTTON, true));
        resume = savedInstanceState.getBoolean(STATE_KEY_RESUME_LIVE_DATA, false);

        progress = savedInstanceState.getInt(STATE_KEY_PROGRESS, 0);
        if (asDialog) {
            pgdialog.setProgress(progress);
        } else {
            pgbar.setProgress(progress);
        }
        Log.w("COUNTER", "Load progress: " + pgbar.getProgress() + "; btStartCount = " + btStartCount.isEnabled() + "; resume = " + resume);
    }

    @Override
    protected void onStart() {
        Log.e("KASSLE", "Main activity onStart");
        super.onStart();

        btStartCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveData();
            }
        });
        btDialogLifecycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLifecycleDialog();
            }
        });

        if (resume) {
            resumeLiveData();
        }

        Log.e("KASSLE", "Main activity onStarted");
    }

    @Override
    protected void onStop() {
        Log.e("KASSLE", "Main activity onStop");
        btStartCount.setOnClickListener(null);
        btDialogLifecycle.setOnClickListener(null);

        if (disposable!= null) {
            disposable.dispose();
        }

        super.onStop();
        Log.e("KASSLE", "Main activity onStoped");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_KEY_BUTTON, btStartCount.isEnabled());
        outState.putInt(STATE_KEY_PROGRESS, pgbar.getProgress());
        outState.putBoolean(STATE_KEY_PROGRESSBAR_AS_DIALOG, cbProgressAsDialog.isChecked());
        outState.putBoolean(STATE_KEY_CHECKBOX_DIALOG, cbProgressAsDialog.isEnabled());
        outState.putBoolean(STATE_KEY_RESUME_LIVE_DATA, resume);

        super.onSaveInstanceState(outState);
    }

    private void startLiveData() {
        resume = true;
        btStartCount.setEnabled(false);
        cbProgressAsDialog.setEnabled(false);

        progress = 0;
        pgbar.setProgress(progress);
        pgdialog.setProgress(progress);

        ValueHolder.getInstance().setObservableSource(producer.retrieve(MAX_PROGRESSBAR));
        resumeLiveData();
    }

    private void resumeLiveData() {
        disposable = ValueHolder.getInstance().getLiveValue()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        btStartCount.setEnabled(true);
                        resume = false;
                        cbProgressAsDialog.setEnabled(true);
                        Toast.makeText(MainActivity.this, R.string.toast_task_completed, Toast.LENGTH_SHORT).show();
                        Log.w("COUNTER", "Completed");
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (cbProgressAsDialog.isChecked()) {
                            pgdialog.dismiss();
                        }
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (cbProgressAsDialog.isChecked()) {
                            pgdialog.show();
                        }

                        updateProgress(progress);
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer progress) throws Exception {
                        Log.w("COUNTER", "Live data " + progress);
                        updateProgress(progress);
                    }
                });
    }

    private void updateProgress(int progress) {
        if (cbProgressAsDialog.isChecked()) {
            pgdialog.setProgress(progress);
        } else {
            pgbar.setProgress(progress);
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("KASSLE", "Main activity onDestroy");
        super.onDestroy();
        Log.e("KASSLE", "Main activity onDestroyed");
    }

    private void showLifecycleDialog() {
        DialogFragment dialogFragment = new LifecycleDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), LifecycleDialogFragment.class.getName());
    }
}
