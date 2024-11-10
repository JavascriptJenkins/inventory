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
        string(name: 'LOAD_REF_DATA', defaultValue: 'no', description: 'Value for load.ref.data (leave blank to keep default)')
        string(name: 'ENVIRONMENT_NAME', defaultValue: 'prod', description: 'environment name, prod and dev1 are the optional values')
        string(name: 'BASE_QR_DOMAIN', defaultValue: '', description: 'probably can delete this')
        string(name: 'TWILIO_API_USER', defaultValue: '', description: 'twilio username')
        string(name: 'TWILIO_API_PASSWORD', defaultValue: '', description: 'twilio password')
        string(name: 'SENDGRID_API_KEY', defaultValue: '', description: 'sendgrid api key')
        string(name: 'DB_TYPE', defaultValue: 'h2', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_H2_CONSOLE_ENABLED', defaultValue: 'true', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_H2_WEB_ALLOWOTHERS', defaultValue: 'true', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_DRIVER_CLASS_NAME', defaultValue: 'org.h2.Driver', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_JPA_DATABASE_PLATFORM', defaultValue: 'org.hibernate.dialect.H2Dialect', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_JPA_HIBERNATE_DIALECT', defaultValue: 'org.hibernate.dialect.H2Dialect', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_URI', defaultValue: 'jdbc:h2:file:./data/demo', description: 'datasource uri')
        string(name: 'DB_USERNAME', defaultValue: '', description: 'spring data username')
        string(name: 'DB_PASSWORD', defaultValue: '', description: 'spring data password')
        string(name: 'SPRING_DATABASE_ACTION', defaultValue: 'update', description: 'this value will control create-drop, update, etc')
        string(name: 'JWT_SECRET_KEY', defaultValue: '', description: 'key for signing tokens')
        string(name: 'MAX_FILE_SIZE', defaultValue: '500MB', description: 'key for signing tokens')
        string(name: 'MAX_REQ_SIZE', defaultValue: '500MB', description: 'key for signing tokens')

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

        stage('Update Properties') {
            steps {
                script {
                    dir('inventory') {
                        sh "ls -l"
                        // Only replace if LOAD_REF_DATA parameter is provided
                        if (params.LOAD_REF_DATA) {
                            // Replace the `load.ref.data` property value in application.properties
                            sh "sed -i 's/^load\\.ref\\.data=.*/load.ref.data=${params.LOAD_REF_DATA}/' src/main/resources/application.properties"
                            sh "sed -i 's/^load\\.ref\\.data=.*/load.ref.data=${params.LOAD_REF_DATA}/' src/main/resources/application.properties"
                            sh "sed -i 's/^load\\.ref\\.data=.*/load.ref.data=${params.LOAD_REF_DATA}/' src/main/resources/application.properties"
                        }
                    }
                }
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
                sshagent(credentials: ['inventory-root-sshkey']) {
                    script {
                        def jarFile = 'inventory/target/inventory-0.0.1-SNAPSHOT.jar'

                        if (fileExists(jarFile)) {
                            // Run remote commands to stop the Java process and move existing JAR to backup
                            sh """
                                ssh -o StrictHostKeyChecking=no ${params.SSHUSER}@${params.HOSTNAME} << EOF
                                    sudo killall java -15 || echo "No Java process found to kill"
                                    sudo mv /root/deployments/inventory/inventory-0.0.1-SNAPSHOT.jar /root/deployments/inventory/backupdep/inventory-0.0.1-SNAPSHOT.jar || echo "No existing JAR to move"
EOF
                            """

                            // Copy the new .jar file to the remote server
                            sh """
                                scp -o StrictHostKeyChecking=no ${jarFile} ${params.SSHUSER}@${params.HOSTNAME}:/root/deployments/inventory/
                            """
                        } else {
                            error "JAR file not found at ${jarFile}"
                        }
                    }
                }
            }

       }
    }
}
