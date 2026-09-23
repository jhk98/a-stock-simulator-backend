pipeline {
    agent any

    stages {
        stage('Build & Test') {
            steps {
                sh './gradlew clean build'
            }
        }
    }
}