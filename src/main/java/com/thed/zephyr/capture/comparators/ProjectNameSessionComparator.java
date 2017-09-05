package com.thed.zephyr.capture.comparators;

import java.util.Comparator;
import java.util.Objects;

import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.service.jira.ProjectService;

/**
 * Compares the session objects using project name property.
 * Sorts the objects using ascending flag to sort in ascending or descending.
 * 
 * @author manjunath
 * @see java.util.Comparator
 *
 */
public class ProjectNameSessionComparator implements Comparator<Session> {
	
	private boolean ascending;

	private ProjectService projectService;

    public ProjectNameSessionComparator(boolean ascending, ProjectService projectService) {
        this.ascending = ascending;
        this.projectService = projectService;
    }

	@Override
	public int compare(Session first, Session second) {
		if (Objects.isNull(first) && Objects.isNull(second)) {
            return 0;
        }
        if (Objects.isNull(first)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(second)) {
            return ascending ? 1 : -1;
        }
        Long firstProjectId = first.getProjectId();
        Long secondProjectId = first.getProjectId();
        CaptureProject firstProject = projectService.getCaptureProject(firstProjectId);
        CaptureProject secondProject = projectService.getCaptureProject(secondProjectId);
        if (Objects.isNull(firstProject) && Objects.isNull(secondProject)) {
            return 0;
        }
        if (Objects.isNull(firstProject)) {
            return ascending ? -1 : 1;
        }
        if (Objects.isNull(secondProject)) {
            return ascending ? 1 : -1;
        }
        if(ascending) {
        	return firstProject.getName().compareTo(secondProject.getName());
        } else {
        	return secondProject.getName().compareTo(firstProject.getName()); 
        }
	}

}
