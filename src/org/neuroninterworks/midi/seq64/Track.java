/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neuroninterworks.midi.seq64;

import javax.sound.midi.MidiEvent;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author boris
 */
public class Track {

    static public final int MAX_BEATS = 17;
    static public final int MAX_TICKS = (24 * 4) * MAX_BEATS;
        
    MidiEvent eventsbuff[][] = new MidiEvent[MAX_TICKS][64];
    HashMap<Integer,Boolean> onbuffer = new HashMap();

    Boolean onmarker = true;
    
    public Track() {
    }

    public MidiEvent[][] getBuff() {
        return eventsbuff;
    }

    public MidiEvent[] get(int tick) {
        return eventsbuff[tick];
    }

    public void add(MidiEvent event) {
        if (event == null) {
            return;
        }
        int tick = (int) event.getTick();
        for (int i = 0; i < 64; i++) {
            if (eventsbuff[tick][i] == null) {
                eventsbuff[tick][i] = event;
                break;
            }
        }
    }

    // avoid stuck notes
    public void remember(javax.sound.midi.ShortMessage msg) {
        onbuffer.put(msg.getData1(), onmarker);
    }

    public void forget(javax.sound.midi.ShortMessage msg) {
        onbuffer.remove(msg.getData1());
    }

    public void forget(Integer note) {
        onbuffer.remove(note);
    }

    public void forgetAll() {
        onbuffer.clear();
    }

    public Set<Integer> getForgotten(){
        return onbuffer.keySet();
    }

    public boolean hasForgottenNote(Integer note){
        if (onbuffer.containsKey(note)) {
                return true; 
        }
        return false;
    }
    
    // 
    
    public void remove(MidiEvent event) {
        if (event == null) {
            return;
        }
        int tick = (int) event.getTick();
        System.out.println("search: " + event);

        for (int i = 0; i < 64; i++) {
            if (eventsbuff[tick][i] == event) {
                System.out.println("found: " + eventsbuff[tick][i]);
                eventsbuff[tick][i] = null;
                break;
            }
        }

    }

    public void flip() {
    }
}
