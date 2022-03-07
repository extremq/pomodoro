package com.codebind;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    private JPanel panelMain;
    private JButton buttonStart;
    private JButton buttonStop;
    private JLabel labelMsg;
    private JLabel labelTimer;
    private JPanel panelButtons;
    private long focusMinutes;
    private static final JFrame frame = new JFrame("App");

    enum State {
        BOOTED,
        FOCUS,
        BREAK
    }

    private State state = State.BOOTED;
    private boolean timerStopped = false;

    // SETTERS & GETTERS
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setTimerStopped(boolean timerStopped) {
        this.timerStopped = timerStopped;
    }

    public long getFocusMinutes() {
        return focusMinutes;
    }

    public void setFocusMinutes(long focusMinutes) {
        this.focusMinutes = focusMinutes;
    }

    public long getBreakMinutes() {
        return 5;
    }

    public void setLabelMsg(String message) {
        this.labelMsg.setText(message);
    }

    public void setLabelTimer(String timeRemaining) {
        this.labelTimer.setText(timeRemaining);
    }

    public void setEnableButtonStart(boolean enabled) {
        this.buttonStart.setEnabled(enabled);
    }

    public void setEnableButtonStop(boolean enabled) {
        this.buttonStop.setEnabled(enabled);
    }

    public boolean isTimerStopped() {
        return this.timerStopped;
    }
    // END SETTERS AND GETTERS

    // Transform the remaining time to a human-readable format.
    public String convertMillisToMinutes(long millis, long periodOfTime) {
        long selectedLength = periodOfTime * 60 * 1000;
        long remainingTime = selectedLength - millis;
        long millisToSeconds = (remainingTime / 1000) % 60;
        long millisToMinutes = remainingTime / (1000 * 60);

        if (remainingTime <= 0) {
            return "00:00";
        }

        return String.format("%02d", millisToMinutes) + ":" + String.format("%02d", millisToSeconds);
    }

    private void startFocusPeriod() {
        setLabelMsg(Locale.FOCUS_MESSAGE);
        setEnableButtonStop(true);
        setEnableButtonStart(false);
        setTimerStopped(false);
    }

    // Creates a dialogue that asks the user for the focus period.
    private void requestFocusTime() {
        String m = JOptionPane.showInputDialog(Locale.TIME_QUESTION_MESSAGE);

        int convertedMinutes;
        try {
            convertedMinutes = Integer.parseInt(m);
        } catch (Exception exception) {
            convertedMinutes = 30;
        }

        if (convertedMinutes < 1 || convertedMinutes > 60) {
            convertedMinutes = 30;
        }
        setFocusMinutes(convertedMinutes);
    }

    private void endFocusPeriod() {
        setState(State.BREAK);

        setLabelMsg(Locale.FOCUS_DONE_MESSAGE);
        setEnableButtonStart(true);
        setEnableButtonStop(false);
        setTimerStopped(false);
        setLabelTimer("00:00");
        flashWindow();

        JOptionPane.showMessageDialog(null, Locale.FOCUS_DONE_MESSAGE_ALERT);
    }

    private void startBreakPeriod() {
        setLabelMsg(Locale.BREAK_MESSAGE);
        setEnableButtonStop(true);
        setEnableButtonStart(false);
        setTimerStopped(false);
    }

    private void endBreakPeriod() {
        setState(State.FOCUS);

        setLabelMsg(Locale.BREAK_DONE_MESSAGE);
        setEnableButtonStart(true);
        setEnableButtonStop(false);
        setTimerStopped(false);
        setLabelTimer("00:00");
        flashWindow();

        JOptionPane.showMessageDialog(null, Locale.BREAK_DONE_MESSAGE_ALERT);
    }

    private static void flashWindow() {
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(false);
    }

    public App() {
        // Start button logic
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getState() == State.FOCUS || getState() == State.BOOTED) {
                    // Only request the focus time the first time.
                    if (getState() == State.BOOTED)
                        requestFocusTime();
                    startFocusPeriod();

                    long startTime = System.currentTimeMillis();
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            long systemTime = System.currentTimeMillis();
                            if (systemTime - startTime > getFocusMinutes() * 60 * 1000 || isTimerStopped()) {
                                this.cancel();
                                endFocusPeriod();
                            } else {
                                setLabelTimer(convertMillisToMinutes(systemTime - startTime, getFocusMinutes()));
                            }
                        }
                    }, 0, 100);
                }
                else if (getState() == State.BREAK) {
                    startBreakPeriod();

                    long startTime = System.currentTimeMillis();
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            long systemTime = System.currentTimeMillis();
                            if (systemTime - startTime > getBreakMinutes() * 60 * 1000 || isTimerStopped()) {
                                this.cancel();
                                endBreakPeriod();
                            } else {
                                setLabelTimer(convertMillisToMinutes(systemTime - startTime, getBreakMinutes()));
                            }
                        }
                    }, 0, 100);
                }
            }
        });
        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTimerStopped(true);
            }
        });
    }

    public static void main(String[] args) {
        // Init settings
        frame.toFront();
        frame.setContentPane(new App().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setTitle("Pomodoro app");
        frame.setLocationRelativeTo(null);
        frame.setSize(350, 225);
    }
}
