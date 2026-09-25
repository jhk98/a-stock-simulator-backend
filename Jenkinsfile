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
                        -e TZ=Asia/Seoul \
                        -v /home/ubuntu/jenkins_home/workspace/a-stock-simulator-backend:/workspace \
                        -w /workspace \
                        eclipse-temurin:25-jdk \
                        ./gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                        -t a-stock-simulator:latest \
                        .
                '''
            }
        }

        stage('ECR Login') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh '''
                        aws ecr get-login-password --region ap-northeast-2 \
                        | docker login \
                            --username AWS \
                            --password-stdin 784917519469.dkr.ecr.ap-northeast-2.amazonaws.com
                    '''
                }
            }
        }

        stage('Docker Tag') {
            steps {
                sh '''
                    docker tag \
                        a-stock-simulator:latest \
                        784917519469.dkr.ecr.ap-northeast-2.amazonaws.com/a-stock-simulator:latest
                '''
            }
        }

        stage('ECR Push') {
            steps {
                sh '''
                    docker push \
                        784917519469.dkr.ecr.ap-northeast-2.amazonaws.com/a-stock-simulator:latest
                '''
            }
        }

        stage('Test EC2 SSM') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh '''
                        aws ssm send-command \
                            --instance-ids "i-037c1a8ae96b5d24c" \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=["echo Jenkins SSM connection success"]' \
                            --region ap-northeast-2
                    '''
                }
            }
        }
    }
}