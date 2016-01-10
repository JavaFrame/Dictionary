package dictionary;

import org.apache.log4j.Logger;
import org.apache.log4j.*;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;



import static org.jnativehook.GlobalScreen.unregisterNativeHook;

/**
 * Created by Sebastian on 25.12.2015.
 */
public class Main implements NativeKeyListener {

    private static Gui gui = null;
    private static Dictionary dictionary;

    private static Logger L;
    private static Level level = Level.ALL;

    public static Logger setupLogger(Logger l) {
        l.setLevel(level);
        return l;
    }

    public static void main(String[] args) throws NativeHookException {
        if(args.length < 1) {
            System.err.println("usage: dictionary pathToDictionary [-logging]");
            System.exit(2);
        }


        level = Level.OFF;
        if(args.length == 2) {
            if(args[1].equalsIgnoreCase("-logging")) {
                level = Level.ALL;

            }
        }
        L = LogManager.getLogger(Main.class.getName());
        L = setupLogger(L);
        L.info("logging is on");

        dictionary = Dictionary.getDictionary(args[0]);
        new Main();
        while(true){}
    }

    public Main() throws NativeHookException {
        java.util.logging.Logger l = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        l.setLevel(java.util.logging.Level.OFF);
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
    }

    public static void destroyGui() {
        gui.dispose();
        gui = null;
        L.info("close Gui");
    }

    boolean ctrl = false;

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if(e.getKeyCode() == NativeKeyEvent.VC_CONTROL_L) {
            ctrl = true;
        } else if(e.getKeyCode() == NativeKeyEvent.VC_CONTROL_R) {
            ctrl = true;
        }

        if(ctrl) {
            if (e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
                L.info("start Gui");
                if(gui != null) {
                    gui.setVisible(false);
                    gui.dispose();
                }
                gui = new Gui(dictionary);
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if(e.getKeyCode() == NativeKeyEvent.VC_CONTROL_L) {
            ctrl = false;
        } else if(e.getKeyCode() == NativeKeyEvent.VC_CONTROL_R) {
            ctrl = false;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

    }
}
