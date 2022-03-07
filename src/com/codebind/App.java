package com.codebind;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    private JMenuBar menubar;
    private JPanel panelMain;
    private JButton buttonStart;
    private JButton buttonStop;
    private JLabel labelMsg;
    private JLabel labelTimer;
    private JPanel panelButtons;
    private long focusMinutes;
    private JFrame frame;

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

    // Creates a dialogue that asks the user for the focus period.
    private void requestFocusTime() {
        String m = JOptionPane.showInputDialog(Locale.TIME_QUESTION_MESSAGE);

        int convertedMinutes;
        try {
            convertedMinutes = Integer.parseInt(m);
        } catch (Exception exception) {
            convertedMinutes = 25;
        }

        if (convertedMinutes < 1 || convertedMinutes > 60) {
            convertedMinutes = 25;
        }
        setFocusMinutes(convertedMinutes);
    }

    // Edits the message and the timer labels.
    private void resetTimerAndMessage(String message) {
        setLabelMsg(message);
        setEnableButtonStart(true);
        setEnableButtonStop(false);
        setTimerStopped(false);
        setLabelTimer("00:00");
    }

    public void finishSessionAndDisplayMessage(String message, JFrame frame) {
        flashWindow(frame);
        Utils.playAlarmSound();

        JOptionPane.showMessageDialog(null, message);
    }

    private void startFocusPeriod() {
        setLabelMsg(Locale.FOCUS_MESSAGE);
        setEnableButtonStop(true);
        setEnableButtonStart(false);
        setTimerStopped(false);
    }

    private void endFocusPeriod() {
        // Ended the focus, we need to take a break now.
        setState(State.BREAK);

        resetTimerAndMessage(Locale.FOCUS_DONE_MESSAGE);
        finishSessionAndDisplayMessage(Locale.FOCUS_DONE_MESSAGE_ALERT, frame);
    }

    private void startBreakPeriod() {
        setLabelMsg(Locale.BREAK_MESSAGE);
        setEnableButtonStop(true);
        setEnableButtonStart(false);
        setTimerStopped(false);
    }

    private void endBreakPeriod() {
        // Ended the break, we need to now.
        setState(State.FOCUS);

        resetTimerAndMessage(Locale.BREAK_DONE_MESSAGE);
        finishSessionAndDisplayMessage(Locale.BREAK_DONE_MESSAGE_ALERT, frame);
    }

    public static void flashWindow(JFrame frame) {
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(false);
    }

    private void startTimerTask() {
        long startTime = System.currentTimeMillis();
        Timer timer = new Timer();

        // Pick whether we choose to subtract from session or break time.
        long sessionMinutes = 0;
        if (getState() == State.BOOTED || getState() == State.FOCUS) {
            sessionMinutes = getFocusMinutes();
        }
        else if (getState() == State.BREAK) {
            sessionMinutes = getBreakMinutes();
        }

        long finalSessionMinutes = sessionMinutes;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long systemTime = System.currentTimeMillis();

                // If the timer is below zero, or it has been stopped, cancel the updates.
                if (systemTime - startTime > finalSessionMinutes * 60 * 1000 || isTimerStopped()) {
                    this.cancel();
                    if (getState() == State.BOOTED || getState() == State.FOCUS) {
                        endFocusPeriod();
                    }
                    else if (getState() == State.BREAK) {
                        endBreakPeriod();
                    }
                }
                // Update the timer with the new remaining time.
                else {
                    setLabelTimer(Utils.convertRemainingMillisToMinutes(systemTime - startTime, getFocusMinutes()));
                }
            }
        }, 0, 100);
    }

    private void initFrame(JFrame frame) {
        frame.toFront();
        frame.setContentPane(this.panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setTitle("Pomodoro app");
        frame.setLocationRelativeTo(null);
        frame.setSize(350, 250);
    }

    private void initMenuBar(JMenuBar menubar, JFrame frame) {
        JMenuItem aboutItem = new JMenuItem("About");
        JMenuItem usageItem = new JMenuItem("Usage");
        JMenu helpMenu = new JMenu("Help");

        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, Locale.ABOUT_MESSAGE);
            }
        });

        usageItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, Locale.USAGE_MESSAGE);
            }
        });

        helpMenu.add(aboutItem);
        helpMenu.add(usageItem);
        menubar.add(helpMenu);
        frame.setJMenuBar(menubar);
    }

    public App() {
        this.frame = new JFrame();
        initFrame(this.frame);

        this.menubar = new JMenuBar();
        initMenuBar(this.menubar, this.frame);

        // Start button logic
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.playClickSound();

                if (getState() == State.FOCUS || getState() == State.BOOTED) {
                    // Only request the focus time the first time.
                    if (getState() == State.BOOTED)
                        requestFocusTime();
                    startFocusPeriod();

                    startTimerTask();
                }
                else if (getState() == State.BREAK) {
                    startBreakPeriod();

                    startTimerTask();
                }
            }
        });
        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.playClickSound();

                setTimerStopped(true);
            }
        });
    }

    public static void main(String[] args) {
        new App();
    }
}
