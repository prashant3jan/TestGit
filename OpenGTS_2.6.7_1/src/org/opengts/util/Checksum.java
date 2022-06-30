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
//  Various checksum calculations
//  http://www.sunshine2k.de/coding/javascript/crc/crc_js.html
//  https://crc64.online/
// ----------------------------------------------------------------------------
// Change History:
//  2010/01/29  Martin D. Flynn
//     -Initial release
//  2012/12/24  Martin D. Flynn
//     -Added "calcCrcXOR8", "calcCrcSum8"
//  2015/05/03  Martin D. Flynn
//     -Added "calcCrc16_modbus" (preload of 0xFFFF)
//     -Added "calcCrcXmodem" (preload of 0x0000)
//  2020/02/19  GTS Development Team
//     -Added CRC-64 ECMA-182
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.io.*;

/**
*** Checksum tools
**/

public class Checksum
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static byte TwosCompliment(byte b)
    {
        return (byte)((~(int)b & 0xFF) + 1);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public enum CRC {
        SUM8            (  8, "Sum8"),
        XOR8            (  8, "Xor8"),
        CRC16           ( 16, "Crc16[0x0000]"),
        CRC16_0000      ( 16, "Crc16[0x0000]"),
        MODBUS          ( 16, "Modbus"),
        CRC16_FFFF      ( 16, "Crc16[0xFFFF]"),
        CCITT           ( 16, "CCITT[0xFFFF]"),
        CCITT_FFFF      ( 16, "CCITT[0xFFFF]"),
        XMODEM          ( 16, "Xmodem"),
        CCITT_0000      ( 16, "CCITT[0x0000]"),
        CCITT_1D0F      ( 16, "CCITT[0x1D0F]"),
        CRC32           ( 32, "Crc32"),
        CRC64_ECMA_182  ( 64, "Crc64[EMCA-182]"), // "HelloWorld" ==> "9fabb91cb3776b02"
        CRC64_ISO_3309  ( 64, "Crc64[ISO-3309]"); // "HelloWorld" ==> "b284c6cc1cbc3eee"
        private int    bb = 0;
        private String dd = null;
        CRC(int b, String d)       { bb = b; dd = d; }
        public int    getBitLen()  { return bb; }
        public long   getBitMask() { return (bb < 64)? ((1L << bb) - 1L) : 0xFFFFFFFFFFFFFFFFL; }
        public String toString()   { return dd; }
    };

    /**
    *** Returns checksum based on specified algorithm
    *** @param crc  The CRC algorithm
    *** @param b    The byte array
    *** @param bOfs The offset into the byte array to begin CRC
    *** @param bLen The number of bytes to include in the CRC
    **/
    public static long calcChecksum(CRC crcAlg, byte b[], int bOfs, int bLen)
    {

        /* CRC not specified? */
        if (crcAlg == null) {
            return 0x0000L;
        }

        /* execute checksum */
        long crcVal;
        long crcMask = crcAlg.getBitMask();
        switch (crcAlg) {
            case SUM8:
                crcVal = (long)Checksum.calcCrcSum8(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case XOR8:
                crcVal = (long)Checksum.calcCrcXOR8(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case CRC16:
            case CRC16_0000:
                crcVal = (long)Checksum.calcCrc16(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case MODBUS:
            case CRC16_FFFF:
                crcVal = (long)Checksum.calcCrc16_modbus(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case CCITT:
            case CCITT_FFFF:
                crcVal = (long)Checksum.calcCrcCCITT(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case XMODEM:
            case CCITT_0000:
                crcVal = (long)Checksum.calcCrcXmodem(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case CCITT_1D0F:
                crcVal = (long)Checksum.calcCrcCCITT_1D0F(b, bOfs, bLen) & crcMask; // mask sign extension
                break;
            case CRC32:
                crcVal = Checksum.calcCrc32(b, bOfs, bLen) & crcMask; // mask is redundant
                break;
            case CRC64_ECMA_182:
                crcVal = Checksum.calcCrc64_ECMA_182(b, bOfs, bLen) & crcMask; // mask is redundant
                break;
            case CRC64_ISO_3309:
                crcVal = Checksum.calcCrc64_ISO_3309(b, bOfs, bLen) & crcMask; // mask is redundant
                break;
            default :
                Print.logError("*** UNRECOGNIZED CRC ALGORITHM: " + crcAlg);
                return 0x0000L & crcMask; // mask is redundant
        }

        /* return crc value */
        //Print.logInfo("CRC=0x"+StringTools.toHexString(crcVal,64)+", MASK=0x"+StringTools.toHexString(crcAlg.getBitMask(),64));
        return crcVal;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- CRC-CCITT (0xFFFF) [x16 + x12 + x5 + 1]
    private static int crc_CCITT_Table[] = null;
    private static synchronized void initCrcCCITT()
    {
        if (crc_CCITT_Table == null) {
            crc_CCITT_Table = new int[256];
            for (int c = 0; c < 256; c++) {
                int fcs = 0;
                int x = (c << 8);
                for (int j = 0; j < 8; j++) {
                    if (((fcs ^ x) & 0x8000) != 0) {
                        fcs = (fcs << 1) ^ 0x1021;
                    } else { 
                        fcs = (fcs << 1);
                    }
                    x <<= 1;
                    fcs &= 0xFFFF;
                }
                crc_CCITT_Table[c] = fcs;
            }
        }
    }

    public static int _calcCrcCCITT(int preload, byte b[], int bOfs, int bLen)
    {
        int W = preload & 0xFFFF; // preload
        if (b != null) {

            /* initialize CRC table */
            if (crc_CCITT_Table == null) { Checksum.initCrcCCITT(); }

            /* adjust offset/length */
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);

            /* calc CRC */
            for (int c = 0; c < len; c++) {
                W = (crc_CCITT_Table[(b[c+ofs] ^ (W >>> 8)) & 0xFF] ^ (W << 8)) & 0xFFFF;
            }

        }
        return W;
    }

    // --------------------------------

    public  static int CRCCCITT_PRELOAD_FFFF    = 0xFFFF; // standard crc16-ccitt

    public static int calcCrcCCITT(byte b[])
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_FFFF, b, 0, -1);
    }

    public static int calcCrcCCITT(byte b[], int bLen)
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_FFFF, b, 0, bLen);
    }

    public static int calcCrcCCITT(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_FFFF, b, bOfs, bLen);
    }

    // --------------------------------

    public  static int CRCCCITT_PRELOAD_0000    = 0x0000;
    public  static int CRCCCITT_PRELOAD_XMODEM  = CRCCCITT_PRELOAD_0000;

    public static int calcCrcXmodem(byte b[])
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_0000, b, 0, -1);
    }

    public static int calcCrcXmodem(byte b[], int bLen)
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_0000, b, 0, bLen);
    }

    public static int calcCrcXmodem(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_0000, b, bOfs, bLen);
    }

    // --------------------------------

    public  static int CRCCCITT_PRELOAD_1D0F    = 0x1D0F;

    public static int calcCrcCCITT_1D0F(byte b[])
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_1D0F, b, 0, -1);
    }

    public static int calcCrcCCITT_1D0F(byte b[], int bLen)
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_1D0F, b, 0, bLen);
    }

    public static int calcCrcCCITT_1D0F(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrcCCITT(CRCCCITT_PRELOAD_1D0F, b, bOfs, bLen);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CRC16

    private static int CRCtab16[] = {
        0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
        0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
        0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
        0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
        0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
        0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
        0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
        0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
        0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
        0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
        0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
        0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
        0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
        0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
        0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
        0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
        0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
        0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
        0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
        0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
        0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
        0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
        0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
        0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
        0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
        0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
        0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
        0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
        0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
        0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
        0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
        0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
    };

    private static int _calcCrc16(int preload, byte b[], int bOfs, int bLen)
    {
        int crc = preload & 0xFFFF;
        if (b != null) {

            /* adjust offset/length */
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);

            /* calc CRC */
            for (int c = 0; c < len; c++) {
                crc = ((crc >> 8) ^ CRCtab16[(crc ^ b[c+ofs]) & 0xFF]); // (crc>>>8) changed to (crc>>8)
            }

        }
        return (crc & 0xFFFF);  
    }

    // --------------------------------

    private static final int CRC16_PRELOAD_0000     = 0x0000;

    public static int calcCrc16(byte b[])
    {
        return Checksum._calcCrc16(CRC16_PRELOAD_0000, b, 0, -1);
    }

    public static int calcCrc16(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrc16(CRC16_PRELOAD_0000, b, bOfs, bLen);
    }

    // --------------------------------

    private static final int CRC16_PRELOAD_MODBUS   = 0xFFFF;

    public static int calcCrc16_modbus(byte b[])
    {
        return Checksum._calcCrc16(CRC16_PRELOAD_MODBUS, b, 0, -1);
    }

    public static int calcCrc16_modbus(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrc16(CRC16_PRELOAD_MODBUS, b, bOfs, bLen);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Used by CITG02 (CRC16-ITU?) - (non standard?)
    **/
    private static int CRCtab16_1[] = {
        0x0000, 0x1189, 0x2312, 0x329B, 0x4624, 0x57AD, 0x6536, 0x74BF,
        0x8C48, 0x9DC1, 0xAF5A, 0xBED3, 0xCA6C, 0xDBE5, 0xE97E, 0xF8F7,
        0x1081, 0x0108, 0x3393, 0x221A, 0x56A5, 0x472C, 0x75B7, 0x643E,
        0x9CC9, 0x8D40, 0xBFDB, 0xAE52, 0xDAED, 0xCB64, 0xF9FF, 0xE876,
        0x2102, 0x308B, 0x0210, 0x1399, 0x6726, 0x76AF, 0x4434, 0x55BD,
        0xAD4A, 0xBCC3, 0x8E58, 0x9FD1, 0xEB6E, 0xFAE7, 0xC87C, 0xD9F5,
        0x3183, 0x200A, 0x1291, 0x0318, 0x77A7, 0x662E, 0x54B5, 0x453C,
        0xBDCB, 0xAC42, 0x9ED9, 0x8F50, 0xFBEF, 0xEA66, 0xD8FD, 0xC974,
        0x4204, 0x538D, 0x6116, 0x709F, 0x0420, 0x15A9, 0x2732, 0x36BB,
        0xCE4C, 0xDFC5, 0xED5E, 0xFCD7, 0x8868, 0x99E1, 0xAB7A, 0xBAF3,
        0x5285, 0x430C, 0x7197, 0x601E, 0x14A1, 0x0528, 0x37B3, 0x263A,
        0xDECD, 0xCF44, 0xFDDF, 0xEC56, 0x98E9, 0x8960, 0xBBFB, 0xAA72,
        0x6306, 0x728F, 0x4014, 0x519D, 0x2522, 0x34AB, 0x0630, 0x17B9,
        0xEF4E, 0xFEC7, 0xCC5C, 0xDDD5, 0xA96A, 0xB8E3, 0x8A78, 0x9BF1,
        0x7387, 0x620E, 0x5095, 0x411C, 0x35A3, 0x242A, 0x16B1, 0x0738,
        0xFFCF, 0xEE46, 0xDCDD, 0xCD54, 0xB9EB, 0xA862, 0x9AF9, 0x8B70,
        0x8408, 0x9581, 0xA71A, 0xB693, 0xC22C, 0xD3A5, 0xE13E, 0xF0B7,
        0x0840, 0x19C9, 0x2B52, 0x3ADB, 0x4E64, 0x5FED, 0x6D76, 0x7CFF,
        0x9489, 0x8500, 0xB79B, 0xA612, 0xD2AD, 0xC324, 0xF1BF, 0xE036,
        0x18C1, 0x0948, 0x3BD3, 0x2A5A, 0x5EE5, 0x4F6C, 0x7DF7, 0x6C7E,
        0xA50A, 0xB483, 0x8618, 0x9791, 0xE32E, 0xF2A7, 0xC03C, 0xD1B5,
        0x2942, 0x38CB, 0x0A50, 0x1BD9, 0x6F66, 0x7EEF, 0x4C74, 0x5DFD,
        0xB58B, 0xA402, 0x9699, 0x8710, 0xF3AF, 0xE226, 0xD0BD, 0xC134,
        0x39C3, 0x284A, 0x1AD1, 0x0B58, 0x7FE7, 0x6E6E, 0x5CF5, 0x4D7C,
        0xC60C, 0xD785, 0xE51E, 0xF497, 0x8028, 0x91A1, 0xA33A, 0xB2B3,
        0x4A44, 0x5BCD, 0x6956, 0x78DF, 0x0C60, 0x1DE9, 0x2F72, 0x3EFB,
        0xD68D, 0xC704, 0xF59F, 0xE416, 0x90A9, 0x8120, 0xB3BB, 0xA232,
        0x5AC5, 0x4B4C, 0x79D7, 0x685E, 0x1CE1, 0x0D68, 0x3FF3, 0x2E7A,
        0xE70E, 0xF687, 0xC41C, 0xD595, 0xA12A, 0xB0A3, 0x8238, 0x93B1,
        0x6B46, 0x7ACF, 0x4854, 0x59DD, 0x2D62, 0x3CEB, 0x0E70, 0x1FF9,
        0xF78F, 0xE606, 0xD49D, 0xC514, 0xB1AB, 0xA022, 0x92B9, 0x8330,
        0x7BC7, 0x6A4E, 0x58D5, 0x495C, 0x3DE3, 0x2C6A, 0x1EF1, 0x0F78,
    };

    public static int calcCrc16_1(byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return Checksum.calcCrc16_1(b, 0, b.length);
        } else {
            return 0;
        }
    }

    public static int calcCrc16_1(byte b[], int bOfs, int bLen)
    {
        int crc = 0x0000; // 0xFFFF;
        if (b != null) {

            // -- adjust offset/length 
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);

            // -- calc CRC 
            for (int c = 0; c < len; c++) {
                crc = (crc >> 8) ^ CRCtab16_1[(crc ^ b[c+ofs]) & 0xFF];
            }

        }
        return (~crc & 0xFFFF);  
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CRC32

    private static final int CRC32_PRELOAD   = 0x0000;

    public static long calcCrc32(byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return Checksum._calcCrc32(CRC32_PRELOAD, b, 0, b.length);
        } else {
            return 0L;
        }
    }

    public static long calcCrc32(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrc32(CRC32_PRELOAD, b, bOfs, bLen);
    }

    private static long _calcCrc32(int preload, byte b[], int bOfs, int bLen)
    {
        int crc = preload; // not used
        if (b != null) {

            // adjust offset/length 
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);

            // calc CRC 32
            java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
            crc32.update(b, ofs, len);
            return crc32.getValue(); // long

        }
        return crc;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CRC-64-ECMA-182
	// https://github.com/ggrandes/sandbox/blob/master/src/CRC64.java
	// 
	// The polynomial code used is 0x42F0E1EBA9EA3693 (CRC64-ECMA-182)
	// x64+x62+x57+x55+x54+x53+x52+x47+x46+x45+x40+x39+x38+x37+x35+x33+
	// x32+x31+x29+x27+x24+x23+x22+x21+x19+x17+x13+x12+x10+x9+x7+x4+x+1
	// 
	// poly=0x42f0e1eba9ea3693 init=0x0 refin=false refout=false xorout=0x0
	// 
	// Ref:
	//   http://en.wikipedia.org/wiki/Cyclic_redundancy_check
	//   http://reveng.sourceforge.net/crc-catalogue/17plus.htm

    private static final long CRC64_ECMA_182_PRELOAD = 0x0000000000000000L;
    private static final long POLY64_ECMA_182        = 0x42F0E1EBA9EA3693L; // ECMA-182

    private static final long CRCtab64_ECMA_182[] = new long[] {
        0x0000000000000000L, 0x42F0E1EBA9EA3693L, 0x85E1C3D753D46D26L, 0xC711223CFA3E5BB5L, 
        0x493366450E42ECDFL, 0x0BC387AEA7A8DA4CL, 0xCCD2A5925D9681F9L, 0x8E224479F47CB76AL, 
        0x9266CC8A1C85D9BEL, 0xD0962D61B56FEF2DL, 0x17870F5D4F51B498L, 0x5577EEB6E6BB820BL, 
        0xDB55AACF12C73561L, 0x99A54B24BB2D03F2L, 0x5EB4691841135847L, 0x1C4488F3E8F96ED4L, 
        0x663D78FF90E185EFL, 0x24CD9914390BB37CL, 0xE3DCBB28C335E8C9L, 0xA12C5AC36ADFDE5AL, 
        0x2F0E1EBA9EA36930L, 0x6DFEFF5137495FA3L, 0xAAEFDD6DCD770416L, 0xE81F3C86649D3285L, 
        0xF45BB4758C645C51L, 0xB6AB559E258E6AC2L, 0x71BA77A2DFB03177L, 0x334A9649765A07E4L, 
        0xBD68D2308226B08EL, 0xFF9833DB2BCC861DL, 0x388911E7D1F2DDA8L, 0x7A79F00C7818EB3BL, 
        0xCC7AF1FF21C30BDEL, 0x8E8A101488293D4DL, 0x499B3228721766F8L, 0x0B6BD3C3DBFD506BL, 
        0x854997BA2F81E701L, 0xC7B97651866BD192L, 0x00A8546D7C558A27L, 0x4258B586D5BFBCB4L, 
        0x5E1C3D753D46D260L, 0x1CECDC9E94ACE4F3L, 0xDBFDFEA26E92BF46L, 0x990D1F49C77889D5L, 
        0x172F5B3033043EBFL, 0x55DFBADB9AEE082CL, 0x92CE98E760D05399L, 0xD03E790CC93A650AL, 
        0xAA478900B1228E31L, 0xE8B768EB18C8B8A2L, 0x2FA64AD7E2F6E317L, 0x6D56AB3C4B1CD584L, 
        0xE374EF45BF6062EEL, 0xA1840EAE168A547DL, 0x66952C92ECB40FC8L, 0x2465CD79455E395BL, 
        0x3821458AADA7578FL, 0x7AD1A461044D611CL, 0xBDC0865DFE733AA9L, 0xFF3067B657990C3AL, 
        0x711223CFA3E5BB50L, 0x33E2C2240A0F8DC3L, 0xF4F3E018F031D676L, 0xB60301F359DBE0E5L, 
        0xDA050215EA6C212FL, 0x98F5E3FE438617BCL, 0x5FE4C1C2B9B84C09L, 0x1D14202910527A9AL, 
        0x93366450E42ECDF0L, 0xD1C685BB4DC4FB63L, 0x16D7A787B7FAA0D6L, 0x5427466C1E109645L, 
        0x4863CE9FF6E9F891L, 0x0A932F745F03CE02L, 0xCD820D48A53D95B7L, 0x8F72ECA30CD7A324L, 
        0x0150A8DAF8AB144EL, 0x43A04931514122DDL, 0x84B16B0DAB7F7968L, 0xC6418AE602954FFBL, 
        0xBC387AEA7A8DA4C0L, 0xFEC89B01D3679253L, 0x39D9B93D2959C9E6L, 0x7B2958D680B3FF75L, 
        0xF50B1CAF74CF481FL, 0xB7FBFD44DD257E8CL, 0x70EADF78271B2539L, 0x321A3E938EF113AAL, 
        0x2E5EB66066087D7EL, 0x6CAE578BCFE24BEDL, 0xABBF75B735DC1058L, 0xE94F945C9C3626CBL, 
        0x676DD025684A91A1L, 0x259D31CEC1A0A732L, 0xE28C13F23B9EFC87L, 0xA07CF2199274CA14L, 
        0x167FF3EACBAF2AF1L, 0x548F120162451C62L, 0x939E303D987B47D7L, 0xD16ED1D631917144L, 
        0x5F4C95AFC5EDC62EL, 0x1DBC74446C07F0BDL, 0xDAAD56789639AB08L, 0x985DB7933FD39D9BL, 
        0x84193F60D72AF34FL, 0xC6E9DE8B7EC0C5DCL, 0x01F8FCB784FE9E69L, 0x43081D5C2D14A8FAL, 
        0xCD2A5925D9681F90L, 0x8FDAB8CE70822903L, 0x48CB9AF28ABC72B6L, 0x0A3B7B1923564425L, 
        0x70428B155B4EAF1EL, 0x32B26AFEF2A4998DL, 0xF5A348C2089AC238L, 0xB753A929A170F4ABL, 
        0x3971ED50550C43C1L, 0x7B810CBBFCE67552L, 0xBC902E8706D82EE7L, 0xFE60CF6CAF321874L, 
        0xE224479F47CB76A0L, 0xA0D4A674EE214033L, 0x67C58448141F1B86L, 0x253565A3BDF52D15L, 
        0xAB1721DA49899A7FL, 0xE9E7C031E063ACECL, 0x2EF6E20D1A5DF759L, 0x6C0603E6B3B7C1CAL, 
        0xF6FAE5C07D3274CDL, 0xB40A042BD4D8425EL, 0x731B26172EE619EBL, 0x31EBC7FC870C2F78L, 
        0xBFC9838573709812L, 0xFD39626EDA9AAE81L, 0x3A28405220A4F534L, 0x78D8A1B9894EC3A7L, 
        0x649C294A61B7AD73L, 0x266CC8A1C85D9BE0L, 0xE17DEA9D3263C055L, 0xA38D0B769B89F6C6L, 
        0x2DAF4F0F6FF541ACL, 0x6F5FAEE4C61F773FL, 0xA84E8CD83C212C8AL, 0xEABE6D3395CB1A19L, 
        0x90C79D3FEDD3F122L, 0xD2377CD44439C7B1L, 0x15265EE8BE079C04L, 0x57D6BF0317EDAA97L, 
        0xD9F4FB7AE3911DFDL, 0x9B041A914A7B2B6EL, 0x5C1538ADB04570DBL, 0x1EE5D94619AF4648L, 
        0x02A151B5F156289CL, 0x4051B05E58BC1E0FL, 0x87409262A28245BAL, 0xC5B073890B687329L, 
        0x4B9237F0FF14C443L, 0x0962D61B56FEF2D0L, 0xCE73F427ACC0A965L, 0x8C8315CC052A9FF6L, 
        0x3A80143F5CF17F13L, 0x7870F5D4F51B4980L, 0xBF61D7E80F251235L, 0xFD913603A6CF24A6L, 
        0x73B3727A52B393CCL, 0x31439391FB59A55FL, 0xF652B1AD0167FEEAL, 0xB4A25046A88DC879L, 
        0xA8E6D8B54074A6ADL, 0xEA16395EE99E903EL, 0x2D071B6213A0CB8BL, 0x6FF7FA89BA4AFD18L, 
        0xE1D5BEF04E364A72L, 0xA3255F1BE7DC7CE1L, 0x64347D271DE22754L, 0x26C49CCCB40811C7L, 
        0x5CBD6CC0CC10FAFCL, 0x1E4D8D2B65FACC6FL, 0xD95CAF179FC497DAL, 0x9BAC4EFC362EA149L, 
        0x158E0A85C2521623L, 0x577EEB6E6BB820B0L, 0x906FC95291867B05L, 0xD29F28B9386C4D96L, 
        0xCEDBA04AD0952342L, 0x8C2B41A1797F15D1L, 0x4B3A639D83414E64L, 0x09CA82762AAB78F7L, 
        0x87E8C60FDED7CF9DL, 0xC51827E4773DF90EL, 0x020905D88D03A2BBL, 0x40F9E43324E99428L, 
        0x2CFFE7D5975E55E2L, 0x6E0F063E3EB46371L, 0xA91E2402C48A38C4L, 0xEBEEC5E96D600E57L, 
        0x65CC8190991CB93DL, 0x273C607B30F68FAEL, 0xE02D4247CAC8D41BL, 0xA2DDA3AC6322E288L, 
        0xBE992B5F8BDB8C5CL, 0xFC69CAB42231BACFL, 0x3B78E888D80FE17AL, 0x7988096371E5D7E9L, 
        0xF7AA4D1A85996083L, 0xB55AACF12C735610L, 0x724B8ECDD64D0DA5L, 0x30BB6F267FA73B36L, 
        0x4AC29F2A07BFD00DL, 0x08327EC1AE55E69EL, 0xCF235CFD546BBD2BL, 0x8DD3BD16FD818BB8L, 
        0x03F1F96F09FD3CD2L, 0x41011884A0170A41L, 0x86103AB85A2951F4L, 0xC4E0DB53F3C36767L, 
        0xD8A453A01B3A09B3L, 0x9A54B24BB2D03F20L, 0x5D45907748EE6495L, 0x1FB5719CE1045206L, 
        0x919735E51578E56CL, 0xD367D40EBC92D3FFL, 0x1476F63246AC884AL, 0x568617D9EF46BED9L, 
        0xE085162AB69D5E3CL, 0xA275F7C11F7768AFL, 0x6564D5FDE549331AL, 0x279434164CA30589L, 
        0xA9B6706FB8DFB2E3L, 0xEB46918411358470L, 0x2C57B3B8EB0BDFC5L, 0x6EA7525342E1E956L, 
        0x72E3DAA0AA188782L, 0x30133B4B03F2B111L, 0xF7021977F9CCEAA4L, 0xB5F2F89C5026DC37L, 
        0x3BD0BCE5A45A6B5DL, 0x79205D0E0DB05DCEL, 0xBE317F32F78E067BL, 0xFCC19ED95E6430E8L, 
        0x86B86ED5267CDBD3L, 0xC4488F3E8F96ED40L, 0x0359AD0275A8B6F5L, 0x41A94CE9DC428066L, 
        0xCF8B0890283E370CL, 0x8D7BE97B81D4019FL, 0x4A6ACB477BEA5A2AL, 0x089A2AACD2006CB9L, 
        0x14DEA25F3AF9026DL, 0x562E43B4931334FEL, 0x913F6188692D6F4BL, 0xD3CF8063C0C759D8L, 
        0x5DEDC41A34BBEEB2L, 0x1F1D25F19D51D821L, 0xD80C07CD676F8394L, 0x9AFCE626CE85B507L, 
    };

    private static void initCrc64_ECMA_182()
    {
        /*
        StringBuffer sb = new StringBuffer();
        sb.append("    private static final long CRCtab64_ECMA_182[] = new long[] {\n");
        for (int i = 0; i < CRCtab64_ECMA_182.length; i += 4) {
            sb.append("        ");
            for (int j = 0; j < 4; j++) {
                sb.append("0x").append(StringTools.toHexString(CRCtab64_ECMA_182[i+j],64)).append("L, ");
            }
            sb.append("\n");
        }
        sb.append("    };\n");
        Print.logInfo("\n" + sb);
        */
    }

    public static long calcCrc64_ECMA_182(byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return Checksum._calcCrc64_ECMA_182(CRC64_ECMA_182_PRELOAD, b, 0, b.length);
        } else {
            return 0L;
        }
    }

    public static long calcCrc64_ECMA_182(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrc64_ECMA_182(CRC64_ECMA_182_PRELOAD, b, bOfs, bLen);
    }

    private static long _calcCrc64_ECMA_182(long preload, byte b[], int bOfs, int bLen)
    {
        long crc = preload; // not used
        if (b != null) {
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);
            for (int i = ofs; i < len; i++) {
                int ndx = ((int)(crc >> 56) ^ b[i]) & 0xFF;
                crc = CRCtab64_ECMA_182[ndx] ^ (crc << 8);
            }
            //Print.logInfo("OFS="+ofs+", LEN="+len+" B=0x"+StringTools.toHexString(b)+", CRC=0x"+StringTools.toHexString(crc,64));
        }
        return crc;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CRC-64-ISO_3309
    // https://www.javatips.net/api/vnluser-master/backend/netty-s2-http-server/src-core/rfx/server/util/CRC64.java
    // https://github.com/52Jolynn/Doodo/blob/master/src/main/java/com/laud/doodo/string/CRC64.java

    private static final long CRC64_ISO_3309_PRELOAD = 0x0000000000000000L;
    private static final long POLY64_ISO_3309        = 0xD800000000000000L; // ISO-3309

    private static final long CRCtab64_ISO_3309[] = new long[] {
        0x0000000000000000L, 0x01B0000000000000L, 0x0360000000000000L, 0x02D0000000000000L, 
        0x06C0000000000000L, 0x0770000000000000L, 0x05A0000000000000L, 0x0410000000000000L, 
        0x0D80000000000000L, 0x0C30000000000000L, 0x0EE0000000000000L, 0x0F50000000000000L, 
        0x0B40000000000000L, 0x0AF0000000000000L, 0x0820000000000000L, 0x0990000000000000L, 
        0x1B00000000000000L, 0x1AB0000000000000L, 0x1860000000000000L, 0x19D0000000000000L, 
        0x1DC0000000000000L, 0x1C70000000000000L, 0x1EA0000000000000L, 0x1F10000000000000L, 
        0x1680000000000000L, 0x1730000000000000L, 0x15E0000000000000L, 0x1450000000000000L, 
        0x1040000000000000L, 0x11F0000000000000L, 0x1320000000000000L, 0x1290000000000000L, 
        0x3600000000000000L, 0x37B0000000000000L, 0x3560000000000000L, 0x34D0000000000000L, 
        0x30C0000000000000L, 0x3170000000000000L, 0x33A0000000000000L, 0x3210000000000000L, 
        0x3B80000000000000L, 0x3A30000000000000L, 0x38E0000000000000L, 0x3950000000000000L, 
        0x3D40000000000000L, 0x3CF0000000000000L, 0x3E20000000000000L, 0x3F90000000000000L, 
        0x2D00000000000000L, 0x2CB0000000000000L, 0x2E60000000000000L, 0x2FD0000000000000L, 
        0x2BC0000000000000L, 0x2A70000000000000L, 0x28A0000000000000L, 0x2910000000000000L, 
        0x2080000000000000L, 0x2130000000000000L, 0x23E0000000000000L, 0x2250000000000000L, 
        0x2640000000000000L, 0x27F0000000000000L, 0x2520000000000000L, 0x2490000000000000L, 
        0x6C00000000000000L, 0x6DB0000000000000L, 0x6F60000000000000L, 0x6ED0000000000000L, 
        0x6AC0000000000000L, 0x6B70000000000000L, 0x69A0000000000000L, 0x6810000000000000L, 
        0x6180000000000000L, 0x6030000000000000L, 0x62E0000000000000L, 0x6350000000000000L, 
        0x6740000000000000L, 0x66F0000000000000L, 0x6420000000000000L, 0x6590000000000000L, 
        0x7700000000000000L, 0x76B0000000000000L, 0x7460000000000000L, 0x75D0000000000000L, 
        0x71C0000000000000L, 0x7070000000000000L, 0x72A0000000000000L, 0x7310000000000000L, 
        0x7A80000000000000L, 0x7B30000000000000L, 0x79E0000000000000L, 0x7850000000000000L, 
        0x7C40000000000000L, 0x7DF0000000000000L, 0x7F20000000000000L, 0x7E90000000000000L, 
        0x5A00000000000000L, 0x5BB0000000000000L, 0x5960000000000000L, 0x58D0000000000000L, 
        0x5CC0000000000000L, 0x5D70000000000000L, 0x5FA0000000000000L, 0x5E10000000000000L, 
        0x5780000000000000L, 0x5630000000000000L, 0x54E0000000000000L, 0x5550000000000000L, 
        0x5140000000000000L, 0x50F0000000000000L, 0x5220000000000000L, 0x5390000000000000L, 
        0x4100000000000000L, 0x40B0000000000000L, 0x4260000000000000L, 0x43D0000000000000L, 
        0x47C0000000000000L, 0x4670000000000000L, 0x44A0000000000000L, 0x4510000000000000L, 
        0x4C80000000000000L, 0x4D30000000000000L, 0x4FE0000000000000L, 0x4E50000000000000L, 
        0x4A40000000000000L, 0x4BF0000000000000L, 0x4920000000000000L, 0x4890000000000000L, 
        0xD800000000000000L, 0xD9B0000000000000L, 0xDB60000000000000L, 0xDAD0000000000000L, 
        0xDEC0000000000000L, 0xDF70000000000000L, 0xDDA0000000000000L, 0xDC10000000000000L, 
        0xD580000000000000L, 0xD430000000000000L, 0xD6E0000000000000L, 0xD750000000000000L, 
        0xD340000000000000L, 0xD2F0000000000000L, 0xD020000000000000L, 0xD190000000000000L, 
        0xC300000000000000L, 0xC2B0000000000000L, 0xC060000000000000L, 0xC1D0000000000000L, 
        0xC5C0000000000000L, 0xC470000000000000L, 0xC6A0000000000000L, 0xC710000000000000L, 
        0xCE80000000000000L, 0xCF30000000000000L, 0xCDE0000000000000L, 0xCC50000000000000L, 
        0xC840000000000000L, 0xC9F0000000000000L, 0xCB20000000000000L, 0xCA90000000000000L, 
        0xEE00000000000000L, 0xEFB0000000000000L, 0xED60000000000000L, 0xECD0000000000000L, 
        0xE8C0000000000000L, 0xE970000000000000L, 0xEBA0000000000000L, 0xEA10000000000000L, 
        0xE380000000000000L, 0xE230000000000000L, 0xE0E0000000000000L, 0xE150000000000000L, 
        0xE540000000000000L, 0xE4F0000000000000L, 0xE620000000000000L, 0xE790000000000000L, 
        0xF500000000000000L, 0xF4B0000000000000L, 0xF660000000000000L, 0xF7D0000000000000L, 
        0xF3C0000000000000L, 0xF270000000000000L, 0xF0A0000000000000L, 0xF110000000000000L, 
        0xF880000000000000L, 0xF930000000000000L, 0xFBE0000000000000L, 0xFA50000000000000L, 
        0xFE40000000000000L, 0xFFF0000000000000L, 0xFD20000000000000L, 0xFC90000000000000L, 
        0xB400000000000000L, 0xB5B0000000000000L, 0xB760000000000000L, 0xB6D0000000000000L, 
        0xB2C0000000000000L, 0xB370000000000000L, 0xB1A0000000000000L, 0xB010000000000000L, 
        0xB980000000000000L, 0xB830000000000000L, 0xBAE0000000000000L, 0xBB50000000000000L, 
        0xBF40000000000000L, 0xBEF0000000000000L, 0xBC20000000000000L, 0xBD90000000000000L, 
        0xAF00000000000000L, 0xAEB0000000000000L, 0xAC60000000000000L, 0xADD0000000000000L, 
        0xA9C0000000000000L, 0xA870000000000000L, 0xAAA0000000000000L, 0xAB10000000000000L, 
        0xA280000000000000L, 0xA330000000000000L, 0xA1E0000000000000L, 0xA050000000000000L, 
        0xA440000000000000L, 0xA5F0000000000000L, 0xA720000000000000L, 0xA690000000000000L, 
        0x8200000000000000L, 0x83B0000000000000L, 0x8160000000000000L, 0x80D0000000000000L, 
        0x84C0000000000000L, 0x8570000000000000L, 0x87A0000000000000L, 0x8610000000000000L, 
        0x8F80000000000000L, 0x8E30000000000000L, 0x8CE0000000000000L, 0x8D50000000000000L, 
        0x8940000000000000L, 0x88F0000000000000L, 0x8A20000000000000L, 0x8B90000000000000L, 
        0x9900000000000000L, 0x98B0000000000000L, 0x9A60000000000000L, 0x9BD0000000000000L, 
        0x9FC0000000000000L, 0x9E70000000000000L, 0x9CA0000000000000L, 0x9D10000000000000L, 
        0x9480000000000000L, 0x9530000000000000L, 0x97E0000000000000L, 0x9650000000000000L, 
        0x9240000000000000L, 0x93F0000000000000L, 0x9120000000000000L, 0x9090000000000000L, 
    };

    private static void initCrc64_ISO_3309()
    {
        /*
        CRCtab64_ISO_3309 = new long[256];
        for (int i = 0; i < 256; i++) {
            long v = i;
            for (int j = 0; j < 8; j++) {
                if ((v & 1) == 1) {
                    v = (v >>> 1) ^ POLY64_ISO_3309;
                } else {
                    v = (v >>> 1);
                }
            }
            CRCtab64_ISO_3309[i] = v;
        }
        // --
        StringBuffer sb = new StringBuffer();
        sb.append("    private static final long CRCtab64_ISO_3309[] = new long[] {\n");
        for (int i = 0; i < CRCtab64_ISO_3309.length; i += 4) {
            sb.append("        ");
            for (int j = 0; j < 4; j++) {
                sb.append("0x").append(StringTools.toHexString(CRCtab64_ISO_3309[i+j],64)).append("L, ");
            }
            sb.append("\n");
        }
        sb.append("    };\n");
        Print.logInfo("\n" + sb);
        */
    }

    public static long calcCrc64_ISO_3309(byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return Checksum._calcCrc64_ISO_3309(CRC64_ISO_3309_PRELOAD, b, 0, b.length);
        } else {
            return 0L;
        }
    }

    public static long calcCrc64_ISO_3309(byte b[], int bOfs, int bLen)
    {
        return Checksum._calcCrc64_ISO_3309(CRC64_ISO_3309_PRELOAD, b, bOfs, bLen);
    }

    private static long _calcCrc64_ISO_3309(long preload, byte b[], int bOfs, int bLen)
    {
        long crc = preload; 
        if (b != null) {
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);
            for (int i = ofs; i < len; i++) {
                int ndx = ((int)(crc) ^ b[i]) & 0xFF;
                crc = (crc >>> 8) ^ CRCtab64_ISO_3309[ndx];
            }
            //Print.logInfo("OFS="+ofs+", LEN="+len+" B=0x"+StringTools.toHexString(b)+", CRC=0x"+StringTools.toHexString(crc,64));
        }
        return ~crc; // NOT(bits)
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CRC XOR-8

    public static byte calcCrcXOR8(byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return Checksum.calcCrcXOR8(b, 0, b.length);
        } else {
            return (byte)0;
        }
    }

    public static byte calcCrcXOR8(byte b[], int bOfs, int bLen)
    {
        int crc = 0x00;
        if (b != null) {

            // adjust offset/length 
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);

            // calc CRC XOR-8
            for (int s = ofs; s < (ofs + len); s++) {
                crc = (crc ^ (int)b[s]) & 0xFF;
            }

        }
        return (byte)crc;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CRC Sum-8

    public static byte calcCrcSum8(byte b[])
    {
        if (!ListTools.isEmpty(b)) {
            return Checksum.calcCrcSum8(b, 0, b.length);
        } else {
            return (byte)0;
        }
    }

    public static byte calcCrcSum8(byte b[], int bOfs, int bLen)
    {
        int crc = 0x00;
        if (b != null) {

            // adjust offset/length 
            int ofs = (bOfs <= 0)? 0 : (bOfs >= b.length)? b.length : bOfs;
            int len = ((bLen >= 0) && (bLen <= (b.length-ofs)))? bLen : (b.length-ofs);

            // calc CRC XOR-8
            for (int s = ofs; s < (ofs + len); s++) {
                crc = (crc + (int)b[s]) & 0xFF;
            }

        }
        return (byte)crc;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* startup init */
    static {
        Checksum.initCrcCCITT();
      //Checksum.initCrc64_ECMA_182();
      //Checksum.initCrc64_ISO_3309();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final String ARG_DATA[]    = { "data"   , "d"  , "crc" };
    private static final String ARG_FILE[]    = { "file"   , "f"  };
    private static final String ARG_PRELOAD[] = { "preload", "pl" };

    public static void main(String args[])
    {
        RTConfig.setCommandLineArgs(args);
        String data    = RTConfig.getString(ARG_DATA,"");
        File   file    = RTConfig.getFile(ARG_FILE,null);
        int    preload = RTConfig.getInt(ARG_PRELOAD,0);

        /* get data bytes */
        byte dataB[] = null;
        if (!StringTools.isBlank(data)) {
            dataB = data.startsWith("0x")? StringTools.parseHex(data,null) : data.getBytes();
            Print.sysPrintln("ASCII           : " + StringTools.toStringValue(dataB,'.'));
            Print.sysPrintln("Hex             : 0x" + StringTools.toHexString(dataB));
        } else 
        if (file != null) {
            dataB = FileTools.readFile(file);
            Print.sysPrintln("File            : " + file + " [length " + ListTools.size(dataB) + "]");
        }

        /* nothing to CRC? */
        if (ListTools.isEmpty(dataB)) {
            Print.sysPrintln("ERROR: No data specified (missing '-data=' parameter)");
            System.exit(1);
        }

        /* loop through CRC algorithms */
        for (Checksum.CRC crc : Checksum.CRC.class.getEnumConstants()) {
            long crcV = Checksum.calcChecksum(crc, dataB, 0, -1) & crc.getBitMask();
            String label = StringTools.padRight(crc.toString(),' ',16);
            Print.sysPrintln(label + ": 0x" + StringTools.toHexString(crcV,crc.getBitLen()));
        }

        /* crcCCITT with custom preload */
        if (preload != 0x0000) {
            long crc = Checksum._calcCrcCCITT(preload, dataB, 0, -1);
            String PL = "0x" + StringTools.toHexString(preload,16);
            Print.sysPrintln("CCITT["+PL+"]   : 0x" + StringTools.toHexString(crc,16));
        }

        /* CRC-16 with custom preload */
        if (preload != 0x0000) {
            long crc = Checksum._calcCrc16(preload, dataB, 0, -1);
            String PL = "0x" + StringTools.toHexString(preload,16);
            Print.sysPrintln("CRC16["+PL+"]   : 0x" + StringTools.toHexString(crc,16));
        }

        /* CRC-16-Alt1 */
        {
            int crc16_1 = Checksum.calcCrc16_1(dataB);
            Print.sysPrintln("CRC16_1         : 0x" + StringTools.toHexString((long)crc16_1,16));
        }

        /* CRC-16-Alt2a */
        //{
        //    int crc16_2a = calcCrc16_2a(dataB);
        //    Print.sysPrintln("CRC 16(2a)    : 0x" + StringTools.toHexString((long)crc16_2a,16));
        //}

        /* CRC-16-Alt2b */
        //{
        //    int crc16_2b = calcCrc16_2b(dataB);
        //    Print.sysPrintln("CRC 16(2b)    : 0x" + StringTools.toHexString((long)crc16_2b,16));
        //}

        /* test */
        if (RTConfig.getBoolean("test",false)) {
            // -- 
            String T1 = "*TS01,863977030783505,050124061217,LBS:65535;65535;FFFF;FFFF;120,STT:21;8001,MGR:4689808,ADC:0;0.00;1;23.52;2;0.00,GFS:0;0,EGT:94411,EVT:1#";
            String T2 = "*TS01,863977030783505,050129061217,LBS:65535;65535;FFFF;FFFF;120,STT:21;8001,MGR:4689808,ADC:0;0.00;1;23.52;2;0.00,GFS:0;0,EGT:94411,EVT:1#";
            String T3 = "*TS01,863977030783505,050134061217,LBS:65535;65535;FFFF;FFFF;120,STT:21;8001,MGR:4689808,ADC:0;0.00;1;23.52;2;0.00,GFS:0;0,EGT:94411,EVT:1#";
            String T4 = "*TS01,863977030783505,050139061217,LBS:65535;65535;FFFF;FFFF;120,STT:21;8001,MGR:4689808,ADC:0;0.00;1;23.52;2;0.00,GFS:0;0,EGT:94411,EVT:1#"; // CCITT[0x0000] : 0xB218
            String T1234 = T1 + T2 + T3 + T4;
            int crc = Checksum.calcCrcXmodem(T1234.getBytes());
            Print.sysPrintln("CRC #1: " + StringTools.toHexString(crc,16));
            // --
            crc = Checksum.CRCCCITT_PRELOAD_0000;
            crc = Checksum._calcCrcCCITT(crc, T1.getBytes(), 0, -1); 
            crc = Checksum._calcCrcCCITT(crc, T2.getBytes(), 0, -1); 
            crc = Checksum._calcCrcCCITT(crc, T3.getBytes(), 0, -1); 
            crc = Checksum._calcCrcCCITT(crc, T4.getBytes(), 0, -1); 
            Print.sysPrintln("CRC #2: " + StringTools.toHexString(crc,16));
        }

    }

}
