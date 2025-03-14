package com.vitez.stopwatch2;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main activity of the app.
 */
public class MainActivity extends AppCompatActivity {

    TextView timerTextView;
    ListView logView;
    Long startTime = 0L;
    HistoryListAdapter listAdapter;
    ArrayList<String> logTimesList;
    Long lastThickTime = 0L;
    Boolean clockTicking = false;
    Boolean clockWasTicking = false;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            lastThickTime = System.currentTimeMillis();
            Long timeDiff = lastThickTime - startTime;
            refreshTextTimer(timeDiff, timerTextView);
            timerHandler.postDelayed(this, 100);
        }
    };

    /**
     * @param timeDiff Time difference in milliseconds.
     * @param textView TextView widget to update text with time.
     */
    private static void refreshTextTimer(Long timeDiff, TextView textView) {
        int seconds = (int) (timeDiff / 1000);
        Integer minutes = seconds / 60;
        int msec = (int) (((timeDiff / 1000.0) - seconds) * 100);
        msec = msec > 10 ? msec / 10 : 0;
        seconds = seconds % 60;

        textView.setText(String.format(Locale.ENGLISH, "%03d:%02d.%d", minutes, seconds, msec));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_clear_history) {
            logTimesList.clear();
            listAdapter.notifyDataSetChanged();
        } else if (itemId == R.id.timer_menu_time_edit) {
            MainActivity.this.openEditAlertDialog();
        } else if (itemId == R.id.timer_menu_clipboard) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(
                    getApplicationContext().getString(R.string.app_name),
                    timerTextView.getText().toString()
            );
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getBaseContext(), String.format(getString(R.string.main_clipboard_copied),
                    timerTextView.getText()), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = (TextView) findViewById(R.id.timerTextView);
        timerTextView.setText(R.string.text_time_start);
        logView = (ListView) findViewById(R.id.logList);

        logTimesList = new ArrayList<>();
        listAdapter = new HistoryListAdapter(this, logTimesList);
        Log.d("adapter", String.valueOf(listAdapter.getCount()));
        logView.setAdapter(listAdapter);

        final Button btnStart = (Button) findViewById(R.id.btnStart);
        final Button btnReset = (Button) findViewById(R.id.btnReset);

        btnStart.setText(R.string.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (clockTicking) {
                    timerHandler.removeCallbacks(timerRunnable);
                    btnReset.setText(R.string.btn_reset);
                    b.setText(R.string.btn_start);
                    clockTicking = false;
                } else {
                    if (clockWasTicking) {
                        // assume startTime was restored
                        clockWasTicking = false;
                    } else {
                        // continue from last time
                        if (lastThickTime > 0 && startTime > 0) {
                            // recalculate measured time and adjust start time
                            startTime = System.currentTimeMillis() - (lastThickTime - startTime);
                        } else {
                            startTime = System.currentTimeMillis();
                        }
                    }

                    clockTicking = true;
                    timerHandler.postDelayed(timerRunnable, 100);
                    b.setText(R.string.btn_stop);
                    btnReset.setText(R.string.btn_mark);
                }
            }
        });

        btnReset.setText(R.string.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String btnResetText = getBaseContext().getString(R.string.btn_reset);

                if (b.getText().equals(btnResetText)) {
                    timerHandler.removeCallbacks(timerRunnable);
                    timerTextView.setText(R.string.text_time_start);
                    startTime = 0L;
                    lastThickTime = 0L;
                } else {
                    logTimesList.add(timerTextView.getText().toString());
                    listAdapter.notifyDataSetChanged();
                }
            }
        });

        logView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(
                        getApplicationContext().getString(R.string.app_name),
                        logTimesList.get(position)
                );
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getBaseContext(), String.format(getString(R.string.main_clipboard_copied),
                        logTimesList.get(position)), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong("lastThickTime", lastThickTime);
        outState.putLong("startTime", startTime);
        outState.putBoolean("clockWasTicking", clockWasTicking);
        outState.putStringArrayList("logTimesList", logTimesList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        lastThickTime = savedInstanceState.getLong("lastThickTime");
        startTime = savedInstanceState.getLong("startTime");
        clockWasTicking = savedInstanceState.getBoolean("clockWasTicking");
        logTimesList = savedInstanceState.getStringArrayList("logTimesList");

        if (logTimesList == null) {
            logTimesList = new ArrayList<>();
        } else {
            listAdapter.setValues(logTimesList);
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (lastThickTime == 0) {
            return;
        }

        // start timer if resuming with last thick time
        if (clockWasTicking) {
            Button btnStart = (Button) findViewById(R.id.btnStart);
            if (btnStart != null) {
                btnStart.performClick();
            }
        } else {
            refreshTextTimer(lastThickTime - startTime, timerTextView);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // save state when android handles state automatically
        clockWasTicking = clockTicking;
        clockTicking = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void openEditAlertDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.timeedit_alert);
        dialog.setTitle(R.string.timer_dialog_title);

        final TextView text = (TextView) dialog.findViewById(R.id.textTimeEdit);
        text.setText(timerTextView.getText());

        Button dialogButton = (Button) dialog.findViewById(R.id.btnEditApply);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("(\\d{3}):(\\d{2})\\.(\\d)");
                Matcher match = pattern.matcher(text.getText());
                boolean validInput = false;
                if (match.matches()) {
                    long minutes = Long.parseLong(Objects.requireNonNull(match.group(1)));
                    long seconds = Long.parseLong(Objects.requireNonNull(match.group(2)));
                    long millis = Long.parseLong(Objects.requireNonNull(match.group(3))) * 100;
                    if (minutes < 1000 && minutes >= 0 && seconds < 60 && seconds >= 0) {
                        Long time = ((minutes * 60 + seconds) * 1000) + millis;
                        lastThickTime = System.currentTimeMillis();
                        startTime = lastThickTime - time;
                        timerTextView.setText(text.getText());
                        validInput = true;
                        dialog.dismiss();
                    }
                }

                if (!validInput) {
                    Toast.makeText(MainActivity.this, R.string.timer_edit_invalid_input, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        dialog.show();
    }
}
