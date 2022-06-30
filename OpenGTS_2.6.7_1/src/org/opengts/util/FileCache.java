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
//  This class provides file based object cache which is re-parsed each time
//  the file is modified.
// ----------------------------------------------------------------------------
// Change History:
//  2018/09/10  GTS Development Team
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import java.io.*;

/**
*** Reads an object from a specified file and retains an instance of the object read.
*** If the file is modified, then the object is re-read, otherwise the previously
*** cached object is returned on subsequent read requests.
**/

// ------------------------------------------------------------------------
// -- Read cached file object example:
// -    FileCache<TYPE> typeCache = new FileCache<TYPE>(filePath) {
// -        protected boolean parseFileInputStream(FileInputStream fis) throws IOException {
// -            TYPE obj = new TYPE();
// -            for (;;) {
// -                String line = FileTools.readLine(fis);
// -                if (line == null) { break; } // end of file
// -                obj.addLine(line);
// -            }
// -            this.setParsedObject(obj);
// -            return true;
// -        }
// -    }
// --   ...
// -    TYPE obj = typeCache.readObject();
// ------------------------------------------------------------------------

public abstract class FileCache<T> // CachedObject?
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private          boolean        debug                = false;

    private          File           cacheFile            = null;
    private          long           minModIntervalMS     = 2000L;   // ms
    private          long           minChkIntervalMS     = 0L;      // ms

    private          ReentrantLock  cacheLock            = new ReentrantLock();

    private          long           lastModCheckTimeMS   = 0L;      // ms
    private          long           filePendModTimeMS    = -1L;     // ms
    private          long           fileLastModTimeMS    = -1L;     // ms

    private volatile T              parsedObject         = null;

    /**
    *** Constructor
    **/
    public FileCache(File file)
    {
        this.cacheFile         = file;  // need not currently exists
        this.fileLastModTimeMS = -1L;    // not yet loaded
        this.setParsedObject(null);
    }

    /**
    *** Constructor
    **/
    public FileCache(String file)
    {
        this(new File(file));
    }

    // ------------------------------------------------------------------------

    /**
    *** Set debug mode
    **/
    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the cache file
    **/
    public File getFile()
    {
        return this.cacheFile;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the minimum settling time after file has been modified
    **/
    public void setMinimumModifiedIntervalMS(long minModIntrvMS)
    {
        this.minModIntervalMS = (minModIntrvMS > 0L)? minModIntrvMS : 0L;
    }

    /**
    *** Gets the minimum settling time after file has been modified
    **/
    public long getMinimumModifiedIntervalMS()
    {
        return this.minModIntervalMS;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the minimum wait time for file "lastModified" checks
    **/
    public void setMinimumCheckIntervalMS(long minUpdIntrvMS)
    {
        this.minChkIntervalMS = (minUpdIntrvMS > 0L)? minUpdIntrvMS : 0L;
    }

    /**
    *** Gets the minimum wait time for file "lastModified" checks
    **/
    public long getMinimumCheckIntervalMS()
    {
        return Math.max(this.minChkIntervalMS, this.minModIntervalMS);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the object parsed from the file
    **/
    protected T getParsedObject()
    {
        T obj;
        synchronized (this) {
            obj = this.parsedObject;
        }
        return obj;
    }

    /**
    *** Sets the object parsed from the file
    **/
    protected void setParsedObject(T obj)
    {
        synchronized (this) {
            this.parsedObject = obj;
        }
    }

    /**
    *** Parse file data into the cached object
    **/
    protected boolean parseFileInputStream(FileInputStream fis)
        throws IOException
    {
        // -- (override) must be called from synchronized block
        //T obj = new TYPE();
        //...
        //this.setParsedObject(obj);
        // -- return true if file modified time "fileLastModTimeMS" should be updated
        return true;
    }

    /** (private)
    *** Reads and returns parsed object from the file.
    **/
    private void _readObject()
    {
        // -- must be called from a synchronized/lock block
        long nowMS = System.currentTimeMillis();

        // -- minimum modified-check interval
        if (this.filePendModTimeMS > 0L) {
            // -- previous file modified detection is pending
            long modIntMS = this.getMinimumModifiedIntervalMS();
            if ((this.filePendModTimeMS + modIntMS) > nowMS) {
                // -- not ready to reload
                if (this.debug) { 
                    long deltaMS = nowMS - (this.filePendModTimeMS + modIntMS);
                    Print.logDebug("Pending file modified delay ... [expires in "+deltaMS+" ms]"); 
                }
                return;
            }
            // -- clear pending file modified detection, and continue reload below
            this.filePendModTimeMS = 0L;
        } else
        if (this.lastModCheckTimeMS > 0L) {
            // -- check last file modified detection time
            long chkIntMS = this.getMinimumCheckIntervalMS();
            if ((this.lastModCheckTimeMS + chkIntMS) > nowMS) {
                // -- too recent since previous lastModified check
                if (this.debug) { 
                    long deltaMS = nowMS - (this.lastModCheckTimeMS + chkIntMS);
                    Print.logDebug("LastModified recently checked ... [expires in "+deltaMS+" ms]"); 
                }
                return;
            }
        } else {
            // -- this is the first time we will check the file modified time
        }
        this.lastModCheckTimeMS = nowMS;

        // -- get/check file modified time
        File cFile = this.getFile(); // allow for null
        long currLastModTimeMS = (cFile != null)? cFile.lastModified() : 0L;
        if (currLastModTimeMS <= 0L) { 
            // -- file does not exist?
            if (currLastModTimeMS < 0L) { 
                // -- unlikely, but make sure it is not < 0
                currLastModTimeMS = 0L;
            }
            // -- continue below
        } else
        if ((currLastModTimeMS + this.getMinimumModifiedIntervalMS()) > nowMS) {
            // -- file was very recently modified, let it settle down first
            this.filePendModTimeMS = currLastModTimeMS; // > 0
            if (this.debug) {
                long deltaMS = nowMS - (currLastModTimeMS + this.getMinimumModifiedIntervalMS());
                Print.logDebug("File recently modified ... [expires in "+deltaMS+" ms]");
            }
            return;
        }

        // -- file changed?
        if (currLastModTimeMS != this.fileLastModTimeMS) {
            // -- file has changed, reload
            if (this.debug) {
                Print.logDebug("Reading object from file: " + cFile);
            }
            // -- open file
            FileInputStream fis = null;
            if (currLastModTimeMS > 0L) {
                // -- file exists, open file ("cFile" is non-null)
                try {
                    fis = new FileInputStream(cFile); // "cFile" is non-null here
                } catch (FileNotFoundException th) {
                    // -- should not occur, since "currLastModTimeMS" is > 0
                    fis = null;
                } catch (SecurityException se) {
                    Print.logError("File access not allowed: " + cFile);
                    fis = null;
                } catch (Throwable th) { // NullPointerException
                    // -- unlikely, but trap error anyway
                    Print.logException("Unexpected Error: " + cFile, th);
                    fis = null;
                }
            }
            // -- read/parse file
            boolean ok;
            try {
                ok = this.parseFileInputStream(fis); // "fis" may be null
            } catch (IOException ioe) {
                Print.logException("FileCache IO Error", ioe);
                ok = true; // continue reset fileLastModTimeMS
            } catch (Throwable th) {
                Print.logException("Unexpected FileCache parse error", th);
                ok = true; // continue reset fileLastModTimeMS
            }
            // -- close file
            FileTools.closeStream(fis);
            // -- save file last modified time and return
            if (ok) {
                this.fileLastModTimeMS = currLastModTimeMS;
            }
            if (this.debug) {
                Print.logDebug("Modified file object updated: "+ok+" ...");
            }
            return;
        }

        // -- file has not changed since last object read
        if (this.debug) {
            Print.logDebug("File has not changed ...");
        }
        return;

    }

    // ------------------------------------------------------------------------

    /**
    *** Reads and returns parsed object from the file.
    **/
    public T readObject()
    {

        // -- non-blocking lock
        if (this.cacheLock.tryLock()) { // synchronized (this)

            // -- only a single thread at a time will execute the following 
            try {
                this._readObject();
            } catch (Throwable th) {
                // -- ignore any errors
                //Print.logException("Error during '_readObject'", th);
                Print.logError("'_readObject' error: " + th);
            } finally {
                this.cacheLock.unlock();
            }

        } // tryLock

        // -- return object
        return this.getParsedObject(); // may be null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Debug/Testing entry point
    *** @param argv  The Command-line args
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

    }

}
