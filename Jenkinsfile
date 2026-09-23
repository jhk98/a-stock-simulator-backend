pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                withCredentials([string(
                    credentialsId: 'mysql-db-password',
                    variable: 'DB_PASSWORD'
                )]) {
                    sh '''
                        docker run --rm \
                        -e DB_PASSWORD="$DB_PASSWORD" \
                        -v /home/ubuntu/jenkins_home/workspace/a-stock-simulator-backend:/workspace \
                        -w /workspace \
                        eclipse-temurin:25-jdk \
                        ./gradlew clean build
                    '''
                }
            }
        }
    }
}