/*
 * Copyright (C) 2013 Thomas Le
 * 
 * This file is part of RCVClient.
 *
 * RCVClient is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RCVClient is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with RCVClient. If not, see <http://www.gnu.org/licenses/>.
 */
package no.haitech.rcvclient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 * This class extracts the MJPEG to individual JPEGs.
 * A JPEG image consists of a sequence of segments, each beginning with a 
 * marker, each of which begins with a 0xFF byte followed by a byte indicating
 * what kind of marker it is.
 * 
 * @author Thomas Le, rewritten and stripped down from original to work with RCV
 * @author Jason Thrasher, original - {@link http://jipcam.svn.sourceforge.net}
 * @see CameraView
 * @see DataInputStream
 * @see {@link http://en.wikipedia.org/wiki/JPEG#Syntax_and_structure}
 * @see {@link http://www.w3.org/Graphics/JPEG/itu-t81.pdf}
 * @see {@link http://jipcam.svn.sourceforge.net}
 */
public class MjpegInputStream extends DataInputStream {
    /**
     * The start byte (0xFF, 0xD8) of image marker.
     */
    private final byte[] SOI_MARKER = { (byte) 0xFF, (byte) 0xD8 };

    /**
     * The end byte (0xFF, 0xD9) of image marker.
     */
    private final byte[] EOI_MARKER = { (byte) 0xFF, (byte) 0xD9 };

    /*
     * A typical header max length. 
     */
    private final static int HEADER_MAX_LENGTH = 100;

    /*
     * A typical JPEG length + the header length = frame length in bytes.
     * 3 bytes per pixel (24bit RGB), multiply by width and height.
     */
    private final static int FRAME_MAX_LENGTH = (3 * 800 * 600) 
            + HEADER_MAX_LENGTH;

    /*
     * Content length in MJPEG frame header. length in bytes.
     */
    private final String CONTENT_LENGTH = "Content-Length";



    /**
     * Constructor
     * @param in 
     *        a valid InputStream.
     */
    public MjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }


    
    /**
     * Method to get the index of the beginning of the byte sequence.
     * Uses getEndOfSequence() method to check if there exist a sequence.
     * 
     * @param in
     *        DataInputStream of the stream.
     * @param sequence
     *        byte sequence / byte marker.
     * @return index of the sequence, -1 if not found.
     * @throws IOException Something went wrong with InputStream.
     */
    private int getStartOfSequence(DataInputStream in, byte[] sequence)
            throws IOException {
        int end = getEndOfSequence(in, sequence);
        
        if(end < 0) return (-1);
        else return (end - sequence.length);
    }


    
    /**
     * Method to get the index of the end of the byte sequence.
     * 
     * @param in
     *        DataInputStream of the stream.
     * @param sequence
     *        byte sequence / byte marker.
     * @return the index of the first byte after the given sequence, or -1 if 
     *         not found.
     * @throws IOException
     */
    private int getEndOfSequence(DataInputStream in, byte[] sequence)
            throws IOException {
        int seqIndex = 0; // tracks number of sequence chars found.
        byte c;

        for (int i = 0; i < (FRAME_MAX_LENGTH); i++) {
            c = (byte) in.readUnsignedByte(); // read next byte.

            if (c == sequence[seqIndex]) {
                seqIndex++; // increment sequence char found index.

                // check if we have the whole sequence.  
                if (seqIndex == sequence.length) {
                    return i + 1;
                }
            } else {
                // reset index if we don't find all sequence characters before 
                // breaking.
                seqIndex = 0;
            }
        }

        return -1;
    }



    /**
     * Method to parse the content length string for a MJPEG frame from the
     * given bytes.
     * The string is parsed into an INT and returned.
     * 
     * @param headerBytes
     *        byte of header to parse.
     * @return the Content-Length, -1 if not found.
     * @throws IOException invalid InputStream.
     */
    private int parseContentLength(byte[] headerBytes) 
            throws IOException {
        ByteArrayInputStream headerIn = new ByteArrayInputStream(headerBytes);
        Properties props = new Properties();
        props.load(headerIn);

        return Integer.parseInt(props.getProperty(CONTENT_LENGTH));
    }
    
    
    
    /**
     * Read the next MjpegFrame from the stream.
     *
     * @return the next MJPEG frame.
     * @throws IOException if there is an error while reading data
     */
    public Bitmap readMjpegFrame() throws IOException {
        //mark the start of the frame
        mark(FRAME_MAX_LENGTH);

        //get length of header
        int headerLen = getStartOfSequence(this, SOI_MARKER);
        reset();

        byte[] header = new byte[headerLen];
        readFully(header);

        // Searching for "Content-Length"
        int mContentLength = -1; // Sets default value to -1 (not found);
        try {
            mContentLength = parseContentLength(header);
        } catch (NumberFormatException nfe) {
            // Runs the slow way if "Content-Length" is not found.
            mContentLength = getEndOfSequence(this, EOI_MARKER);
        }
        reset();

        //create frame array
        byte[] frameData = new byte[mContentLength];
        skipBytes(headerLen);
        readFully(frameData);

        return BitmapFactory.decodeStream(new ByteArrayInputStream(frameData));
    }
}
