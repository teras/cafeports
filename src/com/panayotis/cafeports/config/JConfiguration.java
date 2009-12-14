/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JConfiguration.java
 *
 * Created on 10 Δεκ 2009, 3:28:21 πμ
 */
package com.panayotis.cafeports.config;

import com.panayotis.cafeports.db.PortList;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author teras
 */
public class JConfiguration extends javax.swing.JDialog {

    public static final JConfiguration dialog = new JConfiguration();

    /** Creates new form JConfiguration */
    private JConfiguration() {
        super();
        initComponents();
        BrowseB.putClientProperty("JButton.buttonType", "textured");
        setLocationByPlatform(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        PrefixT = new javax.swing.JTextField();
        BrowseB = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("CaféPorts Configuration");
        setModal(true);
        setResizable(false);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 12, 16));
        jPanel4.setLayout(new java.awt.BorderLayout(0, 4));

        jPanel1.setLayout(new java.awt.BorderLayout(8, 0));

        jLabel1.setText("MacPorts prefix");
        jPanel1.add(jLabel1, java.awt.BorderLayout.LINE_START);

        PrefixT.setColumns(20);
        PrefixT.setEditable(false);
        jPanel1.add(PrefixT, java.awt.BorderLayout.CENTER);

        BrowseB.setText("Browse");
        BrowseB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseBActionPerformed(evt);
            }
        });
        jPanel1.add(BrowseB, java.awt.BorderLayout.LINE_END);

        jPanel4.add(jPanel1, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BrowseBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseBActionPerformed
        File sel = new File(PrefixT.getText());
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setSelectedFile(sel);
        fc.ensureFileIsVisible(sel);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        disableNewFolderButton(fc);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            PortList.invalidatePortLists();
            Config.base.backup();
            Config.base.setPrefix(fc.getSelectedFile().getPath());
            if (!Config.base.isPrefixValid()) {
                Config.base.restore();
                JOptionPane.showMessageDialog(this, "Selected path is not a valid MacPorts distribution");
            }
            PrefixT.setText(Config.base.getPrefix());
        }
    }//GEN-LAST:event_BrowseBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BrowseB;
    private javax.swing.JTextField PrefixT;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables

    public void fireDisplay() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                PrefixT.setText(Config.base.getPrefix());
                setVisible(true);
            }
        });
    }

    private void disableNewFolderButton(Container c) {
        int len = c.getComponentCount();
        for (int i = 0; i < len; i++) {
            Component comp = c.getComponent(i);
            if (comp instanceof JButton) {
                JButton b = (JButton) comp;
                if (b.getText().equals("New Folder"))
                    b.setVisible(false);
            } else if (comp instanceof Container)
                disableNewFolderButton((Container) comp);
        }
    }
}
