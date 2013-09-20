# Building WADL Tools with Maven

WADL Tools is built using Maven 3. The build is configured with 2 profiles; a default profile for daily development and a `sonatype-oss-release` "release" profile for producing a proper release for Maven Central. Several features required for releasing WADL Tools to Maven Central are disabled in the default profile. To build the project with the release profile, use the command line option `-Psonatype-oss-release`. The following table lists the outputs of the build, along with whether or not the output is created by the default and release profiles.

| Output      | Default Profile | Release Profile |
| ----------- | :-------------: | :-------------: |
| wadl-tools  |       X         |        X        |
| GPG Signed  |                 |        X        |
| Javadoc Jar |                 |        X        |
| Source Jar  |                 |        X        |

## Default Profile

The minimal build of WADL Tools can be performed by simply executing `mvn` at the command line. However, the jar file produced by this build will include a `MANIFEST.MF` file with the line `Built-By: ${user.name}`, where `${user.name}` is the username of the user currently logged into the computer. The recommended default build includes an explicitly specified `user.name` property which is used in the generated manifest.

```
mvn "-Duser.name=Your Name"
```

## Release Build

The release build of WADL Tools uses the `sonatype-oss-release` profile and requires additional configuration with the following syntax, where `[GPG options]` refers to the options given in the **Artifact Signing (GPG)** section below.

```
mvn -Psonatype-oss-release "-Duser.name=Your Name" [GPG options]
```

### Artifact Signing (GPG)

In release builds, Maven will sign the build artifacts with GPG, and assumes you have your system configured according to Sonatype's Blog entry ["How to Generate PGP Signatures with Maven"](http://www.sonatype.com/people/2010/01/how-to-generate-pgp-signatures-with-maven/). One of the following should be used each time Maven is used to build the project with the `sonatype-oss-release` profile.

1. Configure the GPG passphrase by passing `-Dgpg.passphrase={passphrase}` to Maven (or simply `-Dgpg.passphrase` if your private key does not require a passphrase).
2. Skip the phase by passing `-Dgpg.skip=true` to Maven (required for users without GPG installed or configured).
3. Run the Maven build without specifying one of the above, in which case signing will be enabled and Maven will ask you to enter the passphrase during the signing phase.

In addition to these options, users with GPG 2 installed may wish to specify the `-Dgpg.useagent=true` argument, which prevents Maven from producing messages about `--use-agent` being an obsolete option.

## Additional Build Options

These options are not required for building WADL Tools, but may be useful in some scenarios.

| Option | Result |
| ------ | ------ |
| `-Dmaven.javadoc.skip=true` | Skips the generation of Javadoc archive for each artifact. This flag only applies to the release profile. |
| `-Dsource.skip=true` | Skips the generation of a source archive for each artifact. This flag only applies to the release profile. |
