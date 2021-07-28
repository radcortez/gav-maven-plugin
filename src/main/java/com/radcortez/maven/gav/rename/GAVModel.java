package com.radcortez.maven.gav.rename;

import java.util.regex.Pattern;

public class GAVModel {
    private final Pattern groupId;
    private final Pattern artifactId;

    private final String newGroupId;
    private final String newArtifactId;

    public GAVModel(final Builder builder) {
        this.groupId = builder.getGroupIdPattern();
        this.artifactId = builder.getArtifactIdPattern();
        this.newGroupId = builder.newGroupId;
        this.newArtifactId = builder.newArtifactId;
    }

    public boolean match(final String groupId, final String artifactId) {
        return this.groupId.matcher(groupId).find() && this.artifactId.matcher(artifactId).find();
    }

    public String renameGroupId(final String groupId) {
        return newGroupId != null ? this.groupId.matcher(groupId).replaceAll(newGroupId) : groupId;
    }

    public String renameArtifactId(final String artifactId) {
        return newArtifactId != null ? this.artifactId.matcher(artifactId).replaceAll(newArtifactId) : artifactId;
    }

    public static class Builder {
        private String groupId;
        private String artifactId;
        private String groupIdPattern;
        private String artifactIdPattern;

        private String newGroupId;
        private String newArtifactId;

        public Builder withGroupId(final String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder withArtifactId(final String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder withGroupIdPattern(final String groupIdPattern) {
            this.groupIdPattern = groupIdPattern;
            return this;
        }

        public Builder withArtifactIdPattern(final String artifactIdPattern) {
            this.artifactIdPattern = artifactIdPattern;
            return this;
        }

        public Builder withNewGroupId(final String newGroupId) {
            this.newGroupId = newGroupId;
            return this;
        }

        public Builder withNewArtifactId(final String newArtifactId) {
            this.newArtifactId = newArtifactId;
            return this;
        }

        Pattern getGroupIdPattern() {
            return this.groupIdPattern != null ? Pattern.compile(groupIdPattern) : Pattern.compile(groupId, Pattern.LITERAL);
        }

        Pattern getArtifactIdPattern() {
            return this.artifactIdPattern != null ? Pattern.compile(artifactIdPattern) : Pattern.compile(artifactId, Pattern.LITERAL);
        }

        public GAVModel build() {
            return new GAVModel(this);
        }
    }
}
