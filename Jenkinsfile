pipeline {
    agent key

    stages {
        stage('Build & Test') {
            steps {
                sh './gradlew clean build'
            }
        }
    }
}