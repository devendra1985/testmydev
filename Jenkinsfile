    pipeline {
    // Pin to the controller's built-in node; that container has docker.sock
    // mounted (Docker-out-of-Docker). The default inbound agent doesn't.
    agent { label 'built-in' }
    parameters {
        string(name: 'VERSION', defaultValue: '1.0', description: 'App version')
    }

    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_IMAGE = 'taledevendra/my-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        GIT_REPO = 'github.com/devendra1985/testmydev.git'
        // GitOps: Kubernetes manifests live in a separate config repo. CI bumps
        // the image tag there; ArgoCD syncs it to the cluster.
        CONFIG_REPO = 'github.com/devendra1985/testmydev-config.git'
        CONFIG_OVERLAY = 'overlays/dev'
        // SonarQube (self-hosted) defaults. Override at job level if needed.
        SONAR_HOST_URL = 'http://host.docker.internal:9002'
        SONAR_PROJECT_KEY = 'testmydev'
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        // stage('Sonar Scan') {
        //     steps {
        //         withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
        //             sh '''
        //                 set -euxo pipefail

        //                 # Ensure Maven wrapper is executable
        //                 chmod +x mvnw

        //                 # Build + test + Sonar analysis (no Jenkins Sonar plugin required)
        //                 ./mvnw -B clean verify sonar:sonar \
        //                   -Dsonar.host.url="${SONAR_HOST_URL}" \
        //                   -Dsonar.login="${SONAR_TOKEN}" \
        //                   -Dsonar.projectKey="${SONAR_PROJECT_KEY}" \
        //                   -Dsonar.projectName="${SONAR_PROJECT_KEY}" \
        //                   -Dsonar.java.binaries=target/classes
        //             '''
        //         }
        //     }
        // }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Version is ${params.VERSION}"
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        // stage('Trivy Scan') {
        //     steps {
        //         sh """
        //             trivy image --exit-code 1 --severity CRITICAL --no-progress --scanners vuln ${DOCKER_IMAGE}:${DOCKER_TAG}
        //         """
        //     }
        // }

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

        stage('Bump Image in Config Repo') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_TOKEN')]) {
                    sh """
                        set -euxo pipefail

                        # Clone the GitOps config repo (manifests live here, not in the app repo)
                        rm -rf config-repo
                        git clone https://\${GIT_USER}:\${GIT_TOKEN}@${CONFIG_REPO} config-repo

                        # Update the image tag via kustomize (structured, no brittle sed)
                        cd config-repo/${CONFIG_OVERLAY}
                        kustomize edit set image ${DOCKER_IMAGE}=${DOCKER_IMAGE}:${DOCKER_TAG}
                        echo "Updated ${CONFIG_OVERLAY}/kustomization.yaml:"
                        cat kustomization.yaml

                        # Commit & push the tag bump back to the config repo
                        cd \${WORKSPACE}/config-repo
                        git config user.name "Jenkins"
                        git config user.email "jenkins@example.com"
                        git add ${CONFIG_OVERLAY}/kustomization.yaml
                        git commit -m "Update inventory-app image to ${DOCKER_IMAGE}:${DOCKER_TAG}" || { echo "No image change to commit"; exit 0; }
                        git push https://\${GIT_USER}:\${GIT_TOKEN}@${CONFIG_REPO} HEAD:main
                    """
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
        success {
            echo "Image pushed successfully: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            withCredentials([string(credentialsId: 'github-pat', variable: 'GH_TOKEN')]) {
                sh '''
                    GH_API="https://api.github.com/repos/devendra1985/testmydev/actions/variables"
                    # -k skips SSL verification (needed inside Rancher Desktop containers)
                    curl -sk -X PATCH \
                      -H "Accept: application/vnd.github+json" \
                      -H "Authorization: Bearer ${GH_TOKEN}" \
                      "$GH_API/PREV_LABEL"   -d '{"name":"PREV_LABEL","value":"0"}'
                    curl -sk -X PATCH \
                      -H "Accept: application/vnd.github+json" \
                      -H "Authorization: Bearer ${GH_TOKEN}" \
                      "$GH_API/BUILDS_SINCE" -d '{"name":"BUILDS_SINCE","value":"0"}'
                    echo "GitHub vars updated: PREV_LABEL=0, BUILDS_SINCE=0"
                '''
            }
        }
        failure {
            echo "Build failed!"
            withCredentials([string(credentialsId: 'github-pat', variable: 'GH_TOKEN')]) {
                sh '''
                    GH_API="https://api.github.com/repos/devendra1985/testmydev/actions/variables"
                    CURRENT=$(curl -sk \
                      -H "Accept: application/vnd.github+json" \
                      -H "Authorization: Bearer ${GH_TOKEN}" \
                      "$GH_API/BUILDS_SINCE" | python3 -c "import sys,json; print(json.load(sys.stdin)['value'])")
                    NEXT=$((CURRENT + 1))
                    curl -sk -X PATCH \
                      -H "Accept: application/vnd.github+json" \
                      -H "Authorization: Bearer ${GH_TOKEN}" \
                      "$GH_API/PREV_LABEL"   -d '{"name":"PREV_LABEL","value":"1"}'
                    curl -sk -X PATCH \
                      -H "Accept: application/vnd.github+json" \
                      -H "Authorization: Bearer ${GH_TOKEN}" \
                      "$GH_API/BUILDS_SINCE" -d "{\"name\":\"BUILDS_SINCE\",\"value\":\"${NEXT}\"}"
                    echo "GitHub vars updated: PREV_LABEL=1, BUILDS_SINCE=${NEXT}"
                '''
            }
        }
    }
}
