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

        stage('Deploy to EC2') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    sh '''
                        Command_ID=$(aws ssm send-command \
                            --instance-ids "i-037c1a8ae96b5d24c" \
                            --document-name "AWS-RunShellScript" \
                            --parameters 'commands=[
                                "aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 784917519469.dkr.ecr.ap-northeast-2.amazonaws.com",
                                "docker pull 784917519469.dkr.ecr.ap-northeast-2.amazonaws.com/a-stock-simulator:latest",
                                "docker stop a-stock-simulator || true",
                                "docker rm a-stock-simulator || true",
                                "docker run -d --name a-stock-simulator --restart unless-stopped --network host --env-file /home/ubuntu/.config/a-stock-simulator/app.env 784917519469.dkr.ecr.ap-northeast-2.amazonaws.com/a-stock-simulator:latest"
                            ]' \
                            --region ap-northeast-2 \
                            --query 'Command.CommandId' \
                            --output text)
                    '''
                }
            }
        }
    }
}