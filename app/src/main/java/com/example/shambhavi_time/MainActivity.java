package com.example.shambhavi_time;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private TextView roundTextView;
    private Button startButton;
    private Button resetButton;

    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private int currentRound = 0;

    private final long[] roundTimes = {120000, 120000, 120000, 390000, 420000, 360000, 240000, 360000}; // Times for each round
    private final long breakTime = 3000; // Break time
    private final long extraBreakTime = 10000; // Extra break time (for sukhasana break)

    private PowerManager.WakeLock wakeLock;
    private MediaPlayer mediaPlayer;
    private long timeLeft;
    private long breakTimeLeft;
    private boolean isBreak;
    private boolean isPaused;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WakeLock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakelockTag");

        timerTextView = findViewById(R.id.timerTextView);
        roundTextView = findViewById(R.id.roundTextView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerRunning) {
                    pauseTimer();
                } else {
                    if (isPaused) {
                        resumeTimer();
                    } else {
                        startNextRound();
                    }
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        updateTimerText(0); // Initialize timer text
    }

    private void startNextRound() {
        if (currentRound < roundTimes.length) {
            long roundTime = roundTimes[currentRound];
            String roundText = "";
            if(currentRound == 0){
                roundText = "Patangasana";
            } else if (currentRound == 1 || currentRound == 2) {
                roundText = "Shishupalasana";
            } else if (currentRound == 3) {
                roundText = "Nadi Vibhajana";
            } else if (currentRound == 4) {
                roundText = "Sukhasana";
            } else if (currentRound == 5) {
                roundText = "Aum Chant";
            } else if (currentRound == 6) {
                roundText = "Vipareeta Shwasa";
            } else if (currentRound == 7) {
                roundText = "Breathe";
            }
            startTimer(roundTime, roundText);
        } else {
            endWorkout();
        }
    }

    private void startBreak(final long breakDuration) {
        breakTimeLeft = breakDuration;
        playBeep(); // Play beep sound when round ends
        roundTextView.setText("Break");
        isBreak = true;

        countDownTimer = new CountDownTimer(breakDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                breakTimeLeft = millisUntilFinished;
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                currentRound++;
                startNextRound();
            }
        }.start();

        timerRunning = true;
        startButton.setText("Pause Timer");
    }

    private void startTimer(final long duration, final String roundText) {
        timeLeft = duration;
        roundTextView.setText(roundText);
        isBreak = false;

        if (!wakeLock.isHeld()) {
            wakeLock.acquire(); // Acquire WakeLock
        }

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimerText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                if (currentRound == 4) { // Special case for Round 5 break
                    startBreak(extraBreakTime);
                } else {
                    startBreak(breakTime);
                }
            }
        }.start();

        timerRunning = true;
        startButton.setText("Pause Timer");
    }

    private void resumeTimer() {
        if (isBreak) {
            startBreak(breakTimeLeft);
        } else {
            String roundText = "";
            if(currentRound == 0){
                roundText = "Patangasana";
            } else if (currentRound == 1 || currentRound == 2) {
                roundText = "Shishupalasana";
            } else if (currentRound == 3) {
                roundText = "Nadi Vibhajana";
            } else if (currentRound == 4) {
                roundText = "Sukhasana";
            } else if (currentRound == 5) {
                roundText = "Aum Chant";
            } else if (currentRound == 6) {
                roundText = "Vipareeta Shwasa";
            } else if (currentRound == 7) {
                roundText = "Breathe";
            }
            startTimer(timeLeft, roundText);
        }
    }
    private void pauseTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        isPaused = true;
        startButton.setText("Start Timer");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (wakeLock.isHeld()) {
            wakeLock.release(); // Release WakeLock
        }
        timerRunning = false;
        currentRound = 0;
        updateTimerText(0);
        roundTextView.setText("");
        startButton.setText("Start Timer");
    }

    private void updateTimerText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeLeftFormatted);
    }

    private void endWorkout() {
        if (wakeLock.isHeld()) {
            wakeLock.release(); // Release WakeLock
        }
        playBeep(); // Play beep sound when workout ends
        roundTextView.setText("Open Your Eyes");
        timerTextView.setText("00:00:00");
        startButton.setText("Start Timer");
        timerRunning = false;
    }

    private void playBeep() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("timerRunning", timerRunning);
        outState.putInt("currentRound", currentRound);
        outState.putLong("timeLeft", timeLeft);
        outState.putLong("breakTimeLeft", breakTimeLeft);
        outState.putBoolean("isBreak", isBreak);
        outState.putBoolean("isPaused", isPaused);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        timerRunning = savedInstanceState.getBoolean("timerRunning");
        currentRound = savedInstanceState.getInt("currentRound");
        timeLeft = savedInstanceState.getLong("timeLeft");
        breakTimeLeft = savedInstanceState.getLong("breakTimeLeft");
        isBreak = savedInstanceState.getBoolean("isBreak");
        isPaused = savedInstanceState.getBoolean("isPaused");

        if (timerRunning) {
            if (isBreak) {
                startBreak(breakTimeLeft);
            } else {
                String roundText = "";
                if(currentRound == 0){
                    roundText = "Patangasana";
                } else if (currentRound == 1 || currentRound == 2) {
                    roundText = "Shishupalasana";
                } else if (currentRound == 3) {
                    roundText = "Nadi Vibhajana";
                } else if (currentRound == 4) {
                    roundText = "Sukhasana";
                } else if (currentRound == 5) {
                    roundText = "Aum Chant";
                } else if (currentRound == 6) {
                    roundText = "Vipareeta Shwasa";
                } else if (currentRound == 7) {
                    roundText = "Breathe";
                }
                startTimer(timeLeft, roundText);
            }
        } else {
            updateTimerText(timeLeft);
            if (isBreak) {
                roundTextView.setText("Break");
            } else {
                String roundText = "";
                if(currentRound == 0){
                    roundText = "Patangasana";
                } else if (currentRound == 1 || currentRound == 2) {
                    roundText = "Shishupalasana";
                } else if (currentRound == 3) {
                    roundText = "Nadi Vibhajana";
                } else if (currentRound == 4) {
                    roundText = "Sukhasana";
                } else if (currentRound == 5) {
                    roundText = "Aum Chant";
                } else if (currentRound == 6) {
                    roundText = "Vipareeta Shwasa";
                } else if (currentRound == 7) {
                    roundText = "Breathe";
                }
                startTimer(timeLeft, roundText);
                roundTextView.setText(roundText);
            }
            startButton.setText("Start Timer");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
