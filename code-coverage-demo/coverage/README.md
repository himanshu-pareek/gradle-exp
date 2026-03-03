# Code Coverage Integration

This document outlines the architecture and usage of the code coverage integration in this project.

## Objective

To generate the code coverage report and enforce the code coverage in the CI pipeline.
The modules will be tested on different machines (GitHub runners), parallely.

## Steps

1. Run the tests along with test execution data - **Jacoco Plugin**
   1. HTML, XML, CSV report for each project
   2. Test execution data
2. Aggregate each subproject's test execution data to get the root report - Jacoco Plugin
   1. Compilation required (slow)
   2. HTML, CSV, XML format
3. Aggregate each subproject's csv report to get the aggregate csv report
   1. Fast, no compilation required
4. Compare the csv report with the baseline in PR checks
5. Update the baseline automatically, if improves
   1. Create a GitHub App
   2. Store the app id and private key in the Repository Secrets
   3. Install the GitHub App inside the repository
   4. Allow the app to push code directly (without creating any PR)
   5. Create GITHUB_TOKEN using **create-github-app-token** action
   6. Use token to checkout the repository

## Architecture

The code coverage solution is built using a custom Gradle plugin located in the `buildSrc` directory. It consists of several key components:

- **`CodeCoveragePlugin.kt`**: The main entry point of the plugin. It applies the Jacoco plugin and registers the custom tasks.
- **`JacocoTasks.kt`**: Configures Jacoco for all subprojects, ensuring that test reports are generated in CSV, XML, and HTML formats.
- **`AggregateCoverageTask.kt`**: A custom task that aggregates the coverage data from all subproject CSV reports into a single CSV file located at `coverage/current.csv`. This provides a project-wide overview of code coverage.
- **`CompareCoverageCsvTask.kt`**: A custom task designed to compare the current code coverage (`coverage/current.csv`) with a baseline coverage report (`coverage/base.csv`). This is useful for tracking coverage changes over time.
- **`CodeCoverageExtension.kt`**: Provides a mechanism for configuring the code coverage plugin, such as specifying files or directories to exclude from coverage analysis.

## Usage

### Generating Coverage Reports

To generate code coverage reports for all subprojects, run the following Gradle command:

```bash
./gradlew test jacocoTestReport
```

This will create individual Jacoco reports for each subproject in their respective `build/reports/jacoco/test` directories.

### Generate Root Coverage Report

To generate the aggregated root coverage report in `html` format for all the modules, run the following command:

```shell
./gradlew jacocoRootReport
```

### Aggregating Coverage Data

To aggregate the coverage data from all subprojects into a single report, run the `aggregateCoverageCsv` task:

```bash
./gradlew aggregateCoverageCsv
```

This will generate the `coverage/current.csv` file, which contains a summary of the coverage metrics for each module.

### Comparing Coverage Against a Baseline

To compare the current code coverage with a baseline, first ensure you have a `coverage/base.csv` file. Then, run the `compareCoverageCsv` task:

```bash
./gradlew compareCoverageCsv
```

This task will compare `coverage/current.csv` with `coverage/base.csv` and provide a summary of any changes in coverage.

## References

* [Jacoco](https://www.jacoco.org/jacoco/)
* [Jacoco Plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
* [upload-artifact github action](https://github.com/actions/upload-artifact)
* [download-artifact github action](https://github.com/actions/download-artifact)
* [create-github-app-token github action](https://github.com/actions/create-github-app-token)
