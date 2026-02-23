pipeline {
    agent any
    parameters {
        string(name: 'VERSION', defaultValue: '1.0', description: 'App version')
    }
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE = 'taledevendra/my-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        GIT_REPO = 'github.com/devendra1985/testmydev.git'
        // SonarQube (self-hosted) defaults. Override at job level if needed.
        SONAR_HOST_URL = 'http://host.docker.internal:9002'
        SONAR_PROJECT_KEY = 'testmydev'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Sonar Scan') {
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    sh '''
                        set -euxo pipefail

                        # Ensure Maven wrapper is executable
                        chmod +x mvnw

                        # Build + test + Sonar analysis (no Jenkins Sonar plugin required)
                        ./mvnw -B clean verify sonar:sonar \
                          -Dsonar.host.url="${SONAR_HOST_URL}" \
                          -Dsonar.login="${SONAR_TOKEN}" \
                          -Dsonar.projectKey="${SONAR_PROJECT_KEY}" \
                          -Dsonar.projectName="${SONAR_PROJECT_KEY}" \
                          -Dsonar.java.binaries=target/classes
                    '''
                }
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
        
        stage('Update Manifest & Push to Git') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh """
                        # Update the container image in the deployment manifest
                        sed -i 's|image:.*|image: ${DOCKER_IMAGE}:${DOCKER_TAG}|' kube/manf.yaml
                        
                        echo "Updated kube/manf.yaml with image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        cat kube/manf.yaml
                        
                        # Configure git
                        git config user.name "Jenkins"
                        git config user.email "jenkins@example.com"
                        
                        # Stage and commit the change
                        git add kube/manf.yaml
                        git commit -m "Update deployment image to ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        
                        # Push to main branch using token-authenticated URL
                        git push https://\${GIT_USER}:\${GIT_TOKEN}@${GIT_REPO} HEAD:main
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
