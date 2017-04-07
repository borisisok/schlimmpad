package org.neuroninterworks.midi.seq64;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;
import com.thoughtworks.xstream.*;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.sound.midi.InvalidMidiDataException;



// vodoo dolls and froglegs below

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;


import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiUnavailableException;

//import javax.sound.midi.Sequence;
//import javax.sound.midi.Track;
/**
 *
 * @author boris Todo: make synth banging: all cc params random in their valid
 * values per synth, selection of morphable controllers for wild modulated
 * sounds
 *
 */
public class Launchpad implements Runnable, MetaEventListener {

    static HashMap<String, Thread> groovyThreads = new HashMap();
    static GroovyWindow groovywindow;
    static boolean dispatchKeys = true;
    GroovyShell shell = null;
    private int padnum;
    static int padcount;
    private MainWindow window = new MainWindow(this);
    int loop = 0;
    private String launchpadDeviceName = null;
    private static boolean DEBUG = false;
    // midi
    HashMap<String, MidiDevice> outDevs = new HashMap();
    private MidiDevice inputLaunchpadDevice = null;
    private MidiDevice inputClockDevice = null;
    private MidiDevice inputNoteDevice = null;
    private MidiDevice outputLaunchpadDevice = null;
    private Receiver receiver = null;
    private LaunchpadSequencer mainSequencer = null;
    private int mainPPQ = 24;
    Receiver padReceiver = new PadReceiver(System.out);
    Receiver noteReceiver;
    // gui
    private BufferedImage font = null;
    PadUpdater padUp = null;
    PadPrinter padPrinter = null;
    Boolean draw = false;
    private final Object drawMutex = new Object();
    public PadView currentView = null;
    private PadView overView = null;
    private static SessionInfo session = null;
    int VIEW_ClipOverview = 0;
    int VIEW_ClipStepview = 1;
    int VIEW_KeyStepview = 2;
    Clip currentClip = null;
    static final HashMap<String, Clip> cliphash = new HashMap();
    static final HashMap<String, GroovyPlug> groovyPlugs = new HashMap();
    static final HashMap<String, Integer> notes = new HashMap();
    static final HashMap<Integer, String> notesByVal = new HashMap();

    static List<Launchpad> padlist = new ArrayList<Launchpad>();
    
    Launchpad lp = null;

    static {
        notes.put("C", 1);
        notes.put("C#", 2);
        notes.put("D", 3);
        notes.put("D#", 4);
        notes.put("E", 5);
        notes.put("F", 6);
        notes.put("F#", 7);
        notes.put("G", 8);
        notes.put("G#", 9);
        notes.put("A", 10);
        notes.put("A#", 11);
        notes.put("B", 12);
        notes.put("H", 12);

        notesByVal.put(1, "C");
        notesByVal.put(2, "C#");
        notesByVal.put(3, "D");
        notesByVal.put(4, "D#");
        notesByVal.put(5, "E");
        notesByVal.put(6, "F");
        notesByVal.put(7, "F#");
        notesByVal.put(8, "G");
        notesByVal.put(9, "G#");
        notesByVal.put(10, "A");
        notesByVal.put(11, "A#");
        notesByVal.put(12, "B");
    }

    public void updateWindow() {

        window.jLabelPadName.setText(padnum+ " (" + launchpadDeviceName + ") ");

        if (this.currentClip != null) {
            try {
                window.length.setText(Integer.toString(currentClip.clipinfo.loopEnd - currentClip.clipinfo.loopStart));
                window.position.setText(Integer.toString(currentClip.clipinfo.xpos) + ", " + Integer.toString(currentClip.clipinfo.ypos));
                window.range.setText(Integer.toString(currentClip.clipinfo.v_xoff.get(padnum)) + ", " + Integer.toString(currentClip.clipinfo.v_yoff.get(padnum)));
                window.running.setText(Boolean.toString(currentClip.clipinfo.play));
                window.start.setText(Integer.toString(currentClip.clipinfo.loopStart));
                window.end.setText(Integer.toString(currentClip.clipinfo.loopEnd));
                window.state.setText("0");
                window.stepsize.setText(Integer.toString(currentClip.clipinfo.v_gridXSize.get(padnum)));
                window.midi.setText(currentClip.clipinfo.outDevice + " / " + currentClip.clipinfo.outChannel);
                int x = currentClip.clipinfo.xpos;
                int y = currentClip.clipinfo.ypos;

//                window.println("CURRENT: " + x + " " + y);
 /*               
                 if (y == 0 && x == 0) { window.b_1_1.setSelected(true);} else { window.b_1_1.setSelected(false);}   
                 if (y == 1 && x == 0) { window.b_2_1.setSelected(true);} else { window.b_2_1.setSelected(false);}
                 if (y == 2 && x == 0) { window.b_3_1.setSelected(true);} else { window.b_3_1.setSelected(false);}
                 if (y == 3 && x == 0) { window.b_4_1.setSelected(true);} else { window.b_4_1.setSelected(false);}
                 if (y == 4 && x == 0) { window.b_5_1.setSelected(true);} else { window.b_5_1.setSelected(false);}
                 if (y == 5 && x == 0) { window.b_6_1.setSelected(true);} else { window.b_6_1.setSelected(false);}
                 if (y == 6 && x == 0) { window.b_7_1.setSelected(true);} else { window.b_7_1.setSelected(false);}
                 if (y == 7 && x == 0) { window.b_8_1.setSelected(true);} else { window.b_1_1.setSelected(false);}
                       
                 if (y == 0 && x == 1) { window.b_1_2.setSelected(true);} else { window.b_1_2.setSelected(false);}
                 if (y == 1 && x == 1) { window.b_2_2.setSelected(true);} else { window.b_2_2.setSelected(false);}
                 if (y == 2 && x == 1) { window.b_3_2.setSelected(true);} else { window.b_3_2.setSelected(false);}
                 if (y == 3 && x == 1) { window.b_4_2.setSelected(true);} else { window.b_4_2.setSelected(false);}
                 if (y == 4 && x == 1) { window.b_5_2.setSelected(true);} else { window.b_5_2.setSelected(false);}
                 if (y == 5 && x == 1) { window.b_6_2.setSelected(true);} else { window.b_6_2.setSelected(false);}
                 if (y == 6 && x == 1) { window.b_7_2.setSelected(true);} else { window.b_7_2.setSelected(false);}
                 if (y == 7 && x == 1) { window.b_8_2.setSelected(true);} else { window.b_8_2.setSelected(false);}

                 if (y == 0 && x == 2) { window.b_1_3.setSelected(true);} else { window.b_1_3.setSelected(false);}
                 if (y == 1 && x == 2) { window.b_2_3.setSelected(true);} else { window.b_2_3.setSelected(false);}
                 if (y == 2 && x == 2) { window.b_3_3.setSelected(true);} else { window.b_3_3.setSelected(false);}
                 if (y == 3 && x == 2) { window.b_4_3.setSelected(true);} else { window.b_4_3.setSelected(false);}
                 if (y == 4 && x == 2) { window.b_5_3.setSelected(true);} else { window.b_5_3.setSelected(false);}
                 if (y == 5 && x == 2) { window.b_6_3.setSelected(true);} else { window.b_6_3.setSelected(false);}
                 if (y == 6 && x == 2) { window.b_7_3.setSelected(true);} else { window.b_7_3.setSelected(false);}
                 if (y == 7 && x == 2) { window.b_8_3.setSelected(true);} else { window.b_8_3.setSelected(false);}
                 if (y == 0 && x == 3) { window.b_1_4.setSelected(true);} else { window.b_1_4.setSelected(false);}

                 if (y == 1 && x == 3) { window.b_2_4.setSelected(true);} else { window.b_2_4.setSelected(false);}
                 if (y == 2 && x == 3) { window.b_3_4.setSelected(true);} else { window.b_3_4.setSelected(false);}
                 if (y == 3 && x == 3) { window.b_4_4.setSelected(true);} else { window.b_4_4.setSelected(false);}
                 if (y == 4 && x == 3) { window.b_5_4.setSelected(true);} else { window.b_5_4.setSelected(false);}
                 if (y == 5 && x == 3) { window.b_6_4.setSelected(true);} else { window.b_6_4.setSelected(false);}
                 if (y == 6 && x == 3) { window.b_7_4.setSelected(true);} else { window.b_7_4.setSelected(false);}
                 if (y == 7 && x == 3) { window.b_8_4.setSelected(true);} else { window.b_8_4.setSelected(false);}
            
                 if (y == 0 && x == 4) { window.b_1_5.setSelected(true);} else { window.b_1_5.setSelected(false);}
                 if (y == 1 && x == 4) { window.b_2_5.setSelected(true);} else { window.b_2_5.setSelected(false);}
                 if (y == 2 && x == 4) { window.b_3_5.setSelected(true);} else { window.b_3_5.setSelected(false);}
                 if (y == 3 && x == 4) { window.b_4_5.setSelected(true);} else { window.b_4_5.setSelected(false);}
                 if (y == 4 && x == 4) { window.b_5_5.setSelected(true);} else { window.b_5_5.setSelected(false);}
                 if (y == 5 && x == 4) { window.b_6_5.setSelected(true);} else { window.b_6_5.setSelected(false);}
                 if (y == 6 && x == 4) { window.b_7_5.setSelected(true);} else { window.b_7_5.setSelected(false);}
                 if (y == 7 && x == 4) { window.b_8_5.setSelected(true);} else { window.b_8_5.setSelected(false);}
            
                 if (y == 0 && x == 5) { window.b_1_6.setSelected(true);} else { window.b_1_6.setSelected(false);}
                 if (y == 1 && x == 5) { window.b_2_6.setSelected(true);} else { window.b_2_6.setSelected(false);}
                 if (y == 2 && x == 5) { window.b_3_6.setSelected(true);} else { window.b_3_6.setSelected(false);}
                 if (y == 3 && x == 5) { window.b_4_6.setSelected(true);} else { window.b_4_6.setSelected(false);}
                 if (y == 4 && x == 5) { window.b_5_6.setSelected(true);} else { window.b_5_6.setSelected(false);}
                 if (y == 5 && x == 5) { window.b_6_6.setSelected(true);} else { window.b_6_6.setSelected(false);}
                 if (y == 6 && x == 5) { window.b_7_6.setSelected(true);} else { window.b_7_6.setSelected(false);}
                 if (y == 7 && x == 5) { window.b_8_6.setSelected(true);} else { window.b_8_6.setSelected(false);}
            
                 if (y == 0 && x == 6) { window.b_1_7.setSelected(true);} else { window.b_1_7.setSelected(false);}
                 if (y == 1 && x == 6) { window.b_2_7.setSelected(true);} else { window.b_2_7.setSelected(false);}
                 if (y == 2 && x == 6) { window.b_3_7.setSelected(true);} else { window.b_3_7.setSelected(false);}
                 if (y == 3 && x == 6) { window.b_4_7.setSelected(true);} else { window.b_4_7.setSelected(false);}
                 if (y == 4 && x == 6) { window.b_5_7.setSelected(true);} else { window.b_5_7.setSelected(false);}
                 if (y == 5 && x == 6) { window.b_6_7.setSelected(true);} else { window.b_6_7.setSelected(false);}
                 if (y == 6 && x == 6) { window.b_7_7.setSelected(true);} else { window.b_7_7.setSelected(false);}
                 if (y == 7 && x == 6) { window.b_8_7.setSelected(true);} else { window.b_8_7.setSelected(false);}

                 if (x == 0 && x == 7) { window.b_1_8.setSelected(true);} else { window.b_1_8.setSelected(false);}
                 if (x == 1 && x == 7) { window.b_2_8.setSelected(true);} else { window.b_2_8.setSelected(false);}
                 if (x == 2 && x == 7) { window.b_3_8.setSelected(true);} else { window.b_3_8.setSelected(false);}
                 if (x == 3 && x == 7) { window.b_4_8.setSelected(true);} else { window.b_4_8.setSelected(false);}
                 if (x == 4 && x == 7) { window.b_5_8.setSelected(true);} else { window.b_5_8.setSelected(false);}
                 if (x == 5 && x == 7) { window.b_6_8.setSelected(true);} else { window.b_6_8.setSelected(false);}
                 if (x == 6 && x == 7) { window.b_7_8.setSelected(true);} else { window.b_7_8.setSelected(false);}
                 if (x == 7 && x == 7) { window.b_8_8.setSelected(true);} else { window.b_8_8.setSelected(false);}
                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void switchView(int x, int y) {
        Clip c = cliphash.get(x+"_"+y);
        if (c == null) {
            return;
        }
        ((ClipOverview) overView).focusedClip = c;
        currentClip = ((ClipOverview) overView).focusedClip;
        clearAll();
        currentView = (PadView) new ClipStepview(((ClipOverview) overView).focusedClip);
    }

    public HashMap<String, GroovyPlug> getGroovyPlugs() {
        return groovyPlugs;
    }

    public SessionInfo getSession() {
        return session;
    }

    public PadView getOverView() {
        return overView;
    }

    public HashMap<String, Clip> getCliphash() {
        return cliphash;
    }

    public PadView getCurrentView() {
        return currentView;
    }

    public LaunchpadSequencer getMainSequencer() {
        return mainSequencer;
    }

    public HashMap<String, MidiDevice> getOutDevs() {
        return outDevs;
    }

    void setInputNoteDevice(MidiDevice md) {
        this.inputNoteDevice = md;
        try {
            inputNoteDevice.open();

//          Receiver receiver = inputNoteDevice.getReceiver();
            Transmitter t = inputNoteDevice.getTransmitter();
//          ((PadReceiver) padReceiver).setLaunchpad(this);
            noteReceiver = new NoteReceiver(System.out);
            ((NoteReceiver) noteReceiver).setLaunchpad(this);
            t.setReceiver(noteReceiver);
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(Launchpad.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    MidiDevice getInputNoteDevice() {
        return this.inputNoteDevice;
    }

    NoteReceiver getNoteReceiver() {
        return (NoteReceiver) this.noteReceiver;
    }

    private void setPadnum(int i) {
        padnum = i;
    }
            
    public interface PadView {

        public PadKey[] getPadKeyGrid();

        public PadKey[] getPadKeyTop();

        public PadKey[] getPadKeyRight();

        public void handleEvent(int key, int value);

        public void redraw();

        public boolean redrawEveryCycle();

        public int getXoff();

        public int getYoff();

        public int getViewType();
    }

    private class ClipOverview implements PadView {

        public PadKey[] padKeyGrid = new PadKey[64];
        public PadKey[] padKeyTop = new PadKey[8];
        public PadKey[] padKeyRight = new PadKey[8];
        private Clip focusedClip = null;
        final static int KEY_NEW = 0;
        final static int KEY_CLIPEDIT = 0;
        final static int KEY_UP = 0;
        final static int KEY_DEL = 1;
        final static int KEY_DOWN = 1;
        final static int KEY_MOVE = 2;
        final static int KEY_COPY = 3;
        final static int KEY_STEPEDIT = 4;
        final static int KEY_CANCEL = 6;
        final static int KEY_OK = 7;
        private int state = 0;
        private int STATE_DEFAULT = 0;
        private int STATE_SELECT_DEVICE = 1;
        private int STATE_SELECT_CHANNEL = 2;
        private int STATE_SELECT_FILE = 3;
        private int STATE_SELECT_TEMPO = 4;
        private int STATE_SELECT_BPM = 4;
        private int STATE_COPY = 5;
        List deviceList = null;
        File[] fileList = null;
        String fileName = "last.xml";
        int deviceSelector = 0;
        int channelSelector = 0;
        int fileSelector = 0;
        int xoff, yoff = 0;

        boolean newEvent = false;
          
        public ClipOverview() {
            initPads(padKeyGrid);
            initPads(padKeyTop);
            initPads(padKeyRight);
        }

        public boolean redrawEveryCycle() {
            return false;
        }

        public int getViewType() {
            return VIEW_ClipOverview;
        }

        public void handleEvent(int key, int value) {
            //window.println("ClipOverview: " + key + " " + value);
            window.println("ClipOverview: " + key + " " + value);
            int[] pos = getXYFromKey(key);
            int x = pos[0];
            int y = pos[1];


            window.println("X: " + pos[0] + " Y: " + pos[1]);

            // handel right function button row
            switch (x) {
                case 8:
                    if (value == 127) {
                        padKeyRight[y].setRed(3); // pressed
                    } else {
                        padKeyRight[y].setRed(0); // released
                    }
                    break;
                case 9:
                    if (value == 127) {
                        window.println("1 true");
                        padKeyTop[1].setPressed(true); // pressed
                    } else {
                        window.println("1 false");
                        padKeyTop[1].setPressed(false); // released
                    }
                    break;
                case 10:
                    if (value == 127) {
                        window.println("2 true");
                        padKeyTop[2].setPressed(true); // pressed
                    } else {
                        window.println("2 false");
                        padKeyTop[2].setPressed(false); // released
                    }
                    break;
                case 11:
                    if (value == 127) {
                        window.println("3 true");
                        padKeyTop[3].setPressed(true); // pressed
                    } else {
                        window.println("3 false");
                        padKeyTop[3].setPressed(false); // released
                    }
            }


            // SAVE SESSION
            if (value == 127 && padKeyRight[KEY_NEW].isSet() && padKeyRight[KEY_DEL].isSet() && (x == 8) && state == STATE_DEFAULT) {
                saveSession();
            } // EXIT PROGRAM
            if (value == 127 && padKeyRight[KEY_NEW].isSet() && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_DEFAULT) {
                exitProgram();

            } // CREATE CLIP
            else if (value == 127 && padKeyRight[KEY_NEW].isSet() && (x < 8) && state == STATE_DEFAULT && cliphash.get(x+"_"+y) == null) {

                // a grid button was pressed, lets light it up
                focusedClip = cliphash.get(x+"_"+y);

                focusedClip = new Clip();
                currentClip = focusedClip;
                focusedClip.clipinfo.xpos = x;
                focusedClip.clipinfo.ypos = y;
                
                for (int i=0;i < padcount ; i++){
                    System.out.println("clipsetup, pad: " + i);
                    if (currentClip.clipinfo.v_xoff.size() < i + 1 ) {
                        System.out.println("clipsetup, add xoff ");
                        currentClip.clipinfo.v_xoff.add(currentClip.clipinfo.xoff);
                    }
                    if (currentClip.clipinfo.v_yoff.size() < i + 1) {
                        System.out.println("clipsetup, add yoff ");
                        currentClip.clipinfo.v_yoff.add(currentClip.clipinfo.yoff);
                    }
                    if (currentClip.clipinfo.v_gridXSize.size() < i + 1) {
                        System.out.println("clipsetup, add gridsize ");
                        currentClip.clipinfo.v_gridXSize.add(currentClip.clipinfo.gridXSize);
                    }
                }                
                
                session.clipInfos.put(x + "_" + y, focusedClip.clipinfo);
                cliphash.put(x + "_" + y, focusedClip);
                deviceList = MidiCommon.getOutputDevices();
                state = STATE_SELECT_DEVICE;
                padPrinter.printScrollLoop((String) deviceList.get(deviceSelector) + "   ");
            } else if (value == 127 && padKeyRight[KEY_CLIPEDIT].isSet() && (x < 8) && state == STATE_DEFAULT && cliphash.get(x+"_"+y) != null) {

                // a grid button was pressed, lets light it up
                focusedClip = cliphash.get(x+"_"+y);

                deviceList = MidiCommon.getOutputDevices();
                state = STATE_SELECT_DEVICE;
                padPrinter.printScrollLoop((String) deviceList.get(deviceSelector) + "   ");
            } else if (value == 127 && padKeyRight[KEY_UP].isSet() && state == STATE_SELECT_DEVICE) {
                if (deviceSelector >= deviceList.size() - 1) {
                    window.println("DeviceSelector to 0");
                    deviceSelector = 0;
                } else {
                    deviceSelector++;
                }
                window.println("DeviceSelector Inc + Print");
                padPrinter.printScrollLoop((String) deviceList.get(deviceSelector) + "   ");
            } else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && state == STATE_SELECT_DEVICE) {
                if (deviceSelector <= 0) {
                    window.println("DeviceSelector to 0");
                    deviceSelector = deviceList.size() - 1;
                } else {
                    deviceSelector--;
                }
                window.println("DeviceSelector Inc + Print");
                padPrinter.printScrollLoop((String) deviceList.get(deviceSelector) + "   ");
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && state == STATE_SELECT_DEVICE) {
                state = STATE_SELECT_CHANNEL;
                focusedClip.setMidiDevice((String) deviceList.get(deviceSelector));
                //            padPrinter.printScrollOnce("OK ");
                padPrinter.printScrollLoop(channelSelector + "   ");
            } // SELECT CHANNEL
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && state == STATE_SELECT_CHANNEL) {
                if (channelSelector >= 15) {
                    channelSelector = 0;
                } else {
                    channelSelector++;
                }
                padPrinter.printScrollLoop(Integer.toString(channelSelector) + "   ");
            } else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && state == STATE_SELECT_CHANNEL) {
                if (channelSelector <= 0) {
                    channelSelector = 15;
                } else {
                    channelSelector--;
                }
                window.println("ChannelSelector Inc + Print");
                padPrinter.printScrollLoop(Integer.toString(channelSelector) + "   ");
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && state == STATE_SELECT_CHANNEL) {
                state = STATE_DEFAULT;
                padPrinter.stop();
                window.println("Selectin channel");
                //          padPrinter.printScrollOnce("OK ");
                focusedClip.setChannel(channelSelector);
                focusedClip.init();

            } // DELETE CLIP
            else if (value == 127 && padKeyRight[KEY_DEL].isSet() && cliphash.get(x+"_"+y) != null && (x < 8) && state == STATE_DEFAULT) {
                focusedClip = cliphash.get(x+"_"+y);
                cliphash.get(x+"_"+y).destroy();
                padKeyGrid[getPos(x, y)].ref = null;
                padKeyGrid[getPos(x, y)].setGreen(0);
                padKeyGrid[getPos(x, y)].setRed(0);

            } // EDIT CLIP
            else if (value == 127 && padKeyRight[KEY_STEPEDIT].isSet() && (x < 8) && state == STATE_DEFAULT && cliphash.get(x+"_"+y) != null) {
                focusedClip = cliphash.get(x+"_"+y);
                currentClip = cliphash.get(x+"_"+y);
                clearAll();
                currentView = (PadView) new ClipStepview(cliphash.get(x+"_"+y));
            } /// LOAD FILE
            else if (value == 127 && padKeyRight[KEY_NEW].isSet() && padKeyRight[KEY_MOVE].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_FILE;
                fileList = getFileList();
                padPrinter.printScrollLoop(fileName);
            } else if (value == 127 && padKeyRight[KEY_UP].isSet() && state == STATE_SELECT_FILE) {
                if (fileSelector >= fileList.length - 1) {
                    window.println("FileSelector to 0");
                    fileSelector = 0;
                } else {
                    fileSelector++;
                }
                window.println("FileSelector Inc + Print");
                fileName = (String) fileList[fileSelector].getName();
                padPrinter.printScrollLoop(fileName + "  ");
            } else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && state == STATE_SELECT_FILE) {
                if (fileSelector <= 0) {
                    window.println("fileSelector to 0");
                    fileSelector = fileList.length - 1;
                } else {
                    fileSelector--;
                }
                window.println("DeviceSelector Inc + Print: " + fileSelector);
                fileName = (String) fileList[fileSelector].getName();
                padPrinter.printScrollLoop(fileName + "  ");
            } else if (padKeyRight[KEY_OK].isSet() && state == STATE_SELECT_FILE) {
                state = STATE_DEFAULT;
                padPrinter.stop();
                window.println("Selectin file");
                loadSession(fileName);
            } else if (padKeyRight[KEY_CANCEL].isSet() && state == STATE_SELECT_FILE) {
                state = STATE_DEFAULT;
                padPrinter.stop();
                window.println("Cancel");
            } ///////////
            else if (padKeyRight[KEY_NEW].isSet() && padKeyRight[KEY_COPY].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_BPM;
                padPrinter.printScrollLoop(session.bpm + "   ");
            } // SELECT CHANNEL
            else if (padKeyRight[KEY_UP].isSet() && state == STATE_SELECT_BPM) {
                if (session.bpm >= 300) {
                    session.bpm = 0;
                } else {
                    session.bpm++;
                }
                padPrinter.printScrollLoop(session.bpm + "   ");
                mainSequencer.setTempoInBPM(session.bpm);
            } else if (padKeyRight[KEY_DOWN].isSet() && state == STATE_SELECT_BPM) {
                if (session.bpm <= 0) {
                    session.bpm = 300;
                } else {
                    session.bpm--;
                }
                padPrinter.printScrollLoop(session.bpm + "   ");
                mainSequencer.setTempoInBPM(session.bpm);
            } else if (padKeyRight[KEY_OK].isSet() && state == STATE_SELECT_BPM) {
                state = STATE_DEFAULT;
                padPrinter.stop();
                window.println("Selectin BPM");
            } // COPY
            else if (value == 127 && padKeyRight[KEY_COPY].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("Copy !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                state = STATE_COPY;
                // a grid button was pressed, lets light it up
                focusedClip = cliphash.get(x+"_"+y);
                currentClip = cliphash.get(x+"_"+y);
            } else if (value == 127 && padKeyRight[KEY_COPY].isSet() && (x < 8) && state == STATE_COPY) {
                window.println("Copy: " + focusedClip);
                copyClip(focusedClip, x, y);
                padKeyGrid[getPos(x, y)].setGreen(3);
                padKeyGrid[getPos(x, y)].setRed(3);
                currentClip = cliphash.get(x+"_"+y);
                state = STATE_DEFAULT;
            } // STOP CLIP
            else if (value == 127 && (x < 8) && cliphash.get(x+"_"+y) != null && cliphash.get(x+"_"+y).isRunning() && state == STATE_DEFAULT) {
                window.println("OFF");
                cliphash.get(x+"_"+y).stop();
                padKeyGrid[getPos(x, y)].setGreen(3);
                padKeyGrid[getPos(x, y)].setRed(3);
                currentClip = cliphash.get(x+"_"+y);
            }// START CLIP
            else if (value == 127 && padKeyTop[1].isPressed() && (x < 8) && cliphash.get(x+"_"+y) != null && cliphash.get(x+"_"+y).isRunning() == false && state == STATE_DEFAULT) {
                window.println("ON IN SYNC");
                window.println(cliphash.get(x+"_"+y).out.getDeviceInfo().getName());
                window.println(cliphash.get(x+"_"+y).clipinfo.name);

//                ((Clip) padKeyGrid[getPos(x, y)].ref).sequencer.setTickPosition(((Clip) padKeyGrid[getPos(x, y)].ref).clipinfo.loopStart);
                cliphash.get(x+"_"+y).startInSync();

                padKeyGrid[getPos(x, y)].setGreen(3);
                padKeyGrid[getPos(x, y)].setRed(0);
                currentClip = cliphash.get(x+"_"+y);
            } // START CLIP
            else if (value == 127 && (x < 8) && cliphash.get(x+"_"+y) != null && cliphash.get(x+"_"+y).isRunning() == false && state == STATE_DEFAULT) {
                window.println("ON");
                window.println(cliphash.get(x+"_"+y).out.getDeviceInfo().getName());
                window.println(cliphash.get(x+"_"+y).clipinfo.name);
                /*                if (starthook!= null) {
                
                 }
                 */
//              ((Clip) padKeyGrid[getPos(x, y)].ref).sequencer.setTickPosition(((Clip) padKeyGrid[getPos(x, y)].ref).clipinfo.loopStart);
                cliphash.get(x+"_"+y).start();
                padKeyGrid[getPos(x, y)].setGreen(3);
                padKeyGrid[getPos(x, y)].setRed(0);
                currentClip = cliphash.get(x+"_"+y);

            }

            draw = new Boolean(true);
            newEvent = true;
        }

        private void createClip(PadKey pad, String device) {
            pad.ref = new Clip();

        }

        private void copyClip(Clip c, int x, int y) {
            if (c == null) {
                System.out.println("DAISTNIX: " + c );
                return;
            }
            PadKey p = padKeyGrid[getPos(x, y)];
            window.println("Copy b");
            Clip n = new Clip();
            p.ref = n;
            n.setMidiDevice(c.clipinfo.outDevice);
            n.setChannel(c.clipinfo.outChannel);
            n.init();
            n.setLoopStartPoint(c.clipinfo.loopStart);
            n.setLoopEndPoint(c.clipinfo.loopEnd);
            n.setTempo(session.bpm);
            n.clipinfo.xpos = x;
            n.clipinfo.ypos = y;
            n.clipinfo.presetBank = c.clipinfo.presetBank;
            n.clipinfo.presetProg = c.clipinfo.presetProg;
            n.clipinfo.gridXSize = c.clipinfo.gridXSize;
//            Collections.copy(n.clipinfo.v_gridXSize,c.clipinfo.v_gridXSize);
            n.clipinfo.v_gridXSize.addAll(c.clipinfo.v_gridXSize);
            n.clipinfo.xoff = c.clipinfo.xoff;
//            Collections.copy(n.clipinfo.v_xoff,c.clipinfo.v_xoff);
            n.clipinfo.v_xoff.addAll(c.clipinfo.v_xoff);
            n.clipinfo.yoff = c.clipinfo.yoff;
//            Collections.copy(n.clipinfo.v_yoff,c.clipinfo.v_yoff);
            n.clipinfo.v_yoff.addAll(c.clipinfo.v_yoff);

            n.clipinfo.PPQS = c.clipinfo.PPQS;
            n.clipinfo.beats = c.clipinfo.beats;
            n.clipinfo.inDevice = c.clipinfo.inDevice;

            session.clipInfos.put(x + "_" + y, n.clipinfo);
            cliphash.put(x + "_" + y, n);
            
            System.out.println("COPIED CLIP: size: " + n.clipinfo.v_xoff.size());

            for (Map.Entry<String, MidiEvent> event : c.notehash.entrySet()) {
                if (event.getKey().endsWith("_off")) {
                    n.setNoteOff((int) event.getValue().getTick(), event.getValue().getMessage().getMessage()[1]);
                } else {
                    n.setNoteOn((int) event.getValue().getTick(), event.getValue().getMessage().getMessage()[1]);
                }
                window.println("Copy c");
            }

        }

        public void redraw() {
            //  window.println("redraw: ");
            System.out.println("redraw: " + this);
            synchronized (drawMutex) {
                int i;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        i = x + ( y * 8 );

                        Clip ref = cliphash.get(x+"_"+y);

                            
                        // TODO: get refs from cliphash, not from padKeys !!!
                        if (ref != null && ref.isRunning()) {
                            //           window.println("Got one: " + i);
                            padKeyGrid[i].setRed(0);
                            padKeyGrid[i].setGreen(3);
                        } else if (ref != null && ref.isRunning() == false) {
                            padKeyGrid[i].setRed(3);
                            padKeyGrid[i].setGreen(3);
                        } else {
                            padKeyGrid[i].setRed(0);
                            padKeyGrid[i].setGreen(0);
                        }
                    }
                }
            }
        }
        
        public PadKey[] getPadKeyGrid() {
            return padKeyGrid;
        }

        public PadKey[] getPadKeyTop() {
            return padKeyTop;
        }

        public PadKey[] getPadKeyRight() {
            return padKeyRight;
        }

        public int getXoff() {
            return xoff;
        }

        public int getYoff() {
            return yoff;
        }
    
    }

    private class ClipStepview implements PadView {

        boolean redraw = true;
        //  int noteLength = 8;
        public PadKey[] padKeyGrid = new PadKey[64];
        public PadKey[] padKeyTop = new PadKey[8];
        public PadKey[] padKeyRight = new PadKey[8];
        Clip clip = null;
        private PadKey focusedPad = null;
        final static int KEY_LOOP_START = 0;
        final static int KEY_UP = 0;
        final static int KEY_PLAY_PAD_A = 1;
        final static int KEY_REC_PAD_A = 1;
        final static int KEY_LOOP_END = 1;
        final static int KEY_DOWN = 1;
        final static int KEY_STEPSIZE = 2;
        final static int KEY_PLAY_PAD_B = 2;
        final static int KEY_NOTEEND = 3;
        final static int KEY_REC_PAD_B = 3;
        final static int KEY_PRESET_BANK = 4;
        final static int KEY_PRESET_PROG = 5;
        final static int KEY_CANCEL = 6;
        final static int KEY_OK = 7;
        final static int KEY_SCROLL = 7;
        private int state = 0;
        private int STATE_DEFAULT = 0;
        private int STATE_SELECT_STEPSIZE = 1;
        private int STATE_SELECT_LOOPSTART = 2;
        private int STATE_SELECT_NOTELENGTH = 3;
        private int STATE_SELECT_BANK = 4;
        private int STATE_SELECT_PROG = 5;
        private int STATE_PLAY_NOTE = 6;
        private int STATE_REC_NOTE = 7;
        private boolean[] keyBoard = {true, false, true, false, true, true, false, true, false, true, false, true};
        int PAGEMODE_DEFAULT = 0;
        int PAGEMODE_SCROLL = 1;
        int PAGEMODE_SWITCH = 2;
        int follow = PAGEMODE_DEFAULT;
        int focusTime = 0;
        int focusRnote = 0;
        int scrollX = 0;
        int scrollY = 0;

        public ClipStepview(Clip c) {
            clip = c;
/*            
            for (int i=0;i < lp.padnum + 1; i++){
                if (c.clipinfo.v_xoff.size() < i) {
                      c.clipinfo.v_xoff.add(c.clipinfo.xoff);
                }
                if (c.clipinfo.v_yoff.size() < i) {
                      c.clipinfo.v_yoff.add(c.clipinfo.yoff);
                }
                if (c.clipinfo.v_gridXSize.size() < i) {
                      c.clipinfo.v_gridXSize.add(c.clipinfo.gridXSize);
                }
            }
 */           
            initPads(padKeyGrid);
            initPads(padKeyTop);
            initPads(padKeyRight);
        }

        public boolean redrawEveryCycle() {
            return true;
        }

        public int getViewType() {
            return VIEW_ClipStepview;
        }

        public void handleEvent(int key, int value) {
            window.println("ClipStepview: " + key + " " + value);
            int[] pos = getXYFromKey(key);
            int x = pos[0];
            int y = pos[1];


            // handel right function button row
            switch (x) {
                case 8:
                    if (value == 127) {
                        padKeyRight[y].setRed(3); // pressed
                    } else {
                        padKeyRight[y].setRed(0); // released
                    }
                    break;
                case 9:
                    if (value == 127) {
                        padKeyTop[1].setPressed(true); // pressed
                    } else {
                        padKeyTop[1].setPressed(false); // released
                    }
                    break;
                case 10:
                    if (value == 127) {
                        padKeyTop[2].setPressed(true); // pressed
                    } else {
                        padKeyTop[2].setPressed(false); // released
                    }
                    break;
                case 11:
                    if (value == 127) {
                        padKeyTop[3].setPressed(true); // pressed
                    } else {
                        padKeyTop[3].setPressed(false); // released
                    }

            }

            //int time = x + clip.clipinfo.xoff;
            int time = x + clip.clipinfo.v_xoff.get(padnum);
            //int note = y + clip.clipinfo.yoff;
            int note = y + clip.clipinfo.v_yoff.get(padnum);
            int rnote = 127 - (note);

            int octanote = rnote + ((6 - x) * 12);

            // PLAY Note
            if (value == 127 && key == 105 && state == STATE_DEFAULT) {
                window.println("PLAY NOTE ON");
                state = STATE_PLAY_NOTE;
            } else if (value == 0 && key == 105 && state == STATE_PLAY_NOTE) {
                window.println("PLAY NOTE OFF");
                state = STATE_DEFAULT;
            } else if (value == 0 && (x < 8) && state == STATE_PLAY_NOTE) {
                window.println("PLAY NOTE OFF: EXEC");
                clip._send(ShortMessage.NOTE_OFF, octanote, 0);
            } else if (value == 127 && (x < 8) && state == STATE_PLAY_NOTE) {
                window.println("PLAY NOTE ON: EXEC");
                clip._send(ShortMessage.NOTE_ON, octanote, 45);
            } // REC NOTE 
            else if (value == 127 && key == 106 && state == STATE_DEFAULT) {
                window.println("REC NOTE ON");
                state = STATE_REC_NOTE;
            } else if (value == 0 && key == 106 && state == STATE_REC_NOTE) {
                window.println("REC NOTE OFF");
                state = STATE_DEFAULT;
            } else if (value == 0 && (x < 8) && state == STATE_REC_NOTE) {
                window.println("REC NOTE OFF: EXEC");
                clip._send(ShortMessage.NOTE_OFF, octanote, 0);
                clip.setNoteOff((int) (((int) clip.getTickPosition() / clip.clipinfo.v_gridXSize.get(padnum)) * clip.clipinfo.v_gridXSize.get(padnum)), rnote + ((6 - x) * 12));
            } else if (value == 127 && (x < 8) && state == STATE_REC_NOTE) {
                window.println("REC NOTE ON: EXEC");
                clip._send(ShortMessage.NOTE_ON, octanote, 45);
                int eventtime = (((int) clip.getTickPosition() / clip.clipinfo.v_gridXSize.get(padnum)) * clip.clipinfo.v_gridXSize.get(padnum));
                if (clip.getEvents().containsKey(tracktime(time) + "_" + octanote)) {
                    eventtime += clip.clipinfo.v_gridXSize.get(padnum);
                    window.println("FIXING ENDTIME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                }
                clip.setNoteOn(eventtime, octanote);
                focusRnote = octanote;
            } // start  
            else if (value == 0 && key == 107 && state == STATE_DEFAULT) {

                if (clip.clipinfo.play == false) {
                    clip.start();
                } else {
                    clip.stop();
                }
            } else if (value == 127 && padKeyRight[KEY_NOTEEND].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("SET NOTEEND: start");
                state = STATE_SELECT_NOTELENGTH;
                focusTime = time;
                focusRnote = rnote;
                //                clip.setNoteLength(tracktime(time), rnote);
            } else if (value == 127 && padKeyRight[KEY_NOTEEND].isSet() && (x < 8) && state == STATE_SELECT_NOTELENGTH) {
                window.println("SET NOTEEND: apply");
                clip.setNoteLength(tracktime(focusTime), focusRnote, tracktime(time - focusTime));
                state = STATE_DEFAULT;
            } // scroll much better
            /*            else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y > 0 && y < 7) && state == STATE_DEFAULT) {
             clip.clipinfo.xoff++;
             } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y > 0 && y < 7) && state == STATE_DEFAULT) {
             if (clip.clipinfo.xoff > 0) {
             clip.clipinfo.xoff--;
             }
             } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 7 && x > 0 && x < 7) && state == STATE_DEFAULT) {
             clip.clipinfo.yoff++;
             } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 0 && x > 0 && x < 7) && state == STATE_DEFAULT) {
             if (clip.clipinfo.yoff > 0) {
             clip.clipinfo.yoff--;
             }
             } // SET LOOP START
             */ // scroll on
            else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 7) && state == STATE_DEFAULT) {
                scrollX = 1;
                scrollY = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 0) && state == STATE_DEFAULT) {
                scrollX = 1;
                scrollY = -1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 7) && state == STATE_DEFAULT) {
                scrollX = -1;
                scrollY = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 0) && state == STATE_DEFAULT) {
                scrollX = -1;
                scrollY = -1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = -1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 7 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 0 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = -1;
            } // scroll off
            else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 7) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 0) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 7) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 0) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (y == 7 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (y == 0 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = 0;
            } // SET LOOP START
            else if (value == 127 && padKeyRight[KEY_LOOP_START].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("SET LOOP START");
//                clip.setLoopStartPoint(tracktime(clip.clipinfo.xoff + x));
                clip.setLoopStartPoint(tracktime(clip.clipinfo.v_xoff.get(padnum) + x));
            } // SET LOOP END
            else if (value == 127 && padKeyRight[KEY_LOOP_END].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("SET LOOP END");
//                clip.setLoopEndPoint(tracktime(clip.clipinfo.xoff + x));
                clip.setLoopEndPoint(tracktime(clip.clipinfo.v_xoff.get(padnum) + x));
            } // SET NOTE
            else if (value == 127 && (x < 8) && (!clip.getEvents().containsKey(tracktime(time) + "_" + rnote)) && state == STATE_DEFAULT) {
                clip.setNote(tracktime(time), rnote);
            } // DELTE NOTE
            else if (value == 127 && (x < 8) && clip.getEvents().containsKey(tracktime(time) + "_" + rnote) && state == STATE_DEFAULT) {
                clip.deleteNote(tracktime(time), rnote);
                clip.turnOffNote(rnote);
            } // DELTE NOTE
            // DELTE NOTE
            // SWITCH TO VIEW: ClipOverview
            else if (value == 127 && padKeyRight[KEY_CANCEL].isSet() && state == STATE_DEFAULT) {
                clearAll();
                currentView = overView;
                currentView.redraw();
                draw = new Boolean(true);
                return;
            } // STEPSIZE
            else if (value == 127 && padKeyRight[KEY_STEPSIZE].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_STEPSIZE;
                redraw = false;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.v_gridXSize.get(padnum)) + "   ");
            } // 
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && state == STATE_SELECT_STEPSIZE) {
//                clip.clipinfo.gridXSize = clip.clipinfo.gridXSize + 1;
                clip.clipinfo.v_gridXSize.set(padnum, new Integer(clip.clipinfo.v_gridXSize.get(padnum).intValue() + 1) );
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.v_gridXSize.get(padnum)) + "   ");
            } // 
            else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && (x == 8) && state == STATE_SELECT_STEPSIZE) {
                if (clip.clipinfo.v_gridXSize.get(padnum) > 1) {
                    //clip.clipinfo.gridXSize = clip.clipinfo.gridXSize - 1;
                    clip.clipinfo.v_gridXSize.set(padnum, new Integer(clip.clipinfo.v_gridXSize.get(padnum).intValue() - 1) );
                }
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.v_gridXSize.get(padnum)) + "   ");
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_SELECT_STEPSIZE) {
                //padPrinter.printScrollLoop(String.valueOf(noteLength)+ "   ");
                state = STATE_DEFAULT;
                padPrinter.stop();
                redraw = true;
            } // TOGGLE FOLLOW ON/OFF
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && padKeyRight[KEY_DOWN].isSet() && state == STATE_DEFAULT) {
                window.println("TOGGLE!!!");
                if (follow == PAGEMODE_DEFAULT) {
                    follow = PAGEMODE_SCROLL;
                } else if (follow == PAGEMODE_SCROLL) {
                    follow = PAGEMODE_SWITCH;
                } else if (follow == PAGEMODE_SWITCH) {
                    follow = PAGEMODE_DEFAULT;
                }
            } // Select Bank
            else if (value == 127 && padKeyRight[KEY_PRESET_BANK].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_BANK;
                redraw = false;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetBank) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && state == STATE_SELECT_BANK) {
                clip.clipinfo.presetBank = clip.clipinfo.presetBank + 1;
                sendBankAndProg();
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetBank) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && (x == 8) && state == STATE_SELECT_BANK) {
                if (clip.clipinfo.presetBank > 0) {
                    clip.clipinfo.presetBank = clip.clipinfo.presetBank - 1;
                }
                sendBankAndProg();

                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetBank) + "   ");
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_SELECT_BANK) {
                //padPrinter.printScrollLoop(String.valueOf(noteLength)+ "   ");
                state = STATE_DEFAULT;
                padPrinter.stop();
                redraw = true;

            } // Select Prog
            else if (value == 127 && padKeyRight[KEY_PRESET_PROG].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_PROG;
                redraw = false;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetProg) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && state == STATE_SELECT_PROG) {
                clip.clipinfo.presetProg = clip.clipinfo.presetProg + 1;
                sendBankAndProg();
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetProg) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && (x == 8) && state == STATE_SELECT_PROG) {
                if (clip.clipinfo.presetProg > 0) {
                    clip.clipinfo.presetProg = clip.clipinfo.presetProg - 1;
                }
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetProg) + "   ");
                sendBankAndProg();
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_SELECT_PROG) {
                //padPrinter.printScrollLoop(String.valueOf(noteLength)+ "   ");
                state = STATE_DEFAULT;
                padPrinter.stop();
                redraw = true;
                // send midi programm change now / make it behave correct for specific midi devices / plugin arch?


            } // TOGGLE FOLLOW ON/OFF


            // TOGGLE FOLLOW ON/OFF
            /*
             else if (value == 127 && padKeyRight[KEY_RIGHT].isSet() && state == STATE_DEFAULT) {
             xoff++;
             } else if (value == 127 && padKeyRight[KEY_LEFT].isSet() && state == STATE_DEFAULT) {
             if (xoff > 0) {
             xoff--;
             }
             } else if (value == 127 && padKeyRight[KEY_UP].isSet() && state == STATE_DEFAULT) {
             yoff++;
             } else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && state == STATE_DEFAULT) {
             if (yoff > 0) {
             yoff--;
             }
             }
             */
//            redraw();
//            draw = new Boolean(true);
        }

        private void sendBankAndProg() {
             System.out.println("sendBankAndProg() " + clip.clipinfo.presetBank + " " + clip.clipinfo.presetProg);

            if (clip.clipinfo.presetBank > -1 && clip.clipinfo.presetProg > -1) {
                // send midi bank change now / make it behave correct for specific midi devices / plugin arch?
                clip._send(ShortMessage.CONTROL_CHANGE, 0, clip.clipinfo.presetBank);
                clip._send(ShortMessage.PROGRAM_CHANGE, clip.clipinfo.presetProg, 0);
            }
        }

        public int padtime(int time) {
            return time / clip.clipinfo.v_gridXSize.get(padnum);
//            return time / clip.clipinfo.PPQS;
        }

        public int tracktime(int time) {
            return time * clip.clipinfo.v_gridXSize.get(padnum);
//            return time * clip.clipinfo.PPQS;
        }

        public void redraw() {

            if (redraw == false) {
                return;
            }
//            window.println("redraw: " + this);
            int xpos = padtime((int) clip.getTickPosition());

            if (scrollX == 1) {
                window.println("clip.clipinfo.xoff: " + clip.clipinfo.v_xoff.get(padnum) + " " + (padtime((Track.MAX_BEATS - 1) * 24) - 8));
                if (clip.clipinfo.v_xoff.get(padnum) < padtime((Track.MAX_BEATS - 1) * 24) - 8) {
                   // clip.clipinfo.xoff++;
                    clip.clipinfo.v_xoff.set(padnum, new Integer(clip.clipinfo.v_xoff.get(padnum).intValue() + 1) );
                } else {
                    scrollX = 0;
                }
            } else if (scrollX == -1) {
                window.println("clip.clipinfo.xoff: " + clip.clipinfo.v_xoff.get(padnum));
                if (clip.clipinfo.v_xoff.get(padnum) > 0) {
                    clip.clipinfo.v_xoff.set(padnum, new Integer(clip.clipinfo.v_xoff.get(padnum).intValue() - 1) );
                   // clip.clipinfo.xoff--;
                } else {
                    scrollX = 0;
                }
            }
            if (scrollY == 1) {
                window.println("clip.clipinfo.yoff: " + clip.clipinfo.v_yoff.get(padnum));
                if (clip.clipinfo.v_yoff.get(padnum) < (127 - 8)) {
                    clip.clipinfo.v_yoff.set(padnum, new Integer(clip.clipinfo.v_yoff.get(padnum).intValue() + 1) );
                  //  clip.clipinfo.yoff++;
                } else {
                    scrollY = 0;
                }
            } else if (scrollY == -1) {
                window.println("clip.clipinfo.yoff: " + clip.clipinfo.v_yoff.get(padnum));
                if (clip.clipinfo.v_yoff.get(padnum) > 0) {
                    clip.clipinfo.v_yoff.set(padnum, new Integer(clip.clipinfo.v_yoff.get(padnum).intValue() - 1) );
                 //   clip.clipinfo.yoff--;
                } else {
                    scrollY = 0;
                }
            }


            if (follow == PAGEMODE_SWITCH && (xpos % 8 == 0)) {
                clip.clipinfo.v_xoff.set(padnum, new Integer(xpos) );
               // clip.clipinfo.xoff = xpos;

            } else if (follow == PAGEMODE_SCROLL) {
                clip.clipinfo.v_xoff.set(padnum, new Integer(xpos) );
             //   clip.clipinfo.xoff = xpos;
            }

            int keyoff, kexoff = 0;
            synchronized (drawMutex) {
                for (int iy = 0; iy < 8; iy++) {
                    // clear top buttons
                    padKeyTop[iy].setRed(0);
                    padKeyTop[iy].setGreen(0);
                    keyoff = (127 - (clip.clipinfo.v_yoff.get(padnum) + iy)) % 12;
                    for (int ix = 0; ix < 8; ix++) {
                        kexoff = (clip.clipinfo.v_xoff.get(padnum) + ix) % clip.clipinfo.beats;
                        if (keyBoard[keyoff] == true && kexoff != 0) {
                            setXY(ix, iy, 0, 0);
                        } else if (kexoff == 0 && keyBoard[keyoff] == true) {
                            setXY(ix, iy, 2, 0);
                        } else if (kexoff == 0 && keyBoard[keyoff] == false) {
                            setXY(ix, iy, 3, 0);
                        } else {
                            setXY(ix, iy, 0, 2);
                        }
                    }
                }

//                window.println("LOOPSter: " + (xoff - clip.clipinfo.loopStart));
                if (xpos >= clip.clipinfo.v_xoff.get(padnum) && xpos < clip.clipinfo.v_xoff.get(padnum) + 8) {
                    //                   window.println("LOOPSTART: " + (xoff + clip.clipinfo.loopStart));
                    padKeyTop[xpos - clip.clipinfo.v_xoff.get(padnum)].setGreen(2);
                }
                if (padtime(clip.clipinfo.loopStart) >= clip.clipinfo.v_xoff.get(padnum) && padtime(clip.clipinfo.loopStart) < clip.clipinfo.v_xoff.get(padnum) + 8) {
//                    window.println("LOOPSTART: " + (xoff + clip.clipinfo.loopStart));
                    padKeyTop[padtime(clip.clipinfo.loopStart) - clip.clipinfo.v_xoff.get(padnum)].setGreen(2);
                }
                if (padtime(clip.clipinfo.loopEnd) >= clip.clipinfo.v_xoff.get(padnum) && padtime(clip.clipinfo.loopEnd) < clip.clipinfo.v_xoff.get(padnum) + 8) {
//                    window.println("LOOPEND: " + (xoff - clip.clipinfo.loopEnd));
                    padKeyTop[padtime(clip.clipinfo.loopEnd) - clip.clipinfo.v_xoff.get(padnum)].setRed(2);
                }

                int type, channel, time, key, rkey;
//              window.println("redraw: " + xoff + " " + yoff);
//              clearGrid();
/*
                 for (MidiEvent ev : clip.getTrack().getEventList()) {
                
                 type = ev.getMessage().getMessage()[0] & 240;
                 channel = ev.getMessage().getMessage()[0] & 15;
                 time = (int) ev.getTick();
                 key = ev.getMessage().getMessage()[1];
                
                 //                  window.println("Type: " + type);
                 //                  window.println("Channel: " + channel);
                 time = padtime(time);
                 //                   System.out.print("Note: " + ev[i].getMessage().getMessage()[0] + " " + time + " " + key + ", ");
                 //        if (key < 8 && time < 8) {
                 //            setXY(time, key, 2, 2);
                 //        }
                 rkey = 127 - key;
                 if (type == 144 && channel == clip.clipinfo.outChannel && rkey - clip.clipinfo.yoff < 8 && time - clip.clipinfo.xoff < 8 && rkey - clip.clipinfo.yoff >= 0 && time - clip.clipinfo.xoff >= 0) {
                 setXY(time - clip.clipinfo.xoff, rkey - clip.clipinfo.yoff, 1, 1);
                 }
                 }
                 * 
                 */
                MidiEvent buff[][] = clip.getTrack().getBuff();

//                time - clip.clipinfo.xoff < 8;
//                && time - clip.clipinfo.xoff >= 0                

                int start = clip.clipinfo.v_gridXSize.get(padnum) * clip.clipinfo.v_xoff.get(padnum);
                int end = clip.clipinfo.v_gridXSize.get(padnum) * (clip.clipinfo.v_xoff.get(padnum) + 8);
                MidiEvent ev = null;

                for (int i = start; i < end; i++) {
                    for (int y = 0; y < 64; y++) {
                        ev = buff[i][y];
                        if (ev == null) {
                            continue;
                        }
                        type = ev.getMessage().getMessage()[0] & 240;
                        channel = ev.getMessage().getMessage()[0] & 15;
                        key = ev.getMessage().getMessage()[1];
                        time = (int) ev.getTick();

                        rkey = 127 - key;
                        if (type == 144 && channel == clip.clipinfo.outChannel && rkey - clip.clipinfo.v_yoff.get(padnum) < 8 && rkey - clip.clipinfo.v_yoff.get(padnum) >= 0) {
                            setXY(padtime(time) - clip.clipinfo.v_xoff.get(padnum), rkey - clip.clipinfo.v_yoff.get(padnum), 2, 2);
                        }

                    }
                }

                //              window.println("start: " + start);
                //              window.println("end: " + end);

            }
            draw = new Boolean(true);
        }

        public PadKey[] getPadKeyGrid() {
            return padKeyGrid;
        }

        public PadKey[] getPadKeyTop() {
            return padKeyTop;
        }

        public PadKey[] getPadKeyRight() {
            return padKeyRight;
        }

        public int getXoff() {
            //return clip.clipinfo.xoff;
            return clip.clipinfo.v_xoff.get(padnum);
        }

        public int getYoff() {
            //return clip.clipinfo.yoff;
            return clip.clipinfo.v_yoff.get(padnum);
        }
    }

    private class KeyStepview implements PadView {

        boolean redraw = true;
        //  int noteLength = 8;
        public PadKey[] padKeyGrid = new PadKey[64];
        public PadKey[] padKeyTop = new PadKey[8];
        public PadKey[] padKeyRight = new PadKey[8];
        Clip clip = null;
        private PadKey focusedPad = null;
        final static int KEY_LOOP_START = 0;
        final static int KEY_UP = 0;
        final static int KEY_PLAY_PAD_A = 1;
        final static int KEY_REC_PAD_A = 1;
        final static int KEY_LOOP_END = 1;
        final static int KEY_DOWN = 1;
        final static int KEY_STEPSIZE = 2;
        final static int KEY_PLAY_PAD_B = 2;
        final static int KEY_NOTEEND = 3;
        final static int KEY_REC_PAD_B = 3;
        final static int KEY_PRESET_BANK = 4;
        final static int KEY_PRESET_PROG = 5;
        final static int KEY_CANCEL = 6;
        final static int KEY_OK = 7;
        final static int KEY_SCROLL = 7;
        private int state = 0;
        private int STATE_DEFAULT = 0;
        private int STATE_SELECT_STEPSIZE = 1;
        private int STATE_SELECT_LOOPSTART = 2;
        private int STATE_SELECT_NOTELENGTH = 3;
        private int STATE_SELECT_BANK = 4;
        private int STATE_SELECT_PROG = 5;
        private int STATE_PLAY_NOTE = 6;
        private int STATE_REC_NOTE = 7;
        private boolean[] keyBoard = {true, false, true, false, true, true, false, true, false, true, false, true};
        int PAGEMODE_MANUAL = 0;
        int PAGEMODE_SCROLL = 1;
        int PAGEMODE_SWITCH = 2;
        int PAGEMODE_DEFAULT = PAGEMODE_SWITCH;
        int follow = PAGEMODE_DEFAULT;
        int focusTime = 0;
        int focusRnote = 0;
        int scrollX = 0;
        int scrollY = 0;

        public KeyStepview(Clip c, int key) {
            clip = c;
            initPads(padKeyGrid);
            initPads(padKeyTop);
            initPads(padKeyRight);
        }

        public boolean redrawEveryCycle() {
            return true;
        }

        public int getViewType() {
            return VIEW_ClipStepview;
        }

        public void handleEvent(int key, int value) {
            window.println("ClipStepview: " + key + " " + value);
            int[] pos = getXYFromKey(key);
            int x = pos[0];
            int y = pos[1];


            // handel right function button row
            switch (x) {
                case 8:
                    if (value == 127) {
                        padKeyRight[y].setRed(3); // pressed
                    } else {
                        padKeyRight[y].setRed(0); // released
                    }
                    break;
                case 9:
                    if (value == 127) {
                        padKeyTop[1].setPressed(true); // pressed
                    } else {
                        padKeyTop[1].setPressed(false); // released
                    }
                    break;
                case 10:
                    if (value == 127) {
                        padKeyTop[2].setPressed(true); // pressed
                    } else {
                        padKeyTop[2].setPressed(false); // released
                    }
                    break;
                case 11:
                    if (value == 127) {
                        padKeyTop[3].setPressed(true); // pressed
                    } else {
                        padKeyTop[3].setPressed(false); // released
                    }

            }

            int time = x + clip.clipinfo.xoff;
            int note = y + clip.clipinfo.yoff;
            int rnote = 127 - (note);

            int octanote = rnote + ((6 - x) * 12);

            // PLAY Note
            if (value == 127 && key == 105 && state == STATE_DEFAULT) {
                window.println("PLAY NOTE ON");
                state = STATE_PLAY_NOTE;
            } else if (value == 0 && key == 105 && state == STATE_PLAY_NOTE) {
                window.println("PLAY NOTE OFF");
                state = STATE_DEFAULT;
            } else if (value == 0 && (x < 8) && state == STATE_PLAY_NOTE) {
                window.println("PLAY NOTE OFF: EXEC");
                clip._send(ShortMessage.NOTE_OFF, octanote, 0);
            } else if (value == 127 && (x < 8) && state == STATE_PLAY_NOTE) {
                window.println("PLAY NOTE ON: EXEC");
                clip._send(ShortMessage.NOTE_ON, octanote, 45);
            } // REC NOTE 
            else if (value == 127 && key == 106 && state == STATE_DEFAULT) {
                window.println("REC NOTE ON");
                state = STATE_REC_NOTE;
            } else if (value == 0 && key == 106 && state == STATE_REC_NOTE) {
                window.println("REC NOTE OFF");
                state = STATE_DEFAULT;
            } else if (value == 0 && (x < 8) && state == STATE_REC_NOTE) {
                window.println("REC NOTE OFF: EXEC");
                clip._send(ShortMessage.NOTE_OFF, octanote, 0);
                clip.setNoteOff((int) (((int) clip.getTickPosition() / clip.clipinfo.gridXSize) * clip.clipinfo.gridXSize), rnote + ((6 - x) * 12));
            } else if (value == 127 && (x < 8) && state == STATE_REC_NOTE) {
                window.println("REC NOTE ON: EXEC");
                clip._send(ShortMessage.NOTE_ON, octanote, 45);
                int eventtime = (((int) clip.getTickPosition() / clip.clipinfo.gridXSize) * clip.clipinfo.gridXSize);
                if (clip.getEvents().containsKey(tracktime(time) + "_" + octanote)) {
                    eventtime += clip.clipinfo.gridXSize;
                    window.println("FIXING ENDTIME !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                }
                clip.setNoteOn(eventtime, octanote);
                focusRnote = octanote;
            } // start  
            else if (value == 0 && key == 107 && state == STATE_DEFAULT) {

                if (clip.clipinfo.play == false) {
                    clip.start();
                } else {
                    clip.stop();
                }
            } else if (value == 127 && padKeyRight[KEY_NOTEEND].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("SET NOTEEND: start");
                state = STATE_SELECT_NOTELENGTH;
                focusTime = time;
                focusRnote = rnote;
                //                clip.setNoteLength(tracktime(time), rnote);
            } else if (value == 127 && padKeyRight[KEY_NOTEEND].isSet() && (x < 8) && state == STATE_SELECT_NOTELENGTH) {
                window.println("SET NOTEEND: apply");
                clip.setNoteLength(tracktime(focusTime), focusRnote, tracktime(time - focusTime));
                state = STATE_DEFAULT;
            } // scroll much better
            /*            else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y > 0 && y < 7) && state == STATE_DEFAULT) {
             clip.clipinfo.xoff++;
             } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y > 0 && y < 7) && state == STATE_DEFAULT) {
             if (clip.clipinfo.xoff > 0) {
             clip.clipinfo.xoff--;
             }
             } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 7 && x > 0 && x < 7) && state == STATE_DEFAULT) {
             clip.clipinfo.yoff++;
             } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 0 && x > 0 && x < 7) && state == STATE_DEFAULT) {
             if (clip.clipinfo.yoff > 0) {
             clip.clipinfo.yoff--;
             }
             } // SET LOOP START
             */ // scroll on
            else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 7) && state == STATE_DEFAULT) {
                scrollX = 1;
                scrollY = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 0) && state == STATE_DEFAULT) {
                scrollX = 1;
                scrollY = -1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 7) && state == STATE_DEFAULT) {
                scrollX = -1;
                scrollY = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 0) && state == STATE_DEFAULT) {
                scrollX = -1;
                scrollY = -1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = -1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 7 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = 1;
            } else if (value == 127 && padKeyRight[KEY_SCROLL].isSet() && (y == 0 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = -1;
            } // scroll off
            else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 7) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y == 0) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 7) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y == 0) && state == STATE_DEFAULT) {
                scrollX = 0;
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 7 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (x == 0 && y > 0 && y < 7) && state == STATE_DEFAULT) {
                scrollX = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (y == 7 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = 0;
            } else if (value == 0 && padKeyRight[KEY_SCROLL].isSet() && (y == 0 && x > 0 && x < 7) && state == STATE_DEFAULT) {
                scrollY = 0;
            } // SET LOOP START
            else if (value == 127 && padKeyRight[KEY_LOOP_START].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("SET LOOP START");
                clip.setLoopStartPoint(tracktime(clip.clipinfo.xoff + x));
            } // SET LOOP END
            else if (value == 127 && padKeyRight[KEY_LOOP_END].isSet() && (x < 8) && state == STATE_DEFAULT) {
                window.println("SET LOOP END");
                clip.setLoopEndPoint(tracktime(clip.clipinfo.xoff + x));
            } // SET NOTE
            else if (value == 127 && (x < 8) && (!clip.getEvents().containsKey(tracktime(time) + "_" + rnote)) && state == STATE_DEFAULT) {
                clip.setNote(tracktime(time), rnote);
            } // DELTE NOTE
            else if (value == 127 && (x < 8) && clip.getEvents().containsKey(tracktime(time) + "_" + rnote) && state == STATE_DEFAULT) {
                clip.deleteNote(tracktime(time), rnote);
            } // DELTE NOTE
            // DELTE NOTE
            // SWITCH TO VIEW: ClipOverview
            else if (value == 127 && padKeyRight[KEY_CANCEL].isSet() && state == STATE_DEFAULT) {
                clearAll();
                currentView = overView;
                currentView.redraw();
                draw = new Boolean(true);
                return;
            } // STEPSIZE
            else if (value == 127 && padKeyRight[KEY_STEPSIZE].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_STEPSIZE;
                redraw = false;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.gridXSize) + "   ");
            } // 
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && state == STATE_SELECT_STEPSIZE) {
                clip.clipinfo.gridXSize = clip.clipinfo.gridXSize + 1;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.gridXSize) + "   ");
            } // 
            else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && (x == 8) && state == STATE_SELECT_STEPSIZE) {
                if (clip.clipinfo.gridXSize > 1) {
                    clip.clipinfo.gridXSize = clip.clipinfo.gridXSize - 1;
                }
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.gridXSize) + "   ");
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_SELECT_STEPSIZE) {
                //padPrinter.printScrollLoop(String.valueOf(noteLength)+ "   ");
                state = STATE_DEFAULT;
                padPrinter.stop();
                redraw = true;
            } // TOGGLE FOLLOW ON/OFF
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && padKeyRight[KEY_DOWN].isSet() && state == STATE_DEFAULT) {
                window.println("TOGGLE!!!");
                if (follow == PAGEMODE_DEFAULT) {
                    follow = PAGEMODE_SCROLL;
                } else if (follow == PAGEMODE_SCROLL) {
                    follow = PAGEMODE_SWITCH;
                } else if (follow == PAGEMODE_SWITCH) {
                    follow = PAGEMODE_DEFAULT;
                }
            } // Select Bank
            else if (value == 127 && padKeyRight[KEY_PRESET_BANK].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_BANK;
                redraw = false;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetBank) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && state == STATE_SELECT_BANK) {
                clip.clipinfo.presetBank = clip.clipinfo.presetBank + 1;
                sendBankAndProg();
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetBank) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && (x == 8) && state == STATE_SELECT_BANK) {
                if (clip.clipinfo.presetBank > 0) {
                    clip.clipinfo.presetBank = clip.clipinfo.presetBank - 1;
                }
                sendBankAndProg();

                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetBank) + "   ");
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_SELECT_BANK) {
                //padPrinter.printScrollLoop(String.valueOf(noteLength)+ "   ");
                state = STATE_DEFAULT;
                padPrinter.stop();
                redraw = true;

            } // Select Prog
            else if (value == 127 && padKeyRight[KEY_PRESET_PROG].isSet() && (x == 8) && state == STATE_DEFAULT) {
                state = STATE_SELECT_PROG;
                redraw = false;
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetProg) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_UP].isSet() && (x == 8) && state == STATE_SELECT_PROG) {
                clip.clipinfo.presetProg = clip.clipinfo.presetProg + 1;
                sendBankAndProg();
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetProg) + "   ");
            } //
            else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && (x == 8) && state == STATE_SELECT_PROG) {
                if (clip.clipinfo.presetProg > 0) {
                    clip.clipinfo.presetProg = clip.clipinfo.presetProg - 1;
                }
                padPrinter.printScrollLoop(String.valueOf(clip.clipinfo.presetProg) + "   ");
                sendBankAndProg();
            } else if (value == 127 && padKeyRight[KEY_OK].isSet() && (x == 8) && state == STATE_SELECT_PROG) {
                //padPrinter.printScrollLoop(String.valueOf(noteLength)+ "   ");
                state = STATE_DEFAULT;
                padPrinter.stop();
                redraw = true;
                // send midi programm change now / make it behave correct for specific midi devices / plugin arch?


            } // TOGGLE FOLLOW ON/OFF


            // TOGGLE FOLLOW ON/OFF
            /*
             else if (value == 127 && padKeyRight[KEY_RIGHT].isSet() && state == STATE_DEFAULT) {
             xoff++;
             } else if (value == 127 && padKeyRight[KEY_LEFT].isSet() && state == STATE_DEFAULT) {
             if (xoff > 0) {
             xoff--;
             }
             } else if (value == 127 && padKeyRight[KEY_UP].isSet() && state == STATE_DEFAULT) {
             yoff++;
             } else if (value == 127 && padKeyRight[KEY_DOWN].isSet() && state == STATE_DEFAULT) {
             if (yoff > 0) {
             yoff--;
             }
             }
             */
//            redraw();
//            draw = new Boolean(true);
        }

        private void sendBankAndProg() {
            if (clip.clipinfo.presetBank > -1 && clip.clipinfo.presetProg > -1) {
                // send midi bank change now / make it behave correct for specific midi devices / plugin arch?
                clip._send(ShortMessage.CONTROL_CHANGE, 0, clip.clipinfo.presetBank);
                clip._send(ShortMessage.PROGRAM_CHANGE, clip.clipinfo.presetProg, 0);
            }
        }

        public int padtime(int time) {
            return time / clip.clipinfo.gridXSize;
//            return time / clip.clipinfo.PPQS;
        }

        public int tracktime(int time) {
            return time * clip.clipinfo.gridXSize;
//            return time * clip.clipinfo.PPQS;
        }

        public void redraw() {

            if (redraw == false) {
                return;
            }
            //          window.println("redraw");
            int xpos = padtime((int) clip.getTickPosition());

            if (scrollX == 1) {
                window.println("clip.clipinfo.xoff: " + clip.clipinfo.xoff + " " + (padtime((Track.MAX_BEATS - 1) * 24) - 8));
                if (clip.clipinfo.xoff < padtime((Track.MAX_BEATS - 1) * 24) - 8) {
                    clip.clipinfo.xoff++;
                } else {
                    scrollX = 0;
                }
            } else if (scrollX == -1) {
                window.println("clip.clipinfo.xoff: " + clip.clipinfo.xoff);
                if (clip.clipinfo.xoff > 0) {
                    clip.clipinfo.xoff--;
                } else {
                    scrollX = 0;
                }
            }
            if (scrollY == 1) {
                window.println("clip.clipinfo.yoff: " + clip.clipinfo.yoff);
                if (clip.clipinfo.yoff < (127 - 8)) {
                    clip.clipinfo.yoff++;
                } else {
                    scrollY = 0;
                }
            } else if (scrollY == -1) {
                window.println("clip.clipinfo.yoff: " + clip.clipinfo.yoff);
                if (clip.clipinfo.yoff > 0) {
                    clip.clipinfo.yoff--;
                } else {
                    scrollY = 0;
                }
            }


            if (follow == PAGEMODE_SWITCH && (xpos % 8 == 0)) {
                clip.clipinfo.xoff = xpos;

            } else if (follow == PAGEMODE_SCROLL) {
                clip.clipinfo.xoff = xpos;
            }

            int keyoff, kexoff = 0;
            synchronized (drawMutex) {
                for (int iy = 0; iy < 8; iy++) {
                    // clear top buttons
                    padKeyTop[iy].setRed(0);
                    padKeyTop[iy].setGreen(0);
                    keyoff = (127 - (clip.clipinfo.yoff + iy)) % 12;
                    for (int ix = 0; ix < 8; ix++) {
                        kexoff = (clip.clipinfo.xoff + ix) % clip.clipinfo.beats;
                        if (keyBoard[keyoff] == true && kexoff != 0) {
                            setXY(ix, iy, 0, 0);
                        } else if (kexoff == 0 && keyBoard[keyoff] == true) {
                            setXY(ix, iy, 2, 0);
                        } else if (kexoff == 0 && keyBoard[keyoff] == false) {
                            setXY(ix, iy, 3, 0);
                        } else {
                            setXY(ix, iy, 0, 2);
                        }
                    }
                }

//                window.println("LOOPSter: " + (xoff - clip.clipinfo.loopStart));
                if (xpos >= clip.clipinfo.xoff && xpos < clip.clipinfo.xoff + 8) {
                    //                   window.println("LOOPSTART: " + (xoff + clip.clipinfo.loopStart));
                    padKeyTop[xpos - clip.clipinfo.xoff].setGreen(2);
                }
                if (padtime(clip.clipinfo.loopStart) >= clip.clipinfo.xoff && padtime(clip.clipinfo.loopStart) < clip.clipinfo.xoff + 8) {
//                    window.println("LOOPSTART: " + (xoff + clip.clipinfo.loopStart));
                    padKeyTop[padtime(clip.clipinfo.loopStart) - clip.clipinfo.xoff].setGreen(2);
                }
                if (padtime(clip.clipinfo.loopEnd) >= clip.clipinfo.xoff && padtime(clip.clipinfo.loopEnd) < clip.clipinfo.xoff + 8) {
//                    window.println("LOOPEND: " + (xoff - clip.clipinfo.loopEnd));
                    padKeyTop[padtime(clip.clipinfo.loopEnd) - clip.clipinfo.xoff].setRed(2);
                }

                int type, channel, time, key, rkey;
//              window.println("redraw: " + xoff + " " + yoff);
//              clearGrid();
/*
                 for (MidiEvent ev : clip.getTrack().getEventList()) {
                
                 type = ev.getMessage().getMessage()[0] & 240;
                 channel = ev.getMessage().getMessage()[0] & 15;
                 time = (int) ev.getTick();
                 key = ev.getMessage().getMessage()[1];
                
                 //                  window.println("Type: " + type);
                 //                  window.println("Channel: " + channel);
                 time = padtime(time);
                 //                   System.out.print("Note: " + ev[i].getMessage().getMessage()[0] + " " + time + " " + key + ", ");
                 //        if (key < 8 && time < 8) {
                 //            setXY(time, key, 2, 2);
                 //        }
                 rkey = 127 - key;
                 if (type == 144 && channel == clip.clipinfo.outChannel && rkey - clip.clipinfo.yoff < 8 && time - clip.clipinfo.xoff < 8 && rkey - clip.clipinfo.yoff >= 0 && time - clip.clipinfo.xoff >= 0) {
                 setXY(time - clip.clipinfo.xoff, rkey - clip.clipinfo.yoff, 1, 1);
                 }
                 }
                 * 
                 */
                MidiEvent buff[][] = clip.getTrack().getBuff();

//                time - clip.clipinfo.xoff < 8;
//                && time - clip.clipinfo.xoff >= 0                

                int start = clip.clipinfo.gridXSize * clip.clipinfo.xoff;
                int end = clip.clipinfo.gridXSize * (clip.clipinfo.xoff + 8);
                MidiEvent ev = null;

                for (int i = start; i < end; i++) {
                    for (int y = 0; y < 64; y++) {
                        ev = buff[i][y];
                        if (ev == null) {
                            continue;
                        }
                        type = ev.getMessage().getMessage()[0] & 240;
                        channel = ev.getMessage().getMessage()[0] & 15;
                        key = ev.getMessage().getMessage()[1];
                        time = (int) ev.getTick();

                        rkey = 127 - key;
                        if (type == 144 && channel == clip.clipinfo.outChannel && rkey - clip.clipinfo.yoff < 8 && rkey - clip.clipinfo.yoff >= 0) {
                            setXY(padtime(time) - clip.clipinfo.xoff, rkey - clip.clipinfo.yoff, 2, 2);
                        }

                    }
                }

                //              window.println("start: " + start);
                //              window.println("end: " + end);

            }
            draw = new Boolean(true);
        }

        public PadKey[] getPadKeyGrid() {
            return padKeyGrid;
        }

        public PadKey[] getPadKeyTop() {
            return padKeyTop;
        }

        public PadKey[] getPadKeyRight() {
            return padKeyRight;
        }

        public int getXoff() {
            return clip.clipinfo.xoff;
        }

        public int getYoff() {
            return clip.clipinfo.yoff;
        }
    }

    private class WindowUpdater implements Runnable {

        public WindowUpdater() {
            new Thread(this).start();
        }

        public void run() {
            while (true) {
                //  System.out.print("Running.");
                try {
                    Thread.currentThread().sleep(100);

                    updateWindow();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PadUpdater implements Runnable {

        ShortMessage[] msgbuf = new ShortMessage[64 + 8 + 8];
        boolean bstate = true;

        public PadUpdater() {
            new Thread(this).start();
        }

        public void run() {
            while (true) {
                //  System.out.print("Running.");
                try {
                    while (draw == false) {
                        Thread.currentThread().sleep(25);
                    }
                    if (currentView.redrawEveryCycle()) {
                        currentView.redraw();
                    }

                    if (currentView instanceof ClipOverview ) {
                        if (((ClipOverview)currentView).newEvent == true) {   
                           ((ClipOverview)currentView).newEvent = false;
                           for (Launchpad temp : padlist) {
                               if (temp != lp && temp.currentView instanceof ClipOverview && temp.draw == false) {
                                   System.out.println("Syncing other lauchnpads clipview");
                                   temp.draw = new Boolean(true);
                                   temp.currentView.redraw();
                               }
                           }
                       }
                    }

                    sendAll();
                    if (currentView.redrawEveryCycle()) {
                        Thread.currentThread().sleep(25);
                        draw = new Boolean(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendAll() {
            ShortMessage msg;
            int idx = 0;
            try {
//            window.println("sendAll");
                synchronized (drawMutex) {
                    for (int i = 0; i < 64; i += 2) {
                        msg = new ShortMessage();
                        msg.setMessage(146, 2, currentView.getPadKeyGrid()[i].getVelocity(), currentView.getPadKeyGrid()[i + 1].getVelocity());
                        msgbuf[idx++] = msg;
                    }
                    for (int i = 0; i
                            < 8; i += 2) {
                        msg = new ShortMessage();
                        msg.setMessage(146, 2, currentView.getPadKeyRight()[i].getVelocity(), currentView.getPadKeyRight()[i + 1].getVelocity());
                        msgbuf[idx++] = msg;
                    }
                    for (int i = 0; i
                            < 8; i += 2) {
                        msg = new ShortMessage();
                        msg.setMessage(146, 2, currentView.getPadKeyTop()[i].getVelocity(), currentView.getPadKeyTop()[i + 1].getVelocity());
                        msgbuf[idx++] = msg;
                    }
                    draw = new Boolean(false);

                }

                if (receiver != null) {
                    for (int i = 0; i < (64 + 8 + 8) / 2; i++) {
                        receiver.send(msgbuf[i], -1);
                    }

                    if (bstate) {
                        bstate = false;
                        send(176, 0, 52);
                    } else {
                        bstate = true;
                        send(176, 0, 49);
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private class PadPrinter implements Runnable {

        String text = null;
        boolean loop = false;
        boolean update = false;
        int loopcount = 0;

        public PadPrinter() {
            new Thread(this).start();
        }

        public void printScrollLoop(String val) {
            text = val;
            loop = true;
            loopcount = 0;
            update = true;
        }

        public void printScrollOnce(String val) {
            text = val;
            loop = false;
            loopcount = 0;
            update = true;
        }

        public void stop() {
            loop = false;
            update = true;
        }

        public void run() {
            while (true) {
                if (text != null && currentView != null) {
                    if (loop) {
                        loopcount = 0;
                        while (loop) {
                            window.println("loop text: " + text);
                            printScroll(text);
                            clearGrid();
                            loopcount++;
                        }
                        currentView.redraw();
                    } else if (loopcount == 0 && text != null) {
                        printScroll(text);
                        currentView.redraw();
                    }

                }
                try {
                    Thread.currentThread().sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void printScroll(String text) {
            boolean empty = false;
            try {
                for (char c : text.toCharArray()) {
                    // window.println((int) c[i]);
                    if (update) {
                        update = false;
                        return;
                    }
                    if ((int) c < 33 || c > 127) {
                        empty = true;
                    } else {
                        empty = false;
                    }

                    int offset = (c - 33) * 8;
                    int end = 8;
                    if (empty) {
                        end = 3;
                    }

                    for (int x = offset; x < offset + end; x++) {
                        boolean skip = true;
                        if (!empty) {
                            for (int y = 0; y < 8; y++) {
                                if (font.getRGB(x, y) < -1) {
                                    skip = false;
                                }
                            }
                            if (skip && (x - offset > 0)) {
                                continue;
                            }
                        }

                        while (draw == true) {
                            Thread.currentThread().sleep(60);
                        }

                        synchronized (drawMutex) {

                            for (int xs = 1; xs < 8; xs++) {
                                for (int ys = 0; ys < 8; ys++) {
                                    currentView.getPadKeyGrid()[getPos(xs - 1, ys)].setGreen(currentView.getPadKeyGrid()[getPos(xs, ys)].getGreen());
                                    currentView.getPadKeyGrid()[getPos(xs - 1, ys)].setRed(currentView.getPadKeyGrid()[getPos(xs, ys)].getRed());
                                }
                            }

                            for (int y = 0; y < 8; y++) {
                                if (!empty && font.getRGB(x, y) < -1) {
                                    setXY(7, y, 2, 2);
                                    //  System.out.print("X");
                                } else {
                                    setXY(7, y, 0, 0);
                                }
                            }
                            draw = new Boolean(true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Clip implements MetaEventListener {

        HashMap<String, MidiEvent> notehash = new HashMap();
        public MidiDevice out = null;
        private MidiDevice in = null;
        private Sequence sequence = null;
        private Track track = null;
        private Receiver receiver = null;
        private Transmitter transmitter = null;
        private int nChannel = 0;
        private String strDeviceName = "Out To MIDI Yoke:  1";
        ClipInfo clipinfo = new ClipInfo();
        MidiEvent endMarker = null;
        MidiEvent tempoMarker = null;
        final static int META_END_OF_TRACK = 47;

        public Clip() {
            window.println("NEW CLIP ");
        }

        public Clip(String devicename) {
            window.println("NEW CLIP: " + devicename);
            clipinfo.outDevice = devicename;
        }

        public void destroy() {
            session.clipInfos.remove(clipinfo.xpos + "_" + clipinfo.ypos);
            cliphash.remove(clipinfo.xpos + "_" + clipinfo.ypos);

            mainSequencer.remove(sequence);
        }

        public Track getTrack() {
            return track;
        }

        public void _send(int type, int a, int b) {
            try {
                window.println("SEND!!!; " + type + " " + clipinfo.outChannel + " " + a + " " + b);
                ShortMessage msg = new ShortMessage();
                msg.setMessage(type, clipinfo.outChannel, a, b);
                if (this.receiver != null) {
                    this.receiver.send(msg, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void _send(int type, int a) {
            try {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(type, clipinfo.outChannel, a);
                if (this.receiver != null) {
                    this.receiver.send(msg, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public HashMap getEvents() {
            return notehash;
        }

        public void setMidiDevice(String value) {
            clipinfo.outDevice = value;
        }

        public void setChannel(int value) {
            clipinfo.outChannel = value;
        }

        public void setLoopStartPoint(int value) {
            int pos = value;
            if (pos >= clipinfo.loopEnd) {
                return;
            }
            if (sequence.getTickLength() <= pos) {
                setEndMarker(pos);
                // insert some cc note at the desired end position
            }
            clipinfo.loopStart = pos;
            mainSequencer.setLoopStartPoint(sequence, clipinfo.loopStart);
        }

        public void setLoopEndPoint(int value) {
            int pos = value;
            if (pos <= clipinfo.loopStart) {
                return;
            }
            if (sequence.getTickLength() <= pos) {
                setEndMarker(pos);
                // insert some cc note at the desired end position
            }

            clipinfo.loopEnd = pos;
        }

        public void setEndMarker(int time) {
            ShortMessage msg = new ShortMessage();
            try {
                window.println("setEndMarker");
                msg.setMessage(ShortMessage.CONTROL_CHANGE, clipinfo.outChannel, 0, 0);
                MidiEvent ev = new MidiEvent(msg, time);
                track.add(ev);
                track.remove(endMarker);
                endMarker = ev;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void deleteNote(MidiEvent e) {
            if (notehash.containsValue(e)) {
                for (Map.Entry<String, MidiEvent> infoSet : notehash.entrySet()) {
                    if (infoSet.getValue() == e) {
                        String keyval[] = infoSet.getKey().split("_");
                        deleteNote(new Integer(keyval[0]).intValue(), new Integer(keyval[1]).intValue());
                    }
                }
            }
        }

        public void deleteNote(int time, int key) {
            window.println("deleteNote: " + time + "_" + key);
            if (notehash.containsKey(time + "_" + key)) {
                track.remove(notehash.get(time + "_" + key));
                track.remove(notehash.get(time + "_" + key + "_off"));
                track.remove(notehash.get(time + "_" + key + "_cc"));
                notehash.remove(time + "_" + key);
                notehash.remove(time + "_" + key + "_off");
                notehash.remove(time + "_" + key + "_cc");
            }
        }

        public void deleteNotes() {
            for (String key : notehash.keySet().toArray(new String[]{})) {
                String keyval[] = key.split("_");
                deleteNote(new Integer(keyval[0]).intValue(), new Integer(keyval[1]).intValue());
            }
        }

        public void setNote(int time, int key) {
            try {
                //          time = time * PPQS;
                window.println("setNote: " + time + "_" + key);
                ShortMessage msg_on = new ShortMessage();
                msg_on.setMessage(ShortMessage.NOTE_ON, clipinfo.outChannel, key, 45);
                ShortMessage msg_off = new ShortMessage();
                msg_off.setMessage(ShortMessage.NOTE_OFF, clipinfo.outChannel, key, 0);
                MidiEvent ev_on = new MidiEvent(msg_on, time);
                MidiEvent ev_off = new MidiEvent(msg_off, time + (clipinfo.v_gridXSize.get(padnum) - 1));
                window.println("setNote: ON: " + time);
                window.println("setNote: OFF: " + (time + clipinfo.v_gridXSize.get(padnum)));

                /*               
                 this.sequence.
                 for (int i = 0; i < track.size(); i++) {
                 r  es[i] = track.get(i);
                 }
                 */
                //              synchronized (track) {
                track.add(ev_off);
                track.add(ev_on);
                //              }
                notehash.put(time + "_" + key, ev_on);
                notehash.put(time + "_" + key + "_off", ev_off);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setTB303Note(int timepos, String note, int transpose, int accent, int slide, int time) {
            if (notes.containsKey(note)) {
                setTB303Note(timepos, (int) notes.get(note), transpose, accent, slide, time);
            }
        }

        public void setTB303Note(int timepos, int note, int transpose, int accent, int slide, int time) {
            timepos = timepos * 6;

            int velocity;
            int cc_slide303 = 1;
            int cc_slide_val;
            try {
                if (accent == 1) {
                    velocity = 127;
                } else {
                    velocity = 99;
                }

                if (slide == 1) {
                    cc_slide_val = 1;
                } else {
                    cc_slide_val = 0;
                }

                int basenote = 36;  // wherever your 303´s rootkey is
                if (transpose == 0) {
                    note = note + 23;
                } else if (transpose == 1) {
                    note = note + 35;
                } else if (transpose == -1) {
                    note = note + 11;
                }
                note = note + basenote;

                //          time = time * PPQS;
                window.println("set303Note: " + timepos + "_" + note);
                ShortMessage msg_on = new ShortMessage();
                msg_on.setMessage(ShortMessage.NOTE_ON, clipinfo.outChannel, note, velocity);
                ShortMessage msg_off = new ShortMessage();
                msg_off.setMessage(ShortMessage.NOTE_OFF, clipinfo.outChannel, note, 0);
                MidiEvent ev_on = new MidiEvent(msg_on, timepos);

                //MidiEvent ev_off = new MidiEvent(msg_off, timepos + ( 6 * time ) + cc_slide_val);
                int endpos = timepos + (3 * time);

                if (endpos >= clipinfo.loopEnd) {
                    endpos = endpos - clipinfo.loopEnd;
                }

                MidiEvent ev_off = new MidiEvent(msg_off, endpos);


                ShortMessage msg_slide = new ShortMessage();
                msg_slide.setMessage(ShortMessage.CONTROL_CHANGE, clipinfo.outChannel, cc_slide303, cc_slide_val);
                MidiEvent ev_slide = new MidiEvent(msg_slide, timepos);

                /*               
                 this.sequence.
                 for (int i = 0; i < track.size(); i++) {
                 r  es[i] = track.get(i);
                 }
                 */
                //              synchronized (track) {
                track.add(ev_off);
                track.add(ev_on);
                track.add(ev_slide);
                //              }
                notehash.put(timepos + "_" + note, ev_on);
                notehash.put(timepos + "_" + note + "_off", ev_off);
                notehash.put(timepos + "_" + note + "_cc", ev_slide);
            } catch (Exception e) {
                e.printStackTrace();
            }



        }

        public void setNoteOn(int time, int key) {
                setNoteOn(time, key, 45 );
        }
        
        public void setNoteOn(int time, int key, int vel) {
            try {
                //          time = time * PPQS;
                window.println("setNote: " + time + "_" + key);
                ShortMessage msg_on = new ShortMessage();
                msg_on.setMessage(ShortMessage.NOTE_ON, clipinfo.outChannel, key, vel);
                MidiEvent ev_on = new MidiEvent(msg_on, time);
                track.add(ev_on);
                notehash.put(time + "_" + key, ev_on);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setNoteOff(int time, int key) {
            try {
                //          time = time * PPQS;
                ShortMessage msg_off = new ShortMessage();
                msg_off.setMessage(ShortMessage.NOTE_OFF, clipinfo.outChannel, key, 0);
                MidiEvent ev_off = new MidiEvent(msg_off, time);
                track.add(ev_off);
                notehash.put(time + "_" + key + "_off", ev_off);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setNoteLength(int time, int key, int length) {
            window.println("setNoteLength: " + time + " " + key + " " + length);
            String hashkey = time + "_" + key + "_off";
            if (length < 0) {
                return;
            }
            if (notehash.containsKey(hashkey)) {
                MidiEvent obsoleteEv = (MidiEvent) this.notehash.get(hashkey);
                MidiEvent modifiedEv = new MidiEvent(obsoleteEv.getMessage(), time + length);
                track.add(modifiedEv);
                notehash.remove(hashkey);
                notehash.put(hashkey, modifiedEv);
                track.remove(obsoleteEv);
            }

        }

        void setTempo(int bpm) {
            track.remove(tempoMarker);

            tempoMarker = new MidiEvent(tempoChangeMessage(bpm), 0);
            track.add(tempoMarker);
        }

        public void meta(MetaMessage event) /* Meta-events trigger this method.
         The end-of-track meta-event signals that the sequence has finished
         */ {
            window.println("META EVENT: " + event.getType() + ", " + event.getStatus());
        } // end of meta()

        public void run() {
            while (true) {
                try {
                    Thread.currentThread().sleep(10000);
                } catch (Exception e) {
                }
            }
        }

        public void init() {
            try {
                clipinfo.events = notehash;
                if (sequence == null) {
                    sequence = new Sequence(Sequence.PPQ, clipinfo.PPQS);
                }
                if (outDevs.containsKey(clipinfo.outDevice)) {
                    window.println("DEVICE CACHED!!! " + clipinfo.outDevice);
                    out = outDevs.get(clipinfo.outDevice);
                    receiver = out.getReceiver();
                } else {
                    window.println("DEVICE NEW!!! " + clipinfo.outDevice);
                    if (clipinfo.outDevice != null) {
                        MidiDevice.Info info = MidiCommon.getMidiDeviceInfo(clipinfo.outDevice, true);
                        if (info == null) {
                            out("no device info found for name " + clipinfo.outDevice);
                            System.exit(1);
                        }
                        out = MidiSystem.getMidiDevice(info);
                        outDevs.put(clipinfo.outDevice, out);
                        if (DEBUG) {
                            out("MidiDevice: " + out);
                        }
                        out.open();
                        if (out == null) {
                            out("wasn't able to retrieve MidiDevice");
                            System.exit(1);
                        }
                        receiver = out.getReceiver();
                    } else {
                        /*	We retrieve a Receiver for the default
                         MidiDevice.
                         */
                        receiver = MidiSystem.getReceiver();
                    }
                    if (receiver == null) {
                        out("wasn't able to retrieve Receiver");
                        System.exit(1);
                    }

                    if (DEBUG) {
                        out("Receiver: " + receiver);
                    }
                }

                if (track == null) {
                    track = sequence.createTrack();
                }


                mainSequencer.setSequence(sequence, this);

            } catch (Exception e) {
                e.printStackTrace();
            }
            setupTrack();

        }

        private void setupTrack() {
            window.println("setupTrack");
            try {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.CONTROL_CHANGE, clipinfo.outChannel, 0, 0);
                MidiEvent ev = new MidiEvent(msg, clipinfo.loopEnd);
                track.add(ev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isRunning() {
            return clipinfo.play;
        }

        public void start() {
            synchronized (sequence) {
                if (clipinfo.play == false) {
                    clipinfo.play = true;
                    window.println("Start: Clip: " + this);
                    sendBankAndProg();
                    mainSequencer.start(sequence);
                }
            }
        }

        public void startInSync() {
            synchronized (sequence) {
                if (clipinfo.play == false) {
                    clipinfo.play = true;
                    window.println("Start: Clip: " + this);
                    sendBankAndProg();
                    mainSequencer.startInSync(sequence);
                }
            }
        }

        public void stop() {
            synchronized (sequence) {
                if (clipinfo.play == true) {
                    clipinfo.play = true;
                    window.println("Stop: Clip: " + this);
                    clipinfo.play = false;
                    mainSequencer.stop(sequence);
                    this.turnOffNotes();
                }
            }
        }

        private void sendBankAndProg() {
            // send midi bank change now / make it behave correct for specific midi devices / plugin arch?
                System.out.println("sendBankAndProg() " + clipinfo.presetBank + " " + clipinfo.presetProg);
            if (clipinfo.presetBank > -1 && clipinfo.presetProg > -1) {
                this._send(ShortMessage.CONTROL_CHANGE, 0, clipinfo.presetBank);
                this._send(ShortMessage.PROGRAM_CHANGE, clipinfo.presetProg, 0);
            }
        }

        public long getTickPosition() {
            return mainSequencer.getTickPosition(sequence);
        }

        public void turnOffNotes() {
            for (Integer note : track.getForgotten()) {
                window.println("Stop: forgotten note: " + note);
                this._send(ShortMessage.NOTE_OFF, note, 0);
            }
            track.forgetAll();
        }

        public void turnOffNote(Integer note) {
            if (track.hasForgottenNote(note)) {
                window.println("Stop: forgotten note: " + note);
                this._send(ShortMessage.NOTE_OFF, note, 0);
                track.forget(note);
            }
        }
    }

    public Launchpad() {
        this.padnum = 0;
        lp = this;
        new Thread(this).start();
    }

    public Launchpad(String devName) {
        this.padnum = 0;
        lp = this;
        launchpadDeviceName = devName;
        new Thread(this).start();
    }

    public void run() {
        if (session == null) {
            session = new SessionInfo();
        }
        loadFont();
        init();
        reset();
         
        padUp = new PadUpdater();
        padPrinter = new PadPrinter();

        WindowUpdater winUp = new WindowUpdater();

        overView = new ClipOverview();
        currentView = overView;

        // show pad ip on startup
        print(padnum+" "+padnum+" "+padnum+" ");
        print(padnum+" "+padnum+" "+padnum+" ");

        //        this.padPrinter.printScroll("3 2 1   ");
        //       print("Start   ");
        clearGrid();
        if (new File(session.storedir + "default.xml").exists()) {
            loadSession("default.xml");
            this.draw = true;
        } else if (new File(session.storedir + "last.xml").exists()) {
            window.println("LOADING last.xml");
            loadSession("last.xml");
            this.draw = true;
        }
        final Launchpad lp = this;
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
//                try {
                //   keepRunning = false;
                lp.stop();
                saveSession();
                //    mainThread.join();
                //               } catch (InterruptedException ex) {
                //                   Logger.getLogger(Launchpad.class.getName()).log(Level.SEVERE, null, ex);
                //               }
            }
        });

        while (true) {
            //  System.out.print("Running.");
            try {
//                print("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!?+-*");
                Thread.sleep(120000);
//                if (session.autosave) {
//                    saveSession();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

        boolean load = false;
        Launchpad lp = null;
   
        HashMap lpnames= new HashMap();
        String launchpadDeviceName = null;
        int num = 0;
            for (Info info : MidiSystem.getMidiDeviceInfo()) {
                if (info.getName().contains("Launchpad") &! lpnames.containsKey(info.getName())) {
                    launchpadDeviceName = info.getName();
                    lpnames.put(launchpadDeviceName, 1);
                    System.out.println("Main: Found Launchpad :) -> " + launchpadDeviceName);

                    try {
                        lp = new Launchpad(launchpadDeviceName);
                        lp.setPadnum(num++);
                        padcount = num;
                        padlist.add(lp);
                        // wait until the main thread is ready..
                        while (lp.getSession() == null) {
                            Thread.sleep(1000);
                        }

                        lp.window.setVisible(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                        //nothing
                    }
                
                }
            }
            if (launchpadDeviceName == null) {
                System.out.println("No Launchpad :( ");
                System.exit(1);
            }        
        
      //      System.exit(1);

       // wait here 
            
            groovywindow = new GroovyWindow();
            groovywindow.setLaunchpad(lp);
            groovywindow.setVisible(true);
            System.out.println("groovywindow: " +groovywindow);

            try {
            
            String script = lp.getSession().groovyScript;
            lp.window.println("LOADINIG:" + Launchpad.class.getResource("/" + script).getPath());
            String[] roots = new String[]{Launchpad.class.getResource("/" + script).getPath()};
            GroovyScriptEngine groovyengine = new GroovyScriptEngine(roots);
            Binding binding = new Binding();
            binding.setVariable("launchpad", (org.neuroninterworks.midi.seq64.Launchpad) lp);
            lp.shell = new GroovyShell(binding);
            long lastModified = 0;
            // check each second if our groovy script changed and execute if 
            while (true) {
                try {
                    if (lastModified != groovyengine.getResourceConnection(script).getLastModified()) {
                        lastModified = groovyengine.getResourceConnection(script).getLastModified();
                        groovyengine.run(script, binding);
                        lp.window.println("GROOVY RELOAD!!!");
//s                        window.println(binding.getVariable("output"));
                    }
                } catch (Exception scriptexception) {
                    scriptexception.printStackTrace();
                    groovyengine = new GroovyScriptEngine(roots);

                    //nothing
                }
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //nothing
        }
    }

    public void eval(String groovy){
           shell.evaluate(groovy);
    }
    
    
    public void loadSession(String filename) {
        XStream xstream = new XStream();
        try {
            window.println("LOADINIG!!! -> " + filename);
            FileInputStream fis = new FileInputStream(session.storedir + filename);
            xstream.fromXML(fis, session);

            if (session.groovyCode != null) { groovywindow.jEditorPane.setText(session.groovyCode); }

            for (Map.Entry<String, ClipInfo> infoSet : session.clipInfos.entrySet()) {

                window.println(infoSet.getValue().outDevice);
                Clip clip = new Clip();
                clip.clipinfo = infoSet.getValue();
                cliphash.put(clip.clipinfo.xpos + "_" + clip.clipinfo.ypos, clip);

                clip.notehash = clip.clipinfo.events;
                clip.init();
                Thread.currentThread().sleep(100);

                for (Map.Entry<String, MidiEvent> event : clip.clipinfo.events.entrySet()) {
                    clip.track.add(event.getValue());
                }

                currentView.getPadKeyGrid()[getPos(clip.clipinfo.xpos, clip.clipinfo.ypos)].ref = clip;
                currentView.redraw();
            }
            this.mainSequencer.setTempoInBPM(session.bpm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File[] getFileList() {
        File dir = new File(session.storedir);

        // This filter only returns directories
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        return dir.listFiles(fileFilter);
    }

    public void exitProgram() {
        saveSession();
        print("BYE ");
        this.clearAll();
        for (MidiDevice device : outDevs.values()) {
            if (device.isOpen()) {
                device.close();
            }
        }
        if (inputLaunchpadDevice.isOpen()) {
            inputLaunchpadDevice.close();
        }
        if (outputLaunchpadDevice.isOpen()) {
            outputLaunchpadDevice.close();
        }
        if (inputClockDevice.isOpen()) {
            try {
                inputClockDevice.getTransmitter().close();
                inputClockDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        System.exit(0);

    }

    public void saveSession() {
        synchronized (cliphash) {
        // claim gui content that must persist
        session.groovyCode = groovywindow.jEditorPane.getText();

        // Stream to write file
        XStream xstream = new XStream();
        xstream.omitField(this.getClass(), "this");
        window.println("saveSession to dir: " + session.storedir);
        try {
            FileOutputStream fout;
            String xml = xstream.toXML(session);
            // Open an output stream
            Calendar cal = Calendar.getInstance();
            java.text.DateFormat df;
            df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM);
//            Launchpad.class.getResource(session.storedir + "/" + df.format(cal.getTime()).replace(" ", "_").replace(":", "-") + ".xml").getFile();
//            fout = new FileOutputStream(Launchpad.class.getResource(session.storedir + "/" + df.format(cal.getTime()).replace(" ", "_").replace(":", "-") + ".xml").getFile());
            fout = new FileOutputStream(session.storedir + df.format(cal.getTime()).replace(" ", "_").replace(":", "-") + ".xml");
            // Print a line of text, aehm complicated type of..
            new PrintStream(fout).println(xml);
            fout.close();
            fout = new FileOutputStream(session.storedir + "last.xml");
            // Print a line of text, aehm complicated type of..
            new PrintStream(fout).println(xml);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
            //nothing
        }
        }
    }

    public void loadFont() {
        try {
            font = ImageIO.read(Launchpad.class.getResource(session.font));

            int h = font.getHeight();
            int w = font.getWidth();
            /*
             for (int y = 0; y
             < h; y++) {
             for (int x = 0; x
             < w; x++) {
             if (font.getRGB(x, y) < -1) {
             //                        System.out.print("X");
             } else {
             //                        System.out.print(" ");
             }
             }
             //                window.println("");
             }
             * 
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(String text) {

        try {
            for (char c : text.toCharArray()) {

                // window.println((int) c[i]);
                while (draw.booleanValue() == true) {
                    Thread.currentThread().sleep(200);
                }
                synchronized (draw) {
                    printChar(c);
//                    window.println(c[i]);
                    draw = new Boolean(true);
                } // Thread.sleep(250);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearGrid() {
        for (int y = 0; y
                < 8; y++) {
            for (int x = 0; x
                    < 8; x++) {
                setXY(x, y, 0, 0);
            }
            //window.println("");
        }
    }

    public void clearRight() {
        for (int y = 0; y
                < 8; y++) {
            currentView.getPadKeyRight()[y].setRed(0);
            currentView.getPadKeyRight()[y].setGreen(0);
        }
    }

    public void clearAll() {
        clearGrid();
        clearTop();
        clearRight();
    }

    public void clearTop() {
        for (int y = 0; y
                < 8; y++) {
            currentView.getPadKeyTop()[y].setRed(0);
            currentView.getPadKeyTop()[y].setGreen(0);
        }
    }

    public void printChar(char c) {
        if ((int) c == 32) {
            clearGrid();
            /*            window.println("        ");
             window.println("        ");
             window.println("        ");
             window.println("        ");
             window.println("        ");
             window.println("        ");
             window.println("        ");
             window.println("        ");
             */ return;
        }
        int offset = (c - 33) * 8;
        for (int y = 0; y
                < 8; y++) {
            for (int x = offset; x
                    < offset + 8; x++) {
                if (font.getRGB(x, y) < -1) {
                    setXY(x - offset, y, 2, 2);
                    //  System.out.print("X");
                } else {
                    setXY(x - offset, y, 0, 0);
                    //   System.out.print(" ");
                }
            }
            //window.println("");
        }
    }

    public void meta(MetaMessage event) {
        //       window.println("MAIN: META EVENT: " + loop + " " + event.getType() + ", " + event.getStatus());
        window.println("Meta WTF ?????????:");
    }

    public void init() {
        try {

            if (launchpadDeviceName == null) {
                for (Info info : MidiSystem.getMidiDeviceInfo()) {
                    window.println("MIDI: " + info.getName());
                    if (info.getName().contains("Launchpad")) {
                        launchpadDeviceName = info.getName();
                        window.println("Found Launchpad :) -> " + launchpadDeviceName);
                    }
                }
            }
            if (launchpadDeviceName == null) {
                window.println("No Launchpad :( ");
                System.exit(1);
            }

            MidiDevice.Info infoClockIn = MidiCommon.getMidiDeviceInfo(session.inputClockDeviceName, false);  // WINDOOM - MICRO MODULAR
            inputClockDevice = MidiSystem.getMidiDevice(infoClockIn);
            mainSequencer = new LaunchpadSequencer(inputClockDevice);


            MidiDevice.Info infoNoteIn = MidiCommon.getMidiDeviceInfo(session.inputNoteDeviceName, false);  // WINDOOM - MICRO MODULAR
            setInputNoteDevice(MidiSystem.getMidiDevice(infoNoteIn));

            //mainSequencer.setTempoInBPM(TEMPO_MESSAGE);
            //            mainSequencer -
            if (launchpadDeviceName != null) {
                //            MidiCommon.listDevicesAndExit(true, false);
                MidiDevice.Info infoOut = MidiCommon.getMidiDeviceInfo(launchpadDeviceName, true);
                MidiDevice.Info infoIn = MidiCommon.getMidiDeviceInfo(launchpadDeviceName, false);

                outputLaunchpadDevice = MidiSystem.getMidiDevice(infoOut);
                outputLaunchpadDevice.open();
                inputLaunchpadDevice = MidiSystem.getMidiDevice(infoIn);
                inputLaunchpadDevice.open();


                if (outputLaunchpadDevice == null) {
                    out("wasn't able to retrieve MidiDevice");
                    System.exit(1);
                }
                receiver = outputLaunchpadDevice.getReceiver();
                Transmitter t = inputLaunchpadDevice.getTransmitter();
                ((PadReceiver) padReceiver).setLaunchpad(this);
                t.setReceiver(padReceiver);
//                  t.setReceiver(new DumpReceiver(System.out));
            } else {
                /*	We retrieve a Receiver for the default
                 MidiDevice.
                 */
                receiver = MidiSystem.getReceiver();
            }
            if (receiver == null) {
                out("wasn't able to retrieve Receiver");
                System.exit(1);
            }
            if (DEBUG) {
                out("Receiver: " + receiver);
            } /*
             *	Turn the note on
             */
            if (DEBUG) {
                out("sending on message...");
            }
            if (DEBUG) {
                out("...sent");
            } /*
             *	Wait for the specified amount of time
             *	(the duration of the note).
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void destroy() {
        window.println("destroy!!!!!!!!!");
        receiver.close();
        if (outputLaunchpadDevice != null) {
            outputLaunchpadDevice.close();
        }
    }

    private void reset() {
        this.send(176, 0, 0);
        this.send(176, 0, 48); // init double buffer
    }

    private void send(int type, int a, int b) {
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(type, 0, a, b);
            if (DEBUG) {
                out("On Msg: " + msg.getStatus() + " " + msg.getData1() + " " + msg.getData2());
            }
            if (receiver != null) {
                receiver.send(msg, -1);
            }
        } catch (Exception e) {
            if (DEBUG) {
                out(e);
            }
        }
    }

    private void setXY(int x, int y, int green, int red) {
        currentView.getPadKeyGrid()[getPos(x, y)].setRed(red);
        currentView.getPadKeyGrid()[getPos(x, y)].setGreen(green);
        currentView.getPadKeyGrid()[getPos(x, y)].setFlags(12);
    }

    public int getPos(int x, int y) {
        return (y * 8) + x;
    }

    private int getKey(int row, int col) {
        return 16 * row + col;
    }

    private int[] getXYFromKey(int key) {
        int x[] = new int[2];
        x[1] = Math.abs(key / 16);
        x[0] = key - (x[1] * 16);
        return x;
    }

    private int getVelocity(int green, int red, int flags) {
        return 16 * green + red + flags;
    }

    public  void out(String strMessage) {
        window.println(strMessage);
      
    }

    private static void out(Throwable t) {
        t.printStackTrace();
    }

    public void initPads(PadKey[] padKeys) {
        for (int i = 0; i < padKeys.length; i++) {
            padKeys[i] = new PadKey();
        }
    }
    final int TEMPO_MESSAGE = 0x51;

    MetaMessage tempoChangeMessage(int bpm) // bpm==beats per minute
    {
        long mpq = 60000000 / bpm; // mpq==microsec per quarter (beat)
        byte[] data = new byte[3];
        data[0] = (byte) ((mpq >> 16) & 0xFF);
        data[1] = (byte) ((mpq >> 8) & 0xFF);
        data[2] = (byte) (mpq & 0xFF);
        MetaMessage m = new MetaMessage();
        try {
            m.setMessage(TEMPO_MESSAGE, data, data.length);
        } catch (InvalidMidiDataException e) {
        }
        return m;
    }

    boolean isTempoChangeMessage(MidiMessage m) {
        if (!(m instanceof MetaMessage)) {
            return false;
        }
        return ((MetaMessage) m).getType() == TEMPO_MESSAGE;
    }

    // returns beats (quarter notes) per minute
    int getTempoFromMessage(MetaMessage m) {
        byte[] byteData = m.getData();
        int[] intData = new int[3];
        // fix for lack of unsigned byte type
        for (int i = 0; i < 3; ++i) {
            if (byteData[i] < 0) {
                intData[i] = byteData[i] + 256;
            } else {
                intData[i] = byteData[i];
            }
        }
        long mpq = (intData[0] << 16) + (intData[1] << 8) + (intData[2]);
        return (mpq == 0 ? 1 : (int) (60000000 / mpq));
    }
    // denominator = 2 ^ denominatorExponent, eg 6, 3 gives 6/8 time
    final int TIME_SIGNATURE_MESSAGE = 0x58;

    MetaMessage timeSignatureChangeMessage(int numerator, int denominatorExponent) {
        byte[] data = new byte[4];
        data[0] = (new Integer(numerator)).byteValue();
        data[1] = (new Integer(denominatorExponent)).byteValue();
        data[2] = (new Integer(24)).byteValue(); // ignoring this
        data[3] = (new Integer(8)).byteValue(); // ignoring this
        MetaMessage m = new MetaMessage();
        try {
            m.setMessage(TIME_SIGNATURE_MESSAGE, data, data.length);
        } catch (InvalidMidiDataException e) {
            // oh well...
        }
        return m;
    }

    boolean isTimeSignatureChangeMessage(MidiMessage m) {
        if (!(m instanceof MetaMessage)) {
            return false;
        }
        return ((MetaMessage) m).getType() == TIME_SIGNATURE_MESSAGE;
    }

    // returns numerator, assume 1/4 notes so ignore denominator exponent etc
    int getTimeSignatureFromMessage(MetaMessage m) {
        byte[] data = m.getData();
        return data[0];
    }

    public void start() {
        window.println("Start global ");
    }

    public void stop() {
        window.println("Stop global ");
        mainSequencer.stop();

        for (String hashkey : session.clipInfos.keySet()) {
            ClipInfo inf = (ClipInfo) session.clipInfos.get(hashkey);
            Clip c = cliphash.get(hashkey);
            if (inf.play == true) {
                if (c.isRunning()) {
                    window.println("MAIN: starting:" + ((String) hashkey));
                    c.stop();
                }
            }
        }
    }

    public static String toString(Object o) {
        ArrayList<String> list = new ArrayList<String>();

        toString(o, o.getClass(), list);

        return o.getClass().getName().concat(list.toString());
    }

    private static void toString(Object o, Class<?> clazz, ArrayList<String> list) {
        Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);

        for (Field f : fields) {
            try {
                list.add(f.getName() + "=" + f.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (clazz.getSuperclass().getSuperclass() != null) {
            toString(o, clazz.getSuperclass(), list);
        }
    }
}
