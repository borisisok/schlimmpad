package org.neuroninterworks.midi.seq64;

import java.io.PrintStream;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Receiver;

public class ClockReceiver
        implements Receiver {

    public LaunchpadSequencer seq;

    public ClockReceiver(PrintStream printStream) {
        this(printStream, false);
    }

    public ClockReceiver(PrintStream printStream,
            boolean bPrintTimeStampAsTicks) {
    }

    public void close() {
    }

    public void setSequencer(LaunchpadSequencer _seq) {
        seq = _seq;
    }

    public void send(MidiMessage message, long lTimeStamp) {
        String strMessage = null;
        if (message instanceof ShortMessage) {
            decodeMessage((ShortMessage) message);
        }
    }

    public void decodeMessage(ShortMessage message) {
        switch (message.getCommand()) {
            case 0xF0:
                if (seq != null) {
                    switch (message.getChannel()) {
                        case 10:
                            seq.start();
                            break;
                        case 8:
                            seq.tick();
                            break;
                        case 12:
                            seq.stop();
                            break;
                    }
                }
                break;
        }
    }
}
