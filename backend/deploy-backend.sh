#!/bin/bash

set -e

SERVER="gutta@100.65.174.126"
JAR="backend-0.0.1-SNAPSHOT.jar"

LOCAL_JAR="target/$JAR"
REMOTE_NEW="/home/gutta/$JAR.new"
REMOTE_JAR="/home/gutta/$JAR"

echo "========================================"
echo " GDC BACKEND DEPLOYMENT"
echo "========================================"

echo ""
echo "1. Building backend..."

mvn clean package -DskipTests

echo "✓ Maven build successful"

echo ""
echo "2. Checking JAR..."

if [ ! -f "$LOCAL_JAR" ]; then
    echo "ERROR: $LOCAL_JAR not found."
    exit 1
fi

echo "✓ JAR found"

echo ""
echo "3. Uploading JAR to rwp-server..."

scp "$LOCAL_JAR" "$SERVER:$REMOTE_NEW"

echo "✓ Upload completed"

echo ""
echo "4. Deploying on rwp-server..."

ssh "$SERVER" << 'EOF'

set -e

echo ""
echo "----- REPLACING JAR -----"

mv ~/backend-0.0.1-SNAPSHOT.jar.new \
   ~/backend-0.0.1-SNAPSHOT.jar

echo "✓ JAR replaced"

echo ""
echo "----- RESTARTING BACKEND -----"

sudo -n systemctl restart gdc-backend

echo "✓ Backend restart requested"

echo ""
echo "Waiting for Spring Boot to start..."

sleep 5

echo ""
echo "----- SERVICE STATUS -----"

systemctl status gdc-backend --no-pager

echo ""
echo "----- PORT 8080 -----"

ss -ltn | grep ':8080'

echo ""
echo "----- DIRECT SPRING BOOT API -----"

curl -f http://localhost:8080/api/v1/patients > /dev/null

echo "✓ Spring Boot API working"

echo ""
echo "----- NGINX API -----"

curl -f http://localhost/api/v1/patients > /dev/null

echo "✓ Nginx → Spring Boot working"

echo ""
echo "✓ Server deployment completed"

EOF

echo ""
echo "========================================"
echo " GDC BACKEND DEPLOYED SUCCESSFULLY ✓"
echo "========================================"
