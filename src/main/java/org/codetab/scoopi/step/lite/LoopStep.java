package org.codetab.scoopi.step.lite;

import org.apache.commons.lang3.Validate;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.model.JobInfo;
import org.codetab.scoopi.model.Payload;
import org.codetab.scoopi.model.StepInfo;
import org.codetab.scoopi.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoopStep extends Step {

    static final Logger LOGGER = LoggerFactory.getLogger(LoopStep.class);

    @Override
    public boolean initialize() {
        LOGGER.info("initialize");
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean store() {
        return true;
    }

    @Override
    public boolean process() {
        setData("dummy");
        setConsistent(true);
        return true;
    }

    @Override
    public boolean handover() {
        try {
            Validate.validState((getData() != null), "data is null");
            Validate.validState(isConsistent(), "step inconsistent");

            String group = "lite1";
            String stepName = "step1";
            String taskName = "simpleTask";

            if (!getPayload().getStepInfo().getNextStepName()
                    .equalsIgnoreCase("end")) {
                StepInfo nextStep =
                        taskProvider.getNextStep(group, taskName, stepName);
                Payload nextStepPayload = new Payload();
                nextStepPayload.setData(getData());
                nextStepPayload.setStepInfo(nextStep);

                JobInfo jobInfo = new JobInfo(0, "acme", group, taskName,
                        getJobInfo().getDataDef());

                nextStepPayload.setJobInfo(jobInfo);
                taskMediator.pushPayload(nextStepPayload);
                LOGGER.info("handover to step: " + nextStep.getStepName());
            }
        } catch (DefNotFoundException | InterruptedException
                | IllegalStateException e) {
            throw new StepRunException("unable to handover", e);
        }
        return true;
    }

}