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
// Change History:
//  2015/06/12  Martin D. Flynn
//     -Initial release
//  2020/02/19  Martin D. Flynn
//     -Added additional countries
// ----------------------------------------------------------------------------
package org.opengts.geocoder.country;

import java.util.*;

import org.opengts.util.*;

public class CountryCode
{

    // ------------------------------------------------------------------------

    public static final String SUBDIVISION_SEPARATOR    = "/";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // British Standard BS 6879 GB-3-letter codes
    //  - https://wikivisually.com/wiki/ISO_3166-2:GB

    private static String   GB_prefix_  = "GB-"; // (see "British Standard 6879")

    private static String   GB_BSCode[] = {
        "ENG",  // England          (GB/UK)
        "SCT",  // Scotland         (GB/UK)
        "WLS",  // Wales            (GB/UK)
        "NIR"   // Northern Ireland (   UK) province
    };

    public static boolean IsUK(String bscc)
    {
        String BSCC3 = StringTools.trim(bscc).toUpperCase();
        if (BSCC3.startsWith(GB_prefix_)) {
            // -- remove prefixing "GB-"
            BSCC3 = BSCC3.substring(GB_prefix_.length());
        }
        return ListTools.contains(GB_BSCode, BSCC3);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static HashMap<String,CountryInfo> GlobalCountryMap = new HashMap<String,CountryInfo>();

    /**
    *** CountryInfo class
    **/
    public static class CountryInfo
    {

        private String       code2    = null; // 2-letter code
        private String       code3    = null; // 3-letter code
        private String       phone    = null; // phone country dialing code
        private String       names[]  = null; // name
        private MethodAction sdMeth   = null; // subdivision name method

        public CountryInfo(String code2, String code3, String phone, String names[]) {
            this(code2, code3, phone, names, null, null);
        }

        public CountryInfo(String code2, String code3, String phone, String names[], Class<?> sdClass, String sdMeth) {
            this.code2   = code2; // may be blank
            this.code3   = code3; // never blank
            this.phone   = phone;
            this.names   = names;
            if (sdClass != null) {
                try {
                    this.sdMeth = new MethodAction(sdClass, sdMeth, String.class, String.class);
                } catch (Throwable th) {
                    Print.logException("Unable to create Subdivision lookup method ["+names[0]+"]",th);
                }
            }
            //String R = StringTools.replicateString(" ", 23-name.length());
            //Print.sysPrintln("new CountryInfo(\""+code2+"\", \""+code3+"\", \""+name+"\""+R+"),");
        }

        public String getCode2() {
            return this.code2; // may be blank
        }

        public String getCode3() {
            return this.code3; // never blank
        }

        public String getCode() {
            String cc2 = this.getCode2(); // may be blank
            String cc3 = this.getCode3(); // never blank
            if (!StringTools.isBlank(cc2)) {
                return cc3;
            } else 
            if (IsUK(cc3)) {
                return GB_prefix_ + cc3;
            } else {
                return cc3;
            }
        }

        public String getDialingCode() {
            return this.phone;
        }

        public String[] getNames() {
            return this.names;
        }

        public String getName() {
            return this.names[0];
        }
        public String getDescription() {
            return this.getName();
        }

        public boolean supportsSubdivisionName() {
            return (this.sdMeth != null)? true : false;
        }

        public String getSubdivisionName(String stateCode) {
            if ((this.sdMeth != null) && !StringTools.isBlank(stateCode)) {
                try {
                    return (String)this.sdMeth.invoke(stateCode,"");
                } catch (Throwable th) {
                    Print.logException("Unable to get Subdivision name ["+this.getDescription()+"]",th);
                }
            }
            return "";
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.getCode2());
            sb.append("/");
            sb.append(this.getCode3());
            sb.append(" ");
            sb.append(this.getDescription());
            return sb.toString();
        }

    }

    // ------------------------------------------------------------------------
    // References:
    //  - https://countrycode.org
    //  - https://www.iso.org/iso-3166-country-codes.html
    //  - https://www.iso.org/obp/ui/#search/code/
    //  - https://wikivisually.com/wiki/ISO_3166-2:GB
    // Notes:
    //  - "England", "Scotland", and "Wales", do not have their own 2-letter or 3-letter
    //    country codes (they share their actual codes with Great Britain - "GB"/"GBR").  
    //    Instead they are given their British Standard BS 6879 3-letter code as as their
    //    ISO_3166 country codes in this module. (fortunately these 3-letter codes do not
    //    clash with any existing ISO_3166 country code).
    //  - "Northern Ireland" (a province of UK, but may sometimes be referred to as a 
    //    country) also does not have its own 2-letter or 3-letter.  Instead it is given 
    //    the British Standard BS 6879 3-letter code as as its ISO_3166 country codes in 
    //    this module. (also fortunately does not clash with any existing ISO_3166 country 
    //    code.
    //  - Unicode conversion tools:
    //      https://r12a.github.io/app-conversion/     # -- uncheck Java "ES6"
    // ------------------------------------------------------------------------

    private static String[] A(String... C)
    {
        return C;
    }

    public static final CountryInfo CountryMapArray[] = new CountryInfo[] {
        // ISO-3166     Alpha Alpha  Phone    Name
        //              2-ltr 3-ltr  Code
        //              ----- ------ -------- -----------------------------------------------
        new CountryInfo("AF", "AFG",     "93", A("Afghanistan","Afghan*"                                        )),
        new CountryInfo("AL", "ALB",    "355", A("Albania"                                                      )),
        new CountryInfo("DZ", "DZA",    "213", A("Algeria","Algerie"                                            )),
        new CountryInfo("AD", "AND",    "376", A("Andorra"                                                      )),
        new CountryInfo("AO", "AGO",    "244", A("Angola"                                                       )),
        new CountryInfo("AI", "AIA",  "1-264", A("Anguilla"                                                     )),
        new CountryInfo("AQ", "ATA",    "672", A("Antarctica"                                                   )),
        new CountryInfo("AG", "ATG",  "1-268", A("Antigua/Barbuda","Antigua*","Barbuda"                         )),
        new CountryInfo("AR", "ARG",     "54", A("Argentina","Argentin*"                                        )),
        new CountryInfo("AM", "ARM",    "374", A("Armenia"                                                      )),
        new CountryInfo("AW", "ABW",    "297", A("Aruba"                                                        )),
        new CountryInfo("AU", "AUS",     "61", A("Australia"                                                    )),
        new CountryInfo("AT", "AUT",     "43", A("Austria","Osterreich","\u00D6sterreich"                       )),
        new CountryInfo("AZ", "AZE",    "994", A("Azerbaijan"                                                   )),
        new CountryInfo("BS", "BHS",  "1-242", A("Bahamas"                                                      )),
        new CountryInfo("BH", "BHR",    "973", A("Bahrain"                                                      )),
        new CountryInfo("BD", "BGD",    "880", A("Bangladesh"                                                   )),
        new CountryInfo("BB", "BRB",  "1-246", A("Barbados"                                                     )),
        new CountryInfo("BY", "BLR",    "375", A("Belarus"                                                      )),
        new CountryInfo("BE", "BEL",     "32", A("Belgium","Belgie","Belgi\u00EB","Belgique"                    )),
        new CountryInfo("BZ", "BLZ",    "501", A("Belize"                                                       )),
        new CountryInfo("BJ", "BEN",    "229", A("Benin"                                                        )),
        new CountryInfo("BM", "BMU",  "1-441", A("Bermuda"                                                      )),
        new CountryInfo("BT", "BTN",    "975", A("Bhutan"                                                       )),
        new CountryInfo("BO", "BOL",    "591", A("Bolivia"                                                      )),
        new CountryInfo("BQ", "BES",    "599", A("Bonaire"                                                      )),
        new CountryInfo("BA", "BIH",    "387", A("Bosnia/Herzegovina","Bosnia*","Herzegovina"                   )),
        new CountryInfo("BW", "BWA",    "267", A("Botswana"                                                     )),
        new CountryInfo("BR", "BRA",     "55", A("Brazil","Brasil"                                              )),
        new CountryInfo("BN", "BRN",    "673", A("Brunei Darussalam", "Brunei"                                  )),
        new CountryInfo("BG", "BGR",    "359", A("Bulgaria"                                                     )),
        new CountryInfo("BF", "BFA",    "226", A("Burkina Faso", "Burkina"                                      )),
        new CountryInfo("BI", "BDI",    "257", A("Burundi"                                                      )),
        new CountryInfo("KH", "KHM",    "855", A("Cambodia"                                                     )),
        new CountryInfo("CM", "CMR",    "237", A("Cameroon","Cameroun",                               "camaroon")),
        new CountryInfo("CA", "CAN",      "1", A("Canada"                                                       ), Canada.class, "getName"),
        new CountryInfo("CV", "CPV",    "238", A("Cape Verde"                                                   )),
        new CountryInfo("KY", "CYM",  "1-345", A("Cayman Islands","*Cayman*"                                    )),
        new CountryInfo("TD", "TCD",    "235", A("Chad"                                                         )),
        new CountryInfo("CL", "CHL",     "56", A("Chile"                                                        )),
        new CountryInfo("CN", "CHN",     "86", A("China"                                                        )),
        new CountryInfo("CO", "COL",     "57", A("Colombia",                                          "columbia")),
        new CountryInfo("KM", "COM",    "269", A("Comoros"                                                      )),
        new CountryInfo("CG", "COG",    "242", A("Republic of the Congo"                                        )),
        new CountryInfo("CD", "COD",    "243", A("Dem Rep of the Congo","DR Congo","DRC","DROC"                 )),
        new CountryInfo("CR", "CRI",    "506", A("Costa Rica"                                                   )),
        new CountryInfo("CU", "CUB",     "53", A("Cuba"                                                         )),
        new CountryInfo("HR", "HRV",    "385", A("Croatia","Croatie"                                            )),
        new CountryInfo("CW", "CUW",    "599", A("Curacao"                                                      )),
        new CountryInfo("CY", "CYP",    "357", A("Cyprus"                                                       )),
        new CountryInfo("CZ", "CZE",    "420", A("Czech Republic","Czech*"                                      )),
        new CountryInfo("CI", "CIV",    "225", A("Ivory Coast","Cote d'Ivoire","C\u00F4te d'Ivoire"             )),
        new CountryInfo("DK", "DNK",     "45", A("Denmark","Danmark"                                            )),
        new CountryInfo("DJ", "DJI",    "253", A("Djibouti"                                                     )),
        new CountryInfo("DM", "DMA",  "1-767", A("Dominica"                                                     )),
        new CountryInfo("DO", "DOM",  "1-809", A("Dominican Republic","Dominican*","Rep\u00FAblica Dominicana", "Republica Dominicana" )), // 1-809, 1-829, 1-849
        new CountryInfo("EC", "ECU",    "593", A("Ecuador"                                                      )),
        new CountryInfo("EG", "EGY",     "20", A("Egypt"                                                        )),
        new CountryInfo("SV", "SLV",    "503", A("El Salvador"                                                  )),
        new CountryInfo("ER", "ERI",    "291", A("Eritrea"                                                      )),
        new CountryInfo("EE", "EST",    "372", A("Estonia"                                                      )),
        new CountryInfo("ET", "ETH",    "251", A("Ethiopia"                                                     )),
        new CountryInfo("FJ", "FJI",    "679", A("Fiji"                                                         )),
        new CountryInfo("FI", "FIN",    "358", A("Finland"                                                      )),
        new CountryInfo("FR", "FRA",     "33", A("France"                                                       )),
        new CountryInfo("GA", "GAB",    "241", A("Gabon"                                                        )),
        new CountryInfo("GM", "GMB",    "220", A("Gambia"                                                       )),
        new CountryInfo("GE", "GEO",    "995", A("Georgia"                                                      )),
        new CountryInfo("DE", "DEU",     "49", A("Germany","Deutschland"                                        )),
        new CountryInfo("GH", "GHA",    "233", A("Ghana"                                                        )),
        new CountryInfo("GI", "GIB",    "350", A("Gibraltar"                                                    )),
        new CountryInfo("GR", "GRC",     "30", A("Greece"                                                       )),
        new CountryInfo("GL", "GRL",    "299", A("Greenland"                                                    )),
        new CountryInfo("GD", "GRD",  "1-473", A("Grenada"                                                      )),
        new CountryInfo("GP", "GLP",    "590", A("Guadeloupe"                                                   )),
        new CountryInfo("GU", "GUM",  "1-671", A("Guam"                                                         )),
        new CountryInfo("GT", "GTM",    "502", A("Guatemala"                                                    )),
        new CountryInfo("GG", "GGY","44-1481", A("Guernsey"                                                     )),
        new CountryInfo("GN", "GIN",    "224", A("Guinea"                                                       )),
        new CountryInfo("GY", "GUY",    "592", A("Guyana"                                                       )),
        new CountryInfo("HT", "HTI",    "509", A("Haiti"                                                        )),
        new CountryInfo("HN", "HND",    "504", A("Honduras"                                                     )),
        new CountryInfo("HK", "HKG",    "852", A("Hong Kong"                                                    )),
        new CountryInfo("HU", "HUN",     "36", A("Hungary"                                                      )),
        new CountryInfo("IS", "ISL",    "354", A("Iceland"                                                      )),
        new CountryInfo("IN", "IND",     "91", A("India","India*"                                               )),
        new CountryInfo("ID", "IDN",     "62", A("Indonesia","Indonesia*"                                       )),
        new CountryInfo("IR", "IRN",     "98", A("Iran"                                                         )),
        new CountryInfo("IQ", "IRQ",    "964", A("Iraq"                                                         )),
        new CountryInfo("IE", "IRL",    "353", A("Ireland",                                             "irland")),
        new CountryInfo("IM", "IMN","44-1624", A("Isle of Man"                                                  )),
        new CountryInfo("IL", "ISR",    "972", A("Israel"                                                       )),
        new CountryInfo("IT", "ITA",     "39", A("Italy","Italia"                                               )),
        new CountryInfo("JM", "JAM",  "1-876", A("Jamaica"                                                      )),
        new CountryInfo("JP", "JPN",     "81", A("Japan"                                                        )),
        new CountryInfo("JE", "JEY","44-1534", A("Jersey"                                                       )),
        new CountryInfo("JO", "JOR",    "962", A("Jordan"                                                       )),
        new CountryInfo("KZ", "KAZ",      "7", A("Kazakhstan"                                                   )),
        new CountryInfo("KE", "KEN",    "254", A("Kenya"                                                        )),
        new CountryInfo("KI", "KIR",    "686", A("Kiribati"                                                     )),
        new CountryInfo("XK", "XKX",    "383", A("Kosovo"                                                       )),
        new CountryInfo("KR", "KOR",     "82", A("South Korea","Korea"                                          )),
        new CountryInfo("KP", "PRK",    "850", A("North Korea"                                                  )),
        new CountryInfo("KW", "KWT",    "965", A("Kuwait"                                                       )),
        new CountryInfo("KG", "KGZ",    "996", A("Kyrgyzstan"                                                   )),
        new CountryInfo("LA", "LAO",    "856", A("Laos"                                                         )),
        new CountryInfo("LV", "LVA",    "371", A("Latvia"                                                       )),
        new CountryInfo("LB", "LBN",    "961", A("Lebanon"                                                      )),
        new CountryInfo("LS", "LSO",    "266", A("Lesotho"                                                      )),
        new CountryInfo("LR", "LBR",    "231", A("Liberia"                                                      )),
        new CountryInfo("LY", "LBY",    "218", A("Libya"                                                        )),
        new CountryInfo("LI", "LIE",    "423", A("Liechtenstein"                                                )),
        new CountryInfo("LT", "LTU",    "370", A("Lithuania"                                                    )),
        new CountryInfo("LU", "LUX",    "352", A("Luxembourg"                                                   )),
        new CountryInfo("MO", "MAC",    "853", A("Macao"                                                        )),
        new CountryInfo("MK", "MKD",    "389", A("Macedonia"                                                    )),
        new CountryInfo("MG", "MDG",    "261", A("Madagascar"                                                   )),
        new CountryInfo("MW", "MWI",    "265", A("Malawi"                                                       )),
        new CountryInfo("MY", "MYS",     "60", A("Malaysia"                                                     )),
        new CountryInfo("MV", "MDV",    "960", A("Maldives"                                                     )),
        new CountryInfo("ML", "MLI",    "223", A("Mali"                                                         )),
        new CountryInfo("MT", "MLT",    "356", A("Malta"                                                        )),
        new CountryInfo("MH", "MHL",    "692", A("Marshall Islands"                                             )),
        new CountryInfo("MQ", "MTQ",    "596", A("Martinique"                                                   )),
        new CountryInfo("MR", "MRT",    "222", A("Mauritania"                                                   )),
        new CountryInfo("MU", "MUS",    "230", A("Mauritius"                                                    )),
        new CountryInfo("YT", "MYT",    "262", A("Mayotte"                                                      )),
        new CountryInfo("MX", "MEX",     "52", A("Mexico","M\u00E9xico"                                         ), Mexico.class, "getName"),
        new CountryInfo("FM", "FSM",    "691", A("Micronesia"                                                   )),
        new CountryInfo("MD", "MDA",    "373", A("Moldova"                                                      )),
        new CountryInfo("MC", "MCO",    "377", A("Monaco"                                                       )),
        new CountryInfo("MN", "MNG",    "976", A("Mongolia"                                                     )),
        new CountryInfo("ME", "MNE",    "382", A("Montenegro"                                                   )),
        new CountryInfo("MS", "MSR",  "1-664", A("Montserrat"                                                   )),
        new CountryInfo("MA", "MAR",    "212", A("Morocco","Maroc",                                    "morroco")),
        new CountryInfo("MZ", "MOZ",    "258", A("Mozambique"                                                   )),
        new CountryInfo("MM", "MMR",     "95", A("Myanmar","Burma"                                              )),
        new CountryInfo("NA", "NAM",    "264", A("Namibia"                                                      )),
        new CountryInfo("NR", "NRU",    "674", A("Nauru"                                                        )),
        new CountryInfo("NP", "NPL",    "977", A("Nepal"                                                        )),
        new CountryInfo("NL", "NLD",     "31", A("Netherlands","Nederland","*Netherlands"                       )),
        new CountryInfo("NC", "NCL",    "687", A("New Caledonia"                                                )),
        new CountryInfo("NZ", "NZL",     "64", A("New Zealand"                                                  )),
        new CountryInfo("NI", "NIC",    "505", A("Nicaragua"                                                    )),
        new CountryInfo("NE", "NER",    "227", A("Niger"                                                        )),
        new CountryInfo("NG", "NGA",    "234", A("Nigeria"                                                      )),
        new CountryInfo("NU", "NIU",    "683", A("Niue"                                                         )),
        new CountryInfo("NF", "NFK",    "672", A("Norfolk Island"                                               )),
        new CountryInfo("NO", "NOR",     "47", A("Norway","Norge"                                               )),
        new CountryInfo("OM", "OMN",    "968", A("Oman"                                                         )),
        new CountryInfo("PG", "PNG",    "675", A("Papua New Guinea"                                             )),
        new CountryInfo("PK", "PAK",     "92", A("Pakistan"                                                     )),
        new CountryInfo("PW", "PLW",    "680", A("Palau"                                                        )),
        new CountryInfo("PS", "PSE",    "970", A("Palestine"                                                    )),
        new CountryInfo("PA", "PAN",    "507", A("Panama","Panam\u00E1"                                         )),
        new CountryInfo("PY", "PRY",    "595", A("Paraguay"                                                     )),
        new CountryInfo("PE", "PER",     "51", A("Peru","Per\u00FA"                                             )),
        new CountryInfo("PH", "PHL",     "63", A("Philippines","Phillippines"                                   )),
        new CountryInfo("PN", "PCN",     "64", A("Pitcairn"                                                     )),
        new CountryInfo("PL", "POL",     "48", A("Poland","Polska"                                              )),
        new CountryInfo("PT", "PRT",    "351", A("Portugal"                                                     )),
        new CountryInfo("PR", "PRI",  "1-787", A("Puerto Rico"                                                  )), // 1-787, 1-939
        new CountryInfo("QA", "QAT",    "974", A("Qatar"                                                        )),
        new CountryInfo("RE", "REU",    "262", A("Reunion"                                                      )),
        new CountryInfo("RO", "ROU",     "40", A("Romania","Rom\u00C2nia"                                       )),
        new CountryInfo("RU", "RUS",      "7", A("Russia","Russia*","CCCP"                                      )),
        new CountryInfo("RW", "RWA",    "250", A("Rwanda"                                                       )),
        new CountryInfo("LC", "LCA",  "1-758", A("Saint Lucia"                                                  )),
        new CountryInfo("MF", "MAF",  "1-784", A("Saint Martin"                                                 )), // (French)
        new CountryInfo("WS", "WSM",    "685", A("Samoa"                                                        )),
        new CountryInfo("SM", "SMR",    "378", A("San Marino"                                                   )),
        new CountryInfo("ST", "STP",    "239", A("Sao Tome and Principe"                                        )),
        new CountryInfo("SA", "SAU",    "966", A("Saudi Arabia","*Arabia","Saudi*"                              )),
        new CountryInfo("SN", "SEN",    "221", A("Senegal","S\u00E9n\u00E9gal"                                  )),
        new CountryInfo("RS", "SRB",    "381", A("Serbia"                                                       )),
        new CountryInfo("SC", "SYC",    "248", A("Seychelles"                                                   )),
        new CountryInfo("SL", "SLE",    "232", A("Sierra Leone"                                                 )),
        new CountryInfo("SG", "SGP",     "65", A("Singapore"                                                    )),
        new CountryInfo("SX", "SXM",  "1-721", A("Sint Maarten"                                                 )), // (Dutch)
        new CountryInfo("SK", "SVK",    "421", A("Slovakia"                                                     )),
        new CountryInfo("SI", "SVN",    "386", A("Slovenia","Slovenija","Sloveni*"                              )),
        new CountryInfo("SO", "SOM",    "252", A("Somalia","Somali*"                                            )),
        new CountryInfo("ZA", "ZAF",     "27", A("South Africa","*South Africa*"                                )),
        new CountryInfo("ES", "ESP",     "34", A("Spain","Espana","Espa\u00F1a"                                 )),
        new CountryInfo("LK", "LKA",     "94", A("Sri Lanka","SriLanka"                                         )),
        new CountryInfo("SD", "SDN",    "249", A("Sudan"                                                        )),
        new CountryInfo("SR", "SUR",    "597", A("Suriname"                                                     )),
        new CountryInfo("SZ", "SWZ",    "268", A("Swaziland"                                                    )),
        new CountryInfo("SE", "SWE",     "46", A("Sweden",                                             "sweeden")),
        new CountryInfo("CH", "CHE",     "41", A("Switzerland"                                                  )),
        new CountryInfo("SY", "SYR",    "963", A("Syria"                                                        )),
        new CountryInfo("TW", "TWN",    "886", A("Taiwan"                                                       )),
        new CountryInfo("TJ", "TJK",    "992", A("Tajikistan"                                                   )),
        new CountryInfo("TZ", "TZA",    "255", A("Tanzania"                                                     )),
        new CountryInfo("TH", "THA",     "66", A("Thailand"                                                     )),
        new CountryInfo("TL", "TLS",    "670", A("Timor-Leste","*Timor*"                                        )), // East Timor
        new CountryInfo("TG", "TGO",    "228", A("Togo"                                                         )),
        new CountryInfo("TK", "TKL",    "690", A("Tokelau"                                                      )),
        new CountryInfo("TO", "TON",    "676", A("Tonga"                                                        )),
        new CountryInfo("TT", "TTO",  "1-868", A("Trinidad/Tobago","Trinidad*","Tobago"                         )),
        new CountryInfo("TN", "TUN",    "216", A("Tunisia","Tunisie"                                            )),
        new CountryInfo("TR", "TUR",     "90", A("Turkey","Turkiye","T\u00FCrkiye"                              )),
        new CountryInfo("TM", "TKM",    "993", A("Turkmenistan"                                                 )),
        new CountryInfo("TV", "TUV",    "688", A("Tuvalu"                                                       )),
        new CountryInfo("UG", "UGA",    "256", A("Uganda"                                                       )),
        new CountryInfo("UA", "UKR",    "380", A("Ukraine","Ukrain"                                             )),
        new CountryInfo("AE", "ARE",    "971", A("United Arab Emirates","UAE"                                   )),
        new CountryInfo("GB", "GBR",     "44", A("United Kingdom","UK","Great Britain","GB-GBN","GB-UKM"        )), // England, Wales, Scotland
        new CountryInfo(""  , "ENG",     "44", A("England",                                             "london")), // "GB-ENG"
        new CountryInfo(""  , "SCT",     "44", A("Scotland"                                                     )), // "GB-SCT"
        new CountryInfo(""  , "WLS",     "44", A("Wales","Cymru","Cymry","Kymry"                                )), // "GB-WLS"
        new CountryInfo(""  , "NIR",     "44", A("Northern Ireland"                                             )), // "GB-NIR" province
        new CountryInfo("US", "USA",      "1", A("United States","United States*","Estados Unidos","U.S."       ), USState.class, "getName"),
        new CountryInfo("UY", "URY",    "598", A("Uruguay"                                                      )),
        new CountryInfo("UZ", "UZB",    "998", A("Uzbekistan"                                                   )),
        new CountryInfo("VU", "VUT",    "678", A("Vanuatu"                                                      )),
        new CountryInfo("VA", "VAT",    "379", A("Vatican"                                                      )),
        new CountryInfo("VE", "VEN",     "58", A("Venezuela"                                                    )),
        new CountryInfo("VN", "VNM",     "84", A("Viet Nam","VietNam"                                           )),
        new CountryInfo("VG", "VGB",  "1-284", A("British Virgin Islands"                                       )),
        new CountryInfo("VI", "VIR",  "1-340", A("US Virgin Islands","Virgin Islands"                           )),
        new CountryInfo("YE", "YEM",    "967", A("Yemen"                                                        )),
        new CountryInfo("ZM", "ZMB",    "260", A("Zambia"                                                       )),
        new CountryInfo("ZW", "ZWE",    "263", A("Zimbabwe"                                                     )),
    };

    // -- startup initialization
    static {
        for (int i = 0; i < CountryMapArray.length; i++) {
            // -- add CODE-2
            String cc2 = CountryMapArray[i].getCode2(); // may be blank (uppercase)
            if (!StringTools.isBlank(cc2)) {
                if (GlobalCountryMap.containsKey(cc2)) {
                    Print.logError("GlobalCountryMap already contains 2-letter country code: " + cc2);
                }
                GlobalCountryMap.put(cc2, CountryMapArray[i]);
            }
            // -- add CODE-3
            String cc3 = CountryMapArray[i].getCode3(); // never blank (uppercase)
            if (!StringTools.isBlank(cc3)) {
                if (!StringTools.isBlank(cc2)) {
                    // -- add 3-letter code
                    GlobalCountryMap.put(cc3, CountryMapArray[i]);
                } else
                if (IsUK(cc3)) {
                    // -- add 3-letter code (assuming that this doesn't clash with an existing country code)
                    if (GlobalCountryMap.containsKey(cc3)) {
                        Print.logError("GlobalCountryMap already contains 3-letter country code: " + cc3);
                    }
                    GlobalCountryMap.put(cc3  , CountryMapArray[i]);
                    // -- add GB-3-letter code
                    String bscc3 = GB_prefix_ + cc3;
                    if (GlobalCountryMap.containsKey(bscc3)) {
                        Print.logError("GlobalCountryMap already contains GB:3-letter country code: " + bscc3);
                    }
                    GlobalCountryMap.put(bscc3, CountryMapArray[i]);
                }
            }
            // -- add NAMEs
            String names[] = CountryMapArray[i].getNames();
            for (String n : names) {
                if (!StringTools.isBlank(n) && !n.startsWith("*") && !n.endsWith("*")) {
                    String ucn = n.toUpperCase();
                    GlobalCountryMap.put(ucn, CountryMapArray[i]);
                }
            }
        }
    }

    /**
    *** Gets the collection of StateInfo keys (state codes)
    **/
    public static Collection<String> getCountryInfoKeys()
    {
        return GlobalCountryMap.keySet();
    }

    /**
    *** Gets the CountryInfo instance for the specified country code
    **/
    public static CountryInfo getCountryInfo(String code)
    {
        if (!StringTools.isBlank(code)) {
            code = code.toUpperCase();
            // -- check codes
            CountryInfo CI = GlobalCountryMap.get(code);
            if (CI != null) {
                // -- found code
                return CI;
            }
            // -- search through names/phone
            for (CountryInfo _ci : CountryMapArray) {
                // -- phone
                if (code.equals(_ci.getDialingCode())) {
                    return _ci;
                }
                // -- names
                for (String n : _ci.getNames()) {
                    n = n.toUpperCase();
                    boolean s = n.startsWith("*");  // "*NAME"
                    boolean e = n.endsWith("*");    // "NAME*"
                    if (s & e) {
                        // -- "*NAME*"
                        if (code.indexOf(n.substring(1,n.length()-1)) >= 0) {
                            return _ci;
                        }
                    } else
                    if (s) {
                        // -- "*NAME"
                        if (code.endsWith(n.substring(1))) {
                            return _ci;
                        }
                    } else 
                    if (e) {
                        // -- "NAME*"
                        if (code.startsWith(n.substring(0,n.length()-1))) {
                            return _ci;
                        }
                    } else {
                        // -- "NAME"
                        if (code.equals(n)) {
                            return _ci;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
    *** Returns true if the specified country code exists
    **/
    public static boolean hasCountryInfo(String code)
    {
        return (CountryCode.getCountryInfo(code) != null)? true : false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified country code is defined
    *** @param code  The country code
    *** @return True if teh specified country is defined, false otherwise
    **/
    public static boolean isCountryCode(String code)
    {
        return CountryCode.hasCountryInfo(code);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the country name for the specified country code
    *** @param code  The country code
    *** @return The country name, or an empty String if the country code was not found
    **/
    public static String getCountryName(String code)
    {
        CountryInfo ci = CountryCode.getCountryInfo(code);
        return (ci != null)? ci.getDescription() : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the subdivision name for the specified CountryCode/StateCode
    **/
    public static String getSubdivisionName(String subDiv)
    {
        if (!StringTools.isBlank(subDiv)) {
            int p = subDiv.indexOf(SUBDIVISION_SEPARATOR);
            if (p >= 0) {
                String CC = subDiv.substring(0,p);
                String SC = subDiv.substring(p+1);
                return CountryCode.getSubdivisionName(CC,SC);
            }
        }
        return "";
    }

    /**
    *** Gets the subdivision name for the specified CountryCode/StateCode
    **/
    public static String getSubdivisionName(String countryCode, String stateCode)
    {
        if (!StringTools.isBlank(countryCode) && !StringTools.isBlank(stateCode)) {
            CountryInfo ci = CountryCode.getCountryInfo(countryCode);
            if ((ci != null) && ci.supportsSubdivisionName()) {
                return ci.getSubdivisionName(stateCode);
            }
        }
        return "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the 2-digit country code for the specified code
    *** @param code  The 2 or 3-digit country code
    *** @param dft   The default code to return if the specified country code is not found
    *** @return The state code
    **/
    public static String getCountryCode(String code, String dft)
    {
        CountryInfo ci = CountryCode.getCountryInfo(code);
        return (ci != null)? ci.getCode() : dft;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

        /* get code */
        String code = RTConfig.getString("code",null);
        if (StringTools.isBlank(code)) {
            Print.sysPrintln("'-code=XXX' not specified");
            System.exit(1);
        }

        /* display country matching code */
        CountryInfo CI = CountryCode.getCountryInfo(code);
        if (CI != null) {
            Print.sysPrintln(CI.getCode2()+"/"+CI.getCode3() + "[" + CI.getCode() + "] " + CI.getDescription());
        } else {
            Print.sysPrintln("Not found ...");
        }
        System.exit(0);

    }

}

