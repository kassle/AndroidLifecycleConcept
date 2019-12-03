package org.krybrig.concept.androidlifecycle;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LifecycleDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("KASSLE", "Lifecycle Dialog onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("KASSLE", "Lifecycle Dialog onCreateView");
        View view = inflater.inflate(R.layout.dialog_lifecycle, container);

        view.findViewById(R.id.btDismiss)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LifecycleDialogFragment.this.dismiss();
                    }
                });

        getDialog().setCanceledOnTouchOutside(false);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        Log.e("KASSLE", "Lifecycle Dialog onAttach");
        super.onAttach(context);
        Log.e("KASSLE", "Lifecycle Dialog onAttached");
    }

    @Override
    public void onDestroy() {
        Log.e("KASSLE", "Lifecycle Dialog onDestroy");
        super.onDestroy();
        Log.e("KASSLE", "Lifecycle Dialog onDestroyed");
    }
}
