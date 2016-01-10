package dictionary;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;

/**
 * Created by Sebastian on 25.12.2015.
 */
public class Popup extends JWindow {
    private static Logger L = LogManager.getLogger(Dictionary.class.getName());
    static {
        L = Main.setupLogger(L);
    }

    private JList<String> wordList;

    private Gui gui;

    private ListSelectionListener listListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            //System.out.println(e.getFirstIndex() + "/" + e.getLastIndex() + "/" + e.getValueIsAdjusting() + "/" + wordList.getSelectedValuesList() + "/" + isFocusable());
        }
    };

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() != 2)
                return;
            if(wordList.isSelectionEmpty()) {
                return;
            }
            wordList.setValueIsAdjusting(true);
            String word = wordList.getSelectedValue();
            gui.type(word);
        }
    };

    private KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                if(wordList.isSelectionEmpty()) {
                    return;
                }
                wordList.setValueIsAdjusting(true);
                String word = wordList.getSelectedValue();
                gui.type(word);
            }
        }
    };

    public Popup(Gui parent) throws HeadlessException {
        super(parent);
        this.gui = parent;
        setLocation(100, 100);
        setFocusable(true);
        setVisible(false);
    }

    public void showPopup(String[] list, int x, int y) {
        if(isVisible()) {
            updatePopup(list, x, y);
            return;
        }
        L.info("show Popup");
        setVisible(true);
        setLocation(x, y);
        wordList = new JList<>(list);
        wordList.addListSelectionListener(listListener);
        wordList.addMouseListener(mouseListener);
        wordList.addKeyListener(keyListener);
        wordList.setSelectedIndex(0);
        wordList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane pane = new JScrollPane(wordList);
        add(pane);
        pack();
        repaint();
    }

    public void updatePopup(String[] list, int x, int y) {
        L.info("update Popup");
        setLocation(x, y);
        wordList.setListData(list);
        pack();
        repaint();
    }




    public void focus() {
        L.info("focus Popup");
        setVisible(true);
        wordList.requestFocus();
    }
}
