pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                sh '''
                    docker run --rm \
                    -v /home/ubuntu/jenkins_home/workspace/a-stock-simulator-backend:/workspace \
                    -w /workspace \
                    eclipse-temurin:25-jdk \
                    ./gradlew clean build
                '''
            }
        }
    }
}