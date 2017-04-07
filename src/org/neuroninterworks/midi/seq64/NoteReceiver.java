package org.neuroninterworks.midi.seq64;

import java.io.PrintStream;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Receiver;
import static org.neuroninterworks.midi.seq64.DumpReceiver.getKeyName;

public class NoteReceiver
        implements Receiver {

    private Launchpad lp;
    private boolean rec = false;
    private boolean quantize = true;

    public NoteReceiver(PrintStream printStream) {
        this(printStream, false);
        System.out.println("new NoteReceiver");
    }

    public NoteReceiver(PrintStream printStream,
            boolean bPrintTimeStampAsTicks) {
    }

    public void close() {
    }

    public void setLaunchpad(Launchpad _lp) {
        lp = _lp;
    }

    public void toggleRecord() {
        if (rec) {
            rec = false;
        } else {
            rec = true;
        }
    }

    public void toggleQuantisation() {
        if (quantize) {
            quantize = false;
        } else {
            quantize = true;
        }
    }

    public void send(MidiMessage message, long lTimeStamp) {
        String strMessage = null;
        System.out.println("MIDI MSG IN: " + message + " "
                + message.getMessage());

        if (message instanceof ShortMessage) {
            decodeMessage((ShortMessage) message);
        }
    }

    public void decodeMessage(ShortMessage message) {
        System.out.println("MIDI MSG IN: " + message.getChannel() + " " + message.getCommand() + " " + message.getData1() + " " + message.getData2());

        if (lp.currentClip != null) {

            int eventtime;
            if (quantize) {
                eventtime = (((int) lp.currentClip.getTickPosition() / lp.currentClip.clipinfo.gridXSize) * lp.currentClip.clipinfo.gridXSize);
            } else {
                eventtime = (int) lp.currentClip.getTickPosition();
            }

            switch (message.getCommand()) {
                case 128:

                    lp.out("note Off " + getKeyName(message.getData1()) + " velocity: " + message.getData2());
                    lp.out("TickPosition: " + lp.currentClip.getTickPosition());
                    lp.out("Quantized TickPosition: " + eventtime);

                    lp.currentClip._send(message.getCommand(), message.getData1() , message.getData2());

                    if (rec && !lp.currentClip.getEvents().containsKey(eventtime + "_" + message.getData1())) {
                        lp.currentClip.setNoteOff(eventtime, message.getData1());
                    }

                    break;
                case 144:
                    lp.out("note On " + getKeyName(message.getData1()) + " velocity: " + message.getData2());
                    lp.currentClip._send(message.getCommand(), message.getData1() , message.getData2());
                    if (rec && !lp.currentClip.getEvents().containsKey(eventtime + "_" + message.getData1())) {
                        lp.currentClip.setNoteOn(eventtime, message.getData1(),message.getData2());
                    }
                    break;
            }
        }
    }
}
