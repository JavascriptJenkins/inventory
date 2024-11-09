// Define parameters
pipeline {
    agent any


    tools {
        maven 'maven3' // Use the name you specified in Global Tool Configuration
    }

    environment {
        SSH_KEY = credentials('inventory-root-sshkey') // Replace 'ssh-key-id' with the Jenkins credentials ID for the SSH key
    }


    // Define a branch parameter to allow selection of the branch at runtime
    parameters {
        string(name: 'BRANCH', defaultValue: 'prod', description: 'Branch to build')
        string(name: 'HOSTNAME', defaultValue: '64.227.4.159', description: 'Target host IP address for deployment')
        string(name: 'SSHUSER', defaultValue: 'root', description: 'user for ssh key')
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

       stage('Deploy') {
            when {
                expression { params.HOSTNAME != '' } // Only run if HOSTNAME parameter is provided
            }
            steps {
                script {
                    def jarFile = 'inventory/inventory/target/inventory-0.0.1-SNAPSHOT.jar' // Replace with actual .jar file path

                    if (fileExists(jarFile)) {
                        // SSH into the remote server to stop the running Java process and move existing JAR to backup
                        sh """
                            ssh -i ${SSH_KEY} ${params.SSHUSER}@${params.HOSTNAME} << EOF
                                sudo killall java -15 || echo "No Java process found to kill"
                                sudo mv /root/deployments/inventory/inventory-0.0.1-SNAPSHOT.jar /root/deployments/inventory/backupdep/inventory-0.0.1-SNAPSHOT.jar || echo "No existing JAR to move"
                            EOF
                        """

                        // Copy the new .jar file to the remote server
                        sh """
                            scp -i ${SSH_KEY} ${jarFile} ${params.SSHUSER}@${params.HOSTNAME}:/root/deployments/inventory/
                        """
                    } else {
                        error "JAR file not found at ${jarFile}"
                    }
                }
            }
       }
    }
}
