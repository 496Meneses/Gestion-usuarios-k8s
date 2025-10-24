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
    volumeMounts:
    - name: workspace-volume
      mountPath: /workspace
  - name: docker
    image: docker:24.0.2-cli
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
    - name: workspace-volume
      mountPath: /workspace
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
  - name: workspace-volume
    emptyDir: {}
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
                container('docker') {
                    echo '🐳 Construyendo y subiendo imagen con Docker CLI...'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                        sh '''
                            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                            echo "${DOCKERHUB_PASS}" | docker login -u "${DOCKERHUB_USER}" --password-stdin
                            docker push ${IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${IMAGE_NAME}:latest
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
