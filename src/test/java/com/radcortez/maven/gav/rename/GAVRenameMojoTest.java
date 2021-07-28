package com.radcortez.maven.gav.rename;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GAVRenameMojoTest {
    @Test
    void exactRename() {
        Model model = new Model();
        model.setGroupId("io.smallrye.config");
        model.setArtifactId("smallrye-config-core");
        model.setVersion("1.0.0");
        Dependency dependency = new Dependency();
        dependency.setGroupId("io.smallrye.config");
        dependency.setArtifactId("smallrye-config-core");
        dependency.setVersion("1.0.0");
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.addDependency(dependency);
        model.setDependencyManagement(dependencyManagement);

        GAVRenameMojo rename = new GAVRenameMojo();
        rename.groupId = "io.smallrye.config";
        rename.artifactId = "smallrye-config-core";
        rename.newGroupId = "io.smallrye";
        rename.newArtifactId = "smallrye-core";

        rename.renameModel(model);

        assertEquals("io.smallrye", model.getGroupId());
        assertEquals("smallrye-core", model.getArtifactId());
        assertEquals("io.smallrye", model.getDependencyManagement().getDependencies().get(0).getGroupId());
        assertEquals("smallrye-core", model.getDependencyManagement().getDependencies().get(0).getArtifactId());
    }

    @Test
    void pattern() {
        Model model = new Model();
        model.setGroupId("io.smallrye.config");
        model.setArtifactId("smallrye-config-core");
        model.setVersion("1.0.0");
        Dependency dependency = new Dependency();
        dependency.setGroupId("io.smallrye.config");
        dependency.setArtifactId("smallrye-config-core");
        dependency.setVersion("1.0.0");
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.addDependency(dependency);
        model.setDependencyManagement(dependencyManagement);

        GAVRenameMojo rename = new GAVRenameMojo();
        rename.artifactIdPattern = "smallrye-config";
        rename.newArtifactId = "smallrye-config-jakarta";

        rename.renameModel(model);

        assertEquals("io.smallrye.config", model.getGroupId());
        assertEquals("smallrye-config-jakarta-core", model.getArtifactId());
        assertEquals("io.smallrye.config", model.getDependencyManagement().getDependencies().get(0).getGroupId());
        assertEquals("smallrye-config-jakarta-core", model.getDependencyManagement().getDependencies().get(0).getArtifactId());
    }

    @Test
    void dependencies() {
        Model model = new Model();
        model.setGroupId("io.smallrye.config");
        model.setArtifactId("smallrye-config-core");
        model.setVersion("1.0.0");
        Dependency dependency = new Dependency();
        dependency.setGroupId("io.smallrye.common");
        dependency.setArtifactId("smallrye-common-core");
        dependency.setVersion("1.0.0");
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.addDependency(dependency);
        model.setDependencyManagement(dependencyManagement);

        GAVRenameMojo rename = new GAVRenameMojo();
        rename.groupId = "io.smallrye.common";
        rename.artifactIdPattern = "smallrye-common";
        rename.newArtifactId = "smallrye-common-jakarta";

        rename.renameModel(model);

        assertEquals("io.smallrye.config", model.getGroupId());
        assertEquals("smallrye-config-core", model.getArtifactId());
        assertEquals("io.smallrye.common", model.getDependencyManagement().getDependencies().get(0).getGroupId());
        assertEquals("smallrye-common-jakarta-core", model.getDependencyManagement().getDependencies().get(0).getArtifactId());
    }
}
