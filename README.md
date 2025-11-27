# Maven dependency explorer sonar plugin

## Description

This [SonarQube](http://www.sonarqube.org) plugin implements new rules upon dependencies.

- Unused dependency

- Waiting dependency update

- Use of transitive dependency

- Uncoherent versions of the same dependency upon 

- Mismatch of dependency license

It also show the dependency tree in synthetic way in SonarQube 

![webapp.png](/home/joshua/bje/repo/buildtools/dependency-explorer-sonar-plugin/doc/webapp.png)

## Compatibility

This plugin work with  :

- SonarQube : v25.11.0.114957

- Maven : v3.9.9

Try with your installation.

## Usage

### Installation Guide

1. [Download and install](http://docs.sonarqube.org/display/SONAR/Setup+and+Upgrade) SonarQube
2. Install the Mercurial plugin by a [direct download](https://github.com/errorscript/dependency-explorer-sonar-plugin/releases) to the extensions folder of you SonarQube installation.
3. Restart your SonarQube
4. Scan your code
5. See blame information on SonarQube
