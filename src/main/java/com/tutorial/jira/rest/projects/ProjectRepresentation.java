package com.tutorial.jira.rest.projects;

import com.atlassian.jira.project.Project;
import net.jcip.annotations.Immutable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB representation of a project's information. This can be marshalled as
 * either JSON or XML, depending on what the client asks for.
 */
@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class ProjectRepresentation {
    @XmlElement
    private String value;

    @XmlElement
    private String label;

    private ProjectRepresentation() {
        value = null;
        label = null;
    }

    /**
     * Initializes the representation's values to those in the specified
     * {@code Project}.
     *
     * @param project the project to use for initialization
     */
    public ProjectRepresentation(Project project) {
        this.value = project.getKey();
        this.label = project.getName();
    }

}
