pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-17
    command:
    - cat
    tty: true
  - name: podman
    image: quay.io/podman/stable
    command:
    - cat
    tty: true
"""
        }
    }

    environment {
        DOCKERHUB_USER = 'acmeneses496'
        IMAGE_NAME = 'acmeneses496/gestion-usuarios'
        IMAGE_TAG = "build-\${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Clonando el repositorio...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                container('maven') {
                    echo '🧪 Compilando y ejecutando pruebas unitarias...'
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build & Push Image') {
            steps {
                container('podman') {
                    echo '🐳 Construyendo y subiendo imagen con Podman...'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                        sh '''
                            podman build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            podman tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                            podman login -u ${DOCKERHUB_USER} -p ${DOCKERHUB_PASS} docker.io
                            podman push ${IMAGE_NAME}:${IMAGE_TAG}
                            podman push ${IMAGE_NAME}:latest
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Imagen subida correctamente: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "❌ Error en la construcción o subida de imagen."
        }
    }
}
