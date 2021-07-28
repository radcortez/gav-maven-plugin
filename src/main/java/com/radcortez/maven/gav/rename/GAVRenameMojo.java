package com.radcortez.maven.gav.rename;

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Mojo(name = "rename", aggregator = true, requiresDirectInvocation = true)
public class GAVRenameMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "groupId")
    String groupId;
    @Parameter(property = "artifactId")
    String artifactId;
    @Parameter(property = "groupIdPattern")
    String groupIdPattern;
    @Parameter(property = "artifactIdPattern")
    String artifactIdPattern;
    @Parameter(property = "newGroupId")
    String newGroupId;
    @Parameter(property = "newArtifactId")
    String newArtifactId;

    public void execute() {
        List<Model> modules = rename(readModel(project.getFile()));
        for (Model model : modules) {
            writeModel(model);
        }
    }

    List<Model> rename(Model main) {
        List<Model> modules = new ArrayList<>();
        modules.add(main);
        modules.addAll(readAllChildren(main.getProjectDirectory(), main));
        rename(modules);
        return modules;
    }

    void rename(List<Model> modules) {
        for (Model model : modules) {
            renameModel(model);
        }
    }

    void renameModel(Model model) {
        String groupId = model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
        String artifactId = model.getArtifactId() != null ? model.getArtifactId() : model.getParent().getArtifactId();

        GAVModel gavModel = new GAVModel.Builder()
            .withGroupId(this.groupId != null ? this.groupId : groupId)
            .withArtifactId(this.artifactId != null ? this.artifactId : artifactId)
            .withGroupIdPattern(this.groupIdPattern)
            .withArtifactIdPattern(this.artifactIdPattern)
            .withNewGroupId(this.newGroupId)
            .withNewArtifactId(this.newArtifactId)
            .build();

        rename(model, gavModel);
    }

    private static void rename(Model model, GAVModel gavModel) {
        if (match(gavModel, model)) {
            model.setGroupId(gavModel.renameGroupId(model.getGroupId()));
            model.setArtifactId(gavModel.renameArtifactId(model.getArtifactId()));
        }

        renameParent(model, gavModel);
        renamePlugins(model.getBuild(), gavModel);
        renameDependencyManagement(model.getDependencyManagement(), gavModel);
        renameDependencies(model.getDependencies(), gavModel);
        renameProfiles(model.getProfiles(), gavModel);
    }

    private static void renameParent(Model model, GAVModel gavModel) {
        if (model.getParent() != null) {
            if (match(gavModel, model.getParent())) {
                model.getParent().setGroupId(gavModel.renameGroupId(model.getParent().getGroupId()));
                model.getParent().setArtifactId(gavModel.renameArtifactId(model.getParent().getArtifactId()));
            }
        }
    }

    private static void renameDependencyManagement(DependencyManagement dependencyManagement, GAVModel gavModel) {
        if (dependencyManagement != null && dependencyManagement.getDependencies() != null) {
            for (final Dependency dependency : dependencyManagement.getDependencies()) {
                if (match(gavModel, dependency)) {
                    dependency.setGroupId(gavModel.renameGroupId(dependency.getGroupId()));
                    dependency.setArtifactId(gavModel.renameArtifactId(dependency.getArtifactId()));
                }
            }
        }
    }

    private static void renameDependencies(List<Dependency> dependencies, GAVModel gavModel) {
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                if (match(gavModel, dependency)) {
                    dependency.setGroupId(gavModel.renameGroupId(dependency.getGroupId()));
                    dependency.setArtifactId(gavModel.renameArtifactId(dependency.getArtifactId()));
                }
            }
        }
    }

    private static void renamePlugins(Build build, GAVModel gavModel) {
        if (build == null) {
            return;
        }

        PluginManagement pluginManagement = build.getPluginManagement();
        if (pluginManagement != null && pluginManagement.getPlugins() != null) {
            for (Plugin plugin : pluginManagement.getPlugins()) {
                renameDependencies(plugin.getDependencies(), gavModel);
            }
        }

        if (build.getPlugins() != null) {
            for (Plugin plugin : build.getPlugins()) {
                renameDependencies(plugin.getDependencies(), gavModel);
            }
        }
    }

    private static void renameProfiles(List<Profile> profiles, GAVModel gavModel) {
        if (profiles != null) {
            for (Profile profile : profiles) {
                renameDependencyManagement(profile.getDependencyManagement(), gavModel);
                renameDependencies(profile.getDependencies(), gavModel);
            }
        }
    }

    private static boolean match(GAVModel gavModel, Model model) {
        String groupId = model.getGroupId() != null ? model.getGroupId() : model.getParent().getGroupId();
        String artifactId = model.getArtifactId() != null ? model.getArtifactId() : model.getParent().getArtifactId();

        return gavModel.match(groupId, artifactId);
    }

    private static boolean match(GAVModel gavModel, Parent parent) {
        return gavModel.match(parent.getGroupId(), parent.getArtifactId());
    }

    private static boolean match(GAVModel gavModel, Dependency dependency) {
        return gavModel.match(dependency.getGroupId(), dependency.getArtifactId());
    }

    private static Model readModel(File projectFile) {
        try (FileInputStream fileInputStream = new FileInputStream(projectFile)) {
            Model model = new MavenXpp3Reader().read(fileInputStream);
            model.setPomFile(projectFile);
            return model;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Model> readAllChildren(File baseDir, Model model) {
        final List<String> allModules = new ArrayList<>();
        allModules.addAll(model.getModules());
        allModules.addAll(model.getProfiles().stream().flatMap(profile -> profile.getModules().stream()).collect(toList()));

        final List<Model> children = new ArrayList<>();
        for (String module : allModules) {
            File modulePom = Paths.get(baseDir.getAbsolutePath(), module, "pom.xml").toFile();
            if (modulePom.isFile() && modulePom.exists()) {
                Model child = readModel(modulePom);
                children.add(child);
                children.addAll(readAllChildren(Paths.get(baseDir.getAbsolutePath(), module).toFile(), child));
            }
        }

        return children;
    }

    private static void writeModel(Model model) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(model.getPomFile())) {
            new MavenXpp3Writer().write(fileOutputStream, model);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
