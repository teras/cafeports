/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.cafeports.gui;

import com.panayotis.cafeports.db.UpdateListener;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author teras
 */
public class JWaitingLabel extends JPanel implements UpdateListener {

    private final JLabel text;
    private final JProgressBar progress;
    private static Icon[] stages = {
        new ImageIcon(JWaitingLabel.class.getResource("/icons/stage1.png")),
        new ImageIcon(JWaitingLabel.class.getResource("/icons/stage2.png"))
    };

    public JWaitingLabel() {
        super();
        text = new JLabel();
        progress = new JProgressBar();

        text.setOpaque(false);
        text.setVerticalTextPosition(JLabel.CENTER);
        text.setIconTextGap(8);

        progress.setMinimum(0);
        progress.setMaximum(100);

        setOpaque(true);
        setBorder(new EmptyBorder(6, 12, 6, 20));
        setLayout(new BorderLayout(20,0));
        add(text, BorderLayout.WEST);
        add(progress, BorderLayout.CENTER);
    }

    public void setInvalidPath() {
        setBackground(Color.red);
        text.setText("MacPorts PREFIX directory is not valid; usually \"/opt/local/\"");
        text.setIcon(null);
        progress.setVisible(false);
        doLayout();
    }

    public void setWaiting(boolean stage) {
        setBackground(new Color(250, 200, 100));
        text.setText("Please wait... reading database");
        doLayout();
    }

    public void setStage(int stage) {
        text.setIcon(stages[stage]);
        progress.setVisible(false);
    }

    public void setPercent(float percent) {
        progress.setVisible(true);
        progress.setValue((int) (100 * percent));
    }
}
