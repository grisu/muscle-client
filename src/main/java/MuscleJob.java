import grisu.control.JobConstants;
import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.job.JobException;
import grisu.frontend.model.job.JobObject;
import grisu.jcommons.constants.Constants;
import grisu.model.FileManager;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.python.google.common.collect.Maps;

public class MuscleJob {

	/**
	 * Just an example control structure to see how one can
	 * submit/monitor/download jobs.
	 * 
	 * @param args
	 *            not used
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void main(String[] args) throws Exception {


		System.out.println("Logging in...");

		// the serviceinterface object only has to be created once. for your
		// portal, we'd do in a different way, come back to me once you are
		// about to integrate this piece of code...
		// for now, this will only ask for a password every 10 day
		ServiceInterface si = LoginManager.loginCommandline("BeSTGRID");

		System.out.println("Creating muscle job...");
		MuscleJob mj = new MuscleJob(si);
		System.out.println("Setting input file: " + args[0]);
		mj.setFastaInputFile(args[0]);
		System.out.println("Submitting job...");
		String jobname = mj.submit();
		System.out.println("Job submitted (jobname: " + jobname
				+ "), now waiting for it to finish...");
		mj.waitForJobToFinish(10);
		System.out.println("Job finished.");

		System.out.println("Downloading stdout & stderr...");

		System.out.println("Stdout:\n");
		System.out.println(mj.getStdoutContent());

		System.out.println("\nStderr:\n");
		System.out.println(mj.getStderrContent());

		String fastaoutputurl = mj.getFastaOutputFileUrl();

		System.out.println("Downloading fasta output file: " + fastaoutputurl);

		File fastaOutputFile = mj.getFastaOutput();
		System.out.println("File downloaded, filesize (in bytes): "
				+ fastaOutputFile.length());
		System.out.println("File location: " + fastaOutputFile.toString());


		// don't do this in your webapp, this is only to close all daemons that
		// might have been created in the cli version...
		System.exit(0);

	}

	/**
	 * Since we only want to submit to pan, we can hardcode the destination of
	 * the job here.
	 */
	private final String QUEUE = "pan:pan.nesi.org.nz#Loadleveler";

	/**
	 * Path to the muscle executable on Pan. I guess we can hardcode this one
	 * too...
	 */
	private final String PATH_TO_MUSCLE_EXE = "/share/apps/muscle-3.8.31/bin/muscle";

	/**
	 * Every job can have properties associated with it. In this case we store
	 * the name of the fasta output file with the job, just in case we might
	 * want to look up later on.
	 */
	private final String FASTA_OUTPUT_FILENAME_KEY = "fastaOutputFilename";

	/**
	 * Default walltime, a bit less than 1 hour. That way we always end up in
	 * the short queue.
	 */
	private final int DEFAULT_WALLTIME = 3500;

	/**
	 * Default group which is used to submit the job. Not sure whether that needs to change, if so, we just create another setter.
	 */
	private final String DEFAULT_GROUP = "/nz/nesi";

	/**
	 * The serviceinterface represents the session within the Grisu API. It can
	 * access/create/delete jobs and files and look up resources (what is
	 * available where).
	 */
	private final ServiceInterface si;
	/**
	 * The {@link JobObject} is the model that contains all the logic that is
	 * needed to submit and control a generic job. We wrap around it with
	 * specific "muscle" logic.
	 */
	private final JobObject job;

	/**
	 * The fasta input file.
	 */
	private String fastaInputFile = null;

	/**
	 * The fasta name, we keep that to name the job and output file.
	 */
	private String fastaName = null;
	private String fastaFileName = null;
	private String fastaOutputFileName = null;

	/**
	 * For every job we submit, we create a new MuscleJob object.
	 * 
	 * @param si
	 *            the serviceInterface
	 */
	public MuscleJob(ServiceInterface si) {
		this.si = si;
		this.job = new JobObject(si);
		this.job.setApplication(Constants.GENERIC_APPLICATION_NAME);
		this.job.setCpus(1);
		this.job.setForce_single(true);
		this.job.setSubmissionLocation(QUEUE);
		this.job.setWalltimeInSeconds(DEFAULT_WALLTIME);
	}

	/**
	 * Downloads and caches the fastaOutputFile in the .grisu/cache directory.
	 * 
	 * After download, you can just move the file whereever you want.
	 * 
	 * @return the File object for the cached fasta output file
	 */
	public File getFastaOutput() {

		int status = this.job.getStatus(true);

		if ( status < JobConstants.FINISHED_EITHER_WAY) {
			throw new JobException(this.job, "Job not finished yet.");
		}

		File outputFile = this.job
				.downloadAndCacheOutputFile(getFastaOutputFilename());
		return outputFile;
	}

	public String getFastaOutputFilename() {
		if ( StringUtils.isBlank(this.fastaOutputFileName ) ) {
			this.fastaOutputFileName = this.job.getJobProperty(FASTA_OUTPUT_FILENAME_KEY);
		}
		return this.fastaOutputFileName;
	}

	/**
	 * Returns the url of the fasta output file (obviously only works once job
	 * was submitted).
	 * 
	 * @return the url
	 */
	public String getFastaOutputFileUrl() {
		String jobdir = this.job.getJobDirectoryUrl();
		String outputFilename = this.job.getJobProperty(FASTA_OUTPUT_FILENAME_KEY);
		return jobdir + "/" + outputFilename;
	}

	/**
	 * Returns the status of the job (look up {@link JobConstants} for what the
	 * int value means.
	 * 
	 * Or use {@link #getJobStatusString()} method.
	 * 
	 * @return the job status
	 */
	public int getJobStatus() {
		return this.job.getStatus(true);
	}

	/**
	 * Returns the job status as a string.
	 * 
	 * @return the job status
	 */
	public String getJobStatusString() {
		return JobConstants.translateStatus(getJobStatus());
	}

	/**
	 * Returns the current content of the stderr file associated with this job.
	 * (Re-)downloads stderr if not already cached...
	 * 
	 * @return the content of the stderr file
	 */
	public String getStderrContent() {
		return this.job.getStdErrContent();
	}

	/**
	 * Returns the current content of the stdout file associated with this job.
	 * (Re-)downloads stdout if not already cached...
	 * 
	 * @return the content of the stdout file
	 */
	public String getStdoutContent() {
		return this.job.getStdOutContent();
	}

	/**
	 * Sets the fasta input file.
	 * 
	 * @param inputFile
	 *            the input file (can be local path or remote gsiftp/grid
	 *            protocol)
	 */
	public void setFastaInputFile(String inputFile) {
		this.fastaInputFile = inputFile;
		// calculating the fasta name
		this.fastaFileName = FileManager.getFilename(this.fastaInputFile);
		this.fastaName = this.fastaFileName.substring(0,
				this.fastaFileName.lastIndexOf("."));
		this.fastaOutputFileName = "Aligned_" + this.fastaFileName;

	}

	/**
	 * The default is a bit less than 1 hour. Set this higher (or lower) if
	 * necessary.
	 * 
	 * @param walltime
	 *            the walltime of the muscle job in seconds
	 */
	public void setWalltimeInSeconds(int walltime) {
		this.job.setWalltimeInSeconds(walltime);
	}

	/**
	 * Submits the job to the cluster.
	 * 
	 * @return the jobname of the submitted job
	 * @throws JobSubmissionException
	 *             if the submission fails for some reason
	 */
	public String submit() throws JobSubmissionException {
		if (StringUtils.isBlank(this.fastaInputFile)) {
			throw new JobSubmissionException("No fasta input file specified.");
		}

		// not sure whether that makes sense in your case, but we'll set the
		// jobname to be the name of the fasta input file plus a timestamp
		job.setTimestampJobname(fastaName);

		job.addInputFileUrl(this.fastaInputFile);
		job.setCommandline(PATH_TO_MUSCLE_EXE + " -in " + fastaFileName
				+ " -out " + fastaOutputFileName + " -maxiters 2");

		try {
			job.createJob(DEFAULT_GROUP);
		} catch (JobPropertiesException e) {
			throw new JobSubmissionException("Can't create job: "
					+ e.getLocalizedMessage());
		}

		Map<String, String> additionalJobProperties = Maps.newHashMap();
		additionalJobProperties.put(FASTA_OUTPUT_FILENAME_KEY,
				fastaOutputFileName);

		try {
			job.submitJob(additionalJobProperties);
		} catch (InterruptedException e) {
			throw new JobSubmissionException(
					"Could not submit job, submission process was interrupted.");
		}

		return this.job.getJobname();
	}

	/**
	 * Call this to wait for the job to finish (either successfully or not).
	 * 
	 * This method checks the job status every x seconds, don't choose too low a
	 * number (except when developing), otherwise the backend may be overloaded.
	 * I'd recommend a value of 10 for development and, depending on how long
	 * you expect the job to run, something between 30 and 120 for production.
	 * 
	 * @param timeToWaitBetweenChecksInSeconds
	 *            how long to wait untie re-checking job status
	 */
	public void waitForJobToFinish(int timeToWaitBetweenChecksInSeconds) {

		int status = this.job.getStatus(true);

		if (status < JobConstants.UNSUBMITTED) {
			throw new JobException(this.job, "Job not submitted yet.");
		}

		this.job.waitForJobToFinish(timeToWaitBetweenChecksInSeconds);
	}

}
