pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:24.0.6
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  - name: maven
    image: maven:3.9.6-eclipse-temurin-17
    command:
    - cat
    tty: true
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }

    tools {
        jdk 'jdk-17'
        maven 'maven-3'
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

        stage('Docker Build & Push') {
            steps {
                container('docker') {
                    echo '🐳 Construyendo y subiendo imagen a Docker Hub...'
                    script {
                        docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                            def appImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                            appImage.push()
                            appImage.push("latest")
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build y push exitosos: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo '❌ Build fallido!'
        }
    }
}
