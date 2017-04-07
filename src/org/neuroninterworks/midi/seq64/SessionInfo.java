/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neuroninterworks.midi.seq64;

import java.util.*;

/**
 *
 * @author boris
 */
public class SessionInfo {

    String name = "";
//  sString storedir = "/tmp/sessions/";
    final String storedir = "./";
    String font = "/font.png";
    String groovyScript = "seq64.groovy";
    int bpm = 126;
//  String inputClockDeviceName = "LoopBe Internal MIDI"; 
//  String inputClockDeviceName = "loopMIDI MiniAK IN";
//  String inputClockDeviceName = "micro lite: Port 2";
    String inputClockDeviceName = "loopMIDI Seq64ClockIn";
    String inputNoteDeviceName = "loopMIDI Seq64NoteIn";
    HashMap<String, ClipInfo> clipInfos = new HashMap();
    boolean autosave = true;
    String groovyCode = "";
}
