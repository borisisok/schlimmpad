package org.neuroninterworks.midi.seq64;

import org.neuroninterworks.midi.seq64.Launchpad.Clip;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;

import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

//import javax.sound.midi.Track;
//import javax.sound.midi.Sequence;
/**
 *
 * @author boris
 */
public class LaunchpadSequencer implements Runnable {

    private int currSeq = 0;
    private ArrayList<Sequence> seqList = new ArrayList();
    private ArrayList<Sequence> activeSeqList = new ArrayList();
    private ArrayList<Sequence> startSeqList = new ArrayList();
    private HashMap<Sequence, MidiDevice> devList = new HashMap();
    private HashMap<Sequence, Long> tickList = new HashMap();
    private HashMap<Sequence, Integer> offsetList = new HashMap();
    private HashMap<Sequence, Clip> clipList = new HashMap();
    private boolean running = false;
    private boolean internal = true;
    private float tempo = 128.00f;
    private long tick = 0L;
    private long end = 0L;
    private ShortMessage shortmsg;
    private MidiDevice clockdevice;
    private ClockReceiver crec;
    private MidiClock launchclock;

    public LaunchpadSequencer() {
        try {
//           MidiDevice.Info infoClockIn = MidiCommon.getMidiDeviceInfo("VirMIDI [hw:1,3]", false); // LINUX
//            MidiDevice.Info infoClockIn = MidiCommon.getMidiDeviceInfo("In From MIDI Yoke:  8", false);  // WINDOOM - GENERIC
//            MidiDevice.Info infoClockIn = MidiCommon.getMidiDeviceInfo("In-B USB MidiSport 4x4", false);  // WINDOOM - MICRO MODULAR
            MidiDevice.Info infoClockIn = MidiCommon.getMidiDeviceInfo("MIDISPORT 4x4 In B", false);  // WINDOOM - MICRO MODULAR

            launchclock = new MidiClock(this);

            System.err.println("infoClockIn: " + infoClockIn);
            MidiDevice inputClockDevice = MidiSystem.getMidiDevice(infoClockIn);
            System.out.println("inputClockDevice: " + inputClockDevice);
            inputClockDevice.open();
            crec = new ClockReceiver(System.out);
            crec.setSequencer(this);
            inputClockDevice.getTransmitter().setReceiver(crec); //

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    public LaunchpadSequencer(MidiDevice mididevice) {
        setClockDevice(mididevice);
    }

    public MidiDevice getClockDevice() {
        return clockdevice;
    }

    public void setClockDevice(MidiDevice mididevice) {
        try {
            System.out.println(mididevice);
            if (clockdevice != null && clockdevice.isOpen()) {
                clockdevice.close();
            }
            clockdevice = mididevice;
            clockdevice.open();
            if (crec == null) {
                crec = new ClockReceiver(System.out);
                crec.setSequencer(this);
            }
            clockdevice.getTransmitter().setReceiver(crec); //
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tick() {
        if (!running) {
            return;
        }
        try {
//                System.out.println("tick!");
            synchronized (activeSeqList) {
                boolean startseq = false;
                int seqid = 0;
                for (Sequence seq : this.activeSeqList) {
                    tick = tickList.get(seq);
                    end = clipList.get(seq).clipinfo.loopEnd;
                    //System.out.println("Seq: " + seq.getTickLength() + " tick: " + tick);
                    for (Track track : seq.getTracks()) {
                        MidiEvent events[] = track.get((int) tick);
                        MidiEvent ev = null;
                        for (int i = 0; i < 64; i++) {
                            ev = events[i];
                            if (ev == null) {
                                continue;
                            }
                            MidiMessage msg = ev.getMessage();
                            clipList.get(seq).out.getReceiver().send(msg, -1);
                            if (msg instanceof ShortMessage) {
                                shortmsg = (ShortMessage) msg;
                                if (shortmsg.getCommand() == ShortMessage.NOTE_ON) {
                                    track.remember(shortmsg);
                                } else if (shortmsg.getCommand() == ShortMessage.NOTE_OFF) {
                                    track.forget(shortmsg);
                                }
                            }

                        }
                    }

                    if (tick < end - 1) {
                        tickList.put(seq, tick + 1);
                    } else {
                        //                      System.out.println("Loop!: " + tick);
                        clipList.get(seq).turnOffNotes();  // avoid stuck notes on shortend loops !
                        tickList.put(seq, (long) clipList.get(seq).clipinfo.loopStart);

                        if (seqid == 0) {
                            startseq = true;
                        }
                    }
                    seqid++;
                }

                synchronized (startSeqList) {
                    if (startseq && !startSeqList.isEmpty()) {
                        activeSeqList.addAll(startSeqList);
                        startSeqList.clear();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("Tick!");
    }

    public void setSequence(Sequence sequence, Clip clip) {
        clipList.put(sequence, clip);
        tickList.put(sequence, (long) clip.clipinfo.loopStart);
        offsetList.put(sequence, clip.clipinfo.loopStart);
        seqList.add(sequence);
    }

    public void remove(Sequence sequence) {
        seqList.remove(sequence);
        tickList.remove(sequence);
        offsetList.remove(sequence);
        clipList.remove(sequence);
    }

    public void setSequence(Sequence sequence) throws InvalidMidiDataException {
        seqList.add(sequence);
        tickList.put(sequence, 0L);
        offsetList.put(sequence, 0);
    }

    public void setSequenceDevice(Sequence sequence, MidiDevice device) {
        devList.put(sequence, device);
    }

    public Sequence getSequence() {
        return seqList.get(currSeq);
    }

    public MidiDevice getSequenceDevice() {
        return devList.get(currSeq);
    }

    public void start() {
        System.out.println("Start!");
        running = true;
        if (internal) {
            launchclock.start();
        }
    }

    public void start(Sequence sequence) {

        running = true;
        tickList.put(sequence, (long) clipList.get(sequence).clipinfo.loopStart);
        offsetList.put(sequence, clipList.get(sequence).clipinfo.loopStart);
        System.out.println("Start!");
        synchronized (activeSeqList) {
            this.activeSeqList.add(sequence);
            if (internal) {
                launchclock.start();
            }

        }
    }

    public void startInSync(Sequence sequence) {
        tickList.put(sequence, (long) clipList.get(sequence).clipinfo.loopStart);
        offsetList.put(sequence, clipList.get(sequence).clipinfo.loopStart);
        System.out.println("Start!");
        if (activeSeqList.isEmpty()) {
            start(sequence);
        } else {
            synchronized (startSeqList) {
                this.startSeqList.add(sequence);
            }
        }
    }

    public void stop() {
        System.out.println("Stop!");
        running = false;
        for (Sequence seq : this.activeSeqList) {
            tickList.put(seq, (long) clipList.get(seq).clipinfo.loopStart);
            clipList.get(seq).turnOffNotes();
        }
        synchronized (startSeqList) {
            if (internal) {
                launchclock.stop();
            }
        }

    }

    public void stop(Sequence seq) {
        System.out.println("Stop! ");
        synchronized (activeSeqList) {
            this.activeSeqList.remove(seq);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void startRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stopRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recordEnable(Track track, int channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recordDisable(Track track) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getTempoInBPM() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTempoInBPM(float bpm) {
            launchclock.setBpm( (int) currSeq );
    }

    public float getTempoInMPQ() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTempoInMPQ(float mpq) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTempoFactor(float factor) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getTempoFactor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getTickLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getTickPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getTickPosition(Sequence seq) {
        return tickList.get(seq);
    }

    public void setTickPosition(long tick) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getMicrosecondLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getMicrosecondPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMicrosecondPosition(long microseconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTrackMute(int track, boolean mute) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getTrackMute(int track) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTrackSolo(int track, boolean solo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean getTrackSolo(int track) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addMetaEventListener(MetaEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeMetaEventListener(MetaEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoopStartPoint(long tick) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoopStartPoint(Sequence seq, long tick) {
    }

    public long getLoopStartPoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoopEndPoint(long tick) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLoopEndPoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLoopCount(int count) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getLoopCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void open() throws MidiUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOpen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxReceivers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxTransmitters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Receiver getReceiver() throws MidiUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Receiver> getReceivers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transmitter getTransmitter() throws MidiUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Transmitter> getTransmitters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     public static void main(String[] args) {
    
     try {
     LaunchpadSequencer bs = new LaunchpadSequencer();
     //bs.start();
    
    
     //Partitur {{Tonhoehe, DauerInViertelNoten, AnzahlWdh},...}
     int DATA[][] = {
     {60, 1, 1}, //C
     {62, 1, 1}, //D
     {64, 1, 1}, //E
     {65, 1, 1}, //F
     {67, 2, 2}, //G,G
     {69, 1, 4}, //A,A,A,A
     {67, 4, 1}, //G
     {69, 1, 4}, //A,A,A,A
     {67, 4, 1}, //G
     {65, 1, 4}, //F,F,F,F
     {64, 2, 2}, //E,E
     {62, 1, 4}, //D,D,D,D
     {60, 4, 1} //C
     };
     int BDATA[][] = {
     {60, 1, 1}, //C
     {62, 1, 1}, //D
     {64, 1, 1}, //E
     {65, 1, 1}, //F
     {67, 3, 3}
     };
     //Sequence bauen
     final int PPQS = 24;
     final int STAKKATO = 4;
     Sequence seq = new Sequence(Sequence.PPQ, PPQS);
     Track track = seq.createTrack();
     long currentTick = 0;
     ShortMessage msg;
     //Kanal 0 auf "EnsembleStrings" umschalten
     msg = new ShortMessage();
     msg.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 48, 0);
     track.add(new MidiEvent(msg, currentTick));
     //Partiturdaten hinzufügen
     for (int i = 0; i < DATA.length; ++i) {
     for (int j = 0; j < DATA[i][2]; ++j) { //Anzahl Wdh. je Note
     msg = new ShortMessage();
     msg.setMessage(ShortMessage.NOTE_ON, 0, DATA[i][0], 64);
     track.add(new MidiEvent(msg, currentTick));
     currentTick += PPQS * DATA[i][1] - STAKKATO;
     msg = new ShortMessage();
     msg.setMessage(ShortMessage.NOTE_OFF, 0, DATA[i][0], 0);
     track.add(new MidiEvent(msg, currentTick));
     currentTick += STAKKATO;
     }
     }
     bs.setSequence(seq);
    
     seq = new Sequence(Sequence.PPQ, PPQS);
     track = seq.createTrack();
     currentTick = 0;
     //Kanal 0 auf "EnsembleStrings" umschalten
     msg = new ShortMessage();
     msg.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 48, 0);
     track.add(new MidiEvent(msg, currentTick));
     //Partiturdaten hinzufügen
     for (int i = 0; i < BDATA.length; ++i) {
     for (int j = 0; j < BDATA[i][2]; ++j) { //Anzahl Wdh. je Note
     msg = new ShortMessage();
     msg.setMessage(ShortMessage.NOTE_ON, 0, BDATA[i][0], 64);
     track.add(new MidiEvent(msg, currentTick));
     currentTick += PPQS * BDATA[i][1] - STAKKATO;
     msg = new ShortMessage();
     msg.setMessage(ShortMessage.NOTE_OFF, 0, BDATA[i][0], 0);
     track.add(new MidiEvent(msg, currentTick));
     currentTick += STAKKATO;
     }
     }
     bs.setSequence(seq);
    
     } catch (Exception e) {
     e.printStackTrace();
     }
     while (true) {
     try {
    
     Thread.sleep(10000);
     } catch (Exception e) {
     //nothing
     }
     }
     }
     */
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
