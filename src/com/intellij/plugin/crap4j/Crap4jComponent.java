package com.intellij.plugin.crap4j;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class Crap4jComponent implements ProjectComponent {
    public Crap4jComponent(Project project) {
    }

    public void initComponent() {
	// TODO: insert component initialization logic here
    }

    public void disposeComponent() {
	// TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
	return "Crap4jComponent";
    }

    public void projectOpened() {
	// called when project is opened
    }

    public void projectClosed() {
	// called when project is being closed
    }
}
