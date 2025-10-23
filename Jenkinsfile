pipeline {
    agent any

    environment {
        JAVA_HOME = tool name: 'jdk-17', type: 'jdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
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
