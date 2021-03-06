package org.codetab.scoopi.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class JobInfoTest {

    private JobInfo jobInfo;

    @Before
    public void setUp() throws Exception {
        jobInfo =
                new JobInfo(1, "locator", "group", "task", "steps", "dataDef");
    }

    @Test
    public void testGetId() {
        assertThat(jobInfo.getId()).isEqualTo(1);
    }

    @Test
    public void testGetName() {
        assertThat(jobInfo.getName()).isEqualTo("locator");
    }

    @Test
    public void testGetGroup() {
        assertThat(jobInfo.getGroup()).isEqualTo("group");
    }

    @Test
    public void testGetTask() {
        assertThat(jobInfo.getTask()).isEqualTo("task");
    }

    @Test
    public void testGetDataDef() {
        assertThat(jobInfo.getDataDef()).isEqualTo("dataDef");
    }

    @Test
    public void testGetLabel() {
        assertThat(jobInfo.getLabel()).isEqualTo("locator:task:dataDef");
    }

    @Test
    public void testToString() {
        assertThat(jobInfo.toString()).isEqualTo(
                "JobInfo [id=1, locator=locator, group=group, task=task, steps=steps, dataDef=dataDef]");
    }

    @Test
    public void testHashCode() {
        int actual = jobInfo.hashCode();
        assertThat(actual).isEqualTo(1945591254);
    }

    @Test
    public void testEqual() {
        JobInfo another =
                new JobInfo(1, "locator", "group", "task", "steps", "dataDef");
        assertThat(jobInfo).isEqualTo(another);
    }
}
