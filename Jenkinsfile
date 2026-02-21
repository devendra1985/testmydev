pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE = 'taledevendra/my-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Install Docker') {
            steps {
                script {
                    sh '''
                        # Check if docker is installed
                        if ! command -v docker &> /dev/null; then
                            echo "Installing Docker..."
                            curl -fsSL https://get.docker.com -o get-docker.sh
                            sh get-docker.sh
                            sudo usermod -aG docker jenkins
                        fi
                        docker --version
                    '''
                }
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                    """
                }
            }
        }
    }
}
