Muscle via the Grid
===============

Prerequisites
--------------------

 * Java ( version 6 )
 * Maven ( version 3 - http://maven.apache.org/ )
 * git
 
Checking out &  Building the client
 ------------------------------------------------
 
### Commandline
 
	 git clone git://github.com/grisu/muscle-client.git
     cd muscle-client
     mvn clean install
	 
After that you should have a file called "muscle-submit-lib.jar" in the target directory. Which you can add in the classpath of your application. You also need to download the following file and put it in the same directory as "muscle-submit-lib.jar":

    http://code.ceres.auckland.ac.nz/webstart/bcprov.jar
	
and run it via

    java -jar muscle-submit-lib.jar <myproxy_username> <myproxy_password> <path_to_fasta_input_file>
	 
### Eclipse

(Eclipse needs m2e plugin)

Check out git repo (either using github client on win/mac) or Eclipse or commandline client

In Eclipse: File -> Import -> Maven -> Existing Maven Projects

Root directory: the local copy of the git repository

Click "Finish"

Right click the "MuscleJob" java class and select Run As -> Java Application

Change Run configuration and specify 3 program arguments (in that order):

 * myproxy username
 * myproxy password
 * path of your fasta input file



