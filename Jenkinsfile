pipeline {
    agent any

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

        stage('Test') {
            steps {
                echo '🧪 Ejecutando pruebas unitarias...'
                sh 'mvn test'
            }
        }

        stage('Docker Build & Push') {
            steps {
                echo '🐳 Construyendo y subiendo imagen a Docker Hub...'
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        def appImage = docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                        appImage.push()
                        // Opcional: también puedes actualizar el tag "latest"
                        appImage.push("latest")
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
