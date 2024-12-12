// Define parameters
pipeline {
    agent any


    tools {
        maven 'maven3' // Use the name you specified in Global Tool Configuration
    }


    // Define a branch parameter to allow selection of the branch at runtime
    parameters {
        string(name: 'BRANCH', defaultValue: 'prod', description: 'Branch to build')
        string(name: 'HOSTNAME', defaultValue: '64.227.4.159', description: 'Target host IP address for deployment')
        string(name: 'SSHUSER', defaultValue: 'root', description: 'user for ssh key')
        string(name: 'LOAD_REF_DATA', defaultValue: 'no', description: 'Value for load.ref.data (leave blank to keep default)')
        string(name: 'ENVIRONMENT_NAME', defaultValue: 'prod', description: 'environment name, prod and dev1 are the optional values')
        string(name: 'BASE_QR_DOMAIN', defaultValue: 'https://jenkins.codes', description: 'probably can delete this')
        string(name: 'DB_TYPE', defaultValue: 'h2', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_H2_CONSOLE_ENABLED', defaultValue: 'true', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_H2_WEB_ALLOWOTHERS', defaultValue: 'true', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_DRIVER_CLASS_NAME', defaultValue: 'org.h2.Driver', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_JPA_DATABASE_PLATFORM', defaultValue: 'org.hibernate.dialect.H2Dialect', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_JPA_HIBERNATE_DIALECT', defaultValue: 'org.hibernate.dialect.H2Dialect', description: 'type of database.  only inventory uses h2')
        string(name: 'DB_URI', defaultValue: 'jdbc:h2:file:./data/demo', description: 'datasource uri')
        string(name: 'DB_USERNAME', defaultValue: 'produser', description: 'spring data username')
        string(name: 'SPRING_DATABASE_ACTION', defaultValue: 'update', description: 'this value will control create-drop, update, etc')
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
                    // Load multiple credentials simultaneously
                    withCredentials([
                        string(credentialsId: 'TWILIO_API_USER', variable: 'TWILIO_API_USER'),
                        string(credentialsId: 'TWILIO_API_PASSWORD', variable: 'TWILIO_API_PASSWORD'),
                        string(credentialsId: 'SENDGRID_API_KEY', variable: 'SENDGRID_API_KEY'),
                         string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD'),
                         string(credentialsId: 'JWT_SECRET_KEY', variable: 'JWT_SECRET_KEY')
                    ])
                    {
                        dir('inventory') {
                            sh "ls -l"
                            // Replace spring.profiles.active if provided
                            if (params.ENVIRONMENT_NAME) {
                                sh "sed -i 's/^spring\\.profiles\\.active=.*/spring.profiles.active=${params.ENVIRONMENT_NAME}/' src/main/resources/application.properties"
                            }
                            // Only replace if LOAD_REF_DATA parameter is provided
                            if (params.LOAD_REF_DATA) {
                                // Replace the `load.ref.data` property value in application.properties
                                sh "sed -i 's/^load\\.ref\\.data=.*/load.ref.data=${params.LOAD_REF_DATA}/' src/main/resources/application.properties"
                            }
                            // Replace spring.datasource.username if provided
                            if (params.DB_USERNAME) {
                                sh "sed -i 's/^spring\\.datasource\\.username=.*/spring.datasource.username=${params.DB_USERNAME}/' src/main/resources/application.properties"
                            }
                            // Replace spring.jpa.hibernate.ddl-auto if provided
                            if (params.SPRING_DATABASE_ACTION) {
                                sh "sed -i 's/^spring\\.jpa\\.hibernate\\.ddl-auto=.*/spring.jpa.hibernate.ddl-auto=${params.SPRING_DATABASE_ACTION}/' src/main/resources/application.properties"
                            }
                            // Replace spring.datasource.driver-class-name if provided
                            if (params.DB_DRIVER_CLASS_NAME) {
                                sh "sed -i 's/^spring\\.datasource\\.driver-class-name=.*/spring.datasource.driver-class-name=${params.DB_DRIVER_CLASS_NAME}/' src/main/resources/application.properties"
                            }
                            // Replace spring.jpa.database-platform if provided
                            if (params.DB_JPA_DATABASE_PLATFORM) {
                                sh "sed -i 's/^spring\\.jpa\\.database-platform=.*/spring.jpa.database-platform=${params.DB_JPA_DATABASE_PLATFORM}/' src/main/resources/application.properties"
                            }
                            // Replace spring.jpa.properties.hibernate.dialect if provided
                            if (params.DB_JPA_HIBERNATE_DIALECT) {
                                sh "sed -i 's/^spring\\.jpa\\.properties\\.hibernate\\.dialect=.*/spring.jpa.properties.hibernate.dialect=${params.DB_JPA_HIBERNATE_DIALECT}/' src/main/resources/application.properties"
                            }
                            // Replace spring.datasource.url if provided
                            if (params.DB_URI) {
                                sh "sed -i 's|^spring\\.datasource\\.url=.*|spring.datasource.url=${params.DB_URI}|' src/main/resources/application.properties"
                            }
                            // Replace spring.h2.console.enabled if provided
                            if (params.DB_H2_CONSOLE_ENABLED) {
                                sh "sed -i 's/^spring\\.h2\\.console\\.enabled=.*/spring.h2.console.enabled=${params.DB_H2_CONSOLE_ENABLED}/' src/main/resources/application.properties"
                            }

                            // Replace spring.h2.console.settings.web-allow-others if provided
                            if (params.DB_H2_WEB_ALLOWOTHERS) {
                                sh "sed -i 's/^spring\\.h2\\.console\\.settings\\.web-allow-others=.*/spring.h2.console.settings.web-allow-others=${params.DB_H2_WEB_ALLOWOTHERS}/' src/main/resources/application.properties"
                            }

                            // Replace the qr ddomain if provided
                            if (params.BASE_QR_DOMAIN) {
                                sh "sed -i 's|^base\\.qr\\.domain=.*|base.qr.domain=${env.BASE_QR_DOMAIN}|' src/main/resources/application.properties"
                            }

                            sh "sed -i 's/^twilio\\.api\\.username=.*/twilio.api.username=${TWILIO_API_USER}/' src/main/resources/application.properties"
                            sh "sed -i 's/^twilio\\.api\\.password=.*/twilio.api.password=${TWILIO_API_PASSWORD}/' src/main/resources/application.properties"
                            sh "sed -i 's/^sendgrid\\.api\\.key=.*/sendgrid.api.key=${SENDGRID_API_KEY}/' src/main/resources/application.properties"
                            sh "sed -i 's/^spring\\.datasource\\.password=.*/spring.datasource.password=${DB_PASSWORD}/' src/main/resources/application.properties"
                            sh "sed -i 's/^security\\.jwt\\.token\\.secret-key=.*/security.jwt.token.secret-key=${JWT_SECRET_KEY}/' src/main/resources/application.properties"


                        }
                    }
                }
            }
        }


        stage('Replace URLs') {
            steps {
                script {
                    // Run within the 'inventory' directory
                    dir('inventory') {
                        // Find all files in src/main/java and src/main/resources and replace occurrences
                        sh """
                            find src/main/java src/main/resources -type f -exec sed -i 's|http://localhost:8080|https://inventory.techvvs.io|g' {} +
                        """
                    }
                }
            }
        }

        stage('Copy Font Files') {
            steps {
                script {
                    dir('inventory') {
                        sshagent(credentials: ['inventory-root-sshkey']) {
                            sh """
                                scp -o StrictHostKeyChecking=no -r uploads/font/ ${params.SSHUSER}@${params.HOSTNAME}:~/deployments/inventory/uploads
                            """
                        }
                    }
                }
            }
        }

        stage('Copy Global User Files') {
            steps {
                script {
                    dir('inventory') {
                        sshagent(credentials: ['inventory-root-sshkey']) {
                            // Ensure the target directory exists
                            sh """
                                ssh -o StrictHostKeyChecking=no ${params.SSHUSER}@${params.HOSTNAME} "mkdir -p ~/deployments/inventory/uploads/globaluserfiles"
                            """

                            // Copy the files
                            sh """
                                scp -o StrictHostKeyChecking=no -r uploads/globaluserfiles ${params.SSHUSER}@${params.HOSTNAME}:~/deployments/inventory/uploads
                            """
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


        stage('Stop Java Process') {
            steps {
                sshagent(credentials: ['inventory-root-sshkey']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${params.SSHUSER}@${params.HOSTNAME} "sudo killall java -15 || echo 'No Java process found to kill'"
                    """
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

                    // Start the application and tail the log for 15 seconds
                            sh """
                                ssh -o StrictHostKeyChecking=no ${params.SSHUSER}@${params.HOSTNAME} << 'EOF'
                                cd /root/deployments/inventory
                                sudo su -c "nohup java -jar inventory-0.0.1-SNAPSHOT.jar > app.log 2>&1 &" -s /bin/sh root
                                sleep 2  # Give a moment for the app to start and write to the log
                                sudo timeout 30 tail -f app.log || true
EOF
                            """
                        } else {
                            error "JAR file not found at ${jarFile}"
                        }
                    }
                }
            }

       }

      stage('Check Application Status') {
           steps {
               script {
                   def response = httpRequest(
                       url: 'https://inventory.techvvs.io/login',
                       httpMode: 'GET',
                       validResponseCodes: '200'  // Expecting a 200 response
                   )
                   echo "Application is up and returned 200 OK."
               }
           }
       }
    }
}
