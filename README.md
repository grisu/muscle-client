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
	 
After that you should have a file called "muscle-submit-lib.jar" in the target directory. Which you can add in the classpath of your application. You also need to download the following library and add it to your classpath:

    http://code.ceres.auckland.ac.nz/webstart/bcprov.jar
	
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

Download prebuild package
----------------------------------------

Download the latest build from:

    https://code.ceres.auckland.ac.nz/jenkins/job/Muscle-Client/
	
In addition, you need the bouncy castle library in your classpath:

    http://code.ceres.auckland.ac.nz/webstart/bcprov.jar
	
Run the client from the commandline
-----------------------------------------------------

Put bcprov.jar in same directory as muscle-submit-lib.jar and start via:

    java -jar muscle-submit-lib.jar <myproxy_username> <myproxy_password> <path_to_fasta_input_file>
	 
In the background, the client authenticates, uploads the fasta files, submits the job, waits for the job to finish, and downloads stdout & stderr as well as the fasta output file....


