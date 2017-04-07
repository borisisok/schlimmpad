package org.neuroninterworks.midi.seq64;

import java.util.*;
import javax.sound.midi.MidiEvent;

/**
 *
 * @author boris
 */
public class ClipInfo {

    String name = "";
    String outDevice = "";
    String inDevice = "";
    int outChannel = 0;
    int inChannel = 0;
    int beats = 4;
//    static int PPQS = 16;
    public static int PPQS = 24;
//    static int PPQS = 96;
    int xpos = 0;
    int ypos = 0;
    int xoff = 0;
    List<Integer> v_xoff = new ArrayList<Integer>(xoff);
    int yoff = 84; // make C-1 sit on the lower button bar
    List<Integer> v_yoff = new ArrayList<Integer>(yoff);
    int gridXSize = 6;
    List<Integer> v_gridXSize = new ArrayList<Integer>(gridXSize);
    int loopStart = 0;
    int loopEnd = beats * PPQS;
    int presetBank = -1;
    int presetProg = -1;
    boolean play = false;
    boolean record = false;
    int loopsPlayed = 0;
    int startedOnLoop = 0;
    HashMap<String, MidiEvent> events = new HashMap();
}
