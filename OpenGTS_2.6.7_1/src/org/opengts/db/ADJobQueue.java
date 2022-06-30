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
//  - This class (and corresponding ADJobHandler class) provides an easy method
//    for iterating over all Accounts, or all Devices (by Account), and performing
//    custom code (via callback) on each Account, or Device.
//  - Multi-threading is implemented to allow simultaneous processing of many
//    accounts (or devices) at a time.
//  - Pool threads can be configured to be reclaimed after an interval of inactivity.
//  - Includes ability to fine-tune the DB read selects of both Accounts and Devices 
//    by allowing additional 'where' clause specifications.
//  - Repeat the processing of account/devices after waiting a specified interval 
//    is also supported.
// ----------------------------------------------------------------------------
// Change History:
//  2020/02/19  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.util.*;
import java.io.*;

import org.opengts.util.*;
import org.opengts.dbtypes.*;
import org.opengts.dbtools.*;

import org.opengts.db.*;
import org.opengts.db.tables.*;

public class ADJobQueue
{

    public static final int     DFT_MaxPoolSize         = 20;
    public static final int     DFT_MaxThreadIdleSec    =  5;

    // ------------------------------------------------------------------------

    public static final String  FLD_AccountID           = Device.FLD_accountID;
    public static final String  FLD_DeviceID            = Device.FLD_deviceID;
    public static final String  FLD_ADJobQueue          = "$ADJobQueue";
    public static final String  FLD_Account             = "$Account";
    public static final String  FLD_Device              = "$Device";
    public static final String  FLD_JobData             = "$JobData";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** ADJobException
    **/
    public static class ADJobException
        extends Exception
    {
        public ADJobException(String msg) {
            super(msg);
        }
        public ADJobException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private int                 threadPoolSize      = ADJobQueue.DFT_MaxPoolSize;
    private int                 threadMaxIdleSec    = ADJobQueue.DFT_MaxThreadIdleSec;
    private ThreadPool          threadPool          = null;

    private boolean             activeOnly          = false;
    private String              accountWhereSelect  = null;
    private String              deviceWhereSelect   = null;

    private ADJobHandler        jobHandlerInstance  = null;
    private Class<ADJobHandler> jobHandlerClass     = null;

    // --------------------------------

    /**
    *** Constructor 
    **/
    public ADJobQueue()
    {
        super();
    }

    // --------------------------------

    /**
    *** Constructor 
    *** @param poolSize   The maximum number of thread to allow processing jobs concurrently.
    ***                   If zero (0), no maximum number of threads is imposed.
    ***                   If -1, the default maximum will be used.
    **/
    public ADJobQueue(int poolSize)
    {
        this();
        this.setMaximumThreadPoolSize(poolSize);
    }

    // --------------------------------

    /**
    *** Constructor 
    *** @param poolSize   The maximum number of thread to allow processing jobs concurrently.
    ***                   If zero (0), no maximum number of threads is imposed.
    ***                   If -1, the default maximum will be used.
    *** @param jhClassN   The String name of the ADJobHandler subclass used to create job instances.
    ***                   Each job will get its own new instance of this class.
    **/
    public ADJobQueue(int poolSize, String jhClassN)
        throws ADJobException
    {
        this(poolSize);
        this.setJobHandlerClass(jhClassN);
    }

    // --------------------------------

    /**
    *** Constructor 
    *** @param poolSize   The maximum number of thread to allow processing jobs concurrently.
    ***                   If zero (0), no maximum number of threads is imposed.
    ***                   If -1, the default maximum will be used.
    *** @param jhClass    The ADJobHandler subclass used to create job instances.
    ***                   Each job will get its own new instance of this class.
    **/
    public ADJobQueue(int poolSize, Class<ADJobHandler> jhClass)
        throws ADJobException
    {
        this(poolSize);
        this.setJobHandlerClass(jhClass);
    }

    // --------------------------------

    /**
    *** Constructor 
    *** @param poolSize   The maximum number of thread to allow processing jobs concurrently.
    ***                   If zero (0), no maximum number of threads is imposed.
    ***                   If -1, the default maximum will be used.
    *** @param jobHandler The ADJobHandler subclass instance used for all running jobs.
    ***                   This instance must be re-entrant, since it will be used for multiple concurrent jobs.
    **/
    public ADJobQueue(int poolSize, ADJobHandler jobHandler)
        throws ADJobException
    {
        this(poolSize);
        this.setJobHandlerInstance(jobHandler);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the ThreadPool maximum pool size 
    *** (maximum number of concurrent threads)
    **/
    public void setMaximumThreadPoolSize(int maxPoolSize)
    {
        this.threadPoolSize = (maxPoolSize >= 0)? maxPoolSize : ADJobQueue.DFT_MaxPoolSize;
        if (this.threadPool != null) {
            this.threadPool.setMaxPoolSize(this.threadPoolSize);
        }
    }

    /**
    *** Sets the ThreadPool maximum Idle seconds 
    *** (maximum number of idle seconds until thread is removed)
    **/
    public void setMaximumThreadIdleSeconds(int maxIdleSec)
    {
        this.threadMaxIdleSec = maxIdleSec;
        if (this.threadPool != null) {
            this.threadPool.setMaxIdleSec(this.threadMaxIdleSec);
        }
    }

    /**
    *** Creates/Gets the ThreadPool instance
    **/
    private ThreadPool _getThreadPool()
    {
        if (this.threadPool == null) {
            // -- lazy create ThreadPool
            this.threadPool = new ThreadPool("ADJobQueue", this.threadPoolSize);
            this.threadPool.setMaxIdleSec(this.threadMaxIdleSec);
        }
        return this.threadPool;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the ADJobHandler class.
    *** @param jhClassName  The name of the ADJobHandler subclass used to create job instances.
    ***                         Each job will get its own new instance of this class.
    **/
    public void setJobHandlerClass(String jhClassName)
        throws ADJobException
    {
        if (StringTools.isBlank(jhClassName)) {
            throw new ADJobException("Specified ADJobHandler subclass name is blank/null");
        } else {
            try {
                Class<ADJobHandler> jhClass = (Class<ADJobHandler>)Class.forName(jhClassName);
                if (!ADJobHandler.class.isAssignableFrom(jhClass)) {
                    throw new ADJobException("Not a ADJobHandler subclass: " + jhClassName);
                }
                this.setJobHandlerClass(jhClass);
            } catch (Throwable th) { 
                // -- ClassNotFoundException,ExceptionInInitializerError,LinkageError
                throw new ADJobException("Unable to get ADJobHandler subclass: " + jhClassName, th);
            }
        }
    }

    /**
    *** Sets the ADJobHandler class.
    *** @param jhClass  The ADJobHandler subclass used to create job instances.
    ***                 Each job will get its own new instance of this class.
    **/
    public void setJobHandlerClass(Class<ADJobHandler> jhClass)
        throws ADJobException
    {
        if (jhClass != null) {
            if (!ADJobHandler.class.isAssignableFrom(jhClass)) {
                throw new ADJobException("Not a ADJobHandler subclass: " + StringTools.className(jhClass));
            }
            this.jobHandlerClass = jhClass;
            // -- clear ADJobHandler instance
            this.jobHandlerInstance = null;
        } else {
            this.jobHandlerClass = null;
        }
    }

    /**
    *** Gets the ADJobHandler class
    **/
    public Class<ADJobHandler> getJobHandlerClass()
    {
        return this.jobHandlerClass; // may be null
    }

    /**
    *** Returns true if the ADJobHandler class has been defined.
    **/
    public boolean hasJobHandlerClass()
    {
        return (this.jobHandlerClass != null)? true : false;
    }

    // --------------------------------

    /**
    *** Sets the ADJobHandler instance
    **/
    public void setJobHandlerInstance(ADJobHandler jobHandler)
    {
        this.jobHandlerInstance = jobHandler;
        if (this.jobHandlerInstance != null) {
            // -- clear ADJobHandler instance
            this.jobHandlerClass = null;
        }
    }

    /**
    *** Gets the ADJobHandler instance.
    *** Does not return null
    **/
    public ADJobHandler getJobHandlerInstance()
        throws ADJobException
    {
        if (this.jobHandlerInstance != null) {
            return this.jobHandlerInstance;
        } else
        if (this.hasJobHandlerClass()) {
            try {
                Class<ADJobHandler> jhClass = this.getJobHandlerClass();
                return (ADJobHandler)jhClass.newInstance();
            } catch (Throwable th) { // ClassNotFoundException, ...
                throw new ADJobException("Error creating ADJobHandler instance", th);
            }
        } else {
            // -- do not have an ADJobHandler instance
            throw new ADJobException("ADJobHandler callback not defined");
        }
    }

    /**
    *** Returns true if the ADJobHandler instance has been defined.
    **/
    public boolean hasJobHandlerInstance()
    {
        if (this.jobHandlerInstance != null) {
            // -- we have an explicit instance of ADJobHandler
            return true;
        } else
        if (this.hasJobHandlerClass()) {
            // -- we can create an instance from the ADJobHandler class
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the Account/Device 'isActive' state requirement.
    *** If true, only active Accounts/Devices will be processed.
    **/
    public void setActiveOnly(boolean actvOnly)
    {
        this.activeOnly = actvOnly;
    }

    /**
    *** Gets the Account/Device 'isActive' state requirement.
    **/
    public boolean getActiveOnly()
    {
        return this.activeOnly;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the additional criteria Where selection used when selecting all accounts.
    **/
    public void setAccountWhereSelect(String selWhere)
    {
        // -- Example:
        // -    DBWhere dwh = new DBWhere(Account.getFactory());
        // -    String selWH = dwh.AND(
        // -        dwh.NE(Account.FLD_isActive   , 0),
        // -        dwh.LIKE(Account.FLD_accountID, "s%")
        // -    );
        this.accountWhereSelect = StringTools.trim(selWhere);
    }

    /**
    *** Gets the additional criteria Where selection used when selecting all accounts.
    **/
    public String getAccountsWhereSelect()
    {
        return this.accountWhereSelect;
    }

    // --------------------------------

    /**
    *** Sets the additional criteria Where selection used when selecting all devices within an account.
    **/
    public void setDeviceWhereSelect(String selWhere)
    {
        // -- Example:
        // -    DBWhere dwh = new DBWhere(Device.getFactory());
        // -    String selWH = dwh.AND(
        // -        dwh.NE(Device.FLD_isActive  , 0),
        // -        dwh.EQ(Device.FLD_deviceCode, "tk10x")
        // -    );
        this.deviceWhereSelect = StringTools.trim(selWhere);
    }

    /**
    *** Gets the additional criteria Where selection used when selecting all devices within an account.
    **/
    public String getDevicesWhereSelect()
    {
        return this.deviceWhereSelect;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Add Account/Device job to queue 
    **/
    protected boolean addJob(Account account, String accountID, String deviceID, Object jobData)
        throws ADJobException
    {
        Map<String,Object> props = new HashMap<String,Object>();
        // -- ADJobQueue
        props.put(FLD_ADJobQueue, this);
        // -- account
        if (account != null) {
            String acctID = account.getAccountID(); // assume non-blank/null
            if (!StringTools.isBlank(accountID) && !accountID.equalsIgnoreCase(acctID)) {
                // -- unlikely, but check anyway ...
                Print.logError("Mismatched Account/AccountID: " + accountID + " != " + acctID);
            }
            props.put(FLD_Account  , account); // may need to be updated when job runs
            props.put(FLD_AccountID, acctID);
        } else 
        if (!StringTools.isBlank(accountID)) {
            props.put(FLD_AccountID, accountID);
        }
        // -- device
        if (!StringTools.isBlank(deviceID)) {
            props.put(FLD_DeviceID , deviceID);
        }
        // -- job data
        if (jobData != null) {
            props.put(FLD_JobData  , jobData);
        }
        // -- add
        return this.addJob(props);
    }

    // --------------------------------

    /**
    *** Add job to queue 
    **/
    protected boolean addJob(final Map<String,Object> props)
        throws ADJobException
    {

        /* no props */
        if (ListTools.isEmpty(props)) {
            Print.logError("'props' is null/empty");
            return false;
        }

        /* add job to queue */
        Runnable job = new Runnable() {
            public void run() {
                try {
                    ADJobQueue.this._runJob(props);
                } catch (ADJobException aje) {
                    Print.logError("Job Error: " + aje);
                }
            }
        };
        ThreadPool thPool = this._getThreadPool();
        thPool.run(job);
        return true;

    }

    // ------------------------------------------------------------------------

    /**
    *** Run the specified job
    **/
    private void _runJob(Map<String,Object> props)
        throws ADJobException
    {

        /* no props */
        if (ListTools.isEmpty(props)) {
            throw new ADJobException("No properties specified");
        }

        /* ADJobHandler class/instance */
        if (!this.hasJobHandlerInstance()) {
            throw new ADJobException("ADJobHandler callback not defined");
        }

        /* Account? */
        Account account   = (Account)props.get(FLD_Account);
        String  accountID = (String) props.get(FLD_AccountID);
        if ((account == null) && !StringTools.isBlank(accountID)) {
            account = this.getAccount(accountID); // throws ADJobException
            if (account != null) {
                props.put(FLD_Account, account);
            }
        }

        /* Device */
        Device device   = (Device)props.get(FLD_Device);
        String deviceID = (String)props.get(FLD_DeviceID);
        if ((device == null) && !StringTools.isBlank(deviceID) && (account != null)) {
            device = this.getDevice(account, deviceID); // throws ADJobException
            if (device != null) {
                props.put(FLD_Device, device);
            }
        }

        /* execute job */
        ADJobHandler jh = this.getJobHandlerInstance(); // throws ADJobException
        try {
            jh.runJob(props);
        } catch (Throwable th) {
            throw new ADJobException("Error running job", th);
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Block/Wait until JobQueue is empty
    *** @return True if JobQueue is empty, false if all threads should stop
    **/
    private boolean _isJobQueueEmpty()
    {
        ThreadPool thPool = this._getThreadPool();
        for (;;) {
            boolean timeout = thPool.waitUntilJobQueueEmpty(30000L);
            if (thPool.isStoppingNow()) {
                // -- we are stopping, exit method now
                return false;
            } else
            if (!timeout) { 
                // -- job queue is empty
                return true; 
            }
        }
    }

    // --------------------------------

    /**
    *** Block/Wait until all Jobs have completed.
    *** @return True if all Jobs have completed, false if all threads should stop
    **/
    private boolean _isAllJobsComplete()
    {
        ThreadPool thPool = this._getThreadPool();
        for (;;) {
            boolean timeout = thPool.waitUntilAllJobsComplete(30000L);
            if (thPool.isStoppingNow()) {
                // -- we are stopping, exit method now
                return false;
            } else
            if (!timeout) { 
                // -- all jobs have completed
                return true; 
            } 
        }
    }
    
    /**
    *** Block/Wait until all Jobs have completed.
    *** @return True if all Jobs have completed, false if all threads should stop
    **/
    private boolean _isAllJobsCompleteLog(long startMS)
    {
        /* wait for running jobs to complete */
        Print.logDebug("Waiting for all jobs to complete  ...");
        boolean jobsComplete = this._isAllJobsComplete();
        long deltaMS = DateTime.getCurrentTimeMillis() - startMS;
        if (jobsComplete) {
            Print.logDebug("... all jobs complete ["+deltaMS+" ms]");
            return true;
        } else {
            Print.logDebug("... threads stopping ["+deltaMS+" ms]");
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Adds all Devices for specified AccountID
    **/
    public void addDeviceJobsForAccount(String accountID, Object jobData)
        throws ADJobException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;

        /* get Account */
        Account account = this.getAccount(accountID); // throws ADJobException
        if (account == null) {
            // -- account does not exist, or is inactive
            return;
        }

        /* get devices for account */
        OrderedSet<String> devIDList;
        try {
            boolean actvOnly = this.getActiveOnly();
            String  selWhere = this.getDevicesWhereSelect();
            if (!StringTools.isBlank(selWhere)) {
                // -- cusstom where-select
                if (actvOnly && (selWhere.indexOf(Device.FLD_isActive) < 0)) {
                    // -- add 'isActive!=0' condition to this where-select
                    DBWhere dwh = new DBWhere(Device.getFactory());
                    selWhere = dwh.AND(
                         dwh.NE(Device.FLD_isActive, 0),
                         selWhere
                         );
                }
                Print.logDebug("**** Device selection: " + selWhere);
            } else
            if (actvOnly) {
                // -- create where-select with 'isActive!=0'
                DBWhere dwh = new DBWhere(Device.getFactory());
                selWhere = dwh.NE(Device.FLD_isActive, 0);
                Print.logDebug("**** Device selection: " + selWhere);
            } else {
                // -- all devices
            }
            devIDList = Device.getDeviceIDsForAccount(rwMode, accountID, selWhere, -1L);
            if (ListTools.isEmpty(devIDList)) {
                Print.logDebug("No Devices for accountID: " + accountID);
                return;
            }
        } catch (DBException dbe) {
            throw new ADJobException("Error geting account Devices",dbe);
        }

        /* add all devices in list */
        Print.logDebug("Adding devices for Account: " + accountID);
        for (String deviceID : devIDList) {
            this.addJob(account, accountID, deviceID, jobData); // throws ADJobException
        }

    }

    // --------------------------------

    /**
    *** Adds/Runs all Devices for all Accounts.
    *** This method waits until the job-queue is empty before adding devices to a new Account.
    *** When this method returns, all jobs have completed running.
    **/
    public boolean runDeviceJobsForAccount(String accountID, Object jobData)
        throws ADJobException
    {
        long startMS = DateTime.getCurrentTimeMillis();

        /* wait here until job-queue is empty (running threads may not be empty) */
        //if (!this._isJobQueueEmpty()) {
        //    long deltaMS = DateTime.getCurrentTimeMillis() - startMS;
        //    Print.logDebug("Threads are stopping, returning now ["+deltaMS+" ms]");
        //    return false;
        //}

        /* add all DeviceIDs for accountID */
        this.addDeviceJobsForAccount(accountID, jobData);

        /* wait for running jobs to complete */
        return this._isAllJobsCompleteLog(startMS);

    }

    // --------------------------------

    /**
    *** Adds/Runs all Devices for all Accounts.
    *** This method waits until the job-queue is empty before adding devices to a new Account.
    *** When this method returns, all jobs have completed running.
    **/
    public boolean runDeviceJobsForAllAccounts(Object jobData)
        throws ADJobException
    {
        long startMS = DateTime.getCurrentTimeMillis();

        /* get a list of all AccountIDs */
        Collection<String> acctIDList = this.getAllAccounts(); // throws ADJobException
        if (ListTools.isEmpty(acctIDList)) {
            Print.logDebug("Warning: No Accounts");
            return false;
        }

        /* add all accounts in list */
        for (String accountID : acctIDList) {

            /* wait here until job-queue is empty (running threads may not be empty) */
            if (!this._isJobQueueEmpty()) {
                long deltaMS = DateTime.getCurrentTimeMillis() - startMS;
                Print.logDebug("Threads are stopping, returning now ["+deltaMS+" ms]");
                return false;
            }

            /* add all DeviceIDs for accountID */
            this.addDeviceJobsForAccount(accountID, jobData);

        } // iterate through accountIDs

        /* wait for running jobs to complete */
        return this._isAllJobsCompleteLog(startMS);

    }

    /**
    *** Adds/Runs all Devices, and repeats at interval.
    **/
    public void repeatDeviceJobsForAllAccounts(Object jobData, long repeatIntervalMS)
        throws ADJobException
    {
        long intvMS = (repeatIntervalMS > 1000L)? repeatIntervalMS : 1000L;
        this.setMaximumThreadIdleSeconds(0); // no max idle time
        ThreadPool thPool = this._getThreadPool();
        for (;!thPool.isStoppingNow();) {
            this.runDeviceJobsForAllAccounts(jobData);
            OSTools.sleepMS(intvMS);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Adds all Accounts
    **/
    public void addAccountJobs(Object jobData)
        throws ADJobException
    {

        /* get a list of all AccountIDs */
        Collection<String> acctIDList = this.getAllAccounts(); // throws ADJobException
        if (ListTools.isEmpty(acctIDList)) {
            Print.logDebug("Warning: No Accounts");
            return;
        }

        /* add all accounts in list */
        for (String accountID : acctIDList) {
            Account account = this.getAccount(accountID); // throws ADJobException
            if (account != null) {
                this.addJob(account, accountID, null, jobData); // throws ADJobException
            }
        }

    }

    // --------------------------------

    /**
    *** Adds/Runs all Accounts.
    *** This method waits until the job-queue is empty before adding devices to a new Account.
    *** When this method returns, all jobs have completed running.
    **/
    public boolean runAccountJobs(Object jobData)
        throws ADJobException
    {
        long startMS = DateTime.getCurrentTimeMillis();

        /* get a list of all AccountIDs */
        Collection<String> acctIDList = this.getAllAccounts();
        if (ListTools.isEmpty(acctIDList)) {
            Print.logDebug("Warning: No Accounts");
            return false;
        }

        /* add all accounts in list */
        for (String accountID : acctIDList) {

            /* wait here until job-queue is empty (running threads may not be empty) */
            //if (!this._isJobQueueEmpty()) {
            //    long deltaMS = DateTime.getCurrentTimeMillis() - startMS;
            //    Print.logDebug("Threads are stopping, returning now ["+deltaMS+" ms]");
            //    return false;
            //}

            /* get Account */
            Account account = this.getAccount(accountID); // throws ADJobException
            if (account == null) {
                // -- not found, not active, ...
                continue;
            }
    
            /* add account */
            Print.logDebug("Adding Account: " + accountID);
            this.addJob(account, accountID, null, jobData);

        } // iterate through accountIDs

        /* wait for running jobs to complete */
        return this._isAllJobsCompleteLog(startMS);

    }

    /**
    *** Adds/Runs all Accounts, and repeats at interval.
    **/
    public void repeatAccountJobs(Object jobData, long repeatIntervalMS)
        throws ADJobException
    {
        long intvMS = (repeatIntervalMS > 1000L)? repeatIntervalMS : 1000L;
        this.setMaximumThreadIdleSeconds(0); // no max idle time
        ThreadPool thPool = this._getThreadPool();
        for (;!thPool.isStoppingNow();) {
            this.runAccountJobs(jobData);
            OSTools.sleepMS(intvMS);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets a Collection of all Account-IDs.
    *** @return A collection of AccountIDs.
    **/
    protected Collection<String> getAllAccounts()
        throws ADJobException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        try {
            boolean actvOnly = this.getActiveOnly();
            String  selWhere = this.getAccountsWhereSelect();
            if (!StringTools.isBlank(selWhere)) {
                // -- cusstom where-select
                if (actvOnly && (selWhere.indexOf(Account.FLD_isActive) < 0)) {
                    // -- add 'isActive!=0' condition to this where-select
                    DBWhere dwh = new DBWhere(Account.getFactory());
                    selWhere = dwh.AND(
                         dwh.NE(Account.FLD_isActive, 0),
                         selWhere
                         );
                }
                Print.logDebug("**** Account selection: " + selWhere);
            } else
            if (actvOnly) {
                // -- create where-select with 'isActive!=0'
                DBWhere dwh = new DBWhere(Account.getFactory());
                selWhere = dwh.NE(Account.FLD_isActive, 0);
                Print.logDebug("**** Account selection: " + selWhere);
            } else {
                // -- all accounts
            }
            return Account.getAllAccounts(rwMode, selWhere);
        } catch (DBException dbe) {
            throw new ADJobException("Error geting Accounts", dbe);
        }
    }

    /**
    *** Gets the Account for the specified accountID
    *** @return The Account, or null if not found or Account is not active.
    *** @throws DBException if a database error occurred.
    **/
    protected Account getAccount(String accountID)
        throws ADJobException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        if (StringTools.isBlank(accountID)) {
            Print.logDebug("'accountID' is blank");
            return null;
        } else {
            try {
                boolean actvOnly = this.getActiveOnly();
                Account account  = Account.getAccount(rwMode, accountID);
                if (account == null) {
                    Print.logDebug("Unable to find Account: " + accountID);
                    return null;
                } else
                if (actvOnly && !Account.GetActiveStatus(account).isActive()) {
                    Print.logDebug("Account is not active: " + accountID);
                    return null;
                }
                return account;
            } catch (DBException dbe) {
                throw new ADJobException("Error reading Account: " + accountID, dbe);
            }
        }
    }

    // --------------------------------

    /**
    *** Gets the Device for the specified Account/DeviceID
    *** @return The Device, or null if not found or if the Device is not active.
    *** @throws DBException if a database error occurred.
    **/
    protected Device getDevice(Account account, String deviceID)
        throws ADJobException
    {
        DBReadWriteMode rwMode = DBReadWriteMode.READ_ONLY;
        if (account == null) {
            Print.logDebug("'Account' is null");
            return null;
        } else 
        if (StringTools.isBlank(deviceID)) {
            Print.logDebug("'deviceID' is blank");
            return null;
        } else {
            boolean actvOnly = this.getActiveOnly();
            String accountID = account.getAccountID();
            try {
                Device device = Device.getDevice(rwMode, account, deviceID);
                if (device == null) {
                    Print.logDebug("Unable to find Device: " + accountID + "/" + deviceID);
                    return null;
                } else
                if (actvOnly && !device.isActive()) {
                    Print.logDebug("Device is not active: " + accountID + "/" + deviceID);
                    return null;
                }
                return device;
            } catch (DBException dbe) {
                throw new ADJobException("Error reading Device: " + accountID + "/" + deviceID, dbe);
            }
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** CLI
    **/
    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main
        
        Print.sysPrintln("\n-----------------------------------");

        /* ADJobHandler instance/class */
        ADJobHandler jh = new ADJobHandler() {
            public void runJob(Map<String,Object> props) {
                // -- get Account/Device
                String accountID = this.getAccountID(props); // likely not-null
                String deviceID  = this.getDeviceID( props); // may be null
                Object jobData   = this.getJobData(  props); // may be null
                // -- process Account/Device
                long   delayMS   = (long)(new Random()).nextInt(2000) + 1000L;
                Print.logInfo("++ ("+accountID+"/"+deviceID + ") start ["+delayMS+"] ...");
                OSTools.sleepMS(delayMS);
                Print.logInfo("-- ("+accountID+"/"+deviceID + ") ... done");
            }
        };
        Class<ADJobHandler> jhClass = (Class<ADJobHandler>)jh.getClass();

        /* ADJobQueue instance */
        ADJobQueue jque = (new ADJobQueue() {
            public ADJobQueue _init(Class<ADJobHandler> jhClass) {
                // -- thread pool size/idle
                this.setMaximumThreadPoolSize(20);
                this.setMaximumThreadIdleSeconds(3);
                // -- job handler class
                try {
                    this.setJobHandlerClass(jhClass);
                } catch (ADJobException aje) {
                    Print.logException("Job Handler Class error", aje);
                }
                // -- init Account/Device where selector
                this.setActiveOnly(true);
                DBWhere dbw = new DBWhere(null); // (Account|Device).getFactory()
                /** /
                this.setAccountWhereSelect(dbw.AND(
                    //dbw.NE(Account.FLD_isActive , 0), <== already specified via 'setActiveOnly)true)'
                    dbw.LIKE(Account.FLD_accountID, "s%")
                ));
                /**/
                /** /
                this.setDeviceWhereSelect(dbw.AND(
                    //dbw.NE(Device.FLD_isActive, 0), <== already specified via 'setActiveOnly)true)'
                    dbw.EQ(Device.FLD_deviceCode, "gos777")
                ));
                /**/
                // -- MUST return 'this'
                return this;
            }
        })._init(jhClass);

        /* Account test */
        String runAccts[] = { "addAllAccounts", "runAccounts" };
        if (RTConfig.getBoolean(runAccts,false)) {
            try {
                Object jobData = null;
                Print.sysPrintln("Starting 'runAccountJobs' ...");
                jque.runAccountJobs(jobData); 
                Print.sysPrintln("... 'runAccountJobs' done.");
            } catch (ADJobException aje) {
                Print.logException("Error running account jobs", aje);
            }
            OSTools.sleepSec(6);
            System.exit(0);
        }

        /* Device test */
        String runDevs[] = { "addAllDevices", "runDevices" };
        if (RTConfig.getBoolean(runDevs,false)) {
            try {
                Object jobData = null;
                Print.sysPrintln("Starting 'runDeviceJobsForAllAccounts' ...");
                jque.runDeviceJobsForAllAccounts(jobData); 
                Print.sysPrintln("... 'runDeviceJobsForAllAccounts' done.");
            } catch (ADJobException aje) {
                Print.logException("Error running device jobs", aje);
            }
            OSTools.sleepSec(6);
            System.exit(0);
        }

    }

}
