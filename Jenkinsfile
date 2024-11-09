// Define parameters
pipeline {
    agent any

    // Define a branch parameter to allow selection of the branch at runtime
    parameters {
        string(name: 'BRANCH', defaultValue: 'prod', description: 'Branch to build')
    }

    stages {
        stage('Checkout') {
            steps {
                // Check out the code from the specified branch
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${params.BRANCH}"]],
                    userRemoteConfigs: [[url: 'https://github.com/javascriptjenkins/inventory.git', credentialsId: 'githubcreds']]
                ])
            }
        }

        stage('Build') {
            steps {
                // Use Maven to build the project
                script {
                    // Ensure Maven is available and run the build
                    if (fileExists('pom.xml')) {
                        sh 'mvn clean install'
                    } else {
                        error 'pom.xml not found! Ensure you are on the correct branch with a Maven project.'
                    }
                }
            }
        }
    }
}

