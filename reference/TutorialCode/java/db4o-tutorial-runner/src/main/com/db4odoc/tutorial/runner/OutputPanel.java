/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OutputPanel.java
 *
 * Created on 21.03.2011, 18:08:36
 */

package com.db4odoc.tutorial.runner;

import com.db4odoc.tutorial.utils.Disposable;
import com.db4odoc.tutorial.utils.Disposer;
import com.db4odoc.tutorial.utils.NoArgAction;

import javax.swing.*;

/**
 *
 * @author Gamlor
 */
public class OutputPanel extends javax.swing.JPanel implements Disposable{

    private final TextViewModel model;
    private final Disposer disposer = new Disposer();

    /** Creates new form OutputPanel
     * @param model*/
    public OutputPanel(final TextViewModel model) {
        this.model = model;
        initComponents();
        disposer.add(this.model.addEventListener(new NoArgAction() {
            @Override
            public void invoke() {
                getOutputTextArea().setText(model.getText());
            }
        }));

    }

    public static OutputPanel create(TextViewModel model) {
        return new OutputPanel(model);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();

        outputTextArea.setColumns(20);
        outputTextArea.setRows(5);
        jScrollPane1.setViewportView(outputTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea outputTextArea;
    // End of variables declaration//GEN-END:variables


    public JTextArea getOutputTextArea() {
        return outputTextArea;
    }

    @Override
    public void dispose() {
        disposer.dispose();
    }
}