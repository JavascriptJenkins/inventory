#!/bin/bash

# Usage function to display help
usage() {
    echo "Usage: $0 -a APP_NAME -e TARGET_ENV -h REMOTE_HOST -b BRANCH -r GIT_REPO -p APP_PROPERTIES -s LOCAL_SSH_PASSWORD -t TENANT"
    exit 1
}

# Parse input parameters
while getopts "a:e:h:b:r:p:s:t:" opt; do
    case "$opt" in
        a) APP_NAME="$OPTARG" ;;
        e) TARGET_ENV="$OPTARG" ;;
        h) REMOTE_HOST="$OPTARG" ;;
        b) BRANCH="$OPTARG" ;;
        r) GIT_REPO="$OPTARG" ;;
        p) APP_PROPERTIES="$OPTARG" ;;
        s) LOCAL_SSH_PASSWORD="$OPTARG" ;;
        t) TENANT="$OPTARG" ;;
        *) usage ;;
    esac
done

# Check if all parameters are provided
if [ -z "$APP_NAME" ] || [ -z "$TARGET_ENV" ] || [ -z "$REMOTE_HOST" ] || [ -z "$BRANCH" ] || [ -z "$GIT_REPO" ] || [ -z "$APP_PROPERTIES" ] || [ -z "$LOCAL_SSH_PASSWORD" ] || [ -z "$TENANT" ]; then
    usage
fi

# Create a log file
LOG_FILE="${APP_NAME}_${TENANT}_deploy.log"
echo "Deployment started for tenant $TENANT" > "$LOG_FILE"

# Step 2: Pull the code from GIT_REPO using the provided BRANCH
echo "Cloning repository..." | tee -a "$LOG_FILE"
git clone -b "$BRANCH" "$GIT_REPO" "$APP_NAME" 2>&1 | tee -a "$LOG_FILE"
if [ $? -ne 0 ]; then
    echo "Git clone failed!" | tee -a "$LOG_FILE"
    exit 1
fi

# Navigate into the cloned repository
cd "$APP_NAME" || exit

# Step 3: Overwrite the application.properties file
echo "Overwriting application.properties..." | tee -a "$LOG_FILE"
cp "$APP_PROPERTIES" "${APP_NAME}"/"${APP_NAME}"src/main/resources/application.properties 2>&1 | tee -a "$LOG_FILE"
if [ $? -ne 0 ]; then
    echo "Failed to copy application.properties!" | tee -a "$LOG_FILE"
    exit 1
fi

# Step 4: Run Maven clean and install
echo "HERE WE ARE"
cd "$APP_NAME" || exit
pwd
echo "Running Maven clean and install..." | tee -a "$LOG_FILE"
mvn clean install 2>&1 | tee -a "$LOG_FILE"
if [ $? -ne 0 ]; then
    echo "Maven build failed!" | tee -a "$LOG_FILE"
    exit 1
fi

# Step 5: SCP the resulting JAR file
echo "Transferring JAR file via SCP..." | tee -a "$LOG_FILE"
JAR_FILE=$(find target -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "No JAR file found in target directory!" | tee -a "$LOG_FILE"
    exit 1
fi

# Use expect to autofill the password for SCP
#expect <<- EOF | tee -a "$LOG_FILE"
#spawn scp "$JAR_FILE" "root@$REMOTE_HOST:deployments/inventory/"
#expect "password:"
#send "$LOCAL_SSH_PASSWORD\r"
#expect eof
#EOF
sshpass -p "$LOCAL_SSH_PASSWORD" scp -v -o StrictHostKeyChecking=no -C "$JAR_FILE" "root@$REMOTE_HOST:deployments/inventory/" | tee -a "$LOG_FILE"

#sshpass -p "$LOCAL_SSH_PASSWORD" scp -C "$JAR_FILE" "root@$REMOTE_HOST:deployments/inventory/" | tee -a "$LOG_FILE"


if [ $? -ne 0 ]; then
    echo "SCP failed!" | tee -a "$LOG_FILE"
    exit 1
fi

echo "Deployment completed successfully!" | tee -a "$LOG_FILE"
