// Define parameters
pipeline {
    agent any


    tools {
        maven 'Maven3' // Use the name you specified in Global Tool Configuration
    }


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
                script {
                    // Change directory to /inventory
                    dir('inventory') {
                        // Check if pom.xml exists within the /inventory directory
                        if (fileExists('pom.xml')) {
                            // Run Maven build
                            sh 'mvn clean install'
                        } else {
                            error 'pom.xml not found in the /inventory directory! Ensure this is a Maven project.'
                        }
                    }
                }
            }
        }
    }
}
