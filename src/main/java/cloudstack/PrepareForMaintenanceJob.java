package cloudstack;

import cloudstack.exception.HostIsAlreadyInMaintenanceModeException;
import cloudstack.exception.JobFailedError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class PrepareForMaintenance is a form of {@code Job} that represents
 * CloudStack's Async Job related to maintenance.
 */

class PrepareForMaintenanceJob {

    private static Logger log = LogManager.getLogger(PrepareForMaintenanceJob.class);
    private Job job;

    PrepareForMaintenanceJob(Job job) {
        this.job = job;
    }

    /**
     * Returns the status of a job. True if it was finished, False otherwise.
     *
     * @return return true if this job finished and false if it was pending.
     * @throws JobFailedError in case of error.
     * @throws HostIsAlreadyInMaintenanceModeException if user try to put already
     * in maintenance mode host to maintenance mode again.
     */

    boolean finished() throws HostIsAlreadyInMaintenanceModeException {
        try {
            return job.finished();
        }catch (JobFailedError j){
            String errorCode = j.getRoot().getElementsByTagName("errorcode").item(0).getTextContent();

            if("530".equals(errorCode) || "431".equals(errorCode)) {
                log.warn("Host is already in maintenance mode.");
                throw new HostIsAlreadyInMaintenanceModeException();
            }
            throw new JobFailedError(j);
        }
    }
}
