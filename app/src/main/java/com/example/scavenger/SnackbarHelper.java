package com.example.scavenger;

import android.app.Activity;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {


        private static final int BACKGROUND_COLOR = 0xbf323232;
        private Snackbar messageSnackbar;
        private enum DismissBehavior { HIDE, SHOW, FINISH }

        private int maxLines = 2;
        private String lastMessage = "";

        /** Shows a snackbar with a given message. */
        public void showMessage(Activity activity, String message) {
            if (!message.isEmpty() && (messageSnackbar == null || !lastMessage.equals(message))) {
                lastMessage = message;
                show(activity, message, DismissBehavior.HIDE);
            }
        }

        private void show(
                final Activity activity, final String message, final DismissBehavior dismissBehavior) {
            activity.runOnUiThread(() -> {
                messageSnackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);

                if (dismissBehavior != DismissBehavior.HIDE) {
                    messageSnackbar.setAction("Dismiss", v -> messageSnackbar.dismiss());

                    if (dismissBehavior == DismissBehavior.FINISH) {

                        messageSnackbar.addCallback(
                                new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        super.onDismissed(transientBottomBar, event);
                                        activity.finish();
                                    }
                                });
                    }
                }
                ((TextView)
                        messageSnackbar.getView().findViewById(R.id.snackbar_text)).setMaxLines(maxLines);
                messageSnackbar.show();
            });
        }
}

