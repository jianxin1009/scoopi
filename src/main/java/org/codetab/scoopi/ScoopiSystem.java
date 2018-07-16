package org.codetab.scoopi;

import java.io.Console;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.codetab.scoopi.defs.ILocatorProvider;
import org.codetab.scoopi.defs.yml.DefsProvider;
import org.codetab.scoopi.exception.ConfigNotFoundException;
import org.codetab.scoopi.exception.CriticalException;
import org.codetab.scoopi.messages.Messages;
import org.codetab.scoopi.metrics.MetricsHelper;
import org.codetab.scoopi.metrics.MetricsServer;
import org.codetab.scoopi.metrics.SystemStat;
import org.codetab.scoopi.misc.ShutdownHook;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.LocatorGroup;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.shared.ConfigService;
import org.codetab.scoopi.step.TaskMediator;
import org.codetab.scoopi.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoopiSystem {

    static final Logger LOGGER = LoggerFactory.getLogger(ScoopiSystem.class);

    @Inject
    private ConfigService configService;
    @Inject
    private DefsProvider defsProvider;
    // @Inject
    // private DataDefService dataDefService;
    @Inject
    private TaskMediator taskMediator;
    @Inject
    private ILocatorProvider locatorProvider;
    @Inject
    private MetricsServer metricsServer;
    @Inject
    private MetricsHelper metricsHelper;

    @Inject
    private ShutdownHook shutdownHook;
    @Inject
    private Runtime runTime;

    @Inject
    public ScoopiSystem() {
    }

    /*
     *
     */
    public boolean initSystem() {
        LOGGER.info(Messages.getString("GSystem.0")); //$NON-NLS-1$
        runTime.addShutdownHook(shutdownHook);

        String userProvidedFile = getPropertyFileName();
        String defaultsFile = "scoopi-default.xml"; //$NON-NLS-1$
        configService.init(userProvidedFile, defaultsFile);

        startMetricsServer();

        String mode = getMode();
        LOGGER.info(mode);
        Date runDate = configService.getRunDate();
        LOGGER.info(Messages.getString("GSystem.3"), runDate); //$NON-NLS-1$

        defsProvider.init();
        defsProvider.initProviders();

        // dataDefService.init();
        // int dataDefsCount = dataDefService.getCount();
        // LOGGER.info(Messages.getString("GSystem.7"), dataDefsCount);
        // //$NON-NLS-1$

        return true;
    }

    /**
     * Get user defined properties file name. The properties file to be is used
     * as user defined properties is set either through environment variable or
     * system property.
     * <p>
     * <ul>
     * <li>if system property [scoopi.propertyFile] is set then its value is
     * used</li>
     * <li>else if system property [scoopi.mode=dev] is set then
     * scoopi-dev.properties file is used</li>
     * <li>else environment variable [scoopi_property_file] is set then its
     * value is used</li>
     * <li>when none of above is set, then default file scoopi.properties file
     * is used</li>
     * </ul>
     * </p>
     *
     * @return
     */
    private String getPropertyFileName() {
        String fileName = null;

        String system = System.getProperty("scoopi.propertyFile"); //$NON-NLS-1$
        if (system != null) {
            fileName = system;
        }

        if (fileName == null) {
            String mode = System.getProperty("scoopi.mode", "prod");
            if (mode != null && mode.equalsIgnoreCase("dev")) {
                fileName = "scoopi-dev.properties";
            }
        }

        if (fileName == null) {
            fileName = System.getenv("scoopi_property_file"); //$NON-NLS-1$
        }

        // default nothing is set then production property file
        if (fileName == null) {
            fileName = "scoopi.properties"; //$NON-NLS-1$
        }
        return fileName;
    }

    /*
     *
     */
    public void pushInitialPayload() {
        LOGGER.info(Messages.getString("GSystem.8")); //$NON-NLS-1$
        try {
            String seederClassName =
                    configService.getConfig("scoopi.seederClass"); //$NON-NLS-1$
            List<LocatorGroup> lGroups = locatorProvider.getLocatorGroups();
            for (LocatorGroup lGroup : lGroups) {
                StepInfo stepInfo =
                        new StepInfo("start", "na", "na", seederClassName);
                JobInfo jobInfo =
                        new JobInfo(0, "na", lGroup.getGroup(), "na", "na");
                Payload payload = new Payload();
                payload.setStepInfo(stepInfo);
                payload.setJobInfo(jobInfo);
                payload.setData(lGroup);
                try {
                    taskMediator.pushPayload(payload);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (ConfigNotFoundException e) {
            throw new CriticalException(Messages.getString("GSystem.10"), e); //$NON-NLS-1$
        }
    }

    private String getMode() {
        String modeInfo = Messages.getString("GSystem.11"); //$NON-NLS-1$
        if (configService.isTestMode()) {
            modeInfo = Messages.getString("GSystem.12"); //$NON-NLS-1$
        }
        if (configService.isDevMode()) {
            modeInfo = Messages.getString("GSystem.13"); //$NON-NLS-1$
        }
        return modeInfo;
    }

    public void waitForHeapDump() {
        String wait = "false"; //$NON-NLS-1$
        try {
            wait = configService.getConfig("scoopi.waitForHeapDump"); //$NON-NLS-1$
        } catch (ConfigNotFoundException e) {
        }
        if (wait.equalsIgnoreCase("true")) { //$NON-NLS-1$
            System.gc();
            Console console = System.console();
            console.printf("%s%s", Messages.getString("GSystem.18"), Util.LINE); //$NON-NLS-1$ //$NON-NLS-2$
            console.printf("%s", Messages.getString("GSystem.20")); //$NON-NLS-1$ //$NON-NLS-2$
            console.readLine();
        }
    }

    public void startMetricsServer() {
        metricsServer.start();
        metricsHelper.initMetrics();
        SystemStat systemStat = new SystemStat();
        metricsHelper.registerGuage(systemStat, this, "system", "stats");
    }

    public void stopMetricsServer() {
        metricsServer.stop();
    }
}