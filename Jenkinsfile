pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                sh '''
                    docker un --rm \
                    -v "$PWD:/workspace" \
                    -w /workspace \
                    eclipse-temurin:25-jdk \
                    ./gradlew clean build
                '''
            }
        }
    }
}