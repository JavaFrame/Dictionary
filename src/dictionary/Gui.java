package dictionary;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Sebastian on 25.12.2015.
 */
public class Gui extends JFrame implements KeyListener {
    private static Logger L = LogManager.getLogger(Dictionary.class.getName());
    static {
        L = Main.setupLogger(L);
    }

    private Popup popup;
    private JTextField inputTF;
    private JButton addToDictionaryB;
    private JButton optionB;

    private Dictionary dictionary;

    public Gui(Dictionary dictionary) {
        super("Dictionary v0.1");
        setSize(600, 25);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setAlwaysOnTop(true);

        Point loc = MouseInfo.getPointerInfo().getLocation();
        setLocation(loc);

        JPanel root = new JPanel(new BorderLayout());
        add(root);

        inputTF = new JTextField("");
        inputTF.addKeyListener(this);
        inputTF.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateInputTF();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateInputTF();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateInputTF();
            }
        });
        inputTF.setToolTipText("Enter drucken, um das geschriebene in die Zwischenablage zu kompieren.");
        root.add(inputTF, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());

        addToDictionaryB = new JButton("+");
        addToDictionaryB.setEnabled(false);
        addToDictionaryB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dictionary.add(getSelectedWord());
                    dictionary.save();
                    JOptionPane.showConfirmDialog(Gui.this, "Das Wort '" + getSelectedWord() + "' wurde gespeichert.",
                            "Das Word wurde gespeichert", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);

                    updateInputTF();
                } catch (IOException | IllegalArgumentException e1) {
                    JOptionPane.showConfirmDialog(Gui.this, e1.toString(), "Eine Exception trat auf",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        addToDictionaryB.setToolTipText("Fügt das aktuelle Wort dem Wörterbuch hinzu.");
        buttons.add(addToDictionaryB);

        //optionB = new JButton("Optionen");
        //buttons.add(optionB);

        root.add(buttons, BorderLayout.EAST);

        setVisible(true);

        this.dictionary = dictionary;
        popup = new Popup(this);
    }

    private void updateInputTF() {
        String[] list = dictionary.update(getSelectedWord());
        if(list[list.length-1].equals("?+")) {
            addToDictionaryB.setEnabled(true);
            list = Arrays.copyOf(list, list.length-1);
        } else {
            addToDictionaryB.setEnabled(false);
        }
        popup.showPopup(list, getX(), getY()+getHeight());
        inputTF.requestFocus();
    }

    private String getSelectedWord() {
        String selectedWord = null;
        String text = inputTF.getText();
        int cp = inputTF.getCaretPosition();

        String[] splited = text.split(" ");
        int end = 0;
        int start = 0;
        for(int i = 1; i < splited.length; i++) {
            start = end;
            String s = splited[i];
            end = text.indexOf(s, start);
            if(start < cp) {
                if(cp <= end) {
                    selectedWord = splited[i-1];
                }
            }
        }

        if(selectedWord == null) {
            selectedWord = splited[splited.length-1];
        }
        return selectedWord;
    }

    public void type(String word) {
        inputTF.setText(inputTF.getText().replace(getSelectedWord(), word));
    }

    private void typeWord(String words) {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(words);
        c.setContents(selection, selection);
    }

    //KeyListener für InputTf
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            popup.focus();
        } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            popup.focus();
        }

        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            Main.destroyGui();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            typeWord(inputTF.getText());
        }

        if(e.getKeyCode() == KeyEvent.VK_F1) {
            Main.destroyGui();
            try {
                dictionary.save();
                GlobalScreen.unregisterNativeHook();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (NativeHookException e1) {
                e1.printStackTrace();
            }

            System.exit(0);
        }

        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Main.destroyGui();
        }
}

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
