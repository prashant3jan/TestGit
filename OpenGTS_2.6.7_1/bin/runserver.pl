#!/usr/bin/perl
# -----------------------------------------------------------------------------
# Project    : OpenGTS - Open GPS Tracking System
# URL        : http://www.opengts.org
# File       : runserver.pl
# Description: Command-line GTS server startup utility
# -----------------------------------------------------------------------------
# Device Parser Server Startup (MySQL datastore)
#  Valid Options:
#    -s       <server> : server name
#    -bind    <addr>   : [optional] local bind address
#    -port    <port>   : [optional] listen port
#    -cmdport <port>   : [optional] command port
#    -i                : [optional] interactive
#    -debug            : [optional] debug logging
#    -memory <mem>     : [optional] allocated memory
#    -kill             : [optional] kill running server
#  Examples:
#     % runserver.pl -s <server> -port 31000 -i
#     % runserver.pl -s <server> -port 31000 -logName <server>2
#     % runserver.pl -s <server> -kill
# -----------------------------------------------------------------------------
# If present, this command will use the following environment variables:
#  GTS_HOME - The GTS installation directory (defaults to ("<commandDir>/..")
#  GTS_CONF - The runtime config file (defaults to "$GTS_HOME/default.conf")
# -----------------------------------------------------------------------------
$GTS_HOME = $ENV{"GTS_HOME"};
if ("$GTS_HOME" eq "") {
    print "!!! ERROR: GTS_HOME not defined !!!\n";
    use Cwd 'realpath'; use File::Basename;
    my $EXEC_BIN = dirname(realpath($0));
    require "$EXEC_BIN/common.pl";
    exit(99); # - exit anyway
} else {
    require "$GTS_HOME/bin/common.pl";
}

# --- echo Java command-line prior to execution?
$ECHO_CMD = ("$GTS_DEBUG" eq "1")? $true : $false;

# -----------------------------------------------------------------------------

# --- version
$VERSION   = "2.0.1";

# --- options
use Getopt::Long;
%argctl = (
    # - DCS options
    "exists:s"      => \$opt_exists,
    "server:s"      => \$opt_server,
    "main:s"        => \$opt_mainClass,
    "main_"         => \$opt_AMBIGUOUS,
    "class:s"       => \$opt_mainClass,
    "class_"        => \$opt_AMBIGUOUS,
    "memory:s"      => \$opt_memory,
    "mem:s"         => \$opt_memory,
    "context:s"     => \$opt_context,
    "context_"      => \$opt_AMBIGUOUS,
    "dcs:s"         => \$opt_context,
    "dcs_"          => \$opt_AMBIGUOUS,
    "autorestart"   => \$opt_autoRestart,
   #"start"         => \$opt_start,  <== do not define! (conflicts with "server")
    "restart:s"     => \$opt_restart,
    "restart_"      => \$opt_AMBIGUOUS,
    # - Jar authorization string
    "jarKeys"       => \$opt_jarKeys,
    "jarKeys_"      => \$opt_AMBIGUOUS,
    "jarAuth"       => \$opt_jarAuth,
    "jarAuth_"      => \$opt_AMBIGUOUS,
    "version"       => \$opt_version,
    "version_"      => \$opt_AMBIGUOUS,
    # - port overrides
    "bind:s"        => \$opt_bind,
    "bindAddress:s" => \$opt_bind,
    "port:s"        => \$opt_port,
    "cmdport:s"     => \$opt_cmdport,
    "command:s"     => \$opt_cmdport,
    # - interactive
    "i"             => \$opt_interactive,
    # - debug options
    "help"          => \$opt_help,
    "debugMode"     => \$opt_debug,
    "debug"         => \$opt_debug,
    "verbose"       => \$opt_verbose,
    # - logging options
    "log"           => \$opt_logLevel,
    "logName:s"     => \$opt_logName,
    # - terminate options
    "kill:s"        => \$opt_kill,      # - optional kill sig (default "term")
    "kill_"         => \$opt_AMBIGUOUS,
    "term"          => \$opt_term,
    "term_"         => \$opt_AMBIGUOUS,
    # - parse file options
    "parseFile:s"   => \$opt_parseFile,
    "insert:s"      => \$opt_parseInsert,
    "insert_"       => \$opt_AMBIGUOUS,
    # - Unique-ID lookup
    "lookup:s"      => \$opt_lookup,
    # - psjava
    "psjava"        => \$opt_psjava,
);
$optok = &GetOptions(%argctl);
if    (!$optok) {
    &printUsageAndExit();
} 
elsif (defined $opt_help) {
    &printUsageAndExit();
}

# -----------------------------------------------------------------------------

# --- lookup unique-id
if (defined $opt_lookup) {
    my $rtn = &sysCmd("$GTS_HOME/bin/exeJava org.opengts.db.DCServerFactory -lookup=$opt_lookup",$ECHO_CMD);
    exit($rtn);
}

# --- psjava
if (defined $opt_psjava) {
    my $rtn = &sysCmd("$GTS_HOME/bin/psjava -jaronly", $ECHO_CMD);
    exit($rtn);
}

# -----------------------------------------------------------------------------

# --- server (or restart=ALL) required beyond this point
if (defined $opt_exists) { 
    $opt_server = $opt_exists; 
}
# - check for "-restart=tk10x" (not currently allowed)
#if (!(defined $opt_server) && (defined $opt_restart) && ($opt_restart ne "") && ($opt_restart ne "ALL") && ($opt_restart ne "DCS")) {
#    $opt_server = $opt_restart;
#    $opt_restart = "";
#}
# - check for "-s tk10x -restart=X"
if ((defined $opt_server) && (defined $opt_restart) && ($opt_restart ne "")) {
    print "'-restart' must have no parameters when used with '-server'\n";
    exit(1);
}
# - check for missing '-s" and "-restart"
if (!(defined $opt_server) && (!(defined $opt_restart) || ($opt_restart eq ""))) {
    &printUsageAndExit();
    exit(1); # <-- control does not reach here
}

# --- server name
$SERVER_NAME    = $opt_server;

# --- restart exit-code
$RESTART_CODE   = (defined $opt_autoRestart)? 247 : 0;

# --- log file directory
$GTS_LOGDIR     = $ENV{"GTS_LOGDIR"}; # - ie. "/var/log/gts"
if ("$GTS_LOGDIR" eq "") {
    $GTS_LOGDIR = "$GTS_HOME/logs";
}
$LOG_DIR        = "$GTS_LOGDIR";

# --- pid file directory
$GTS_PIDDIR     = $ENV{"GTS_PIDDIR"}; # - ie. "/var/run/gts"
if ("$GTS_PIDDIR" eq "") {
    $GTS_PIDDIR = "$GTS_HOME/logs";
}
$PID_DIR        = "$GTS_PIDDIR";

# --- log/pid file names
$LOG_NAME       = (defined $opt_logName)? $opt_logName : (defined $opt_context)? $opt_context : $SERVER_NAME;
$LOG_FILE       = "$LOG_DIR/${LOG_NAME}.log";
$LOG_FILE_OUT   = "$LOG_DIR/${LOG_NAME}.out";
$PID_FILE       = "$PID_DIR/${LOG_NAME}.pid";

# --- default kill signal
$KILL_SIG = "9";

# --- lib dir: build/lib
$LIB_DIR = "${GTS_HOME}/build/lib";

# --- command-line memory
$MEMORY = "";
if (defined $opt_memory) {
    # - specified on command-line (override)
    $MEMORY = $opt_memory; 
}

# -----------------------------------------------
# --- restart "ALL"|"DCS"
if ((defined $opt_restart) && (($opt_restart eq "ALL") || ($opt_restart eq "DCS"))) {
    my $LINE_SEP1 = "----------";
    my $LINE_SEP2 = "============================================================================================";
    # --
    print "Restarting all currently running DCS modules (using original directory and memory config)\n";
    print "'psjava -dcs -context -mem' before restart:\n";
    system("$GTS_HOME/bin/psjava -dcs -context -mem");
    print "$LINE_SEP2\n";
    print "\n";
    # --
    my $PSJAVA_CMD = "( ${GTS_HOME}/bin/psjava -m2m -dcs -context -home -memory -args )";
    # 38310|1|1|user|/usr/local/GTS_2.6.7-B99/build/lib/tk10x.jar|tk10x|/usr/local/GTS_2.6.7-B99|-Xmx300m|-conf= ...
    # 0---- 1 2 3--- 4------------------------------------------- 5---- 6----------------------- 7------- 8--------
    # |     | | |    |                                            |     |                        |        |> Args
    # |     | | |    |                                            |     |                        |> Memory
    # |     | | |    |                                            |     |> GTS_HOME
    # |     | | |    |                                            |> Context (DCS name)
    # |     | | |    |> Jar file
    # |     | | |> User
    # |     | |> Level (always "1" here)
    # |     |> ParentPID
    # |> PID
    my @PSJAVA_RCDS = split('\n', `$PSJAVA_CMD`);
    foreach ( @PSJAVA_RCDS ) {
        my @F = split('\|', $_); # regex
        # --
        my $PID  = $F[0];
        my $USER = $F[3];
        my $JARF = $F[4];
        my $CONT = $F[5];
        my $HOME = $F[6];
        my $MEMX = $F[7];
        my $ARGS = $F[8];
        # -- DCS name
        my $DCS = &getFileName($JARF);
        if ($DCS ne "") {
            $DCS =~ s/\.jar$//;
        }
        # -- memory
        $MEM = $MEMX;
        if ($MEM ne "") {
            $MEM =~ s/^-Xmx//;
        }
        # -- parse args
        my $CRONTAB = ""; # "cron" only
        my $CONTEXT = "";
        my $LOGNAME = "";
        my $PORT    = "";
        my $DEBUGMO = $false;
        foreach ( split(' ',$ARGS) ) {
            if ($_ =~ /^-crontab=/) {
                $CRONTAB = $_;
                $CRONTAB =~ s/^-crontab=//;
            }
            if ($_ =~ /^-rtcontext\.name=/) {
                $CONTEXT = $_;
                $CONTEXT =~ s/^-rtcontext\.name=//;
            }
            if ($_ =~ /^-log\.name=/) {
                $LOGNAME = $_;
                $LOGNAME =~ s/^-log\.name=//;
            }
            if ($_ =~ /^-port=/) {
                $PORT = $_;
                $PORT =~ s/^-port=//;
            }
            if ($_ eq "-debugMode") {
                $DEBUGMO = $true;
            }
        }
        print "PORT = $PORT\n"; # *mdf*
        # --
        print "$LINE_SEP1\n";
        if ($DCS eq "cron") {
            # -- skip "cron"?
            if ($opt_restart ne "ALL") { # -- "DCS" only
                print "Skipping $JARF ($CRONTAB) ...\n";
                next; # continue;
            }
            # -- create restart command
            my $RESTART_CMD;
            $RESTART_CMD = "( export GTS_HOME=$HOME; cd \$GTS_HOME;";
           #$RESTART_CMD = "bin/runserver.pl -restart $DCS [-context $CONTEXT]"; # <== not yet supported
            $RESTART_CMD = "$RESTART_CMD bin/runserver.pl -s $DCS";
            if ($CONTEXT ne "") {
                $RESTART_CMD = "$RESTART_CMD -context $CONTEXT";
            }
            $RESTART_CMD = "$RESTART_CMD -kill";
            $RESTART_CMD = "$RESTART_CMD &&";
            $RESTART_CMD = "$RESTART_CMD bin/runserver.pl -s $DCS";
            if ($DEBUGMO) {
                $RESTART_CMD = "$RESTART_CMD -debug";
            }
            if ($MEM ne "") {
                $RESTART_CMD = "$RESTART_CMD -mem $MEM";
            }
            if ($CONTEXT ne "") {
                $RESTART_CMD = "$RESTART_CMD -context $CONTEXT";
            }
            if ($PORT ne "") {
                $RESTART_CMD = "$RESTART_CMD -port $PORT";
            }
            if ($LOGNAME ne "") {
                $RESTART_CMD = "$RESTART_CMD -logName $LOGNAME";
            }
            if ($CRONTAB ne "") {
                $RESTART_CMD = "$RESTART_CMD -- -crontab=$CRONTAB";
            }
            $RESTART_CMD = "$RESTART_CMD; )";
            print "Restarting $JARF($CRONTAB) ...\n";
           #print "PID=$PID, User=$USER, DCS=$DCS, Home=$HOME, Memory=$MEM\n";
            print "$RESTART_CMD\n";
           #print "'cron' restart is not yet supported\n";
            system($RESTART_CMD);
        } elsif (($HOME ne "") && ($DCS ne "")) {
            my $RESTART_CMD;
            $RESTART_CMD = "( export GTS_HOME=$HOME; cd \$GTS_HOME;";
           #$RESTART_CMD = "bin/runserver.pl -restart $DCS"; # <== v2.6.7-B25d+ only
            $RESTART_CMD = "$RESTART_CMD bin/runserver.pl -s $DCS";
            if ($CONTEXT ne "") {
                $RESTART_CMD = "$RESTART_CMD -context $CONTEXT";
            }
            $RESTART_CMD = "$RESTART_CMD -kill";
            $RESTART_CMD = "$RESTART_CMD &&";
            $RESTART_CMD = "$RESTART_CMD bin/runserver.pl -s $DCS";
            if ($DEBUGMO) {
                $RESTART_CMD = "$RESTART_CMD -debug";
            }
            if ($MEM ne "") {
                $RESTART_CMD = "$RESTART_CMD -mem $MEM";
            }
            if ($CONTEXT ne "") {
                $RESTART_CMD = "$RESTART_CMD -context $CONTEXT";
            }
            if ($PORT ne "") {
                $RESTART_CMD = "$RESTART_CMD -port $PORT";
            }
            if ($LOGNAME ne "") {
                $RESTART_CMD = "$RESTART_CMD -logName $LOGNAME";
            }
            $RESTART_CMD = "$RESTART_CMD; )";
            print "Restarting $JARF ...\n";
           #print "PID=$PID, User=$USER, DCS=$DCS, Home=$HOME, Memory=$MEM\n";
            print "$RESTART_CMD\n";
            system($RESTART_CMD);
        } else {
            print "PID=$PID, User=$USER, DCS=$DCS, Home=$HOME, Memory=$MEM\n";
            print "Missing HOME/DCS: $_\n";
        }
    }
    print "$LINE_SEP1\n";
    print "\n";
    print "$LINE_SEP2\n";
    print "'psjava -dcs -context -mem' after restart:\n";
    system("$GTS_HOME/bin/psjava -dcs -context -mem");
    #print "'-restart=ALL' not yet fully supported\n";
    exit(0);
}

# -----------------------------------------------
# --- begin command assembly
$Command = "$cmd_java";

# --- initial DCS options
$DCSOPT_CONF="${GTS_HOME}/dcsoption.conf";
if (-e "${DCSOPT_CONF}") {
    # - extract single entry from dcs option config file
    my $DCS_OPT = `($cmd_grep -m1 ^${SERVER_NAME}= ${DCSOPT_CONF} | $cmd_sed 's/^.*=//') 2>/dev/null`; chomp $DCS_OPT;
    if ("$DCS_OPT" ne "") {
        print "Initial DCS options [${DCSOPT_CONF}]: ${DCS_OPT}\n";
        $Command .= " ${DCS_OPT}";
    }
}

# --- memory configuration
#$DCSMEM_CONF = "${GTS_HOME}/dcsmemory.conf";
#if (("$MEMORY" eq "") && (-e "${DCSMEM_CONF}")) { # file exists
#    # - extract single entry from dcs memory config file
#    $MEMORY = `($cmd_grep -m1 ${SERVER_NAME}= ${DCSMEM_CONF} | $cmd_sed 's/^.*=//') 2>/dev/null`; chomp $MEMORY;
#}
if ("$MEMORY" ne "") {
    #print "Runtime allocated memory (override): ${MEMORY}\n";
    $Command .= " -Xmx${MEMORY}";
}

# --- Java Main start-server command
$SERVER_JAR = "${LIB_DIR}/${SERVER_NAME}.jar";
if (!(-e "${SERVER_JAR}")) { # not exists?
    # - not found, check for self contained version
    my $SelfContained = "${LIB_DIR}/${SERVER_NAME}_SC.jar";
    if (-e "${SelfContained}") {
        $SERVER_JAR = "${SelfContained}";
    }
}
if (-e "$SERVER_JAR") {
    if ((defined $opt_mainClass) && ("$opt_mainClass" ne "")) {
        # - explicitly specify the main startup class
        if    (("$opt_mainClass" eq "opt"    ) || 
               ("$opt_mainClass" eq "Main"   ) || 
               ("$opt_mainClass" eq "optMain")   ) {
            $MAIN_CLASS = "org.opengts.opt.servers.${SERVER_NAME}.Main";
        }
        elsif (("$opt_mainClass" eq "gtse"   ) ||
               ("$opt_mainClass" eq "gtsMain")   ) {
            $MAIN_CLASS = "org.opengts.extra.servers.${SERVER_NAME}.Main";
        }
        elsif (("$opt_mainClass" eq "opengts") ||
               ("$opt_mainClass" eq "osMain" )   ) {
            $MAIN_CLASS = "org.opengts.servers.${SERVER_NAME}.Main";
        } else {
            $MAIN_CLASS = $opt_mainClass;
        }
        $ALL_JARS = &getJarClasspath($PWD_,"./build/lib",$PATHSEP); # `($cmd_ls -1 ./build/lib/*.jar | $cmd_tr '\n' ${PATHSEP})`;
        $Command .= " -classpath $ALL_JARS $MAIN_CLASS";
    } else {
        # - The jar file knows how to start itself (via "Main-Class: ...")
        # - (this may still depend on external jars: gtsdb.jar)
        $Command .= " -jar $SERVER_JAR";
    }
} else {
    # - not found
    if (defined $opt_exists) {
        exit(1);
    } else {
        print "Server not found: $SERVER_JAR\n";
        exit(1);
    }
}

# --- test for server existance only
if (defined $opt_exists) {
    exit(0);
}

# --- Constants parameters
$Constants = $false;
if (defined $opt_jarKeys) {
    if (!$Constants) { $Command .= " Constants"; }
    $Command .= " -jarKeys";
    $Constants = $true;
}
if (defined $opt_jarAuth) {
    if (!$Constants) { $Command .= " Constants"; }
    $Command .= " -jarAuth";
    $Constants = $true;
}
if (defined $opt_version) {
    if (!$Constants) { $Command .= " Constants"; }
    $Command .= " -version";
    $Constants = $true;
}

# --- context name
if (defined $opt_context) {
    $Command .= " -rtcontext.name=$opt_context";
    #$ECHO_CMD = $true;
}

# --- debug mode (instruction to DCS startup logging)
if (defined $opt_debug) {
    $Command .= " -debugMode";
    #$ECHO_CMD = $true;
}

# --- verbose (instruction to 'runserver.pl' itself)
if (defined $opt_verbose) {
    $ECHO_CMD = $true;
}

# --- log level
if (defined $opt_logLevel) {
    $Command .= " -log.level=$opt_logLevel";
}

# --- config file (should be first argument)
$Command .= " -conf=$GTS_CONF -log.name=$LOG_NAME";

# --- jar auth
if ($Constants) {
    &sysCmd($Command, $ECHO_CMD);
    exit(0);
}

# --- stop process?
if ((defined $opt_kill) || (defined $opt_term) || (defined $opt_restart)) {
    # - signal
    my $sig = (defined $opt_term)? "term" : ("$opt_kill" ne "")? $opt_kill : $KILL_SIG;
    # - pid file
    if (-e "$PID_FILE") {
        my $pid = `$cmd_cat $PID_FILE`; chomp $pid;
        if ($pid ne "") {
            print "Killing '$LOG_NAME' PID: $pid (via signal '-$sig')\n";
            my $rtn = &sysCmd("$cmd_kill -$sig $pid ; $cmd_rm $PID_FILE", $ECHO_CMD);
            if ($rtn != 0) {
                # - kill failed
                print "Error killing server: $rtn\n";
                exit($rtn); # even if restart
            }
            # - kill successful, but continue if restart
            if (!(defined $opt_restart)) {
                exit(0);
            }
        } else {
            # - blank PID?
            print "Invalid PID: $pid\n";
            exit(99); # -- even if restart
        }
    } else {
        # - could not find PID file at expected location
        print "PidFile not found: $PID_FILE\n";
        exit(99); # -- even if restart
    }
}

# --- parse file?
if (defined $opt_parseFile) {
    print "Server jar: $SERVER_JAR\n";
    print "Parsing file: $opt_parseFile\n";
    my $parseCmd = $Command . " -parseFile=$opt_parseFile";
    if (defined  $opt_parseInsert) {
        $parseCmd .= " -insert=$opt_parseInsert";
    }
    &sysCmd($parseCmd, $ECHO_CMD);
    exit(99);
}

# --- assemble "start" command
my $DCSCommand = $Command . " -start";
if (defined $opt_bind) {
    $DCSCommand .= " -bindAddress=$opt_bind";
}
if (defined $opt_port) {
    #$DCSCommand .= " -${SERVER_NAME}.port=$opt_port";
    $DCSCommand .= " -port=$opt_port";
}
if (defined $opt_cmdport) {
    $DCSCommand .= " -${SERVER_NAME}.commandPort=$opt_cmdport";
    #$DCSCommand .= " -commandPort=$opt_cmdport";
}
$DCSCommand .= " " . join(' ', @ARGV);

# --- start interactive
if (defined $opt_interactive) {
    # - ignore $PID_FILE for interactive 
    print "Server '$LOG_NAME' jar: $SERVER_JAR\n";
    $DCSCommand .= " -log.file.enable=false";
    print "Command: $DCSCommand\n";
    &sysCmd($DCSCommand, $ECHO_CMD);
    # - actually, we wait above until the user hits Control-C
    exit(99); # <-- never gets here (unless startup fails)
}

# --- already running?
if (-e "$PID_FILE") {
    my $pid = `$cmd_cat $PID_FILE`; chomp $pid;
    print "PID file already exists: $PID_FILE  [pid $pid]\n";
    if ($cmd_ps eq "") {
        print "The '${LOG_NAME}' server may already be running.\n";
        print "If server has stopped, delete the server pid file and rerun this command.\n";
        print "Aborting ...\n";
        exit(99);
    }
    my $rtn = &sysCmd("$cmd_ps -p $pid >/dev/null", $ECHO_CMD);
    if ($rtn == 0) {
        print "The '${LOG_NAME}' server is likely already running using pid $pid.\n";
        print "Make sure this server is stopped before attempting to restart.\n";
        print "Aborting ...\n";
        exit(99);
    } else {
        print "(Service on pid $pid seems to have stopped, continuing ...)\n";
    }
    &sysCmd("$cmd_rm -f $PID_FILE", $ECHO_CMD);
}

# --- create logging directory
if (!(-d "$LOG_DIR")) {
    my $rtn = &sysCmd("$cmd_mkdir -p $LOG_DIR", $ECHO_CMD);
    if (!(-d "$LOG_DIR")) {
        # - still does not exist
        print "Unable to create log directory: $LOG_DIR";
        print "Aborting ...\n";
        exit(99);
    }
}

# --- log messages to file
$DCSCommand .= " -log.file.enable=true -log.file=$LOG_FILE";

# --- background server (save the pid)
my $pid = &forkCmd($DCSCommand, $LOG_FILE_OUT, $RESTART_CODE, $ECHO_CMD);
&sysCmd("echo $pid > $PID_FILE", $ECHO_CMD);

# --- display "Started" message
sleep(1);
my $SHOWMEM = ($MEMORY ne "")? "(-Xmx${MEMORY})" : "";
print "Started '$LOG_NAME': $SERVER_JAR ${SHOWMEM} [PID: $pid]";
if (($RESTART_CODE > 0) && ($RESTART_CODE <= 255)) {
    print " - with auto-restart";
}
print "\n";

# --- exit (successful)
exit(0);

# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------

sub getJarClasspath(\$\$\$) {
    my ($DIR,$LIB,$SEP) = @_;
    my $CP = "";
    foreach ( `$cmd_ls $LIB/*.jar 2>/dev/null` ) {
        my $file = &deblank($_);
        if ($file =~ /^$DIR/) { $file = substr($file, length($DIR)); }
        if ("$CP" ne "") { $CP .= $SEP; }
        $CP .= $file; 
    }
    return $CP;
}

# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------

sub printUsageAndExit() {
    print "\n";
    print "Version: $VERSION\n";
    print "\n";
    print "Usage: $0 [OPTIONS]\n";
    print "Options:\n";
    print "  -s[erver]=<dcs>        DCS name (to start/kill/restart)\n";
    print "  -mem[ory]=<memory>     Maximum allocated memory (in format required by Java '-Xmx' option)\n";
    print "  -port=<port>           DCS port override\n";
    print "  -context=<dcsContext>  DCS context name\n";
    print "  -i                     Interactive mode Must be used with '-s' (output to stdout)\n";
    print "  -restart               Restart named ('-s') DCS module (must already be running)\n";
    print "  -restart=DCS           Restart all currently running DCS modules (with original directory, memory, etc)\n";
    print "  -restart=ALL           Restart all currently running modules (with original directory, memory, etc)\n";
    print "  -psjava                Runs 'bin/psjava -jaronly'\n";
    print "  -lookup=<mobileID>     Runs DCServerFactory to lookup any/all matching MobileID's defined in Device table\n";
    print "  -h                     This help\n";
    print "\n";
    print "Examples:\n";
    print "\n";
    print "  Start a server:\n";
    print "    runserver.pl -s <server> [-context <context>] [-port <port>] [-mem <memory>] [-i]\n";
    print "\n";
    print "  Stop a server:\n";
    print "    runserver.pl -s <server> [-context <context>] -kill\n";
    print "\n";
    print "  Restart (stop/start) a server:\n";
    print "    runserver.pl -s <server> [-context <context>] [-port <port>] [-mem <memory>] -restart\n";
    print "\n";
    print "  Restart all currently running jar-based DCS modules (excludes 'cron'):\n";
    print "    runserver.pl -restart=DCS\n";
    print "  DCS modules will be restarted with the same home/context/port/memory settings\n";
    print "\n";
    print "  Restart all currently running jar-based modules (includes 'cron'):\n";
    print "    runserver.pl -restart=ALL\n";
    print "  Modules will be restarted with the same home/context/port/memory/crontab settings\n";
    print "\n";
    print "  Run a server in interactive mode:\n";
    print "    runserver.pl -s <server> -i [-port <port>] [-mem <memory>] [-i]\n";
    print "\n";
    print "  Display running jar-based DCS modules (uses 'bin/psjava'):\n";
    print "    runserver.pl -psjava\n";
    print "\n";
    print "  Lookup a Unique-ID (uses '.../DCServerFactory'):\n";
    print "    runserver.pl -lookup=<mobileID>\n";
    print "\n";
    print "  Display this help:\n";
    print "    runserver.pl -h\n";
    print "\n";
   #print "  Parse a file containing static data:\n";
   #print "    runserver.pl -s <server> -parseFile=<file>\n";
    exit(99);
}

# ---
