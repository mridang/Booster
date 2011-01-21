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

import java.io.*;
import java.nio.*;
import java.util.*;
import java.nio.charset.Charset;

/**
 * A set of utility methods to encode a Map into a bencoded array of byte.
 * integer are represented as Long, String as byte[], dictionnaries as Map, and list as List.
 */
public class BEncoder {

   /**
    * Constants for encoding purposes
    */
    public static final String DEFAULT_ENCODING = "UTF8";
    public static final String BYTE_ENCODING = "ISO-8859-1";
    public static Charset BYTE_CHARSET;
    public static Charset DEFAULT_CHARSET;

   /**
    * Static initialisation of the character sets
    */
    static {
        try {

            BYTE_CHARSET = Charset.forName(BEncoder.BYTE_ENCODING);
            DEFAULT_CHARSET = Charset.forName(BEncoder.DEFAULT_ENCODING);

        } catch (Throwable e) {

            e.printStackTrace();
        }
    }

    public static byte[] encode(Map object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new BEncoder().encode(baos, object);
        return baos.toByteArray();
    }

    private void
            encode(
                    ByteArrayOutputStream baos,
                    Object object)

            throws IOException {

        if (object instanceof String || object instanceof Float) {

            String tempString = (object instanceof String) ? (String) object :
                                String.valueOf((Float) object);

            ByteBuffer bb = DEFAULT_CHARSET.encode(tempString);

            write(baos,
                  DEFAULT_CHARSET.encode(String.valueOf(bb.limit())));

            baos.write(':');

            write(baos, bb);

        } else if (object instanceof Map) {

            Map tempMap = (Map) object;

            SortedMap tempTree = null;

            // unfortunately there are some occasions where we want to ensure that
            // the 'key' of the map is not mangled by assuming its UTF-8 encodable.
            // In particular the response from a tracker scrape request uses the
            // torrent hash as the KEY. Hence the introduction of the type below
            // to allow the constructor of the Map to indicate that the keys should
            // be extracted using a BYTE_ENCODING

            boolean byte_keys = false; //object instanceof ByteEncodedKeyHashMap;

            //write the d
            baos.write('d');

            //are we sorted?
            if (tempMap instanceof TreeMap) {

                tempTree = (TreeMap) tempMap;

            } else {

                //do map sorting here

                tempTree = new TreeMap(tempMap);
            }

            Iterator it = tempTree.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry entry = (Map.Entry) it.next();

                Object o_key = entry.getKey();

                Object value = entry.getValue();

                if (value != null) {

                    if (o_key instanceof byte[]) {

                        encode(baos, (byte[]) o_key);

                        encode(baos, value);

                    } else {

                        String key = (String) o_key;

                        if (byte_keys) {

                            try {

                                encode(baos, BYTE_CHARSET.encode(key));

                                encode(baos, tempMap.get(key));

                            } catch (UnsupportedEncodingException e) {

                                throw (new IOException(
                                        "BEncoder: unsupport encoding: " +
                                        e.getMessage()));
                            }

                        } else {

                            encode(baos, key); // Key goes in as UTF-8

                            encode(baos, value);
                        }
                    }
                }
            }

            baos.write('e');

        } else if (object instanceof List) {

            List tempList = (List) object;

            //write out the l

            baos.write('l');

            for (int i = 0; i < tempList.size(); i++) {

                encode(baos, tempList.get(i));
            }

            baos.write('e');

        } else if (object instanceof Long) {

            Long tempLong = (Long) object;
            //write out the l
            baos.write('i');
            write(baos, DEFAULT_CHARSET.encode(tempLong.toString()));
            baos.write('e');
        } else if (object instanceof Integer) {

            Integer tempInteger = (Integer) object;
            //write out the l
            baos.write('i');
            write(baos, DEFAULT_CHARSET.encode(tempInteger.toString()));
            baos.write('e');

        } else if (object instanceof byte[]) {

            byte[] tempByteArray = (byte[]) object;
            write(baos,
                  DEFAULT_CHARSET.encode(String.valueOf(tempByteArray.
                    length)));
            baos.write(':');
            baos.write(tempByteArray);

        } else if (object instanceof ByteBuffer) {

            ByteBuffer bb = (ByteBuffer) object;
            write(baos,
                  DEFAULT_CHARSET.encode(String.valueOf(bb.limit())));
            baos.write(':');
            write(baos, bb);
        }
    }

    protected void
            write(
                    OutputStream os,
                    ByteBuffer bb)

            throws IOException {
        os.write(bb.array(), 0, bb.limit());
    }

    private static boolean
            objectsAreIdentical(
                    Object o1,
                    Object o2) {
        if (o1 == null && o2 == null) {

            return (true);

        } else if (o1 == null || o2 == null) {

            return (false);
        }

        if (o1 instanceof Integer) {
            o1 = new Long(((Integer) o1).longValue());
        }
        if (o2 instanceof Integer) {
            o2 = new Long(((Integer) o2).longValue());
        }

        if (o1 instanceof Float) {
            o1 = String.valueOf((Float) o1);
        }
        if (o2 instanceof Float) {
            o2 = String.valueOf((Float) o2);
        }

        if (o1.getClass() != o2.getClass()) {

            return (false);
        }

        if (o1 instanceof Long) {

            return (o1.equals(o2));

        } else if (o1 instanceof byte[]) {

            return (Arrays.equals((byte[]) o1, (byte[]) o2));

        } else if (o1 instanceof ByteBuffer) {

            return (o1.equals(o2));

        } else if (o1 instanceof String) {

            return (o1.equals(o2));

        } else if (o1 instanceof List) {

            return (listsAreIdentical((List) o1, (List) o2));

        } else if (o1 instanceof Map) {

            return (mapsAreIdentical((Map) o1, (Map) o2));

        } else {

            System.err.println("Invalid type: " + o1);
            return (false);
        }
    }

    public static boolean
            listsAreIdentical(
                    List list1,
                    List list2) {
        if (list1 == null && list2 == null) {

            return (true);

        } else if (list1 == null || list2 == null) {

            return (false);
        }

        if (list1.size() != list2.size()) {

            return (false);
        }

        for (int i = 0; i < list1.size(); i++) {

            if (!objectsAreIdentical(list1.get(i), list2.get(i))) {

                return (false);
            }
        }

        return (true);
    }

    public static boolean
            mapsAreIdentical(
                    Map map1,
                    Map map2) {
        if (map1 == null && map2 == null) {

            return (true);

        } else if (map1 == null || map2 == null) {

            return (false);
        }

        if (map1.size() != map2.size()) {

            return (false);
        }

        Iterator it = map1.keySet().iterator();

        while (it.hasNext()) {

            Object key = it.next();

            Object v1 = map1.get(key);
            Object v2 = map2.get(key);

            if (!objectsAreIdentical(v1, v2)) {

                return (false);
            }
        }

        return (true);
    }
}
