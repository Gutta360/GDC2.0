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

if [ -z "${JAVA_HOME:-}" ] && [ -x /usr/libexec/java_home ]; then
    export JAVA_HOME
    JAVA_HOME="$(/usr/libexec/java_home -v 21)"
fi

if [ -n "${JAVA_HOME:-}" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "Using Java:"
java -version

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
echo "2a. Verifying Flyway auto-configuration is packaged..."

if ! jar tf "$LOCAL_JAR" | grep -q 'BOOT-INF/lib/spring-boot-flyway-'; then
    echo "ERROR: spring-boot-flyway is missing from $LOCAL_JAR."
    echo "This JAR would not run Flyway before Hibernate validation."
    exit 1
fi

if ! jar tf "$LOCAL_JAR" | grep -q 'BOOT-INF/lib/flyway-database-postgresql-'; then
    echo "ERROR: flyway-database-postgresql is missing from $LOCAL_JAR."
    echo "This JAR cannot run PostgreSQL Flyway migrations."
    exit 1
fi

if ! jar tf "$LOCAL_JAR" | grep -q 'BOOT-INF/classes/db/migration/V6__create_treatment_pharmacy_payment_tables.sql'; then
    echo "ERROR: V6 treatment/pharmacy/payment migration is missing from $LOCAL_JAR."
    exit 1
fi

echo "✓ Flyway auto-configuration packaged"

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
echo "----- VERIFYING REMOTE JAR -----"

jar tf ~/backend-0.0.1-SNAPSHOT.jar | grep 'BOOT-INF/lib/spring-boot-flyway-'
jar tf ~/backend-0.0.1-SNAPSHOT.jar | grep 'BOOT-INF/lib/flyway-database-postgresql-'
jar tf ~/backend-0.0.1-SNAPSHOT.jar | grep 'BOOT-INF/classes/db/migration/V6__create_treatment_pharmacy_payment_tables.sql'

echo "✓ Remote JAR contains Spring Boot Flyway support"

echo ""
echo "----- RESTARTING BACKEND -----"

sudo -n systemctl restart gdc-backend

echo "✓ Backend restart requested"

echo ""
echo "Waiting for Spring Boot to start..."

for attempt in {1..30}; do
    if curl -fsS http://localhost:8080/api/v1/patients > /dev/null; then
        break
    fi

    if [ "$attempt" -eq 30 ]; then
        echo "ERROR: Spring Boot API did not become ready within 30 seconds."
        systemctl status gdc-backend --no-pager -l
        exit 1
    fi

    sleep 1
done

echo ""
echo "----- SERVICE STATUS -----"

systemctl status gdc-backend --no-pager -l

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
