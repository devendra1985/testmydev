pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE = 'taledevendra/my-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                // Or specify repo explicitly:
                // git branch: 'main', url: 'https://github.com/your-org/your-repo.git'
            }
        }
    

        
        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    sh """
                        echo ${DOCKER_HUB_CREDENTIALS_PSW} | docker login -u ${DOCKER_HUB_CREDENTIALS_USR} --password-stdin
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        stage('Update Deployment Manifest') {
            steps {
                script {
                    sh """
                        # Update the container image in the deployment manifest
                        # Replace the image line with the new Docker image and tag
                        sed -i.bak 's|image:.*|image: ${DOCKER_IMAGE}:${DOCKER_TAG}|' kube/manf.yaml
                        # Remove backup file
                        rm -f kube/manf.yaml.bak
                        # Display the updated manifest
                        echo "Updated deployment manifest with image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        cat kube/manf.yaml
                    """
                }
            }
        }
    }
    
    post {
        always {
            sh 'docker logout'
        }
        success {
            echo "Image pushed successfully: ${DOCKER_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo "Build failed!"
        }
    }
}
