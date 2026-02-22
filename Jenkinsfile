pipeline {
    agent any
    
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        GIT_CREDENTIALS = credentials('github-credentials')
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
        
        stage('Commit and Push to Git') {
            steps {
                script {
                    sh """
                        # Configure git user (if not already configured)
                        git config user.name "Jenkins" || true
                        git config user.email "jenkins@example.com" || true
                        
                        # Get the current branch name
                        BRANCH_NAME=\$(git rev-parse --abbrev-ref HEAD)
                        echo "Current branch: \$BRANCH_NAME"
                        
                        # Add the updated manifest file
                        git add kube/manf.yaml
                        
                        # Check if there are changes to commit
                        if git diff --staged --quiet; then
                            echo "No changes to commit in kube/manf.yaml"
                        else
                            # Commit the changes
                            git commit -m "Update deployment manifest with image ${DOCKER_IMAGE}:${DOCKER_TAG}"
                            
                            # Push to the repository
                            # Try pushing with existing credentials from checkout scm
                            if git push origin \$BRANCH_NAME; then
                                echo "Successfully pushed updated manifest to repository"
                            else
                                echo "Push failed with existing credentials, attempting with explicit credentials..."
                                # Fallback: use credentials from environment
                                git remote set-url origin https://\${GIT_CREDENTIALS_USR}:\${GIT_CREDENTIALS_PSW}@github.com/devendra1985/testmydev.git
                                git push origin \$BRANCH_NAME
                                echo "Successfully pushed updated manifest to repository"
                            fi
                        fi
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
