package com.codebind;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

public class App {
    private JPanel panelMain;
    private JButton buttonStart;
    private JButton buttonStop;
    private JLabel labelMsg;
    private JLabel labelTimer;
    private JPanel panelButtons;
    private long minutes;
    private boolean focusStopped = false;

    public void setFocusStopped(boolean focusStopped) {
        this.focusStopped = focusStopped;
    }
    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }

    public void setLabelMsg(String message) {
        this.labelMsg.setText(message);
    }

    public void setLabelTimer(String timeRemaining) {
        this.labelTimer.setText(timeRemaining);
    }

    public String convertMillisToMinutes(long millis) {
        long selectedLength = getMinutes() * 60 * 1000;
        long remainingTime = selectedLength - millis;
        long millisToSeconds = (remainingTime / 1000) % 60;
        long millisToMinutes = remainingTime / (1000 * 60);

        if (remainingTime <= 0) {
            return "00:00";
        }

        return String.format("%02d", millisToMinutes) + ":" + String.format("%02d", millisToSeconds);
    }

    public void setEnableButtonStart(boolean enabled) {
        this.buttonStart.setEnabled(enabled);
    }

    public void setEnableButtonStop(boolean enabled) {
        this.buttonStop.setEnabled(enabled);
    }

    public boolean isFocusStopped() {
        return this.focusStopped;
    }

    public void endFocusPeriod() {
        setLabelMsg(Locale.GREET_MESSAGE);
        setEnableButtonStart(true);
        setEnableButtonStop(false);
        setFocusStopped(false);
        setLabelTimer("00:00");

        JOptionPane.showMessageDialog(null, Locale.FOCUS_DONE_MESSAGE);
    }

    public App() {
        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String m = JOptionPane.showInputDialog(Locale.TIME_QUESTION_MESSAGE);

                int convertedMinutes;
                try {
                    convertedMinutes = Integer.parseInt(m);
                }
                catch (Exception exception) {
                    convertedMinutes = 30;
                }

                if (convertedMinutes < 1 || convertedMinutes > 60) {
                    convertedMinutes = 30;
                }
                setMinutes(convertedMinutes);

                setLabelMsg(Locale.FOCUS_MESSAGE);
                setEnableButtonStop(true);
                setEnableButtonStart(false);
                setFocusStopped(false);

                long startTime = System.currentTimeMillis();
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        long systemTime = System.currentTimeMillis();
                        if (systemTime - startTime > getMinutes() * 60 * 1000 || isFocusStopped()) {
                            this.cancel();
                            endFocusPeriod();
                        }
                        else {
                            setLabelTimer(convertMillisToMinutes(systemTime - startTime));
                        }
                    }
                }, 0, 500);

            }
        });
        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFocusStopped(true);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("App");

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
