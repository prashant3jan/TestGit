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
//  JSON data format support.
//  This version includes support for the following additional non-standard features:
//      - Commented areas can be specified within /* ... */
//      - Hexadecimal numeric value types are supported (ie. 0xABCD)
//  The following data-types/primitive-types are supported:
//      - Boolean
//      - Long (Integer, Short, Byte)
//      - Double (Float)
//      - JSON._Array
//      - JSON._Object (JSON.JSONBean)
// ----------------------------------------------------------------------------
// Change History:
//  2011/07/15  Martin D. Flynn
//     -Initial release
//  2011/08/21  Martin D. Flynn
//     -Fixed JSON parsing.
//  2011/10/03  Martin D. Flynn
//     -Added multiple-name lookup support
//  2013/03/01  Martin D. Flynn
//     -Added 'null' object support
//  2013/04/08  Martin D. Flynn
//     -Handle parsing of arrays within arrays
//  2013/08/06  Martin D. Flynn
//     -Added "JSONParsingContext" for easier debugging of syntax errors
//     -Added support for "/*...*/" comments (NOTE: this is a non-standard feature
//      which is NOT supported by other JSON parsers, including JavaScript).
//  2013/11/11  Martin D. Flynn
//     -Added additional overflow checking.
//  2014/09/25  Martin D. Flynn
//     -Added "toString(boolean inclPrefix)" to JSON object.
//  2016/09/01  Martin D. Flynn
//     -Added support to "parse_Number" for parsing hex integers (non-standard)
//  2017/02/05  Martin D. Flynn
//     -Added reading JSON object from URL (se "ReadJSON")
//     -Added JSON object path traversal (see "GetValueForPath")
//     -Disallow control characters in String values (ie. embedded '\n', etc)
//     -Escape special key String characters when displaying JSON
//  2018/09/10  Martin D. Flynn
//     -Added JSONBean support [see "JSONBean"]
//     -Added support for converting Properties into JSON objects.
//     -Added support for converting Collections into JSON arrays.
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.lang.Integer;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;

import java.net.URL;
import java.net.MalformedURLException;

public class JSON
{

    public static final boolean ALLOW_OBJECT_DUPLICATES = false;
    public static final String  PROPERTY_MAP_SEP        = ".";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- This JSON implementation supports encoding/serializing a JavaBean into 
    // -  a JSONBean.  The process for supporting a JSONBean within an class
    // -  definition is as follows:
    // -    1) Add the following interface implementation to the class:
    // -          public class SomeClass
    // -              implements JSON.JSONBean
    // -       This is used as a hint to the JsonBean parser that it will have 
    // -       annotations for converting a JavaBean value into a JSON key/value pair.
    // -    2) For every getter method in the class, for which you want a JSON 
    // -       key/value entry in the resulting JSON, add the following annotation:
    // -          @JSONBeanGetter()
    // -          public String getSomeValue() {
    // -              return this.someValue;
    // -          }
    // -       The above would return the following example JSON key/value:
    // -          ...
    // -          "someValue" : "This is Sample text",
    // -          ...
    // -       If the getter returns an object instance which also implements the 
    // -       JSON.JSONBean interface, it will also be converted to a JSON object 
    // -       and assigned to the getter name in the main JSON object.
    // -    3) The "@JSONBeanGetter" annotation supports several optional parameters:
    // -       a) (name="SomeName") 
    // -         This option allows overriding the JSON key in the key/value pair. 
    // -         For example, specifying name="SomeName" will change the JSON key/value 
    // -         pair to the following:
    // -           ...
    // -           "SomeName" : "This is Sample text",
    // -           ...
    // -       b) (ignore="$blank")
    // -         This option allows omitting/ignoring JSON key/value entries if the 
    // -         resulting value is blank/null.  For example, if "getSampleValue()" 
    // -         returns null/blank, the JSON key/value entry would be omitted.
    // -         Other possible "ignore" values are:
    // -           "$null" - omit if the return value is null
    // -           "==0" - omit if the numeric return value is equal-to zero.
    // -           "<=0" - omit if the numeric return value is less-than-or-equal-to zero.
    // -           "<0" - omit if the numeric return value is less-than zero.
    // -           ">=0" - omit if the numeric return value is greater-than-or-equal-to zero.
    // -           ">0" - omit if the numeric return value is greater-than zero.
    // -           "!=0" - omit if the numeric return value is not-equal-to zero.
    // -         The "0" used in the above ignore parameter is just an example, it 
    // -         can be any numeric value.
    // -       c) (tags="maint,odom,fuel")
    // -         This option allows the JSON bean output process to search and output 
    // -         only selected JSON key/value pairs.  The tagID specified on the 
    // -         "new JSON._Object(SomeObject,filter)" allows selecting only those 
    // -         @JSONBeanGetter" methods that match the specified tag.
    // -       d) (expandArray="true")
    // -         This option allows specifying whether a returned array type is converted 
    // -         to JSON as an array, or whether the array is expanded into separate JSON 
    // -         key/value pairs.  For example assume the following getter:
    // -           @JSONBeanGetter()
    // -           public String[] getSomeArray() {
    // -               return new String[] { "A", "B", "C" };
    // -           }
    // -         When left as the default, this would be converted to JSON like the following:
    // -           "someArray" : [ "A", "B", "C" ],
    // -         If the annotation was changed to @JSONBeanGetter(expandArray="true"), 
    // -         the following JSON would be returned:
    // -           "someArray0" : "A",
    // -           "someArray1" : "B",
    // -           "someArray2" : "C",

    // --------------------------------

    /**
    *** Interface used to indicate that the implementing object can be converted 
    *** to a JSON bean object.
    **/
    public interface JSONBean
    {
        // -- Nothing needed here
        // -    The class just needs to implement this interface in order to tag it
        // -    as a target for converting to a JSON object.
    }

    /**
    *** Class used to wrap a non-JSONBean object inside a JSOBBean object.
    *** To allow creating a JsonBean out of the getter methods of any object.
    **/
    public static class JSONBeanWrap
        implements JSONBean
    {
        private Object javaBean = null;
        public JSONBeanWrap(Object jb) {
            if (jb instanceof JSON.JSONBeanWrap) {
                //Print.logWarn("Object is already a JSONBeanWrap");
                this.javaBean = ((JSON.JSONBeanWrap)jb).getJavaBean();
            } else {
                this.javaBean = jb;
            }
        }
        // ----------------------------
        public boolean isScalar() {
            Object jb = this.getJavaBean();
            return JSON.IsScalarValue(jb);
        }
        public Object getJavaBean() {
            // -- this returns the instance to be converted to a JSON object
            Object jb = this.javaBean;
            while (jb instanceof JSON.JSONBeanWrap) {
                // -- a JSONBeanWrap was wrapped in this JSONBeanWrap
                jb = ((JSON.JSONBeanWrap)jb).getJavaBean();
            }
            return jb;
        }
        // ----------------------------
        public boolean isArray() {
            // -- returns true if the contained object is an array
            Object jb = this.getJavaBean();
            return ((jb != null) && jb.getClass().isArray())? true : false;
        }
        public JSON.JSONBean[] getArray() {
            if (!this.isArray()) {
                // -- not an array
                return null;
            } else {
                // -- wrap each array element in a JSONBeanWrap
                Object jb = this.getJavaBean();
                try {
                    int size = Array.getLength(jb);
                    JSON.JSONBean jbArr[] = new JSON.JSONBean[size];
                    for (int i = 0; i < size; i++) {
                        Object v = Array.get(jb,i);
                        if (v instanceof JSON.JSONBean) {
                            // -- array element is already a JSONBean
                            jbArr[i] = (JSON.JSONBean)v;
                        } else {
                            // -- wrap non-JSONBean object in a JSONBeanWrap
                            jbArr[i] = new JSON.JSONBeanWrap(v);
                        }
                    }
                    return jbArr;
                } catch (Throwable th) { // IllegalArgumentException, ArrayIndexOutOfBoundsException
                    Print.logException("Error converting object to Array: " + StringTools.className(jb), th);
                    return null;
                }
            }
        }
        // ----------------------------
        public boolean isCollection() {
            // -- returns true if the contained object is a Collection
            Object jb = this.getJavaBean();
            return ((jb != null) && (jb instanceof Collection))? true : false;
        }
        public Collection<JSON.JSONBean> getCollection() {
            if (!this.isCollection()) {
                // -- not a Collection
                return null;
            } else {
                // -- wrap each Collection element in a JSONBeanWrap
                Object jb = this.getJavaBean();
                try {
                    Collection<?> objList = (Collection<?>)jb;
                    Collection<JSON.JSONBean> jbList = new Vector<JSON.JSONBean>();
                    for (Object v : objList) {
                        if (v instanceof JSON.JSONBean) {
                            jbList.add((JSON.JSONBean)v);
                        } else {
                            jbList.add(new JSON.JSONBeanWrap(v));
                        }
                    }
                    return jbList;
                } catch (Throwable th) { // ClassCastException
                    Print.logException("Error converting object to Collection: " + StringTools.className(jb), th);
                    return null;
                }
            }
        }
    }

    /**
    *** Wraps a non-JSONBean object inside a JSONBean object.<br/>
    *** The following are equivalent:<br/>
    ***     <code>JSON.JSONBean jb1 = new JSON.JSONBeanWrap(obj);</code><br/>
    ***     <code>JSON.JSONBean jb2 = JSON.JSONBeanWrap(obj);</code>
    **/
    public static JSON.JSONBean JSONBeanWrap(Object jb)
    {
        if (jb instanceof JSON.JSONBeanWrap) {
            return (JSON.JSONBeanWrap)jb;
        } else {
            return new JSON.JSONBeanWrap(jb);
        }
    }

    // --------------------------------

    /**
    *** Annotation for JSONBean object 'getters'
    **/
    @Retention(RetentionPolicy.RUNTIME)
    public @interface JSONBeanGetter
    {
        // @JSONBeanGetter(
        //      name="myName",      // name used for the JSON key
        //      ignore="$blank",    // ignore entry if value is blank, etc
        //      tags="maint,odom",  // allows including only selected 'tag' items
        //      expandArray="true", // if value is an array, each element will become a key/value pair
        //      enumClass="..."     // will attempt to convert the value into the Enum element name
        //      unitsEnum="..."     // the units enum for the current value
        //      arg=null            // String arg to use if method takes a single argument
        // )
        String name()        default "";
        String type()        default ""; // not used
        String expandArray() default "false";
        String ignore()      default IGNORE_NONE;
        String tags()        default "";
        String enumClass()   default "";
        String unitsEnum()   default "";
        String arg()         default "${null}";
    }

    /**
    *** Annotation for JSONBean object 'setters'
    *** (not yet fully implemented)
    **/
    @Retention(RetentionPolicy.RUNTIME)
    public @interface JSONBeanSetter
    {
        String name()        default "";
        String unitsEnum()   default "";
    }

    // ------------------------------------------------------------------------

    public  static final String IGNORE_NONE     = "$none";
    public  static final String IGNORE_NULL     = "$null";
    public  static final String IGNORE_BLANK    = "$blank";
    public  static final String IGNORE_EMPTY    = "$empty";
    public  static final String IGNORE_ZERO     = "$zero"; 

    /**
    *** JSONBean filter used to select specific methods to include in the JSONBean output 
    **/
    public static class JSONBeanFilter // JSONBeanSelector
    {
        private Set<String>                 tags          = null; // OrderedSet
        private Set<Class<?>>               interfClasses = null;
        private Set<String>                 keyNames      = null;
        private Comparator<JSON._KeyValue>  comparator    = null;
        // ----------------------------
        public JSONBeanFilter() {
            super();
        }
        public JSONBeanFilter(String tags[], Class<?> interfClasses[]) {
            this.setTags(tags);
            this.setInterfaces(interfClasses);
        }
        public JSONBeanFilter(String... tags) {
            this.setTags(tags);
        }
        public JSONBeanFilter(Class<?>... interfClasses) {
            this.setInterfaces(interfClasses);
        }
        public JSONBeanFilter(Comparator<JSON._KeyValue>  comp) {
            this.setComparator(comp);
        }
        // ----------------------------
        public JSON.JSONBeanFilter setInterfaces(String interfClassesCSV) {
            if (StringTools.isBlank(interfClassesCSV)) {
                this.interfClasses = null; // no interface filter
            } else {
                Set<Class<?>> icSet = new HashSet<Class<?>>();
                for (String ics : StringTools.split(interfClassesCSV,',')) {
                    // -- get interface class
                    Class<?> iClass;
                    try {
                        iClass = Class.forName(ics);
                    } catch (Throwable th) { // ClassNotFoundException
                        Print.logWarn("Class not found: " + ics);
                        iClass = null; // class not found
                    }
                    // -- filter/save interface class
                    if (iClass == null) {
                        continue;
                    } else
                    if (!iClass.isInterface()) {
                        // -- omit non-interface classes here
                        //continue; <-- comment to allow super-classes
                    }
                    icSet.add(iClass);
                }
                this.interfClasses = !ListTools.isEmpty(icSet)? icSet : null;
            }
            return this;
        }
        public JSON.JSONBeanFilter setInterfaces(Class<?>... interfClasses) {
            // -- IF an encountered object implements one of these interfaces, then
            // -  only those 'getters' in the interface (and are annotated with
            // -  @JSONBeanGetter) will be converted to the resulting JSON object.
            // -  Encountered objects which do not implement any of the interfaces
            // -  will be converted without regard to an interface.
            // -- Superclasses may also be specified to limit the JsonBean conversion
            // -  to only those 'getter' methods present in the superclass.
            if (interfClasses == null) {
                this.interfClasses = null;
            } else {
                Set<Class<?>> icSet = new HashSet<Class<?>>();
                for (Class<?> iClass : interfClasses) {
                    // -- filter/save interface class
                    if (iClass == null) {
                        continue;
                    } else
                    if (!iClass.isInterface()) {
                        // -- omit non-interface classes here
                        //continue; <-- comment to allow super-classes
                    }
                    icSet.add(iClass);
                }
                this.interfClasses = !ListTools.isEmpty(icSet)? icSet : null;
            }
            return this;
        }
        public Set<Class<?>> getInterfaces() {
            return this.interfClasses; // may be null
        }
        public String getInterfacesString() {
            // -- Debug purpose only: gets a comma-separated list of interfaces
            return StringTools.join(this.interfClasses,",");
        }
        public Set<Class<?>> getMatchingInterfaces(Class<?> mainClass) {
            if (this.interfClasses == null) {
                // -- filter does not care about interfaces
                return null;
            } else {
                // -- return all matching interfaces (may be empty)
                Set<Class<?>> classSet = new HashSet<Class<?>>();
                if (mainClass != null) {
                    Class<?> intrf[] = mainClass.getInterfaces();
                    if (!ListTools.isEmpty(intrf)) {
                        // -- iterate through main class interfaces, looking for a match
                        for (Class<?> Fi : this.interfClasses) {
                            if (Fi == null) {
                                // -- skip nulls (will not occur since nulls have been removed)
                            } else
                            if (Fi.isAssignableFrom(mainClass)) {
                                // -- 'Fi' is a super class/interface of mainClass ie. "(Fi)mainClass"
                                classSet.add(Fi);
                            }
                            /*
                            if (mainClass.equals(Fi)) {
                                // -- main class was listed in the filter
                                classSet.add(Fi);
                            } else {
                                // -- check all main class interfaces
                                for (Class<?> Mi : intrf) {
                                    if ((Mi != null) && Mi.equals(Fi)) {
                                        classSet.add(Fi);
                                        break;
                                    }
                                }
                            }
                            */
                        }
                    }
                }
                return classSet;
            }
        }
        public boolean interfaceContainsMethod(Set<Class<?>> interfaceSet, Method mainMeth) {
            if (mainMeth == null) {
                // -- main method was not specified
                return false;
            } else
            if (interfaceSet == null) { // also (this.interfClasses == null)
                // -- filter does not care about interfaces, thus always match
                return true;
            } else
            if (ListTools.isEmpty(interfaceSet)) {
                // -- no interface match found, thus the method can't be in in any of them
                return false;
            } else {
                // -- find matching method in any listed interface
                for (Class<?> I : interfaceSet) { // for each interface
                    for (Method M : I.getMethods()) { // for each method
                        if (!M.getName().equals(mainMeth.getName())) {
                            // -- method name mismatch
                            continue;
                        } else
                        if (!Arrays.equals(M.getParameterTypes(),mainMeth.getParameterTypes())) {
                            // -- parameter list mismatch
                            continue;
                        } else {
                            // -- they are equal, found at-least one match
                            return true;
                        }
                    }
                }
                // -- no matches found
                return false;
            }
        }
        // ----------------------------
        public JSON.JSONBeanFilter setTags(String... tagsCSVarray) {
            if (tagsCSVarray == null) {
                this.tags = null;
            } else {
                Set<String> tagSet = new OrderedSet<String>(); // needs to be ordered
                for (String tagsCSV : tagsCSVarray) {
                    for (String tag : StringTools.split(tagsCSV,',')) {
                        if (!StringTools.isBlank(tag)) {
                            tagSet.add(tag);
                        }
                    }
                }
                this.tags = !ListTools.isEmpty(tagSet)? tagSet : null;
            }
            return this;
        }
        public Set<String> getTags() {
            return this.tags;
        }
        public String getTagsString() {
            // -- Debug purpose only: gets a comma-separated list of tags
            return StringTools.join(this.tags,",");
        }
        public boolean isTagMatch(String annotTagsCSV) {
            String annotTags[] = !StringTools.isBlank(annotTagsCSV)? 
                StringTools.split(annotTagsCSV,',') : 
                StringTools.EMPTY_STRING_ARRAY;
            return this.isTagMatch(annotTags);
        }
        public boolean isTagMatch(String annotTags[]) {
            if (ListTools.isEmpty(this.tags)) {
                // -- filter does not care about tags
                return true;
            } else
            if (ListTools.isEmpty(annotTags)) {
                // -- filter requires tags and no annotation tags are provided
                return false;
            } else
            if ((annotTags.length == 1) && StringTools.isBlank(annotTags[0])) {
                // -- filter requires tags and no annotation tags are provided
                return false;
            } else {
                // -- any filter tags found in provided annotation tag list?
                // -    ie. "fuel,temperature" ==> "temperature,environment,!fuel" ==> true
                // -    ie. "fuel,temperature" ==> "!fuel,temperature,environment" ==> false
                for (String filterTag : this.tags) {
                    if (!filterTag.startsWith("!")) {
                        // -- check for 'contains'
                        if (ListTools.containsIgnoreCase(annotTags,filterTag)) {
                            // -- annotation contains a filter-tag
                            return true;
                        }
                    } else {
                        // -- check for 'not-contains'
                        String notFilterTag = filterTag.substring(1); // remove prefixing "!"
                        if (ListTools.containsIgnoreCase(annotTags,filterTag)) {
                            // -- annotation contains a not-filter-tag
                            return false;
                        }
                    }
                }
                // -- no matching tags
                return false;
            }
        }
        // ----------------------------
        public JSON.JSONBeanFilter setKeyNames(String... keyNames) {
            if (keyNames == null) {
                this.keyNames = null;
            } else {
                Set<String> keyNameSet = new HashSet<String>();
                for (String keyName : keyNames) {
                    if (!StringTools.isBlank(keyName)) {
                        keyNameSet.add(keyName);
                    }
                }
                this.keyNames = !ListTools.isEmpty(keyNameSet)? keyNameSet : null;
            }
            return this;
        }
        public Set<String> getKeyNames() {
            return this.keyNames;
        }
        public String getKeyNameString() {
            // -- Debug purpose only: gets a comma-separated list of names
            return StringTools.join(this.keyNames,",");
        }
        public boolean isKeyNameMatch(String keyName) {
            if (ListTools.isEmpty(this.keyNames)) {
                // -- filter does not care about key-names
                return true;
            } else
            if (StringTools.isBlank(keyName)) {
                // -- filter requires key-names and no key-name provided
                return false;
            } else {
                // -- key-name found?
                if (ListTools.containsIgnoreCase(this.keyNames,keyName)) {
                    return true;
                }
                // -- no matching key-name
                return false;
            }
        }
        // ----------------------------
        public JSON.JSONBeanFilter setComparator(Comparator<JSON._KeyValue> comp) {
            this.comparator = comp;
            return this;
        }
        public JSON._Object sort(JSON._Object jsonBean, Class<?> javaBeanClass) {
            if ((this.comparator != null) && (jsonBean != null)) {
                jsonBean.sortByComparator(this.comparator,false); // non-recursive
            }
            return jsonBean;
        }
        // ----------------------------
    }

    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified JSON value matches the 'ignoreType' definition of a blank value.
    **/
    public static boolean isBlankJsonValue(String methName, Object jsonValue, String ignoreCSV)
    {
        boolean ignoreBlankValues = true;
        if (!ignoreBlankValues) {
            // -- do not ignore blank values
            return false;
        }

        /* get JSON primitive type */
        if (jsonValue instanceof JSON._Value) {
            jsonValue = ((JSON._Value)jsonValue).getJavaObject();
        }

        /* check for blank item */
        for (String ignoreType : StringTools.split(ignoreCSV,',')) {
            ignoreType = StringTools.trim(ignoreType);
            if (StringTools.isBlank(ignoreType)) {
                // -- skip blank ignore types
                continue;
            } else
            if (jsonValue == null) {
                if (ignoreType.equalsIgnoreCase(IGNORE_NULL)  || 
                    ignoreType.equalsIgnoreCase(IGNORE_EMPTY) ||
                    ignoreType.equalsIgnoreCase(IGNORE_BLANK)   ) {
                    // -- ignore nulls
                    return true;
                }
            } else
            if (jsonValue instanceof Boolean) {
                boolean V = ((Boolean)jsonValue).booleanValue(); // Boolean
                if (ignoreType.startsWith("==")) {
                    String boolStr = ignoreType.substring(2);
                    if ((V == true ) && StringTools.isBoolean_TRUE(boolStr,true)) {
                        return true;
                    } else
                    if ((V == false) && StringTools.isBoolean_FALSE(boolStr,true)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith("!=")) {
                    String boolStr = ignoreType.substring(2);
                    if ((V == false) && StringTools.isBoolean_TRUE(boolStr,true)) {
                        return true;
                    } else
                    if ((V == true ) && StringTools.isBoolean_FALSE(boolStr,true)) {
                        return true;
                    }
                } else
                if (!ignoreType.startsWith("$")) {
                    boolean I = StringTools.parseBoolean(ignoreType,false);
                    if (V == I) {
                        return true;
                    }
                }
            } else
            if (jsonValue instanceof Number) {
                double V = ((Number)jsonValue).doubleValue(); // Double/Long
                if (ignoreType.startsWith(IGNORE_ZERO)) {
                    if (V == 0.0) {
                        return true;
                    }
                } else
                if ((ignoreType.length() > 0) && Character.isDigit(ignoreType.charAt(0))) {
                    double I = StringTools.parseDouble(ignoreType,Double.NaN);
                    if (!Double.isNaN(I) && (V == I)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith("==")) {
                    double I = StringTools.parseDouble(ignoreType.substring(2),Double.NaN);
                    if (!Double.isNaN(I) && (V == I)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith("!=")) {
                    double I = StringTools.parseDouble(ignoreType.substring(2),Double.NaN);
                    if (!Double.isNaN(I) && (V > I)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith("<=")) {
                    double I = StringTools.parseDouble(ignoreType.substring(2),Double.NaN);
                    if (!Double.isNaN(I) && (V <= I)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith("<")) {
                    double I = StringTools.parseDouble(ignoreType.substring(1),Double.NaN);
                    if (!Double.isNaN(I) && (V < I)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith(">=")) {
                    double I = StringTools.parseDouble(ignoreType.substring(2),Double.NaN);
                    if (!Double.isNaN(I) && (V >= I)) {
                        return true;
                    }
                } else
                if (ignoreType.startsWith(">")) {
                    double I = StringTools.parseDouble(ignoreType.substring(1),Double.NaN);
                    if (!Double.isNaN(I) && (V > I)) {
                        return true;
                    }
                }
            } else
            if ((jsonValue instanceof String) && StringTools.isBlank((String)jsonValue)) {
                if (ignoreType.equalsIgnoreCase(IGNORE_EMPTY) || 
                    ignoreType.equalsIgnoreCase(IGNORE_BLANK)   ) {
                    // -- ignore empty/blank Strings
                    return true;
                }
            } else
            if (ListTools.isContainer(jsonValue) && ListTools.isEmpty(jsonValue)) {
                if (ignoreType.equalsIgnoreCase(IGNORE_EMPTY)  ) {
                    // -- ignore empty arrays/lists
                    return true;
                }
            } else {
                if (ignoreType.startsWith(IGNORE_ZERO)) {
                    if ("0".equalsIgnoreCase(jsonValue.toString())) {
                        return true;
                    }
                } else
                if (ignoreType.equalsIgnoreCase(jsonValue.toString())) {
                    return true;
                }
            }
        }

        /* not a blank */
        return false;

    }

    // ------------------------------------------------------------------------

    /**
    *** Sorts the specified JsonBean object
    **/
    public static JSON._Object sortJsonBean(JSON._Object jsonBean, JSON.JSONBeanFilter filter, Class<?> javaBeanClass)
    {
        if (filter != null) {
            filter.sort(jsonBean,javaBeanClass);
        }
        return jsonBean;
    }

    /**
    *** Creates/Returns a JSON._Object containing specific getter fields from the specified object.
    *** @param javaBean  The JSONBean instance for which the JSON bean object is returned
    *** @param filter  The tag-id for the specific getter methods/fields to include.  null for all defined getter fields.
    *** @return The JSON bean object (does not return null)
    **/
    public static JSON._Object toJsonBean(JSON.JSONBean javaBeanObj, JSON.JSONBeanFilter filter, JSON._Object jsonBean)
    {
        boolean debug = false; // false for production
        if (debug) { Print.logInfo("'JSON.toJsonBean' logging enabled ..."); }

        /* create new JSON object? */
        if (jsonBean == null) {
            jsonBean = new JSON._Object();
        }

        /* JavaBean object/class */
        Object javaBean;
        if (javaBeanObj == null) {
            // -- JavaBean object not specified
            return jsonBean; // JSON.sortJsonBean(jsonBean,filter,null); // return as-is
        } else
        if (javaBeanObj instanceof JSONBeanWrap) {
            // -- unwrap JSONBean (object is likely not a JSONBean)
            javaBean = ((JSONBeanWrap)javaBeanObj).getJavaBean();
            if (javaBean == null) {
                Print.logWarn("JSONBean wrapper returned a null object");
                return jsonBean; // JSON.sortJsonBean(jsonBean,filter,null); // return as-is
            }
        } else {
            // -- a JSONBean
            javaBean = javaBeanObj;
        }

        /* JavaBean class */
        Class<?> javaBeanClass = javaBean.getClass();

        /* SpecialCase: non-JSONBean instances */
        if (!(javaBean instanceof JSON.JSONBean)) {
            // -- this can only occur if wrapped inside a JSONBeanWrap

            /* SpecialCase: unconvertable types */
            // -- can't turn these into a JSON key/value object
            if (JSON.IsScalarValue(javaBean)) { // String, Number, Boolean
                Print.logWarn("Cannot convert scalar to JSON._Object");
                return jsonBean; // JSON.sortJsonBean(jsonBean,filter,javaBeanClass);
            } else
            if (javaBean instanceof JSON._Array) {
                Print.logWarn("Cannot convert JSON._Array to JSON._Object");
                return jsonBean; // JSON.sortJsonBean(jsonBean,filter,javaBeanClass);
            } else
            if (javaBeanClass.isArray()) {
                Print.logWarn("Cannot convert Array[] to JSON._Object");
                return jsonBean; // JSON.sortJsonBean(jsonBean,filter,javaBeanClass);
            }

            /* SpecialCase: JSON._Object (shallow copy) */
            if (javaBean instanceof JSON._Object) {
                JSON._Object jObj = (JSON._Object)javaBean;
                int cnt = jObj.getKeyValueCount();
                for (int i = 0; i < cnt; i++) {
                    JSON._KeyValue kv = jObj.getKeyValueAt(i);
                    jsonBean.addKeyValue(kv); // shallow copy
                }
            }

            /* SpecialCase: Properties */
            if (javaBean instanceof Properties) {
                Properties props = (Properties)javaBean;
                for (String key : props.stringPropertyNames()) {
                    String val = props.getProperty(key);
                    jsonBean.addKeyValue(key, val);
                }
                return JSON.sortJsonBean(jsonBean,filter,javaBeanClass);
            }

            /* SpecialCase: RTProperties */
            if (javaBean instanceof RTProperties) {
                RTProperties props = (RTProperties)javaBean;
                for (Object key : props.getPropertyKeys()) {
                    if (key instanceof String) {
                        Object val = props.getProperty(key, null);
                        jsonBean.addKeyValue((String)key, val);
                    }
                }
                return JSON.sortJsonBean(jsonBean,filter,javaBeanClass);
            }

            /* SpecialCase: HashMap */
            if (javaBean instanceof Map) { // HashMap
                Map<?,?> props = (Map<?,?>)javaBean;
                for (Object key : props.keySet()) {
                    if (key instanceof String) {
                        Object val = props.get(key);
                        jsonBean.addKeyValue((String)key, val);
                    }
                }
                return JSON.sortJsonBean(jsonBean,filter,javaBeanClass);
            }

            /* SpecialCase: Dictionary */
            if (javaBean instanceof Dictionary) { // Hashtable
                Dictionary<?,?> props = (Dictionary<?,?>)javaBean;
                for (Enumeration<?> e = props.keys(); e.hasMoreElements();) {
                    Object key = e.nextElement();
                    if (key instanceof String) {
                        Object val = props.get(key);
                        jsonBean.addKeyValue((String)key, val);
                    }
                }
            }

        }

        // --------------------------------------------------------------------
        // -- use reflection to examine the getters of this instance

        /* cache filtered interface list */
        Set<Class<?>> filterInterfaceList = null;
        if (filter != null) {
            filterInterfaceList = filter.getMatchingInterfaces(javaBeanClass);
            if (filterInterfaceList == null) {
                // -- filter doesn't care about interfaces
            } else
            if (ListTools.isEmpty(filterInterfaceList)) {
                // -- main class does not implement any filter interfaces
                //Print.logWarn("JavaBean class does not implement any filtered interface: [" + StringTools.className(javaBeanClass)+"] " + filter.getInterfacesString());
                //return JSON.sortJsonBean(jsonBean,filter,javaBeanClass); // return empty/unchanged JsonBean object as-is
                // -- continue below to indicate that no-interfaces means any-interfaces
            }
        }

        /* iterate through all JavaBean getter methods */
        for (Method meth : javaBeanClass.getMethods()) {

            /* Method attributes */
            String   methName     = meth.getName();
            int      methMods     = meth.getModifiers();
            Class<?> paramClass[] = meth.getParameterTypes();
            Class<?> rtnTypeClass = meth.getReturnType();

            /* JSONBeanGetter annotation */
            JSON.JSONBeanGetter beanGetter = meth.getAnnotation(JSON.JSONBeanGetter.class);
            if (beanGetter != null) {
                // -- we have a JSONBeanGetter annotation
            } else
            if (!(javaBean instanceof JSON.JSONBean)) {
                // -- not a JSON.JSONBean, annotation is not required
                // -  (this would only occur if the object arrived inside a JSONBeanWrap instance)
            } else {
                // -- no JSONBeanGetter annotation, and one is required
                continue;
            }
            String jsonName  = (beanGetter != null)? beanGetter.name()        : "";
            String jsonType  = (beanGetter != null)? beanGetter.type()        : ""; // not currently used
            String jsonTags  = (beanGetter != null)? beanGetter.tags()        : "";
            String expArray  = (beanGetter != null)? beanGetter.expandArray() : "";
            String ignoreVal = (beanGetter != null)? beanGetter.ignore()      : "";
            String enumClass = (beanGetter != null)? beanGetter.enumClass()   : "";
            String unitsEnum = (beanGetter != null)? beanGetter.unitsEnum()   : "";
            String methArg   = (beanGetter != null)? beanGetter.arg()         : null;

            /* convert Enum int/String values to the Enum name */
            boolean useEnumName = !StringTools.isBlank(enumClass)? true : false;

            /* find a reason to reject this method */
            final String jbStr = (javaBean instanceof JSON.JSONBean)? "JSONBean " : "non-JSONBean ";
            if (!Modifier.isPublic(methMods)) {
                // -- ignore non-"public" methods
                if (debug) { Print.logInfo(jbStr+"Method ignored [not 'public']: " + methName); }
                continue;
            } else
            if (Modifier.isStatic(methMods)) {
                // -- ignore "static" methods
                if (debug) { Print.logInfo(jbStr+"Method ignored [not an instance method]: " + methName); }
                continue;
            } else
            if (!methName.startsWith("get") && !(javaBean instanceof JSON.JSONBean)) {
                // -- "is" method prefix not allowed for non-JSONBean classes
                if (debug) { Print.logInfo(jbStr+"Method ignored [does not begin with 'get']: " + methName); }
                continue;
            } else
            if (!methName.startsWith("get") && !methName.startsWith("is")) {
                // -- not a getter
                if (debug) { Print.logInfo(jbStr+"Method ignored [does not begin with 'get'/'is']: " + methName); }
                continue;
            } else
            if ((rtnTypeClass == null) || (rtnTypeClass == Void.class) || (rtnTypeClass == Void.TYPE)) {
                // -- getter method has a Void return type
                if (debug) { Print.logInfo(jbStr+"Method ignored [getter method returns void]: " + methName); }
                continue;
            } else
            if (methName.startsWith("is") && (rtnTypeClass != Boolean.class) && (rtnTypeClass != Boolean.TYPE)) {
                // -- "is" getter method return type is not boolean
                if (debug) { Print.logInfo(jbStr+"Method ignored ['is' getter method returns non-boolean]: " + methName); }
                continue;
            }

            /* at most one argument is allowed */
            Class<?> argClass0 = null;
            if (ListTools.size(paramClass) > 0) {
                argClass0 = paramClass[0];
                if (beanGetter == null) {
                    // -- parameter specified, but no "@JSONBeanGetter" specified
                    if (debug) { Print.logInfo(jbStr+"Method ignored [parameter specified, but no '@JSONBeanGetter' specified]: " + methName); }
                    continue;
                } else
                if (ListTools.size(paramClass) > 1) {
                    // -- more than 1 parameter specified
                    if (debug) { Print.logInfo(jbStr+"Method ignored [more than 1 parameter specified]: " + methName); }
                    continue;
                } else 
                if (!String   .class.equals(argClass0) &&
                    !Double   .class.equals(argClass0) && !Double .TYPE.equals(argClass0) &&
                    !Float    .class.equals(argClass0) && !Float  .TYPE.equals(argClass0) &&
                    !Long     .class.equals(argClass0) && !Long   .TYPE.equals(argClass0) &&
                    !Integer  .class.equals(argClass0) && !Integer.TYPE.equals(argClass0) &&
                    !Short    .class.equals(argClass0) && !Short  .TYPE.equals(argClass0) &&
                    !Byte     .class.equals(argClass0) && !Byte   .TYPE.equals(argClass0) &&
                    !Boolean  .class.equals(argClass0) && !Boolean.TYPE.equals(argClass0) &&
                    !Locale   .class.equals(argClass0) &&
                    !TimeZone .class.equals(argClass0) &&
                    !argClass0.isEnum()
                    ) {
                    // -- not a supported convertable parameter type (must be supported by "StringTools.toObjectValue(...)"
                    if (debug) { Print.logInfo(jbStr+"Method ignored [non-convertable parameter type specified]: " + methName); }
                    continue;
                }
                // -- a single convertable parameter was specified
                // -  argClass0 = paramClass[0];
            }

            /* get JSON key name ("getIsActive"==>"isActive", "isActive"==>"isActive") */
            // -- "isXXXXXX" is considered an abbreviation of "getIsXXXXXX"
            if (StringTools.isBlank(jsonName)) {
                int p = methName.startsWith("get")? 3/*get*/ : 0/*is*/;
                jsonName = Character.toLowerCase(methName.charAt(p)) + methName.substring(p+1);
                if (StringTools.isBlank(jsonName)) {
                    // -- ie. skip "get()" methods
                    if (debug) { Print.logInfo(jbStr+"Method ignored [invalid method name length]: " + methName); }
                    continue;
                }
            }

            /* check filter */
            if (filter != null) {
                // -- filter was specified
                if (!ListTools.isEmpty(filterInterfaceList) && 
                    !filter.interfaceContainsMethod(filterInterfaceList,meth)) {
                    // -- method not in any filter interface
                    if (debug) { Print.logInfo(jbStr+"Method ignored [not contained in filter interface]: " + methName); }
                    continue;
                } else
                if (!filter.isTagMatch(jsonTags)) {
                    // -- annotation tag does not match any filter tag
                    if (debug) { Print.logInfo(jbStr+"Method ignored [does not match filter tag]: " + methName); }
                    continue;
                } else
                if (!filter.isKeyNameMatch(jsonName)) {
                    // -- json key-name does not match any required key-name
                    if (debug) { Print.logInfo(jbStr+"Method ignored [does not match filter key name]: " + methName); }
                    continue;
                }
            }

            /* get JSON data type (not currently used) */
            if (StringTools.isBlank(jsonType)) {
                // -- parseReturnType(...) toJsonValue
                jsonType = StringTools.className(rtnTypeClass);
            }

            /* get getter value */
            Object value = null;
            try {
                if (argClass0 == null) {
                    // -- no arguments
                    MethodAction ma = new MethodAction(javaBean, methName);
                    ma.setAccessible(true); // suppress Java language access checking
                    value = ma.invoke();
                } else {
                    // -- one argument
                    if ((methArg == null) || methArg.trim().equalsIgnoreCase("${NULL}")) {
                        // -- leave null
                        methArg = null;
                    } else {
                        // -- replace all property values
                        final Object _javaBean = javaBean;
                        methArg = StringTools.replaceKeys(methArg, new StringTools.KeyValueMap() {
                            public String getKeyValue(String key, String arg, String dft) {
                                if (key.startsWith("%") && (key.length() > 1) && !StringTools.isBlank(arg)) {
                                    // arg="${%GetterPropKey:fieldName}"
                                    Object obj;
                                    String objKey = key.substring(1);
                                    if (objKey.equals("this")) {
                                        obj = _javaBean;
                                    } else {
                                        obj = RTConfig.getProperty(objKey,null);
                                    }
                                    // --
                                    if ((obj instanceof String) || 
                                        (obj instanceof Number)   ) {
                                        // -- invalid object type, just return as String
                                        return obj.toString();
                                    } else
                                    if (obj instanceof RTConfig.PropertyGetter) {
                                        // -- get property value
                                        Object val = ((RTConfig.PropertyGetter)obj).getProperty(arg,dft);
                                        return (val != null)? val.toString() : dft;
                                    } else
                                    if (obj != null) {
                                        // -- return Object 'getter' value
                                        try {
                                            Object val = MethodAction.invokeGetterMethod(obj,arg);
                                            return (val != null)? val.toString() : dft;
                                        } catch (Throwable th) {
                                            // -- error
                                            Print.logException(jbStr+"Error executing 'getter' (returning default): " + arg, th);
                                            return dft;
                                        }
                                    } else {
                                        // -- try getting full property key name
                                        return RTConfig.getString(key,dft);
                                    }
                                } else {
                                    return RTConfig.getString(key,dft);
                                }
                            }
                        });
                    }
                    // -- call method
                    MethodAction ma = new MethodAction(javaBean, methName, argClass0); // String.class);
                    ma.setAccessible(true); // suppress Java language access checking
                    Object methArgObj = StringTools.toObjectValue(methArg, argClass0); // convert String to type
                    if (methArgObj == null) {
                        Print.logError(jbStr+"Method ignored [unable to convert String to parameter class]: " + methName);
                        continue;
                    }
                    value = ma.invoke(methArgObj);
                }
            } catch (Throwable th) {
                // -- error executing getter
                Print.logException(jbStr+"Method ignored [error executing method]: " + methName, th);
                continue;
            }

            /* convert to enum text String? */
            if (useEnumName && (value != null) && !StringTools.isBlank(enumClass)) {
                enumConversion:
                do { // single-pass-loop
                    // -- get Enum class
                    Class<?> _enumClass;
                    try {
                        _enumClass = (Class<?>)Class.forName(enumClass);
                    } catch (Throwable th) { // ClassNotFoundException
                        // -- was this a FQN?
                        if (enumClass.indexOf(".") >= 0) {
                            Print.logError("Specified 'enumClass' not found: " + enumClass);
                            break enumConversion;
                        }
                        // -- try again
                        enumClass = StringTools.className(javaBeanClass) + "$" + enumClass;
                        try {
                            _enumClass = (Class<?>)Class.forName(enumClass);
                        } catch (Throwable th2) {
                            Print.logError("Specified 'enumClass' not found: " + enumClass);
                            break enumConversion;
                        }
                    }
                    if ((_enumClass == null) || !_enumClass.isEnum()) {
                        Print.logError("Specified 'enumClass' not an Enum: " + enumClass);
                        break enumConversion;
                    }
                    // -- Enum constants
                    Object _enumConst[] = _enumClass.getEnumConstants();
                    if (ListTools.isEmpty(_enumConst)) {
                        Print.logWarn("Enum is empty: " + enumClass);
                        break enumConversion;
                    }
                    // -- search Enum Constants matching getter value
                    String vs = StringTools.trim(value);
                    long   vi = StringTools.parseLong(value,Long.MIN_VALUE);
                    for (Object e : _enumConst) {
                        if ((e instanceof EnumTools.IntValue) && (((EnumTools.IntValue)e).getIntValue() == (int)vi)) {
                            value = ((Enum<?>)e).name();
                            break enumConversion;
                        } else
                        if ((e instanceof EnumTools.LongValue) && (((EnumTools.LongValue)e).getLongValue() == vi)) {
                            value = ((Enum<?>)e).name();
                            break enumConversion;
                        } else
                        if ((e instanceof EnumTools.StringValue) && vs.equalsIgnoreCase(((EnumTools.StringValue)e).getStringValue())) {
                            value = ((Enum<?>)e).name();
                            break enumConversion;
                        } else
                        if ((e instanceof Enum) && vs.equalsIgnoreCase(((Enum)e).name())) {
                            value = ((Enum<?>)e).name();
                            break enumConversion;
                        }
                    }
                    // -- not found
                    Print.logError("Enum not found: " + enumClass + " ["+value+"]");
                    break enumConversion;
                } while (false); // single-pass-loop
            }

            /* convert value to JSON data type */
            Object jsonValue = JSON.toJsonValue(value, filter); // toJsonBean

            // -- add to JSON bean object */
            boolean expandArray = StringTools.parseBoolean(expArray,false);
            if (!expandArray) {
                // -- add 'jsonValue' as-is
                if (JSON.isBlankJsonValue(methName,jsonValue,ignoreVal)) {
                    if (debug) { Print.logInfo(jbStr+"Method ignored [blank return value]: " + methName); }
                    continue;
                }
                jsonBean.addKeyValue(jsonName, jsonValue);
            } else
            if (jsonValue instanceof JSON._Array) {
                // -- expand array
                JSON._Array jva = (JSON._Array)jsonValue;
                for (int i = 0; i < jva.size(); i++) {
                    JSON._Value jv = jva.getValueAt(i);
                    if (JSON.isBlankJsonValue(methName+"["+i+"]",jv,ignoreVal)) {
                        if (debug) { Print.logInfo(jbStr+"Method ignored [blank array element value #"+i+"]: " + methName); }
                        continue;
                    }
                    jsonBean.addKeyValue(jsonName + i, jv);
                }
            } else {
                // -- expandArray is true, but 'jsonValue' is not an array
                if (JSON.isBlankJsonValue(methName,jsonValue,ignoreVal)) {
                    if (debug) { Print.logInfo(jbStr+"Method ignored [blank return value]: " + methName); }
                    continue;
                }
                Print.logWarn("'expandArray=true' specification ignored (not an array): " + methName);
                jsonBean.addKeyValue(jsonName, jsonValue);
            }

        } // loop through methods

        /* done */
        return JSON.sortJsonBean(jsonBean,filter,javaBeanClass);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified value represents scalar quantity.
    *** Includes types: String, Number(Double,Long,etc), and Boolean
    **/
    public static boolean IsScalarValue(Object val) 
    {
        if (val instanceof String) {
            return true;
        } else
        if (val instanceof Number) { // Double, Float, Long, Integer, Short, Byte, etc.
            return true;
        } else
        if (val instanceof Boolean) {
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final int MAX_READ_LEN = 100000;

    /**
    *** Reads a JSON object from the specified URL
    *** @param url        The URL from which the JSON object is read
    *** @param timeoutMS  The specified timeout (in milliseconds)
    *** @return The JSON object
    *** @throws MalformedURLException      If an invalid URL syntax is specified
    *** @throws HTMLTools.HttpIOException  If a not-found(404) or forbidden(403) error occurs
    *** @throws IOException                If a general IO error occurs
    *** @throws JSON.JSONParsingException  If a JSON parsing exception occurs
    **/
    public static JSON ReadJSON_GET(
        URL url, 
        int timeoutMS)
        throws JSON.JSONParsingException, HTMLTools.HttpIOException, IOException
    {
        JSON   jsonObj = null;
        String jsonStr = null;
        try {
            byte b[] = HTMLTools.readPage_GET(
                url,
                timeoutMS, MAX_READ_LEN);
            jsonStr  = StringTools.toStringValue(b);
            jsonObj  = new JSON(jsonStr);
        } catch (JSON.JSONParsingException jpe) {
            // -- invalid JSON object
            //Print.logError("Invalid JSON object (GET):\n" + jsonStr);
            throw jpe;
        } catch (HTMLTools.HttpIOException hioe) {
            // -- possible not-found(404) or forbidden(403) error 
            throw hioe;
        } catch (MalformedURLException mue) {
            // -- invalid URL format
            throw mue;
        } catch (IOException ioe) {
            // -- general IO error
            throw ioe;
        }
        return jsonObj;
    }

    /**
    *** Reads a JSON object from the specified URL
    *** @param url         The URL from which the JSON object is read
    *** @param contentType The MIME type of the POST data sent to the server
    *** @param postData    Data to send in a POST, if null then GET will be used
    *** @param timeoutMS   The specified timeout (in milliseconds)
    *** @return The JSON object
    *** @throws MalformedURLException      If an invalid URL syntax is specified
    *** @throws HTMLTools.HttpIOException  If a not-found(404) or forbidden(403) error occurs
    *** @throws IOException                If a general IO error occurs
    *** @throws JSON.JSONParsingException  If a JSON parsing exception occurs
    **/
    public static JSON ReadJSON_POST(
        URL url, 
        String contentType, byte postData[], 
        int timeoutMS)
        throws JSON.JSONParsingException, HTMLTools.HttpIOException, IOException
    {
        JSON   jsonObj = null;
        String jsonStr = null;
        try {
            byte b[] = HTMLTools.readPage_POST(
                url, 
                contentType, postData, 
                timeoutMS, MAX_READ_LEN);
            jsonStr  = StringTools.toStringValue(b);
            jsonObj  = new JSON(jsonStr);
        } catch (JSON.JSONParsingException jpe) {
            // -- invalid JSON object
            //Print.logError("Invalid JSON object (POST): " + jpe + "\n" + jsonStr);
            throw jpe;
        } catch (HTMLTools.HttpIOException hioe) {
            // -- possible not-found(404) or forbidden(403) error 
            throw hioe;
        } catch (MalformedURLException mue) {
            // -- invalid URL format
            throw mue;
        } catch (IOException ioe) {
            // -- general IO error
            throw ioe;
        }
        return jsonObj;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Traverses the specified JSON Object/Array and returns the Value found
    *** at the specified path.
    **/
    private static JSON._Value GetValueForPath(Object obj, String path) 
    {
        if (!StringTools.isBlank(path)) {
            char sep = (path.indexOf("/") >= 0)? '/' : '.';
            return JSON.GetValueForPath(obj, StringTools.split(path,sep));
        } else {
            return null;
        }
    }

    /**
    *** Traverses the specified JSON Object/Array and returns the Value found
    *** at the specified path.
    **/
    private static JSON._Value GetValueForPath(Object obj, String... path) 
    {
        // -- no Object?
        if (obj == null) {
            return null;
        }
        // -- no path?
        if (path == null) {
            return null;
        }
        // -- traverse path
        JSON._Value val = null;
        for (int p = 0; p < path.length; p++) {
            if (obj instanceof JSON._Object) {
                // -- JSON Object
                JSON._Object target = (JSON._Object)obj;
                JSON._KeyValue kv = target.getKeyValue(path[p]);
                if (kv == null) {
                    // -- path not found
                    return null;
                }
                val = kv.getValue();
            } else
            if (obj instanceof JSON._Array) {
                // -- JSON Array
                JSON._Array target = (JSON._Array)obj;
                int ndx = StringTools.parseInt(path[p],-1);
                if ((ndx < 0) || (ndx >= target.size())) {
                    // -- outside of array bounds
                    return null;
                }
                val = target.getValueAt(ndx);
            } else {
                // -- cannot traverse a scalar type
                return null;
            }
            // -- next Object
            obj = val.getJavaObject();
        }
        return val;
    }

    // ------------------------------------------------------------------------

    /**
    *** Flattens a JSON Object into a property map
    **/
    public static Map<String,Object> createPropertyMap(JSON._Object obj, String keyPfx)
    {
        Map<String,Object> propMap = new HashMap<String,Object>();
        JSON._flattenObject(propMap, obj, keyPfx, PROPERTY_MAP_SEP);
        return propMap;
    }

    /**
    *** Flattens a JSON Array into a property map
    **/
    public static Map<String,Object> createPropertyMap(JSON._Array array, String keyPfx)
    {
        Map<String,Object> propMap = new HashMap<String,Object>();
        JSON._flattenArray(propMap, array, keyPfx, PROPERTY_MAP_SEP);
        return propMap;
    }

    /**
    *** Flattens the specified JSON Value into a the property map
    **/
    private static void _flattenValue(Map<String,Object> map, JSON._Value val, String keyPfx, String sep)
    {
        if (val == null) {
            // -- ignore
        } else
        if (val.isScalarValue()) {
            // -- scalar value
            map.put(keyPfx, val.getJavaObject());
        } else
        if (val.isObjectValue()) {
            // -- flatten object
            JSON._flattenObject(map, val.getObjectValue(null), keyPfx, sep);
        } else
        if (val.isArrayValue()) {
            // -- flatten array
            JSON._flattenArray(map, val.getArrayValue(null), keyPfx, sep);
        } else
        if (val.isNullValue()) {
            // -- null
            map.put(keyPfx, null);
        } else {
            // -- unlikely: not sure what this could be, ignore it
        }
    }

    /**
    *** Flattens the specified JSON Object into a the property map
    **/
    private static void _flattenObject(Map<String,Object> map, JSON._Object obj, String keyPfx, String sep)
    {
        if (obj != null) {
            for (int i = 0; i < obj.getKeyValueCount(); i++) {
                JSON._KeyValue kv = obj.getKeyValueAt(i);
                String         ks = ((keyPfx!=null)?(keyPfx+sep):"") + kv.getKey();
                JSON._Value    v  = kv.getValue();
                JSON._flattenValue(map, v, ks, sep);
            }
        }
    }

    /**
    *** Flattens the specified JSON Array into a the property map
    **/
    private static void _flattenArray(Map<String,Object> map, JSON._Array array, String keyPfx, String sep)
    {
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                String      ks = ((keyPfx!=null)?(keyPfx+sep):"") + i;
                JSON._Value v  = array.getValueAt(i);
                JSON._flattenValue(map, v, ks, sep);
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final boolean CASE_SENSITIVE = false;

    private static boolean NameEquals(String n1, String n2)
    {
        if ((n1 == null) || (n2 == null)) {
            return false;
        } else
        if (CASE_SENSITIVE) {
            return n1.equals(n2);
        } else {
            return n1.equalsIgnoreCase(n2);
        }
    }

    // ------------------------------------------------------------------------

    private static final String INDENT = "   ";

    /**
    *** Return indent spaces
    **/
    private static String indent(int count)
    {
        return StringTools.replicateString(INDENT,count);
    }

    // ------------------------------------------------------------------------

    private static final char ESCAPE_CHAR = '\\';

    /**
    *** Converts the specified String to a JSON escaped value String.<br>
    *** @param s  The String to convert to a JSON encoded String
    *** @return The JSON encoded String
    **/
    public static String escapeJSON(String s)
    {
        if (s != null) {
            StringBuffer sb = new StringBuffer();
            int len = s.length();
            for (int i = 0; i < len; i++) {
                char ch = s.charAt(i);
                if (ch == ESCAPE_CHAR) {
                    // -- "\\"
                    sb.append(ESCAPE_CHAR).append(ESCAPE_CHAR);
                } else
                if (ch == '\n') {
                    // -- newline
                    sb.append(ESCAPE_CHAR).append('n');
                } else
                if (ch == '\r') {
                    // -- carriage-return
                    sb.append(ESCAPE_CHAR).append('r');
                } else
                if (ch == '\t') {
                    // -- horizontal tab
                    sb.append(ESCAPE_CHAR).append('t');
                } else
                if (ch == '\b') {
                    // -- backspace
                    sb.append(ESCAPE_CHAR).append('b');
                } else
                if (ch == '\f') {
                    // -- formfeed
                    sb.append(ESCAPE_CHAR).append('f');
                } else
              //if (ch == '\'') {
              //    // -- single-quote
              //    sb.append(ESCAPE_CHAR).append('\''); <-- should not be escaped
              //} else
                if (ch == '\"') { // double-quote
                    // -- "\""
                    sb.append(ESCAPE_CHAR).append('\"');
                } else
                if ((ch >= 0x0020) && (ch <= 0x007e)) {
                    // -- ASCII
                    sb.append(ch);
                } else
                if (ch < 0x0020) {
                    // -- control characters: "/u00FF"
                    sb.append(ESCAPE_CHAR).append("u").append(StringTools.toHexString((int)ch,16));
                } else 
                if (ch < 0x00FF) {
                    // -- non-ASCII characters: "/u00FF"
                    sb.append(ESCAPE_CHAR).append("u").append(StringTools.toHexString((int)ch,16));
                } else {
                    // -- unicode characters: "/uFFFF"
                    //sb.append(ch);
                    sb.append(ESCAPE_CHAR).append("u").append(StringTools.toHexString((int)ch,16));
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- References:
    // -    https://self-issued.info/docs/draft-ietf-oauth-json-web-token.html#ctyHdrDef
    
    public static final String JWT_SIG_ALGORITHM    = "HmacSHA256";
    public static final String JWT_SIG_ALGNAME      = "HS256";
    public static final String JWT_TYPE_NAME        = "JWT";

    /* header */
    public static final String JWT_algorithm        = "alg";
    public static final String JWT_type             = "typ";
    public static final String JWT_contentType      = "cty";

    /* payload (reserved) */
    public static final String JWT_issuer           = "iss";
    public static final String JWT_subject          = "sub";
    public static final String JWT_audience         = "aud";
    public static final String JWT_expiration       = "exp";
    public static final String JWT_notBefore        = "nbf";
    public static final String JWT_issuedAt         = "iat";
    public static final String JWT_jwtID            = "jti";
    public static final String JWT_name             = "name";
    public static final String JWT_givenName        = "given_name";
    public static final String JWT_familyName       = "family_name";
    public static final String JWT_middleName       = "middle_name";
    public static final String JWT_nickname         = "nickname";
    public static final String JWT_prefUsername     = "preferred_username";
    public static final String JWT_profileURL       = "profile";
    public static final String JWT_pictureURL       = "picture";
    public static final String JWT_websiteURL       = "website";
    public static final String JWT_email            = "email";
    public static final String JWT_emailVerified    = "email_verified";
    public static final String JWT_gender           = "gender";
    public static final String JWT_birthdate        = "birthdate";
    public static final String JWT_timezone         = "zoneinfo";
    public static final String JWT_locale           = "locale";
    public static final String JWT_phoneNumber      = "phone_number";
    public static final String JWT_phoneVerified    = "phone_number_verified";
    public static final String JWT_address          = "address";
    public static final String JWT_updateTime       = "updated_at";
    public static final String JWT_authorizedParty  = "azp";
    public static final String JWT_sessionID        = "nonce";
    public static final String JWT_authTime         = "auth_time";
    public static final String JWT_accessTokenHash  = "at_hash";
    public static final String JWT_codeHash         = "c_hash";
    public static final String JWT_authContextRef   = "acr";
    public static final String JWT_authMethodRef    = "amr";
    public static final String JWT_publicKeySig     = "sub_jwk";
    public static final String JWT_confirmation     = "cnf";
    public static final String JWT_sipFrom          = "sip_from_tag";
    public static final String JWT_sipDate          = "sip_date";
    public static final String JWT_sipCallID        = "sip_callid";
    public static final String JWT_sipCSeqNumber    = "sip_cseq_num";
    public static final String JWT_sipViaBranch     = "sip_via_branch";
    public static final String JWT_originID         = "orig";
    public static final String JWT_destinationID    = "dest";
    public static final String JWT_mediaKey         = "mky";

    // --------------------------------

    /**
    *** Gets the web-token signature for the specified String
    *** @param sigTarget  The String to be signed
    *** @param secret     The secret key
    **/
    private static String CreateWebTokenSignature(String sigTarget, byte secret[])
    {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance(JWT_SIG_ALGORITHM); // NoSuchAlgorithmException
            byte sec[] = !ListTools.isEmpty(secret)? secret : "secret".getBytes(); // cannot be null/empty
            mac.init(new javax.crypto.spec.SecretKeySpec(sec,mac.getAlgorithm())); // InvalidKeyException, IllegalArgumentException
            return Base64.encodeURL(mac.doFinal(sigTarget.getBytes())); // UnsupportedEncodingException
        } catch (Throwable th) {
            // -- unlikely, unable to obtain signature
            Print.logException("Unable to create signature", th);
            return null;
        }
    }

    // --------------------------------

    /**
    *** Gets the JSON Web Token (JWT) for the specified payload and secret
    **/
    public static String CreateWebToken(JSON._Object payload, String secret)
    {
        byte sec[] = (secret != null)? secret.getBytes() : null;
        return JSON.CreateWebToken(payload, sec);
    }

    /**
    *** Gets the JSON Web Token (JWT) for the specified payload and secret
    **/
    public static String CreateWebToken(JSON._Object payload, byte secret[])
    {

        /* header */
        JSON._Object jwtHeader = new JSON._Object();
        jwtHeader.addKeyValue(JWT_algorithm, JWT_SIG_ALGNAME);
        jwtHeader.addKeyValue(JWT_type     , JWT_TYPE_NAME);
        String b64Header = Base64.encodeURL(jwtHeader.toString());

        /* payload */
        JSON._Object jwtPayload = (payload != null)? payload : new JSON._Object();
        String b64Payload = Base64.encodeURL(jwtPayload.toString());

        /* signature */
        String sigTarget = b64Header + "." + b64Payload;
        String b64Sig    = CreateWebTokenSignature(sigTarget, secret);
        if (StringTools.isBlank(b64Sig)) {
            return null;
        }

        /* token */
        return sigTarget + "." + b64Sig;

    }

    // --------------------------------

    /**
    *** Validates the JSON Web Token (JWT) with the specified secret
    **/
    public static boolean ValidateWebToken(String token, String secret)
    {
        byte sec[] = (secret != null)? secret.getBytes() : null;
        return JSON.ValidateWebToken(token, sec);
    }

    /**
    *** Validates the JSON Web Token (JWT) with the specified secret
    **/
    public static boolean ValidateWebToken(String token, byte secret[])
    {
        // -- only algorithm "HS256" is supported 

        /* split token into components */
        String jwt[] = StringTools.split(token,'.');
        if (jwt.length != 3) {
            // -- invalid number of components
            return false;
        }

        /* validate components (cannot be blank) */
        for (String C : jwt) {
            if (StringTools.isBlank(C)) {
                // -- component cannot be blank/null
                return false;
            }
        }

        /* header */
        JSON._Object jwtHeader;
        try {
            jwtHeader = JSON.parse_Object(StringTools.toStringValue(Base64.decode(jwt[0])));
        } catch (Base64.Base64DecodeException bde) {
            // -- invalid Base64 value
            return false;
        } catch (JSONParsingException jpe) {
            // -- invalid JSON value
            return false;
        }

        /* check algorithm */
        String alg = jwtHeader.getStringForName(JWT_algorithm,"");
        if (!StringTools.isBlank(alg) && !alg.equalsIgnoreCase(JWT_SIG_ALGNAME)) {
            // -- "alg" is not blank and not equal to "HS256"
            return false;
        }

        /* validate signature */
        String sig = CreateWebTokenSignature((jwt[0]+"."+jwt[1]), secret);
        if (StringTools.isBlank(sig)) {
            // -- unable to create signature?
            return false;
        } else
        if (!sig.equals(jwt[2])) {
            // -- signature does not match
            return false;
        }

        /* validated */
        return true;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** JSON Parsing Context
    **/
    public static class JSONParsingContext
    {
        private int index       = 0;
        private int line        = 1;
        private int indexAtLine = 0;
        // --
        public JSONParsingContext() {
            this.index       = 0;
            this.line        = 1;
            this.indexAtLine = this.index;
        }
        public JSONParsingContext(int ndx, int lin) {
            this.index = ndx;
            this.line  = lin;
            this.indexAtLine = this.index;
        }
        // --
        public int getIndex() {
            return this.index;
        }
        public void incrementIndex(int val) {
            this.index += val;
        }
        public void incrementIndex() {
            this.index++;
        }
        // --
        public int getLine() {
            return this.line;
        }
        public int getIndexAtLine() {
            return this.indexAtLine;
        }
        public void incrementLine() {
            this.line++;
            this.indexAtLine = this.index;
        }
        // --
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.line);
            sb.append("/");
            sb.append(this.index);
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** JSON Parse Exception
    **/
    public static class JSONParsingException
        extends Exception
    {
        private int    index       = 0;
        private int    line        = 0;
        private int    indexAtLine = 0;
        private String jsonSrc     = null;
        public JSONParsingException(String msg, JSONParsingContext context, String jsonS) {
            super(msg);
            this.index       = (context != null)? context.getIndex()       : -1;
            this.line        = (context != null)? context.getLine()        : -1;
            this.indexAtLine = (context != null)? context.getIndexAtLine() : -1;
            this.jsonSrc     = jsonS;
        }
        public int getIndex() {
            return this.index;
        }
        public int getLine() {
            return this.line;
        }
        public int getIndexAtLine() {
            return this.indexAtLine;
        }
        public String getJsonSource() {
            return this.jsonSrc;
        }
        public String getParseErrorDisplay() {
            int    ndx = this.getIndex();
            int    L1  = this.getLine();
            int    Lx  = this.getIndexAtLine();
            String JS  = this.getJsonSource();
            if (StringTools.isBlank(JS) || (ndx < 0) || (ndx > JS.length()) || (L1 < 1)) {
                // -- nothing to display
                return "";
            }
            // --
            /*
            if (Lx >= 0) {
                int B = Lx;
                int E = Lx;
                for (;(B > 0) && (JS.charAt(B-1) != '\n');B--);
                for (;(E < JS.length()) && (JS.charAt(E) != '\n');E++);
            }
            */
            // --
            int ndxB = ndx;
            int ndxE = ndx;
            for (;(ndxB > 0) && (JS.charAt(ndxB-1) != '\n');ndxB--);
            for (;(ndxE < JS.length()) && (JS.charAt(ndxE) != '\n');ndxE++);
            // --
            StringBuffer sb = new StringBuffer();
            sb.append("---------------------------------------------------------\n");
            sb.append("Line " + L1 + ": \n");
            sb.append(JS.substring(ndxB,ndxE)).append("\n");
            sb.append(StringTools.replicateString(" ", (ndx-ndxB)));
            sb.append("^\n");
            sb.append("---------------------------------------------------------\n");
            return sb.toString();
        }
        public String toString() { // JSON.JSONParsingException
            String s = super.toString();
            return s + " ["+this.line+"/"+this.index+"]";
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // JSON._Object
    
    /**
    *** JSON Object class
    **/
    public static class _Object
        extends Vector<JSON._KeyValue>
    {

        private boolean formatIndent = true;
        private boolean wrapJsonBeanInClassName = false; // EXPERIMENTAL

        // -----------------------------------------

        /**
        *** _Object: Constructor
        **/
        public _Object() {
            super();
        }

        /**
        *** _Object: clone Constructor (shallow copy)
        **/
        public _Object(JSON._Object jObj) {
            this();
            if (jObj != null) {
                int cnt = jObj.getKeyValueCount();
                for (int i = 0; i < cnt; i++) {
                    JSON._KeyValue kv = jObj.getKeyValueAt(i);
                    this.addKeyValue(kv); // shallow copy
                }
            }
        }

        /**
        *** _Object: Constructor
        **/
        public _Object(Vector<JSON._KeyValue> list) {
            this();
            this.addAll(list);
        }

        /**
        *** _Object: Constructor
        **/
        public _Object(JSON._KeyValue... kv) {
            this();
            if (kv != null) {
                for (int i = 0; i < kv.length; i++) {
                    this.add(kv[i]);
                }
            }
        }

        /**
        *** _Object: Constructor
        **/
        public _Object(JSON.JSONBean jb) {
            this(jb, null/*filter*/);
        }

        /**
        *** _Object: Constructor
        **/
        public _Object(JSON.JSONBean jb, JSON.JSONBeanFilter filter) {
            this();
            JSON.toJsonBean(jb, filter, this);
        }

        /**
        *** _Object: Constructor
        *** Convenience/Shortcut for wrapping an _Object as a key/value pair.
        **/
        public _Object(String k, JSON._Object v) {
            this();
            this.addKeyValue(k, v);
        }

        // --------------------------------------

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(JSON._KeyValue kv) {
            return this.add(kv);
        }
        public boolean add(JSON._KeyValue kv) {
            if (kv == null) {
                return false;
            } else {
                if (!ALLOW_OBJECT_DUPLICATES) {
                    JSON._KeyValue _kv = this.getKeyValue(kv.getKey());
                    if (_kv != null) {
                        // -- found existing entry, replace with new _Value
                        _kv._setValue(kv.getValue());
                        return true;
                    }
                }
                return super.add(kv);
            }
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, String value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, JSON.JSONBean value) {
            return this.add(new JSON._KeyValue(key, value, null/*filter*/));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, JSON.JSONBean value, JSON.JSONBeanFilter filter) {
            return this.add(new JSON._KeyValue(key, value, filter));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, int value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, long value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, double value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, double value, String format) {
            return this.add(new JSON._KeyValue(key, value, format));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, boolean value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, Object value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, JSON._Array value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, JSON._Object value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        /**
        *** _Object: Adds a key/value pair to this object
        **/
        public boolean addKeyValue(String key, JSON._Value value) {
            return this.add(new JSON._KeyValue(key, value));
        }

        // --------------------------------------

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, String value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, JSON.JSONBean value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, JSON.JSONBean value, JSON.JSONBeanFilter filter) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value, filter);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, int value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, long value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, double value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, double value, String format) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value, format);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, boolean value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, Object value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, JSON._Array value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, JSON._Object value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        /**
        *** _Object: Replaces the specified key with the specified value
        **/
        public boolean replaceKeyValue(String key, JSON._Value value) {
            this.removeKeyValue(key);
            return this.addKeyValue(key, value);
        }

        // --------------------------------------

        /**
        *** _Object: Gets the number of key/value pairs in this object
        **/
        public int getKeyValueCount() {
            return super.size();
        }

        /**
        *** _Object: Gets the key/value pair at the specified index
        **/
        public JSON._KeyValue getKeyValueAt(int ndx) {
            if ((ndx >= 0) && (ndx < this.size())) {
                return this.get(ndx);
            } else {
                return null;
            }
        }

        // --------------------------------------

        /**
        *** _Object: Gets the key/value pair for the specified name
        **/
        public JSON._KeyValue getKeyValue(String n) {
            if (n != null) {
                for (JSON._KeyValue kv : this) {
                    String kvn = kv.getKey();
                    if (JSON.NameEquals(n,kvn)) {
                        return kv;
                    }
                }
            }
            return null;
        }

        // --------------------------------------

        /**
        *** _Object: Removes all key/value entries from this Object which match the specified key
        *** @return The last key/value entry removed, or null if no entries were removed
        **/
        public JSON._KeyValue removeKeyValue(String n)
        {
            JSON._KeyValue rtn = null;
            if (n != null) {
                for (int i = 0; i < this.getKeyValueCount();) {
                    JSON._KeyValue kv = this.getKeyValueAt(i);
                    String kvn = kv.getKey();
                    if (JSON.NameEquals(n,kvn)) {
                        rtn = this.remove(i);
                    } else {
                        i++;
                    }
                }
            }
            return rtn;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the JSON._Value for the specified name
        **/
        public JSON._Value getValueForName(String n) {
            JSON._KeyValue kv = this.getKeyValue(n);
            return (kv != null)? kv.getValue() : null;
        }

        /**
        *** _Object: Gets the JSON._Value for the specified name
        **/
        public JSON._Value getValueForName(String name[]) {
            if (name != null) {
                for (String n : name) {
                    JSON._Value jv = this.getValueForName(n);
                    if (jv != null) {
                        return jv;
                    }
                }
            }
            return null;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the JSON._Value for the specified path
        **/
        public JSON._Value getValueForPath(String path) {
            return JSON.GetValueForPath(this, path);
        }

        /**
        *** _Object: Gets the JSON._Value for the specified path
        **/
        public JSON._Value getValueForPath(String... path) {
            return JSON.GetValueForPath(this, path);
        }

        /**
        *** _Object: Gets the Java Object for the specified path
        **/
        public Object getJavaObjectForPath(String path) {
            JSON._Value val = this.getValueForPath(path);
            return (val != null)? val.getJavaObject() : null;
        }

        /**
        *** _Object: Gets the Java Object for the specified path
        **/
        public Object getJavaObjectForPath(String... path) {
            JSON._Value val = this.getValueForPath(path);
            return (val != null)? val.getJavaObject() : null;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the JSON._Array for the specified name
        **/
        public JSON._Array getArrayForName(String name, JSON._Array dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getArrayValue(dft) : dft;
        }

        /**
        *** _Object: Gets the JSON._Array for the specified name
        **/
        public JSON._Array getArrayForName(String name[], JSON._Array dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getArrayValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the JSON._Object value for the specified name
        **/
        public JSON._Object getObjectForName(String name, JSON._Object dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getObjectValue(dft) : dft;
        }

        /**
        *** _Object: Gets the JSON._Object value for the specified name
        **/
        public JSON._Object getObjectForName(String name[], JSON._Object dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getObjectValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the String value for the specified name
        **/
        public String getStringForName(String name, String dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getStringValue(dft) : dft;
        }

        /**
        *** _Object: Gets the String value for the specified name
        **/
        public String getStringForName(String name[], String dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getStringValue(dft) : dft;
        }

        /**
        *** _Object: Gets the JSON._Array for the specified name
        **/
        public String[] getStringArrayForName(String name, String dft[]) {
            JSON._Value jv = this.getValueForName(name);
          //JSON._Array ar = (jv != null)? jv.getArrayValue(null) : null;
          //return (ar != null)? ar.getStringArray() : dft;
            return (jv != null)? jv.getStringArrayValue(dft) : dft;
        }

        /**
        *** _Object: Gets the JSON._Array for the specified name
        **/
        public String[] getStringArrayForName(String name[], String dft[]) {
            JSON._Value jv = this.getValueForName(name);
          //JSON._Array ar = (jv != null)? jv.getArrayValue(null) : null;
          //return (ar != null)? ar.getStringArray() : dft;
            return (jv != null)? jv.getStringArrayValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the Integer value for the specified name
        **/
        public int getIntForName(String name, int dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getIntValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Integer value for the specified name
        **/
        public int getIntForName(String name[], int dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getIntValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Integer array value for the specified name
        **/
        public int[] getIntArrayForName(String name, int[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getIntArrayValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Integer array value for the specified name
        **/
        public int[] getIntArrayForName(String name[], int[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getIntArrayValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the Long value for the specified name
        **/
        public long getLongForName(String name, long dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getLongValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Long value for the specified name
        **/
        public long getLongForName(String name[], long dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getLongValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Long array value for the specified name
        **/
        public long[] getLongArrayForName(String name, long[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getLongArrayValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Long array value for the specified name
        **/
        public long[] getLongArrayForName(String name[], long[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getLongArrayValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the Double value for the specified name
        **/
        public double getDoubleForName(String name, double dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getDoubleValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Double value for the specified name
        **/
        public double getDoubleForName(String name[], double dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getDoubleValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Double array value for the specified name
        **/
        public double[] getDoubleArrayForName(String name, double[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getDoubleArrayValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Double array value for the specified name
        **/
        public double[] getDoubleArrayForName(String name[], double[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getDoubleArrayValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Gets the Boolean value for the specified name
        **/
        public boolean getBooleanForName(String name, boolean dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getBooleanValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Boolean value for the specified name
        **/
        public boolean getBooleanForName(String name[], boolean dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getBooleanValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Boolean array value for the specified name
        **/
        public boolean[] getBooleanArrayForName(String name, boolean[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getBooleanArrayValue(dft) : dft;
        }

        /**
        *** _Object: Gets the Boolean array value for the specified name
        **/
        public boolean[] getBooleanArrayForName(String name[], boolean[] dft) {
            JSON._Value jv = this.getValueForName(name);
            return (jv != null)? jv.getBooleanArrayValue(dft) : dft;
        }

        // --------------------------------------

        /**
        *** _Object: Sort by key name
        **/
        public void sortByName(boolean recursive) {
            Comparator<JSON._KeyValue> comp = new Comparator<JSON._KeyValue>() {
                public int compare(JSON._KeyValue kv1, JSON._KeyValue kv2) {
                    String k1 = StringTools.trim(kv1.getKey());
                    String k2 = StringTools.trim(kv2.getKey());
                    return k1.compareTo(k2);
                }
            };
            this._sort(comp, recursive);
        }

        /**
        *** _Object: Sort by Comparator
        **/
        public void sortByComparator(Comparator<JSON._KeyValue> comp, boolean recursive) {
            this._sort(comp, recursive);
        }

        /**
        *** _Object: Sort by Comparator
        **/
        private void _sort(Comparator<JSON._KeyValue> comp, boolean recursive) {
            Collections.sort(this, comp);
            if (recursive) {
                for (JSON._KeyValue kv : this) {
                    JSON._Value  val = (kv  != null)? kv.getValue() : null;
                    JSON._Object obj = (val != null)? val.getObjectValue(null) : null;
                    if (obj != null) {
                        obj._sort(comp, true);
                    }
                }
            }
        }

        // --------------------------------------

        /**
        *** _Object: Gets a list of all key names in this object
        **/
        public Collection<String> getKeyNames() {
            Collection<String> keyList = new Vector<String>();
            for (JSON._KeyValue kv : this) {
                keyList.add(kv.getKey());
            }
            return keyList;
        }

        /**
        *** _Object: Print object contents (for debug purposes only)
        **/
        public void debugDisplayObject(int level) {
            String pfx0 = StringTools.replicateString(INDENT,level);
            String pfx1 = StringTools.replicateString(INDENT,level+1);
            for (String key : this.getKeyNames()) {
                JSON._KeyValue kv = this.getKeyValue(key);
                Object val = kv.getValue().getJavaObject();
                Print.sysPrintln(pfx0 + key + " ==> " + StringTools.className(val));
                if (val instanceof JSON._Object) {
                    JSON._Object obj = (JSON._Object)val;
                    obj.debugDisplayObject(level+1);
                } else
                if (val instanceof JSON._Array) {
                    JSON._Array array = (JSON._Array)val;
                    for (JSON._Value jv : array) {
                        Object av = jv.getJavaObject();
                        Print.sysPrintln(pfx1 + " ==> " + StringTools.className(av));
                        if (av instanceof JSON._Object) {
                            JSON._Object obj = (JSON._Object)av;
                            obj.debugDisplayObject(level+2);
                        }
                    }
                }
            }
        }

        // --------------------------------------

        /**
        *** _Object: Set format indent state
        **/
        public _Object setFormatIndent(boolean indent) {
            this.formatIndent = indent;
            return this;
        }

        /**
        *** _Object: Write a String representation of this instance to the StringBuffer
        **/
        public StringBuffer toStringBuffer(int prefix, StringBuffer sb) {
            if (sb == null) { sb = new StringBuffer(); }
            boolean fullFormat = this.formatIndent && (prefix >= 0);
            String pfx0 = fullFormat? JSON.indent(prefix)   : "";
            String pfx1 = fullFormat? JSON.indent(prefix+1) : "";
            sb.append("{");
            if (fullFormat) {
                sb.append("\n");
            }
            if (this.size() > 0) {
                int size = this.size();
                for (int i = 0; i < size; i++) {
                    JSON._KeyValue kv = this.get(i);
                    sb.append(pfx1);
                    kv.toStringBuffer((fullFormat?(prefix+1):-1),sb);
                    if ((i + 1) < size) {
                        sb.append(",");
                    }
                    if (fullFormat) {
                        sb.append("\n");
                    }
                }
            }
            sb.append(pfx0).append("}");
            if (fullFormat && (prefix == 0)) {
                sb.append("\n");
            }
            return sb;
        }

        /**
        *** _Object: Returns a String representation of this instance 
        **/
        public String toString() { // JSON._Object
            return this.toStringBuffer(0,null).toString();
        }

        /**
        *** _Object: Returns a String representation of this instance 
        **/
        public String toString(boolean inclPrefix/*indent*/) { // JSON._Object
            return this.toStringBuffer((inclPrefix/*indent*/?0:-1),null).toString();
        }

    } // _Object

    // ------------------------------------------------------------------------

    /**
    *** Parse a JSON Comment from the specified String, starting at the 
    *** specified location
    **/
    public static String parse_Comment(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int    len  = StringTools.length(v);
        String val  = null;

        /* skip leading whitespace */
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else {
                break;
            }
        }

        /* next characters must be "/*" */
        int startLine  = context.getLine();
        int startIndex = context.getIndex();
        if ((startIndex + 2) >= len) {
            throw new JSONParsingException("Overflow", context, v);
        } else
        if ((v.charAt(startIndex  ) != '/') ||
            (v.charAt(startIndex+1) != '*')   ) {
            throw new JSONParsingException("Invalid beginning of comment", context, v);
        }
        context.incrementIndex(2);

        /* parse comment body */
        StringBuffer comment = new StringBuffer();
        commentParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                comment.append(ch);
                continue; // skip space
            } else
            if (ch == '*') {
                context.incrementIndex();
                int ndx = context.getIndex();
                if (ndx >= len) {
                    throw new JSONParsingException("Overflow", context, v);
                } else
                if ((v.charAt(ndx) == '/')) {
                    context.incrementIndex(); // consume final '/'
                    break commentParse;
                } else {
                    comment.append(ch);
                }
                continue;
            } else {
                comment.append(ch);
                context.incrementIndex();
            }
        } // commentParse
        val = comment.toString().trim();

        /* return comment */
        return val;

    }

    // ------------------------------------------------------------------------

    /**
    *** Parse a JSON Object from the specified String.
    *** Does not return null.
    **/
    public static _Object parse_Object(String v)
        throws JSONParsingException 
    {
        return JSON.parse_Object(v,new JSONParsingContext());
    }

    /**
    *** Parse a JSON Object from the specified String, starting at the 
    *** specified location.
    *** Does not return null.
    **/
    public static _Object parse_Object(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int          len  = StringTools.length(v);
        JSON._Object obj  = null;
        boolean      comp = false;

        objectParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                // -- skip whitespace
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else
            if (ch == '/') {
                // -- start of comment (non-standard JSON)
                String comment = JSON.parse_Comment(v, context);
                continue; // skip comment
            } else
            if (ch == '{') {
                // -- start of object
                if (obj != null) {
                    throw new JSONParsingException("Object already started", context, v);
                }
                context.incrementIndex();
                obj = new JSON._Object();
            } else
            if (ch == '\"') {
                // -- "key": VALUE
                if (obj == null) {
                    throw new JSONParsingException("No start of Object", context, v);
                }
                JSON._KeyValue kv = JSON.parse_KeyValue(v, context);
                if (kv == null) {
                    throw new JSONParsingException("Invalid KeyValue ...", context, v);
                }
                obj.add(kv);
            } else
            if (ch == ',') {
                // -- ignore extraneous commas (non-standard JSON)
                context.incrementIndex();
            } else
            if (ch == '}') {
                // -- end of object
                context.incrementIndex();
                if (obj == null) {
                    throw new JSONParsingException("No start of Object", context, v);
                }
                comp = true; // iff Object is defined
                break objectParse;
            } else {
                // -- invalid character
                throw new JSONParsingException("Invalid JSON syntax ...", context, v);
            }
        } // objectParse

        /* object completed? */
        if (!comp || (obj == null)) {
            throw new JSONParsingException("Incomplete Object", context, v);
        }

        /* return object */
        return obj;
        
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // JSON._KeyValue

    /**
    *** JSON Key/Value pair
    **/
    public static class _KeyValue
    {

        private String      key   = "";
        private JSON._Value value = null;

        // -----------------------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, String value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, JSON.JSONBean value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value,null/*filter*/);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, JSON.JSONBean value, JSON.JSONBeanFilter filter) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value,filter);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Byte value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, byte value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Short value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, short value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Integer value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, int value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Long value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, long value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Float value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, float value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Double value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, double value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, double value, String format) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value, format);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Boolean value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, boolean value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, JSON._Value value) {
            this.key   = StringTools.trim(key);
            this.value = value;
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, JSON._Array value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, JSON._Object value) {
            this.key   = StringTools.trim(key);
            this.value = new JSON._Value(value);
        }

        // ----------------------------

        /**
        *** _KeyValue: Constructor 
        **/
        public _KeyValue(String key, Object value) {
            this.key   = StringTools.trim(key);
            if (value instanceof JSON._Value) {
                this.value = (JSON._Value)value;
            } else {
                this.value = new JSON._Value(value);
            }
        }

        // -----------------------------------------

        /**
        *** _KeyValue: Gets the key of this key/value pair 
        **/
        public String getKey() {
            return this.key;
        }

        /**
        *** _KeyValue: Gets the value of this key/value pair 
        **/
        public JSON._Value getValue() {
            return this.value;
        }

        /**
        *** _KeyValue: Sets the value of this key/value pair
        **/
        protected JSON._Value _setValue(JSON._Value val) {
            JSON._Value oldValue = this.value;
            this.value = (val != null)? val : new JSON._Value((Object)null);
            return oldValue;
        }

        /**
        *** _KeyValue: Gets the Java Object value of this key/value pair
        **/
        public Object getJavaObjectValue()
        {
            return (this.value != null)? this.value.getJavaObject() : null;
        }

        // -----------------------------------------

        /**
        *** _KeyValue: Write a String representation of this instance to the StringBuffer
        **/
        public StringBuffer toStringBuffer(int prefix, StringBuffer sb) {
            if (sb == null) { sb = new StringBuffer(); }
            sb.append("\"");
            sb.append(JSON.escapeJSON(this.key));
            sb.append("\"");
            sb.append(":");
            if (prefix >= 0) {
                sb.append(" ");
            }
            if (this.value != null) {
                this.value.toStringBuffer(prefix,sb);
            } else {
                sb.append("null");
            }
            return sb;
        }
        
        /**
        *** _KeyValue: Returns a String representation of this instance 
        **/
        public String toString() { // JSON._KeyValue
            return this.toStringBuffer(1,null).toString();
        }

        /**
        *** _KeyValue: Returns a String representation of this instance 
        **/
        public String toString(boolean inclPrefix/*indent*/) { // JSON._KeyValue
            return this.toStringBuffer((inclPrefix/*indent*/?1:-1),null).toString();
        }

    }

    /**
    *** Parse a Key/Value pair from the specified String at the specified location
    **/
    public static JSON._KeyValue parse_KeyValue(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int            len  = StringTools.length(v);
        JSON._KeyValue kv   = null;
        boolean        comp = false;

        String key = null;
        boolean colon = false;
        keyvalParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else
            if (ch == '/') {
                String comment = JSON.parse_Comment(v, context);
                continue; // skip comment
            } else
            if (!colon && (ch == '\"')) {
                // -- Key
                key = JSON.parse_String(v, context);
                if (key == null) {
                    throw new JSONParsingException("Invalid key String", context, v);
                }
            } else
            if (ch == ':') {
                if (colon) {
                    throw new JSONParsingException("More than one ':'", context, v);
                } else
                if (key == null) {
                    throw new JSONParsingException("Key not defined", context, v);
                }
                context.incrementIndex();
                colon = true;
            } else {
                // -- JSON._Value
                JSON._Value val = JSON.parse_Value(v, context);
                if (val == null) {
                    throw new JSONParsingException("Invalid value", context, v);
                }
                kv = new JSON._KeyValue(key,val);
                comp = true;
                break keyvalParse;
            }
        } // keyvalParse

        /* key/value completed? */
        if (!comp) {
            throw new JSONParsingException("Incomplete Key/Value", context, v);
        }

        /* return key/value */
        return kv; // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- JSON._Value
    
    /**
    *** Converts the specified object into a JSON value type.
    **/
    public static Object toJsonValue(Object v, JSON.JSONBeanFilter filter)
    {

        /* misc JSON object types */
        if (v == null) {
            return null;
        } else
        if (v instanceof JSON._Value) {
            return ((JSON._Value)v).getJavaObject(); // already converted to valid JSON type
        } else
        if (v instanceof JSON._Array) {
            return v;
        }

        /* JSON._Object */
        if (v instanceof JSON._Object) {
            return v;
        } else
        if (v instanceof JSON._Object[]) {
            return new JSON._Array((JSON._Object[])v);
        }

        /* String */
        if (v instanceof String) {
            return v;
        } else
        if (v instanceof String[]) {
            return new JSON._Array((String[])v);
        }

        /* Double */
        if (v instanceof Double) {
            return v;
        } else
        if (v instanceof double[]) {
            return new JSON._Array((double[])v);
        }

        /* Float */
        if (v instanceof Float) {
            return new Double(((Float)v).doubleValue());
        } else
        if (v instanceof float[]) {
            return new JSON._Array((float[])v);
        }

        /* Long */
        if (v instanceof Long) {
            return v;
        } else
        if (v instanceof long[]) {
            return new JSON._Array((long[])v);
        }

        /* Integer */
        if (v instanceof Integer) {
            return new Long(((Integer)v).longValue());
        } else
        if (v instanceof int[]) {
            return new JSON._Array((int[])v);
        } 

        /* Short */
        if (v instanceof Short) {
            return new Long(((Short)v).longValue());
        } else
        if (v instanceof short[]) {
            return new JSON._Array((short[])v);
        }

        /* Byte */
        if (v instanceof Byte) {
            return new Long(((Byte)v).longValue());
        } else
        if (v instanceof byte[]) {
            return new JSON._Array((byte[])v);
        }

        /* Boolean */
        if (v instanceof Boolean) {
            return v;
        } else
        if (v instanceof boolean[]) {
            return new JSON._Array((boolean[])v);
        }

        /* JSON.JSONBeanWrap/JSON.JSONBean */
        if (v instanceof JSON.JSONBeanWrap) {
            JSON.JSONBeanWrap jbw = (JSON.JSONBeanWrap)v;
            if (jbw.isScalar()) {
                return JSON.toJsonValue(jbw.getJavaBean(), filter);
            } else
            if (jbw.isArray()) {
                return new JSON._Array(jbw.getArray(), filter);
            } else
            if (jbw.isCollection()) {
                return new JSON._Array(jbw.getCollection(), filter);
            } else {
                return new JSON._Object((JSON.JSONBean)v, filter); // toJsonBean
            }
        } else
        if (v instanceof JSON.JSONBean) {
            return new JSON._Object((JSON.JSONBean)v, filter); // toJsonBean
        } else
        if (v instanceof JSON.JSONBean[]) {
            return new JSON._Array((JSON.JSONBean[])v, filter); // toJsonBean
        }

        /* Collection (Set, Vector, ...) */
        if (v instanceof Collection) {
            Collection<?> list = (Collection<?>)v;
            JSON._Value jbVal[] = new JSON._Value[list.size()];
            int i = 0;
            for (Object obj : list) {
                if (obj instanceof String) {
                    jbVal[i++] = new JSON._Value((String)obj);
                } else
                if (obj instanceof Byte) {
                    jbVal[i++] = new JSON._Value((Byte)obj);
                } else
                if (obj instanceof Short) {
                    jbVal[i++] = new JSON._Value((Short)obj);
                } else
                if (obj instanceof Integer) {
                    jbVal[i++] = new JSON._Value((Integer)obj);
                } else
                if (obj instanceof Long) {
                    jbVal[i++] = new JSON._Value((Long)obj);
                } else
                if (obj instanceof Float) {
                    jbVal[i++] = new JSON._Value((Float)obj);
                } else
                if (obj instanceof Double) {
                    jbVal[i++] = new JSON._Value((Long)obj);
                } else
                if (obj instanceof Boolean) {
                    jbVal[i++] = new JSON._Value((Boolean)obj);
                } else
                if (obj instanceof JSON._Array) {
                    jbVal[i++] = new JSON._Value((JSON._Array)obj);
                } else
                if (obj instanceof JSON._Object) {
                    jbVal[i++] = new JSON._Value((JSON._Object)obj);
                } else
                if (obj instanceof JSON.JSONBean) {
                    jbVal[i++] = new JSON._Value((JSON.JSONBean)obj, filter);
                } else {
                    jbVal[i++] = new JSON._Value(new JSON.JSONBeanWrap(obj), filter);
                }
            }
            return new JSON._Array(jbVal);
        }

        /* Properties */
        if (v instanceof Properties) {
            return new JSON._Object(new JSON.JSONBeanWrap(v), filter); // toJsonBean
        } else
        if (v instanceof Properties[]) {
            Properties props[] = (Properties[])v;
            JSON.JSONBean jbArr[] = new JSON.JSONBean[props.length];
            for (int i = 0; i < jbArr.length; i++) {
                jbArr[i] = new JSON.JSONBeanWrap(props[i]);
            }
            return new JSON._Array(jbArr, filter);
        }

        /* RTProperties */
        if (v instanceof RTProperties) {
            return new JSON._Object(new JSON.JSONBeanWrap(v), filter); // toJsonBean
        } else
        if (v instanceof RTProperties[]) {
            RTProperties props[] = (RTProperties[])v;
            JSON.JSONBean jbArr[] = new JSON.JSONBean[props.length];
            for (int i = 0; i < jbArr.length; i++) {
                jbArr[i] = new JSON.JSONBeanWrap(props[i]);
            }
            return new JSON._Array(jbArr, filter);
        }

        /* HashMap */
        if (v instanceof Map) { // HashMap
            return new JSON._Object(new JSON.JSONBeanWrap(v), filter); // toJsonBean
        } else
        if (v instanceof Map[]) { // HashMap[]
            Map<?,?> props[] = (Map[])v;
            JSON.JSONBean jbArr[] = new JSON.JSONBean[props.length];
            for (int i = 0; i < jbArr.length; i++) {
                jbArr[i] = new JSON.JSONBeanWrap(props[i]);
            }
            return new JSON._Array(jbArr, filter);
        }

        /* Hashtable */
        if (v instanceof Dictionary) { // Hashtable
            return new JSON._Object(new JSON.JSONBeanWrap(v), filter); // toJsonBean
        } else
        if (v instanceof Dictionary[]) { // Hashtable[]
            Dictionary<?,?> props[] = (Dictionary[])v;
            JSON.JSONBean jbArr[] = new JSON.JSONBean[props.length];
            for (int i = 0; i < jbArr.length; i++) {
                jbArr[i] = new JSON.JSONBeanWrap(props[i]);
            }
            return new JSON._Array(jbArr, filter);
        }

        /* array? */
        if (v.getClass().isArray()) {
            try {
                int aLen = Array.getLength(v);
                JSON.JSONBean jbArr[] = new JSON.JSONBean[aLen];
                for (int i = 0; i < aLen; i++) {
                    Object val = Array.get(v,i);
                    jbArr[i] = new JSON.JSONBeanWrap(val);
                }
                return new JSON._Array(jbArr, filter);
            } catch (Throwable th) { // NPE, IllegalArgumentException, ArrayIndexOutOfBoundsException
                // -- unlikely
                return v.toString();
            }
        }

        /* otherwise unknown, convert to String */
        return v.toString();

    }

    /**
    *** JSON Value
    **/
    public static class _Value
    {

        private Object value = null;
        // -- "value" must be one of the following datatypes:
        // -    null
        // -    JSON._Array
        // -    JSON._Object
        // -    String
        // -    Double (Float converted to Double)
        // -    Long (Integer/Short/Byte converted to Long)
        // -    Boolean

        private String valueFmt = null; // double format

        // -----------------------------------------

        /**
        *** _Value: Constructor 
        **/
        public _Value() {
            this.value = null;
        }

        // ----------------------------

        /**
        *** _Value: Generic "Object" Constructor.
        *** Value must be one of type String, Float(converted to Double), Double, Integer(converted to Long),
        *** Long, Boolean, JSON._Value, JSON._Array, or JSON._Object. (may also be null).
        **/
        public _Value(Object v) {
            this.value = JSON.toJsonValue(v, null); // null iff 'v' is null
        }

        // ----------------------------

        /**
        *** _Value: "JSONBean" Constructor 
        **/
        public _Value(JSON.JSONBean v) {
            this(v, null);
        }

        /**
        *** _Value: "JSONBean" Constructor 
        **/
        public _Value(JSON.JSONBean v, JSON.JSONBeanFilter filter) {
            this.value = JSON.toJsonValue(v, filter);
        }

        // ----------------------------

        /**
        *** _Value: "String" Constructor 
        **/
        public _Value(String v) {
            this.value = v; // may be null
        }

        // ----------------------------

        /**
        *** _Value: "Byte" Constructor (converted to Long)
        **/
        public _Value(Byte v) {
            this((v != null)? new Long(v.longValue()) : null);
        }

        /**
        *** _Value: "byte" Constructor (converted to Long)
        **/
        public _Value(byte v) {
            this(new Long((long)v));
        }

        // ----------------------------

        /**
        *** _Value: "Short" Constructor (converted to Long)
        **/
        public _Value(Short v) {
            this((v != null)? new Long(v.longValue()) : null);
        }

        /**
        *** _Value: "short" Constructor (converted to Long)
        **/
        public _Value(short v) {
            this(new Long((long)v));
        }

        // ----------------------------

        /**
        *** _Value: "Integer" Constructor (converted to Long)
        **/
        public _Value(Integer v) {
            this((v != null)? new Long(v.longValue()) : null);
        }

        /**
        *** _Value: "int" Constructor (converted to Long)
        **/
        public _Value(int v) {
            this(new Long((long)v));
        }

        // ----------------------------

        /**
        *** _Value: "Long" Constructor 
        **/
        public _Value(Long v) {
            this.value = v; // may be null
        }

        /**
        *** _Value: "long" Constructor 
        **/
        public _Value(long v) {
            this(new Long(v));
        }

        // ----------------------------

        /**
        *** _Value: "Float" Constructor (converted to Double)
        **/
        public _Value(Float v) {
            this((v != null)? new Double(v.doubleValue()) : null);
        }

        /**
        *** _Value: "float" Constructor 
        **/
        public _Value(Float v, String format) {
            this(v);
            this.valueFmt = format;
        }

        /**
        *** _Value: "float" Constructor (converted to Double)
        **/
        public _Value(float v) {
            this(new Double((double)v));
        }

        /**
        *** _Value: "float" Constructor 
        **/
        public _Value(float v, String format) {
            this(new Double((double)v), format);
        }

        // ----------------------------

        /**
        *** _Value: "Double" Constructor 
        **/
        public _Value(Double v) {
            this.value = v; // may be null
        }

        /**
        *** _Value: "double" Constructor 
        **/
        public _Value(Double v, String format) {
            this(v);
            this.valueFmt = format;
        }

        /**
        *** _Value: "double" Constructor 
        **/
        public _Value(double v) {
            this(new Double(v));
        }

        /**
        *** _Value: "double" Constructor 
        **/
        public _Value(double v, String format) {
            this(new Double(v), format);
        }

        // ----------------------------

        /**
        *** _Value: "Boolean" Constructor 
        **/
        public _Value(Boolean v) {
            this.value = v;
        }

        /**
        *** _Value: "boolean" Constructor 
        **/
        public _Value(boolean v) {
            this(new Boolean(v));
        }

        // ----------------------------

        /**
        *** _Value: "_Value" clone Constructor 
        **/
        public _Value(JSON._Value v) {
            this.value = (v != null)? v.value : null; // shallow copy
        }

        // ----------------------------

        /**
        *** _Value: "_Array" Constructor 
        **/
        public _Value(JSON._Array v) {
            this.value = v; // may be null
        }

        // ----------------------------

        /**
        *** _Value: "_Object" Constructor 
        **/
        public _Value(JSON._Object v) {
            this.value = v; // may be null
        }

        // -----------------------------------------

        /**
        *** _Value: Gets the value as a Java Object.
        *** Will be one of the following class types:<br>
        ***  - JSON._Object<br>
        ***  - JSON._Array<br>
        ***  - Double<br>
        ***  - Long<br>
        ***  - String<br>
        ***  - Boolean<br>
        ***  - null<br>
        **/
        public Object getJavaObject() {
            return this.value;
        }

        //@Deprecated
        //public Object _getObjectValue() {
        //    return this.value;
        //}

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value represents scalar quantity.
        *** Includes types: String, Number(Double,Long,etc), and Boolean
        **/
        public boolean isScalarValue() {
            return JSON.IsScalarValue(this.value);
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value represents a nul Object 
        **/
        public boolean isNullValue() {
            return (this.value == null);
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value parsed into a String 
        **/
        public boolean isStringValue(boolean strict) {
            if (strict) {
                return (this.value instanceof String);
            } else {
                return (this.value != null);
            }
        }

        /**
        *** _Value: Returns true if this value represents a String 
        **/
        public boolean isStringValue() {
            return this.isStringValue(true);
        }

        /**
        *** _Value: Gets the String representation of this value if the value type is one of
        *** String, Long, Double, or Boolean
        **/
        public String getStringValue(String dft) {
            if (this.value instanceof String) {
                return (String)this.value;
            } else
            if (this.value instanceof Number) { // Long/Double
                return this.value.toString();
            } else
            if (this.value instanceof Boolean) {
                return this.value.toString();
            } else  {
                return dft;
            }
        }

        /**
        *** _Value: Gets the contents as a String array
        *** (if not an array, will attempt parse and return as a single element array)
        **/
        public String[] getStringArrayValue(String dft[]) {
            if (this.isArrayValue()) {
                JSON._Array strA = this.getArrayValue(null); // will not be null here
                return (strA != null)? strA.getStringArray() : dft;
            } else
            if (!this.isNullValue()) { // if non-null, can be converted to String
                String s = this.getStringValue(null); // likely non-null
                return (s != null)? new String[] { s } : dft;
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value can be parsed into a Long 
        **/
        public boolean isLongValue(boolean strict) {
            if (strict) {
                return (this.value instanceof Long);
            } else {
                return StringTools.isLong(this.value, false);
            }
        }

        /**
        *** _Value: Returns true if this value represents a Long 
        **/
        public boolean isLongValue() {
            return this.isLongValue(true);
        }

        /**
        *** _Value: Gets the Long representation of this value if the value type is one of
        *** Number(longValue), String(parseLong), or Boolean('0' if false, '1' otherwise)
        **/
        public long getLongValue(long dft) {
            if (this.value instanceof Number) { // Long/Double
                return ((Number)this.value).longValue();
            } else
            if (this.value instanceof Boolean) {
                return ((Boolean)this.value).booleanValue()? 1L : 0L;
            } else
            if (this.value instanceof String) {
                return StringTools.parseLong(this.value,dft);
            } else {
                return dft;
            }
        }

        /**
        *** _Value: Gets the contents as a Long array
        *** (if not an array, will attempt parse and return as a single element array)
        **/
        public long[] getLongArrayValue(long dft[]) {
            if (this.isArrayValue()) {
                JSON._Array strA = this.getArrayValue(null); // will not be null here
                return (strA != null)? strA.getLongArray() : dft;
            } else
            if (this.isLongValue(false)) {
                long v = this.getLongValue(0L);
                return new long[] { v };
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value can be parsed into a Long 
        **/
        public boolean isIntValue(boolean strict) {
            return this.isLongValue(strict);
        }

        /**
        *** _Value: Returns true if this value represents a Long 
        **/
        public boolean isIntValue() {
            return this.isLongValue(true);
        }

        /**
        *** _Value: Gets the Integer representation of this value if the value type is one of
        *** Number(intValue), String(parseInt), or Boolean('0' if false, '1' otherwise)
        **/
        public int getIntValue(int dft) {
            return (int)this.getLongValue((long)dft);
        }

        /**
        *** _Value: Gets the contents as a int array
        *** (if not an array, will attempt parse and return as a single element array)
        **/
        public int[] getIntArrayValue(int dft[]) {
            if (this.isArrayValue()) {
                JSON._Array strA = this.getArrayValue(null); // will not be null here
                return (strA != null)? strA.getIntArray() : dft;
            } else
            if (this.isIntValue(false)) {
                int v = this.getIntValue(0);
                return new int[] { v };
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value can be parsed into a Double 
        **/
        public boolean isDoubleValue(boolean strict) {
            if (strict) {
                return (this.value instanceof Double);
            } else {
                return StringTools.isDouble(this.value, false);
            }
        }

        /**
        *** _Value: Returns true if this value represents a Double 
        **/
        public boolean isDoubleValue() {
            return this.isDoubleValue(true);
        }

        /**
        *** _Value: Gets the Double representation of this value if the value type is one of
        *** Number(doubleValue), String(parseDouble), or Boolean('0.0' if false, '1.0' otherwise)
        **/
        public double getDoubleValue(double dft) {
            if (this.value instanceof Number) { // Long/Double
                return ((Number)this.value).doubleValue();
            } else
            if (this.value instanceof Boolean) {
                return ((Boolean)this.value).booleanValue()? 1.0 : 0.0;
            } else
            if (this.value instanceof String) {
                return StringTools.parseDouble(this.value,dft);
            } else {
                return dft;
            }
        }

        /**
        *** _Value: Gets the contents as a Double array
        *** (if not an array, will attempt parse and return as a single element array)
        **/
        public double[] getDoubleArrayValue(double dft[]) {
            if (this.isArrayValue()) {
                JSON._Array strA = this.getArrayValue(null); // will not be null here
                return (strA != null)? strA.getDoubleArray() : dft;
            } else
            if (this.isDoubleValue(false)) {
                double v = this.getDoubleValue(Double.NaN);
                return !Double.isNaN(v)? new double[] { v } : dft;
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Gets the Float representation of this value if the value type is one of
        *** Number, String, or Boolean
        **/
        public float getFloatValue(float dft) {
            return (float)this.getDoubleValue((double)dft);
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value can be parsed into a Boolean 
        **/
        public boolean isBooleanValue(boolean strict) {
            if (strict) {
                return (this.value instanceof Boolean);
            } else {
                return StringTools.isBoolean(this.value, false);
            }
        }

        /**
        *** _Value: Returns true if this value represents a Boolean 
        **/
        public boolean isBooleanValue() {
            return this.isBooleanValue(true);
        }

        /**
        *** _Value: Gets the Boolean representation of this value if the value type is one of
        *** Boolean(booleanValue), String(parseBoolean), or Number(false if '0', true otherwise)
        **/
        public boolean getBooleanValue(boolean dft) {
            if (this.value instanceof Boolean) {
                return ((Boolean)this.value).booleanValue();
            } else
            if (this.value instanceof Number) { // Long/Double
                return (((Number)this.value).longValue() != 0L)? true : false;
            } else
            if (this.value instanceof String) {
                return StringTools.parseBoolean(this.value,dft);
            } else {
                return dft;
            }
        }

        /**
        *** _Value: Gets the contents as a Boolean array
        *** (if not an array, will attempt parse and return as a single element array)
        **/
        public boolean[] getBooleanArrayValue(boolean dft[]) {
            if (this.isArrayValue()) {
                JSON._Array strA = this.getArrayValue(null); // will not be null here
                return (strA != null)? strA.getBooleanArray() : dft;
            } else
            if (this.isBooleanValue(false)) {
                boolean v = this.getBooleanValue(false);
                return new boolean[] { v };
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value represents a JSON._Array 
        **/
        public boolean isArrayValue() {
            return (this.value instanceof JSON._Array);
        }

        /**
        *** _Value: Gets the JSON._Array value 
        **/
        public JSON._Array getArrayValue(JSON._Array dft) {
            if (this.value instanceof JSON._Array) {
                return (JSON._Array)this.value;
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Returns true if this value represents a JSON._Object 
        **/
        public boolean isObjectValue() {
            return (this.value instanceof JSON._Object);
        }

        /**
        *** _Value: Gets the JSON._Object value 
        **/
        public JSON._Object getObjectValue(JSON._Object dft) {
            if (this.value instanceof JSON._Object) {
                return (JSON._Object)this.value;
            } else {
                return dft;
            }
        }

        // -----------------------------------------

        /**
        *** _Value: Returns the class of the value object
        **/
        public Class<?> getValueClass() {
            return (this.value != null)? this.value.getClass() : null;
        }
        
        /**
        *** _Value: Write a String representation of this instance to the StringBuffer
        **/
        public StringBuffer toStringBuffer(int prefix, StringBuffer sb) {
            if (sb == null) { sb = new StringBuffer(); }
            if (this.value == null) {
                sb.append("null");
            } else 
            if (this.value instanceof JSON._Array) {
                ((JSON._Array)this.value).toStringBuffer(prefix, sb);
            } else
            if (this.value instanceof JSON._Object) {
                ((JSON._Object)this.value).toStringBuffer(prefix, sb);
            } else
            if (this.value instanceof String) {
                sb.append("\"");
                sb.append(JSON.escapeJSON((String)this.value));
                sb.append("\"");
            } else
            if ((this.value instanceof Double) && !StringTools.isBlank(this.valueFmt)) { // Double
                sb.append(StringTools.format(((Double)this.value).doubleValue(),this.valueFmt));
            } else
            if (this.value instanceof Number) { // Long/Double
                sb.append(this.value.toString());
            } else
            if (this.value instanceof Boolean) {
                sb.append(this.value.toString());
            } else {
                // ignore
            }
            return sb;
        }

        /**
        *** _Value: Returns a String representation of this instance 
        **/
        public String toString() { // JSON._Value
            return this.toStringBuffer(0,null).toString();
        }

        /**
        *** _Value: Returns a String representation of this instance 
        **/
        public String toString(boolean inclPrefix/*indent*/) { // JSON._Value
            return this.toStringBuffer((inclPrefix/*indent*/?0:-1),null).toString();
        }

    }

    /**
    *** Parse a JSON Array from the specified String
    **/
    public static JSON._Value parse_Value(String v)
        throws JSONParsingException 
    {
        return JSON.parse_Value(v, new JSONParsingContext());
    }

    /**
    *** Parse JSON Value
    **/
    public static JSON._Value parse_Value(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int          len  = StringTools.length(v);
        JSON._Value  val  = null;
        boolean      comp = false;

        valueParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else
            if (ch == '/') {
                String comment = JSON.parse_Comment(v, context);
                continue; // skip comment
            } else
            if (ch == '\"') {
                // -- parse String
                String sval = JSON.parse_String(v, context);
                if (sval == null) {
                    throw new JSONParsingException("Invalid String value", context, v);
                } else {
                    val = new JSON._Value(sval);
                }
                comp = true;
                break valueParse;
            } else
            if ((ch == '-') || (ch == '+') || Character.isDigit(ch)) {
                // -- parse Number (Long/Double)
                Number num = JSON.parse_Number(v, context); // Long/Double
                if (num == null) {
                    throw new JSONParsingException("Invalid Number value", context, v);
                } else
                if (num instanceof Float) {
                    val = new JSON._Value((Float)num);
                } else
                if (num instanceof Double) {
                    val = new JSON._Value((Double)num);
                } else
                if (num instanceof Byte) {
                    //boolean hex = false;
                    val = new JSON._Value((Byte)num);
                } else
                if (num instanceof Short) {
                    //boolean hex = false;
                    val = new JSON._Value((Short)num);
                } else
                if (num instanceof Integer) {
                    //boolean hex = false;
                    val = new JSON._Value((Integer)num);
                } else
                if (num instanceof Long) {
                    //boolean hex = false;
                    val = new JSON._Value((Long)num);
                } else {
                    throw new JSONParsingException("Unsupported Number type: " + StringTools.className(num), context, v);
                }
                comp = true;
                break valueParse;
            } else
            if (ch == 't') { 
                // -- true
                context.incrementIndex();
                int ndx = context.getIndex();
                if ((ndx + 2) >= len) {
                    throw new JSONParsingException("Overflow", context, v);
                } else
                if ((v.charAt(ndx  ) == 'r') && 
                    (v.charAt(ndx+1) == 'u') && 
                    (v.charAt(ndx+2) == 'e')   ) {
                    context.incrementIndex(3);
                    val = new JSON._Value(Boolean.TRUE);
                } else {
                    throw new JSONParsingException("Invalid Boolean 'true'", context, v);
                }
                comp = true;
                break valueParse;
            } else
            if (ch == 'f') { 
                // -- false
                context.incrementIndex();
                int ndx = context.getIndex();
                if ((ndx + 3) >= len) {
                    throw new JSONParsingException("Overflow", context, v);
                } else
                if ((v.charAt(ndx  ) == 'a') && 
                    (v.charAt(ndx+1) == 'l') && 
                    (v.charAt(ndx+2) == 's') &&
                    (v.charAt(ndx+3) == 'e')   ) {
                    context.incrementIndex(4);
                    val = new JSON._Value(Boolean.FALSE);
                } else {
                    throw new JSONParsingException("Invalid Boolean 'false'", context, v);
                }
                comp = true;
                break valueParse;
            } else
            if (ch == 'n') { 
                // -- null
                context.incrementIndex();
                int ndx = context.getIndex();
                if ((ndx + 2) >= len) {
                    throw new JSONParsingException("Overflow", context, v);
                } else
                if ((v.charAt(ndx  ) == 'u') && 
                    (v.charAt(ndx+1) == 'l') && 
                    (v.charAt(ndx+2) == 'l')   ) {
                    context.incrementIndex(3);
                    val = new JSON._Value((JSON._Object)null); // null object
                } else {
                    throw new JSONParsingException("Invalid 'null'", context, v);
                }
                comp = true;
                break valueParse;
            } else
            if (ch == '[') {
                // -- JSON._Array
                JSON._Array array = JSON.parse_Array(v, context);
                if (array == null) {
                    throw new JSONParsingException("Invalid array", context, v);
                }
                val = new JSON._Value(array);
                comp = true;
                break valueParse;
            } else
            if (ch == '{') {
                // -- JSON._Object
                JSON._Object obj = JSON.parse_Object(v, context);
                if (obj == null) {
                    throw new JSONParsingException("Invalid object", context, v);
                }
                val = new JSON._Value(obj);
                comp = true;
                break valueParse;
            } else {
                throw new JSONParsingException("Unexpected character", context, v);
            }
        } // valueParse

        /* value completed? */
        if (!comp) {
            throw new JSONParsingException("Incomplete Value", context, v);
        }

        /* return value */
        return val; // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // JSON._Array

    /**
    *** JSON Array 
    **/
    public static class _Array
        extends Vector<JSON._Value>
    {

        private boolean formatIndent = true;

        // -----------------------------------------

        /**
        *** _Array: Constructor 
        **/
        public _Array() {
            super();
        }

        /**
        *** _Array: Constructor 
        *** An Array of other Values
        **/
        public _Array(JSON._Value... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.add(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of JSONBeans
        **/
        public _Array(JSON.JSONBean... array) {
            this(array,null/*filter*/);
        }
        /**
        *** _Array: Constructor 
        *** An Array of JSONBeans
        **/
        public _Array(JSON.JSONBean array[], JSON.JSONBeanFilter filter) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i], filter); // toJsonBean
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** A Collection of JSONBeans
        **/
        public _Array(Collection<JSON.JSONBean> list, JSON.JSONBeanFilter filter) {
            if (list != null) {
                for (JSON.JSONBean jb : list) {
                    this.addValue(jb, filter); // toJsonBean
                }
            }
        }


        /**
        *** _Array: Constructor 
        *** An Array of Strings
        **/
        public _Array(String... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Bytes
        **/
        public _Array(byte... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Shorts
        **/
        public _Array(short... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Integers
        **/
        public _Array(int... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Longs
        **/
        public _Array(long... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Doubles
        **/
        public _Array(double... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Floats
        **/
        public _Array(float... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Booleans
        **/
        public _Array(boolean... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of Objects
        **/
        public _Array(JSON._Object... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        /**
        *** _Array: Constructor 
        *** An Array of other Arrays
        **/
        public _Array(JSON._Array... array) {
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    this.addValue(array[i]);
                }
            }
        }

        // --------------------------------------

        /**
        *** _Array: Add a JSON._Value to this JSON._Array 
        **/
        public boolean add(JSON._Value value) {
            return super.add(value);
        }

        // ----------------------------

        /**
        *** _Array: Add a JSON._Value to this JSON._Array 
        **/
        public boolean addValue(JSON._Value value) {
            return this.add(value);
        }

        // ----------------------------

        /**
        *** _Array: Add a JSONBean to this JSON._Array 
        **/
        public boolean addValue(JSON.JSONBean value) {
            return this.add(new JSON._Value(value,null/*filter*/));
        }

        /**
        *** _Array: Add a JSONBean to this JSON._Array 
        **/
        public boolean addValue(JSON.JSONBean value, JSON.JSONBeanFilter filter) {
            return this.add(new JSON._Value(value,filter)); // toJsonBean
        }

        // ----------------------------

        /**
        *** _Array: Add a String to this JSON._Array 
        **/
        public boolean addValue(String value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Byte to this JSON._Array (converted to Long)
        **/
        public boolean addValue(Byte value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a byte to this JSON._Array (converted to Long)
        **/
        public boolean addValue(byte value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Short to this JSON._Array (converted to Long)
        **/
        public boolean addValue(Short value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a short to this JSON._Array (converted to Long)
        **/
        public boolean addValue(short value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Integer to this JSON._Array (converted to Long)
        **/
        public boolean addValue(Integer value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a int to this JSON._Array (converted to Long)
        **/
        public boolean addValue(int value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Long to this JSON._Array 
        **/
        public boolean addValue(Long value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a Long to this JSON._Array 
        **/
        public boolean addValue(long value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Float to this JSON._Array  (converted to Double)
        **/
        public boolean addValue(Float value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a float to this JSON._Array (converted to Double)
        **/
        public boolean addValue(float value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Double to this JSON._Array 
        **/
        public boolean addValue(Double value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a Double to this JSON._Array 
        **/
        public boolean addValue(double value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Boolean to this JSON._Array 
        **/
        public boolean addValue(Boolean value) {
            return this.add(new JSON._Value(value));
        }

        /**
        *** _Array: Add a Boolean to this JSON._Array 
        **/
        public boolean addValue(boolean value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a JSON._Object to this JSON._Array 
        **/
        public boolean addValue(JSON._Object value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a JSON._Array to this JSON._Array 
        **/
        public boolean addValue(JSON._Array value) {
            return this.add(new JSON._Value(value));
        }

        // ----------------------------

        /**
        *** _Array: Add a Object to this JSON._Array 
        **/
        public boolean addValue(Object value) {
            return this.add(new JSON._Value(value));
        }

        // --------------------------------------

        /**
        *** _Array: Returns the JSON._Value at the specified index
        **/
        public JSON._Value getValueAt(int ndx) {
            if ((ndx >= 0) && (ndx < this.size())) {
                return this.get(ndx);
            } else {
                return null;
            }
        }

        /**
        *** _Array: Returns the JSON._Array value at the specified index
        **/
        public JSON._Array getArrayValueAt(int ndx, JSON._Array dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getArrayValue(dft) : dft;
            } else {
                return dft;
            }
        }

        // --------------------------------------

        /**
        *** _Array: Returns the String value at the specified index
        **/
        public String getStringValueAt(int ndx, String dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getStringValue(dft) : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns a String array of values contained in this JSON Array
        **/
        public String[] getStringArray() {
            String v[] = new String[this.size()];
            for (int i = 0; i < v.length; i++) {
                v[i] = this.getStringValueAt(i,"");
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Returns the Integer value at the specified index
        **/
        public int getIntValueAt(int ndx, int dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getIntValue(dft) : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns an int array of values contained in this JSON Array
        **/
        public int[] getIntArray() {
            int v[] = new int[this.size()];
            for (int i = 0; i < v.length; i++) {
                v[i] = this.getIntValueAt(i,0);
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Returns the Long value at the specified index
        **/
        public long getLongValueAt(int ndx, long dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getLongValue(dft) : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns a long array of values contained in this JSON Array
        **/
        public long[] getLongArray() {
            long v[] = new long[this.size()];
            for (int i = 0; i < v.length; i++) {
                v[i] = this.getLongValueAt(i,0L);
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Returns the Double value at the specified index
        **/
        public double getDoubleValueAt(int ndx, double dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getDoubleValue(dft) : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns a double array of values contained in this JSON Array
        **/
        public double[] getDoubleArray() {
            double v[] = new double[this.size()];
            for (int i = 0; i < v.length; i++) {
                v[i] = this.getDoubleValueAt(i,0.0);
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Returns the Boolean value at the specified index
        **/
        public boolean getBooleanValueAt(int ndx, boolean dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getBooleanValue(dft) : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns a boolean array of values contained in this JSON Array
        **/
        public boolean[] getBooleanArray() {
            boolean v[] = new boolean[this.size()];
            for (int i = 0; i < v.length; i++) {
                v[i] = this.getBooleanValueAt(i,false);
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Returns the JSON._Object value at the specified index
        **/
        public JSON._Object getObjectValueAt(int ndx, JSON._Object dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getObjectValue(dft) : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns a JSON._Object array of values contained in this JSON Array
        **/
        public JSON._Object[] getObjectArray() {
            JSON._Object v[] = new JSON._Object[this.size()];
            for (int i = 0; i < v.length; i++) {
                v[i] = this.getObjectValueAt(i,null);
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Returns the Java Object value at the specified index
        **/
        public Object getJavaObjectValueAt(int ndx, Object dft) {
            if ((ndx >= 0) && (ndx < this.size())) {
                JSON._Value jv = this.get(ndx);
                return (jv != null)? jv.getJavaObject() : dft;
            } else {
                return dft;
            }
        }

        /**
        *** _Array: Returns an array of Java Object values contained in this JSON Array
        **/
        public Object[] _getJavaObjectArray() {
            Object v[] = new Object[this.size()];
            for (int i = 0; i < v.length; i++) {
                JSON._Value jv = this.get(i);
                v[i] = (jv != null)? jv.getJavaObject() : null;
            }
            return v;
        }

        // --------------------------------------

        /**
        *** _Array: Gets the JSON._Value for the specified path
        **/
        public JSON._Value getValueForPath(String path) {
            return JSON.GetValueForPath(this, path);
        }

        /**
        *** _Array: Gets the JSON._Value for the specified path
        **/
        public JSON._Value getValueForPath(String... path) {
            return JSON.GetValueForPath(this, path);
        }

        /**
        *** _Array: Gets the Java Object for the specified path
        **/
        public Object getJavaObjectForPath(String path) {
            JSON._Value val = this.getValueForPath(path);
            return (val != null)? val.getJavaObject() : null;
        }

        /**
        *** _Array: Gets the Java Object for the specified path
        **/
        public Object getJavaObjectForPath(String... path) {
            JSON._Value val = this.getValueForPath(path);
            return (val != null)? val.getJavaObject() : null;
        }

        // --------------------------------------

        /**
        *** _Array: Gets the number of items in this array
        *** @return The number of items in this array
        **/
        public int size() {
            return super.size();
        }

        /**
        *** _Array: Returns true if this array is empty 
        *** @return True if this array is empty
        **/
        public boolean isEmpty() {
            return super.isEmpty();
        }

        // --------------------------------------

        /**
        *** _Array: Print array contents (for debug purposes only)
        **/
        public void debugDisplayArray(int level) {
            String pfx0 = StringTools.replicateString(INDENT,level);
            String pfx1 = StringTools.replicateString(INDENT,level+1);
            for (JSON._Value jv : this) {
                Object av = jv.getJavaObject();
                Print.sysPrintln(pfx1 + " ==> " + StringTools.className(av));
                if (av instanceof JSON._Object) {
                    JSON._Object obj = (JSON._Object)av;
                    obj.debugDisplayObject(level+2);
                }
            }
        }

        // --------------------------------------

        /**
        *** _Array: Set format indent state
        **/
        public _Array setFormatIndent(boolean indent) {
            this.formatIndent = indent;
            return this;
        }

        /**
        *** _Array: Write a String representation of this instance to the StringBuffer
        **/
        public StringBuffer toStringBuffer(int prefix, StringBuffer sb) {
            if (sb == null) { sb = new StringBuffer(); }
            boolean fullFormat = this.formatIndent && (prefix >= 0);
            String pfx0 = fullFormat? JSON.indent(prefix)   : "";
            String pfx1 = fullFormat? JSON.indent(prefix+1) : "";
            sb.append("[");
            if (fullFormat) {
                sb.append("\n");
            }
            int size = this.size();
            for (int i = 0; i < this.size(); i++) {
                JSON._Value v = this.get(i);
                sb.append(pfx1);
                v.toStringBuffer((fullFormat?(prefix+1):-1), sb);
                if ((i + 1) < size) { 
                    sb.append(","); 
                }
                if (fullFormat) {
                    sb.append("\n");
                }
            }
            sb.append(pfx0).append("]");
            return sb;
        }

        /**
        *** _Array: Returns a String representation of this instance 
        **/
        public String toString() { // JSON._Array
            return this.toStringBuffer(1,null).toString();
        }

        /**
        *** _Array: Returns a String representation of this instance 
        **/
        public String toString(boolean inclPrefix/*indent*/) { // JSON._Array
            return this.toStringBuffer((inclPrefix/*indent*/?1:-1),null).toString();
        }

    }

    /**
    *** Parse a JSON Array from the specified String
    **/
    public static JSON._Array parse_Array(String v)
        throws JSONParsingException 
    {
        return JSON.parse_Array(v, new JSONParsingContext());
    }

    /**
    *** Parse JSON Array from the specified String
    **/
    public static JSON._Array parse_Array(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int          len   = StringTools.length(v);
        JSON._Array  array = null;
        boolean      comp  = false;

        arrayParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else
            if (ch == '/') {
                String comment = JSON.parse_Comment(v, context);
                continue; // skip comment
            } else
            if (ch == '[') {
                if (array == null) {
                    context.incrementIndex();
                    array = new JSON._Array();
                } else {
                    // -- array within array
                    JSON._Value val = JSON.parse_Value(v, context);
                    if (val == null) {
                        throw new JSONParsingException("Invalid Value", context, v);
                    }
                    array.add(val);
                }
            } else
            if (ch == ',') {
                // -- ignore item separators
                // -  TODO: should insert a placeholder for unspecified values?
                context.incrementIndex();
            } else
            if (ch == ']') {
                // end of array
                context.incrementIndex();
                comp = true;
                break arrayParse;
            } else {
                JSON._Value val = JSON.parse_Value(v, context);
                if (val == null) {
                    throw new JSONParsingException("Invalid Value", context, v);
                }
                array.add(val);
            }
        }

        /* array completed? */
        if (!comp) {
            throw new JSONParsingException("Incomplete Array", context, v);
        }

        /* return array */
        return array;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // String

    /**
    *** Parse a JSON String
    **/
    public static String parse_String(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int          len  = StringTools.length(v);
        String       val  = null;
        boolean      comp = false;

        stringParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else
            if (ch == '/') {
                String comment = JSON.parse_Comment(v, context);
                continue; // skip comment
            } else
            if (ch == '\"') {
                // parse String
                context.incrementIndex(); // consume initial quote
                StringBuffer sb = new StringBuffer();
                quoteParse:
                for (;context.getIndex() < len;) {
                    ch = v.charAt(context.getIndex());
                    if (ch == '\\') {
                        context.incrementIndex(); // skip '\'
                        if (context.getIndex() >= len) {
                            throw new JSONParsingException("Overflow", context, v);
                        }
                        ch = v.charAt(context.getIndex());
                        context.incrementIndex(); // skip char
                        switch (ch) {
                            case '"' : sb.append('"' ); break;
                            case '\\': sb.append('\\'); break;
                            case '/' : sb.append('/' ); break;
                            case 'b' : sb.append('\b'); break;
                            case 'f' : sb.append('\f'); break;
                            case 'n' : sb.append('\n'); break;
                            case 'r' : sb.append('\r'); break;
                            case 't' : sb.append('\t'); break;
                            case 'u' : {
                                int ndx = context.getIndex();
                                if ((ndx + 4) >= len) {
                                    throw new JSONParsingException("Overflow", context, v);
                                }
                                String hex = v.substring(ndx,ndx+4);
                                int uchi = StringTools.parseHexInt(hex,-1);
                                if ((uchi & 0xFF) == uchi) {
                                    // -- ASCII character
                                    sb.append((char)uchi);
                                } else {
                                    // -- Unicode [TODO:]
                                }
                                context.incrementIndex(4); // additional length of char hex
                                break;
                            }
                            default  : sb.append(ch); break;
                        }
                    } else
                    if (ch == '\"') {
                        context.incrementIndex();  // consume final quote
                        comp = true;
                        break quoteParse; // we're done
                    } else
                    if (ch < ' ' ) { // included \n \r 
                        throw new JSONParsingException("Invalid character in String", context, v);
                    } else {
                        sb.append(ch);
                        context.incrementIndex();
                        if (context.getIndex() >= len) {
                            throw new JSONParsingException("Overflow", context, v);
                        }
                    }
                } // quoteParse
                val = sb.toString();
                break stringParse;
            } else {
                throw new JSONParsingException("Missing initial String quote", context, v);
            }
        } // stringParse

        /* String completed? */
        if (!comp) {
            throw new JSONParsingException("Incomplete String", context, v);
        }

        /* return String */
        return val; // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // -- Number

    /**
    *** Parse a JSON Number
    *** Returns Long, Double, or null (if invalid number)
    **/
    public static Number parse_Number(String v, JSONParsingContext context)
        throws JSONParsingException 
    {
        if (context == null) { context = new JSONParsingContext(); }
        int          len  = StringTools.length(v);
        Number       val  = null;
        boolean      comp = false;

        numberParse:
        for (;context.getIndex() < len;) {
            char ch = v.charAt(context.getIndex());
            if (Character.isWhitespace(ch)) {
                context.incrementIndex(); // consume space
                if (ch == '\n') { context.incrementLine(); }
                continue; // skip space
            } else
            if (ch == '/') {
                String comment = JSON.parse_Comment(v, context);
                continue; // skip comment
            } else
            if ((ch == '-') || (ch == '+') || Character.isDigit(ch)) {
                StringBuffer num = new StringBuffer();
                num.append(ch); // save first character
                context.incrementIndex();
                int intDig = Character.isDigit(ch)? 1 : 0; // count first digit
                int frcDig = 0; // digits in fraction (after decimal)
                int expDig = 0; // digits in exponent (after 'E')
                int hexDig = 0; // hex digits
                boolean frcCh = false; // '.'
                boolean esnCh = false; // '+'/'-' (exponent sign)
                boolean expCh = false; // 'e'/'E'
                boolean hexCh = false; // 'x'/'X'
                digitParse:
                for (;context.getIndex() < len;) {
                    char d = v.charAt(context.getIndex());
                    if (Character.isDigit(d)) {
                        if (expCh) {
                            expDig++; // digits after exponent
                        } else
                        if (frcCh) {
                            frcDig++; // digits after decimal
                        } else {
                            intDig++; // leading digits
                        }
                        num.append(d);
                        context.incrementIndex();
                    } else
                    if ((d == 'e') || (d == 'E')) {
                        if (frcCh && (frcDig == 0)) {
                            // -- no digits after decimal
                            throw new JSONParsingException("Invalid numeric value (no digits after '.')", context, v);
                        } else
                        if (expCh) {
                            // -- more than one 'E'
                            throw new JSONParsingException("Invalid numeric value (multiple 'E')", context, v);
                        } else
                        if (hexCh) {
                            // -- 'E' allowed in hex value
                            hexDig++; // hex digits after 'x'
                        } else {
                            // -- assume exponent
                            expCh = true;
                        }
                        num.append(d);
                        context.incrementIndex();
                    } else
                    if (StringTools.isHexDigit(d)) { // A/B/C/D/F (0..9 and 'E' handled above)
                        if (!hexCh) {
                            // -- hex value not prefaced with '0x'
                            throw new JSONParsingException("Invalid numeric value (invalid hex value)", context, v);
                        }
                        hexDig++; // hex digits after 'x'
                        num.append(d);
                        context.incrementIndex();
                    } else
                    if (d == '.') {
                        if (frcCh) {
                            // -- more than one '.'
                            throw new JSONParsingException("Invalid numeric value (multiple '.')", context, v);
                        } else
                        if (intDig == 0) {
                            // -- no digits before decimal
                            throw new JSONParsingException("Invalid numeric value (no digits before '.')", context, v);
                        } else
                        if (hexCh) {
                            // -- decimal not allow in hex value
                            throw new JSONParsingException("Invalid numeric value (invalid hex value)", context, v);
                        }
                        frcCh = true;
                        num.append(d);
                        context.incrementIndex();
                    } else
                    if ((d == '-') || (d == '+')) {
                        if (!expCh) {
                            // -- no 'E'
                            throw new JSONParsingException("Invalid numeric value (no 'E')", context, v);
                        } else
                        if (esnCh) {
                            // -- more than one '-/+'
                            throw new JSONParsingException("Invalid numeric value (more than one '+/-')", context, v);
                        }
                        esnCh = true;
                        num.append(d);
                        context.incrementIndex();
                    } else
                    if ((d == 'x') || (d == 'X')) {
                        if (hexCh) {
                            // -- more than one 'x'
                            throw new JSONParsingException("Invalid numeric value (more than one 'x')", context, v);
                        } else
                        if ((intDig != 1) || !num.toString().equals("0")) {
                            // -- missing "0" before 'x'
                            throw new JSONParsingException("Invalid numeric value (invalid hex value)", context, v);
                        }
                        hexCh = true;
                        num.append(d);
                        context.incrementIndex();
                    } else {
                        comp = true;
                        break digitParse; // first non-numeric character
                    }
                } // digitParse
                if (context.getIndex() >= len) {
                    throw new JSONParsingException("Overflow", context, v);
                }
                String numStr = num.toString();
                if (frcCh || expCh) {
                    final double D = StringTools.parseDouble(numStr,0.0);
                    val = (Number)(new Double(D));
                } else
                if (hexCh) {
                    final long L = StringTools.parseLong(numStr,0L);
                    // -- unfortunately cannot subclass "Long"
                    //val = (Number)(new Long(L) { public String toString() { return StringTools.toHexString(this.longValue(),16); } } );
                    val = (Number)(new Long(L));
                } else {
                    final long L = StringTools.parseLong(numStr,0L);
                    val = (Number)(new Long(L));
                }
                break numberParse;
            } else {
                throw new JSONParsingException("Missing initial Numeric +/-/0", context, v);
            }
        } // numberParse

        /* number completed? */
        if (!comp) {
            throw new JSONParsingException("Incomplete Number", context, v);
        }

        /* return number */
        return val; // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private JSON._Object object = null;
    private JSON._Array  array  = null;

    /**
    *** JSON: Constructor 
    **/
    public JSON()
    {
        super();
    }

    /**
    *** JSON: Constructor 
    **/
    public JSON(JSON._Object obj)
    {
        this.object = obj;
    }

    /**
    *** JSON: Constructor 
    **/
    public JSON(JSON._Array array)
    {
        this.array = array;
    }

    /**
    *** JSON: Constructor 
    **/
    public JSON(String json)
        throws JSONParsingException 
    {

        /* parse object/array */
        this._parseJSON(json);

    }

    /**
    *** JSON: Constructor 
    **/
    public JSON(InputStream input)
        throws JSONParsingException, IOException
    {

        /* nothing to parse */
        if (input == null) {
            throw new JSONParsingException("Invalid object/array", null, null);
        }

        /* read JSON string */
        this._parseJSON(StringTools.toStringValue(FileTools.readStream(input)));

    }

    /**
    *** JSON: Constructor 
    **/
    public JSON(File file)
        throws JSONParsingException, IOException
    {

        /* nothing to parse */
        if (file == null) {
            throw new JSONParsingException("Invalid object/array", null, null);
        }

        /* read/parse JSON string */
        this._parseJSON(StringTools.toStringValue(FileTools.readFile(file)));

    }

    /**
    *** JSON: Common JSON Object/Array parser
    **/
    private void _parseJSON(String json)
        throws JSONParsingException
    {

        /* first non-whitespace */
        int c = StringTools.indexOfFirstNonWhitespace(json);
        if (c < 0) { // also handles null String
            throw new JSONParsingException("Invalid object/array", null, json);
        }

        /* parse Object/Array */
        switch (json.charAt(c)) {
            case '{' :
                this.object = JSON.parse_Object(json);
                break;
            case '[' :
                this.array = JSON.parse_Array(json);
                break;
            default :
                JSONParsingContext context = new JSONParsingContext(c,1);
                throw new JSONParsingException("Invalid object/array", context, json);
        }

    }

    // ------------------------------------------------------------------------

    /** 
    *** JSON: Returns true if an object is defined
    **/
    public boolean hasObject()
    {
        return (this.object != null);
    }

    /** 
    *** JSON: Gets the main JSON._Object
    **/
    public JSON._Object getObject()
    {
        return this.object;
    }

    /** 
    *** JSON: Sets the main JSON._Object
    **/
    public void setObject(JSON._Object obj)
    {
        this.object = obj;
        this.array  = null;
    }

    // ------------------------------------------------------------------------

    /** 
    *** JSON: Returns true if an array is defined
    **/
    public boolean hasArray()
    {
        return (this.array != null);
    }

    /** 
    *** JSON: Gets the main JSON._Array
    **/
    public JSON._Array getArray()
    {
        return this.array;
    }

    /** 
    *** JSON: Sets the main JSON._Array
    **/
    public void setArray(JSON._Array array)
    {
        this.array  = array;
        this.object = null;
    }

    // ------------------------------------------------------------------------

    /**
    *** JSON: Gets the JSON._Value for the specified path
    **/
    public JSON._Value getValueForPath(String path) 
    {
        if (this.hasObject()) {
            return JSON.GetValueForPath(this.getObject(), path);
        } else
        if (this.hasArray()) {
            return JSON.GetValueForPath(this.getArray(), path);
        } else {
            return null;
        }
    }

    /**
    *** JSON: Gets the JSON._Value for the specified path
    **/
    public JSON._Value getValueForPath(String... path) 
    {
        if (this.hasObject()) {
            return JSON.GetValueForPath(this.getObject(), path);
        } else
        if (this.hasArray()) {
            return JSON.GetValueForPath(this.getArray(), path);
        } else {
            return null;
        }
    }

    /**
    *** JSON: Gets the Java Object for the specified path
    **/
    public Object getJavaObjectForPath(String path) {
        JSON._Value val = this.getValueForPath(path);
        return (val != null)? val.getJavaObject() : null;
    }

    /**
    *** JSON: Gets the Java Object for the specified path
    **/
    public Object getJavaObjectForPath(String... path) {
        JSON._Value val = this.getValueForPath(path);
        return (val != null)? val.getJavaObject() : null;
    }
    
    // ------------------------------------------------------------------------

    /**
    *** JSON: Return a String representation of this instance
    **/
    public String toString()  // JSON
    {
        if (this.object != null) {
            return this.object.toString();
        } else
        if (this.array != null) {
            return this.array.toString();
        } else {
            return "";
        }
    }

    /**
    *** JSON: Return a String representation of this instance
    **/
    public String toString(boolean inclPrefix/*indent*/)  // JSON
    {
        if (this.object != null) {
            return this.object.toString(inclPrefix/*indent*/);
        } else
        if (this.array != null) {
            return this.array.toString(inclPrefix/*indent*/);
        } else {
            return "";
        }
    }

    /**
    *** JSON: Print object contents (debug purposes only)
    **/
    public void debugDisplayObject()
    {
        if (this.object != null) {
            this.object.debugDisplayObject(0);
        } else
        if (this.array != null) {
            this.array.debugDisplayArray(0);
        } else {
            Print.sysPrintln("n/a");
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
