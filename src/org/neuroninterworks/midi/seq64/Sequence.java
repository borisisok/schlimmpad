/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neuroninterworks.midi.seq64;

import java.util.ArrayList;

/**
 *
 * @author boris
 */
public class Sequence {

    public static float PPQ = 1f;
    ArrayList<Track> tracks = new ArrayList();
    Track trackarray[] = new Track[0];
    long length = 0l;
    float divisionType;
    int resolution;

    public Sequence(float _divisionType, int _resolution) {
        this.divisionType = _divisionType;
        this.resolution = _resolution;
    }

    public Track createTrack() {
        Track track = new Track();
        tracks.add(track);
        return track;
    }

    public Track[] getTracks() {
        return tracks.toArray(trackarray);
    }

    public long getTickLength() {
        return length;
    }

    public void flip() {
        for (Track track : tracks) {
            track.flip();
        }
    }
}
