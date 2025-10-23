pipeline {
    agent any

    tools {
        jdk 'jdk-17'
        maven 'maven-3'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Clonando el repositorio...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Compilando con Maven...'
                sh 'mvn clean install'
            }
        }

        stage('Package') {
            steps {
                echo 'Empaquetando la aplicación...'
                sh 'mvn package'
            }
        }
    }

    post {
        success {
            echo 'Build exitoso!'
        }
        failure {
            echo 'Build fallido!'
        }
    }
}
