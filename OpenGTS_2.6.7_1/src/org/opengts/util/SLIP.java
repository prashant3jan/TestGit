// ----------------------------------------------------------------------------
// Copyright 2007-2020, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  SLIP (Serial Line Internet Protocol) decode/encode
// ----------------------------------------------------------------------------
// Change History:
//  2018/09/10  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.*;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/**
*** SLIP (Serial Line Internet Protocol) decode/encode
*** - https://en.wikipedia.org/wiki/Serial_Line_Internet_Protocol
**/

public class SLIP
{

    // ------------------------------------------------------------------------

    public static final int  END        = 0xC0;
    public static final int  ESC        = 0xDB;
    public static final int  ESC_END    = 0xDC;
    public static final int  ESC_ESC    = 0xDD;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Local class for abstracting objects that can provide bytes
    **/
    public static interface ByteReader
    {
        public boolean hasAvailableRead();
        public int     readByte();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Decode SLIP encoded bytes from Objects that implement ByteReader 
    **/
    public static byte[] decode(ByteReader br)
    {

        /* nothing to decode? */
        if ((br == null) || !br.hasAvailableRead()) {
            //Print.logWarn("Invalid SLIP bytes");
            return new byte[0];
        }

        /* decode */
        ByteArrayOutputStream na = new ByteArrayOutputStream();
        int readCnt = 0;
        for (;br.hasAvailableRead();) {
            int ch = br.readByte();
            readCnt++;
            if (readCnt == 1) {
                // -- first byte
                if (ch == END) {
                    // -- expected, skip to next byte
                } else {
                    //Print.logWarn("Missing 'END' at first byte");
                    na.write(ch);
                }
            } else
            if (ch == END) {
                // -- end of SLIP bytes (should be last byte)
                //if (br.hasAvailableRead()) {
                    // -- found "END", but more bytes remain
                    //Print.logWarn("Bytes remaining after 'END'");
                //}
                break;
            } else
            if (!br.hasAvailableRead()) {
                // -- last byte, missing "END"
                //Print.logWarn("Missing 'END'");
                na.write(ch);
                break; // explicit break, but not necessary, since we would exit anyway
            } else
            if (ch == ESC) {
                ch = br.readByte(); // next byte
                readCnt++;
                if (ch == ESC_END) {
                    na.write(END);
                } else
                if (ch == ESC_ESC) {
                    na.write(ESC);
                } else {
                    //Print.logWarn("Invalid Escape sequence?");
                    na.write(ch);
                }
            } else {
                na.write(ch);
            }
        }

        /* return */
        return na.toByteArray();

    }

    /**
    *** Decode SLIP encoded bytes
    **/
    public static byte[] decode(final byte b[])
    {
        if (b != null) {
            return SLIP.decode(new ByteReader() {
                int bi = 0;
                public boolean hasAvailableRead() {
                    return (bi < b.length)? true : false;
                }
                public int readByte() {
                    return (bi < b.length)? ((int)b[bi++] & 0xFF) : -1;
                }
            });
        } else {
            return SLIP.decode((ByteReader)null);
        }
    }

    /**
    *** Decode SLIP encoded Payload
    **/
    public static byte[] decode(final Payload p)
    {
        if (p != null) {
            return SLIP.decode(new ByteReader() {
                public boolean hasAvailableRead() {
                    return p.hasAvailableRead();
                }
                public int readByte() {
                    return p.readByte();
                }
            });
        } else {
            return SLIP.decode((ByteReader)null);
        }
    }

    /**
    *** Decode SLIP encoded InputStream
    **/
    public static byte[] decode(final InputStream i)
    {
        if (i != null) {
            return SLIP.decode(new ByteReader() {
                public boolean hasAvailableRead() {
                    try {
                        return (i.available() > 0)? true : false;
                    } catch (IOException ioe) {
                        return false;
                    }
                }
                public int readByte() {
                    try {
                        return i.read();
                    } catch (IOException ioe) {
                        return -1;
                    }
                }
            });
        } else {
            return SLIP.decode((ByteReader)null);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Encode bytes
    **/
    public static byte[] encode(ByteReader br)
    {
        ByteArrayOutputStream na = new ByteArrayOutputStream();
        na.write(END);
        if (br != null) {
            for (;br.hasAvailableRead();) {
                int ch = br.readByte();
                switch (ch) {
                    case END:
                        na.write(ESC);
                        na.write(ESC_END);
                        break;
                    case ESC:
                        na.write(ESC);
                        na.write(ESC_ESC);
                        break;
                    default :
                        na.write(ch);
                        break;
                }
            }
        }
        na.write(END);
        return na.toByteArray();
    }

    /**
    *** Encode bytes from byte-array
    **/
    public static byte[] encode(final byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return SLIP.encode(new ByteReader() {
                int bi = 0;
                public boolean hasAvailableRead() {
                    return ((b != null) && (bi < b.length))? true : false;
                }
                public int readByte() {
                    return ((b != null) && (bi < b.length))? ((int)b[bi++] & 0xFF) : -1;
                }
            });
        } else {
            return SLIP.encode((ByteReader)null);
        }
    }

    /**
    *** Encode bytes from Payload
    **/
    public static byte[] encode(final Payload p)
    {
        if (p != null) {
            return SLIP.encode(new ByteReader() {
                public boolean hasAvailableRead() {
                    return (p != null)? p.hasAvailableRead() : false;
                }
                public int readByte() {
                    return (p != null)? p.readByte() : -1;
                }
            });
        } else {
            return SLIP.encode((ByteReader)null);
        }
    }

    /**
    *** Encode bytesc from InputStream
    **/
    public static byte[] encode(final InputStream i)
    {
        if (i != null) {
            return SLIP.encode(new ByteReader() {
                public boolean hasAvailableRead() {
                    try {
                        return (i.available() > 0)? true : false;
                    } catch (IOException ioe) {
                        return false;
                    }
                }
                public int readByte() {
                    try {
                        return i.read();
                    } catch (IOException ioe) {
                        return -1;
                    }
                }
            });
        } else {
            return SLIP.encode((ByteReader)null);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_DECODE[]    = { "decode", "d" };
    private static final String ARG_ENCODE[]    = { "encode", "e" };

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

        /* decode */
        if (RTConfig.hasProperty(ARG_DECODE)) {
            byte b[] = StringTools.parseHex(RTConfig.getString(ARG_DECODE,""),null);
            Print.sysPrintln("Decoding: 0x" + StringTools.toHexString(b));
            byte d[] = SLIP.decode(b);
            Print.sysPrintln("Decoded : 0x" + StringTools.toHexString(d));
            Print.sysPrintln("Encoded : 0x" + StringTools.toHexString(SLIP.encode(d)));
            System.exit(0);
        }

        /* encode */
        if (RTConfig.hasProperty(ARG_ENCODE)) {
            byte b[] = StringTools.parseHex(RTConfig.getString(ARG_ENCODE,""),null);
            Print.sysPrintln("Encoding: 0x" + StringTools.toHexString(b));
            byte e[] = SLIP.encode(b);
            Print.sysPrintln("Encoded : 0x" + StringTools.toHexString(e));
            Print.sysPrintln("Decoded : 0x" + StringTools.toHexString(SLIP.decode(e)));
            System.exit(0);
        }

    }
    
}
