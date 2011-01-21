/*
 *  (c) Copyright (c) 2010 Mridang Agarwalla
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package clibooster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.*;
import java.io.FileReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * The class that provides the CLI application
 */
public class CliBoosterCommand {

   /** The messages instance that will be used to get messages
    */
    private ResourceBundle rB;
   /** The locale that will be used when fetching the messages
    */
    private Locale sL = new Locale("en", "us");

   /**
    * Constructor.
    *
    * @return
    */
    public CliBoosterCommand() {
        rB = ResourceBundle.getBundle("MessagesBundle", sL);
    }

   /**
    * This reads the torrent file from the specified location and returns the
    * BDecoded value
    *
    * @returns
    */
    private byte[] readTorrentFile(File torrentFile) throws Exception {
        try {
            System.out.println(rB.getString("readingTorrentFile"));

            InputStream torrentFileReader = new FileInputStream(torrentFile);
            Integer offset = 0;
            Integer numRead = 0;
            long length = torrentFile.length();
            byte[] bytes = new byte[(int) length];

            while (offset < bytes.length
                   && (numRead=torrentFileReader.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }

            torrentFileReader.close();

            return bytes;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(rB.getString("readingTorrentFileErr"));
            throw e;
        }
    }

   /**
    * This writes the torrent file to the specified location
    *
    * @params  torrentFile     The torrent file to write to
    * @params  torrentFileData The content to write into the torrent
    * @returns                 The File object of the new torrent file.
    */
    private File writeTorrentFile(File torrentFile, byte[] torrentFileData) throws Exception {
        try {
            System.out.println(rB.getString("writingTorrentFile"));

            OutputStream torrentFileWriter;

            torrentFileWriter = new FileOutputStream(torrentFile);
            torrentFileWriter.write(torrentFileData);
            torrentFileWriter.close();

            return torrentFile;
        }
        catch (Exception e) {
            System.out.println(rB.getString("writingTorrentFileErr"));
            throw e;
        }
    }

   /**
    * This adds the trackers to the torrent. It doesn't do any checking if the
    * tracker already exists in the torrent. It simply adds them.
    *
    * @params  torrentData     The torrent file to add trackers to
    * @params  torrentTrackers The set of trackers to be added
    * @returns                 The torrent with the trackers added
    */
    private Map addTrackers(Map torrentData, List<URL> torrentTrackers) throws Exception {
        try {
            System.out.println(rB.getString("existingTrackers"));

            List trackers = (List) torrentData.get("announce-list");
            for (Integer i = 0; i < trackers.size(); i++) {
                List set = (List) trackers.get(i);
                for (Integer j = 0; j < set.size(); j++) {
                    System.out.println("  " + new String((byte[]) set.get(j)));
                }
            }

            System.out.println(rB.getString("newTrackers"));
            for (Integer k = 0; k < torrentTrackers.size(); k++) {
                System.out.println("  " + torrentTrackers.get(k).toString());
                List set = new ArrayList<byte[]>();
                set.add(new String(torrentTrackers.get(k).toString()).getBytes());
                trackers.add(set);
            }

            return torrentData;

        }
        catch (Exception e) {
            throw e;
        }
    }

   /**
    * This fetches the URLs from the defined website and then puts them into an
    * array to be added to the torrent
    *
    * @returns an array of URLs fetched from the site to be added to the torrent
    */
    private List<URL> getTrackers() throws Exception {
        try {
            System.out.println(rB.getString("gettingTrackers"));

            URL httpURL;
            URLConnection httpFetcher;
            BufferedReader httpBuffer;
            InputStreamReader httpStream;
            String httpResponseLine;
            List<URL> trackerURLs = new ArrayList<URL>();

            httpURL = new URL("http://www.trackon.org/api/live");
            httpFetcher = httpURL.openConnection();
            httpStream = new InputStreamReader(httpFetcher.getInputStream());
            httpBuffer = new BufferedReader(httpStream);
            while ((httpResponseLine = httpBuffer.readLine()) != null) {
                try {
                    trackerURLs.add(new URL(httpResponseLine));
                }
                catch (MalformedURLException e) {
                    continue;
                }
            }

            return trackerURLs;
        }
        catch (IOException e) {
            System.out.println(rB.getString("gettingTrackersErr"));
            throw e;
        }
    }

   /**
    * This paeses the command line arguments and gets the torrent file from the
    * path specified.
    *
    * @params  args The command-line arguments passed to the application
    * @returns
    */
    public File getFilenameFromArguments(String[] args) throws Exception {
        try {
            File torrentFile;

            if (args.length == 1) {
                torrentFile = new File(args[0]);
            }
            else {
                throw new Exception();
            }
            return torrentFile;
        }
        catch (Exception e) {
            System.out.println(rB.getString("gettingArgumentsErr"));
            throw e;
        }
    }

   /**
    * This is the main procedure which acts as the entry point for the
    * application
    *
    * @params  args The command-line arguments passed to the application
    * @returns
    */
    public static void main(String[] args) {
        CliBoosterCommand cli = new CliBoosterCommand();
        File torrentFile;
        Map torrentData;

        try {
            torrentFile = cli.getFilenameFromArguments(args);
            torrentData = BDecoder.decode(cli.readTorrentFile(torrentFile));
            torrentData = cli.addTrackers(torrentData, cli.getTrackers());
            torrentFile = cli.writeTorrentFile(torrentFile, BEncoder.encode(torrentData));
        }
        catch (Exception e) {
            return;
        }
    }

}
