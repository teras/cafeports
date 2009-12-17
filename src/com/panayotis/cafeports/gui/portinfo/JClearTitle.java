/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.cafeports.gui.portinfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author teras
 */
public class JClearTitle extends JClearPanel {

    private final JLabel label;
    private final JRounder tl;
    private final JRounder tr;
    private final JClearButton closeB;
    private final JClearButton clipboardB;

    public JClearTitle() {
        label = new JClearLabel();
        tl = new JRounder(JRounder.Location.TOPLEFT);
        tr = new JRounder(JRounder.Location.TOPRIGHT);
        closeB = new JClearButton("/icons/buttons/close.png");
        clipboardB = new JClearButton("/icons/buttons/clipboard.png");
        JClearPanel left = new JClearPanel();
        JClearPanel right = new JClearPanel();

        Dimension size = new Dimension(10, 1);
        tl.setPreferredSize(size);
        tr.setPreferredSize(size);

        closeB.setToolTipText("Close info window");
        clipboardB.setToolTipText("Copy information to clipboard");

        left.setLayout(new BorderLayout());
        left.add(tl, BorderLayout.WEST);
        left.add(closeB, BorderLayout.CENTER);

        right.setLayout(new BorderLayout());
        right.add(tr, BorderLayout.EAST);
        right.add(clipboardB, BorderLayout.CENTER);

        label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | java.awt.Font.BOLD));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setBorder(new EmptyBorder(2, 4, 2, 4));

        setLayout(new BorderLayout());
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
        add(label, BorderLayout.CENTER);
        setBackground(getBackground());
        setForeground(getForeground());
        setOpaque(true);
    }

    public void setText(String text) {
        label.setText(text);
        doLayout();
    }

    public void addCloseListener(ActionListener l) {
        closeB.addActionListener(l);
    }
    public void addClipBoardListener(ActionListener l) {
        clipboardB.addActionListener(l);
    }
}
