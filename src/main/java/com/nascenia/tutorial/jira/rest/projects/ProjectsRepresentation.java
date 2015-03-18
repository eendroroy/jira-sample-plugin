package com.nascenia.tutorial.jira.rest.projects;

import net.jcip.annotations.Immutable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * JAXB representation of a group of options.
 */
@Immutable
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class ProjectsRepresentation {
    @XmlElement
    private Collection<ProjectRepresentation> options;


    private ProjectsRepresentation() {
        options = null;
    }

    /**
     * Stores the specified {@code Project}s in this representation.
     *
     * @param options the options to store
     */
    public ProjectsRepresentation(Iterable<ProjectRepresentation> options) {
        this.options = new HashSet<ProjectRepresentation>();
        for (ProjectRepresentation representation : options) {
            this.options.add(representation);
        }
    }
}
