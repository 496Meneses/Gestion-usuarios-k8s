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
  - name: kaniko
    image: gcr.io/kaniko-project/executor:latest
    command:
    - /busybox/sh
    args:
    - -c
    - "while true; do sleep 30; done"
    tty: true
    volumeMounts:
    - name: kaniko-secret
      mountPath: /kaniko/.docker/
  volumes:
  - name: kaniko-secret
    secret:
      secretName: dockerhub-credentials
"""
        }
    }

    environment {
        DOCKERHUB_USER = 'acmeneses496'
        IMAGE_NAME = 'acmeneses496/gestion-usuarios'
        IMAGE_TAG = 'latest'
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
                    sh 'mvn clean test'
                }
            }
        }

        stage('Build & Push Image') {
            steps {
                container('kaniko') {
                    echo '🐳 Construyendo y subiendo imagen con Kaniko...'
                    sh """
                    /kaniko/executor \
                      --context `pwd` \
                      --dockerfile `pwd`/Dockerfile \
                      --destination=${IMAGE_NAME}:${IMAGE_TAG} \
                      --destination=${IMAGE_NAME}:latest
                    """
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
