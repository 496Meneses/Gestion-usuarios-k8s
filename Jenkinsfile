pipeline {
  agent {
    kubernetes {
      // Pod template for Jenkins K8s plugin
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:latest
      command: ["cat"]
      tty: true
      volumeMounts:
        # Mount Docker config for authentication to Docker Hub
        - name: kaniko-secret
          mountPath: /kaniko/.docker
          readOnly: true
        # Optional but recommended: enable Kaniko layer caching
        - name: kaniko-cache
          mountPath: /kaniko/cache
  volumes:
    - name: kaniko-secret
      secret:
        secretName: dockerhub-config
        items:
          - key: .dockerconfigjson
            path: config.json
    - name: kaniko-cache
      emptyDir: {}
"""
    }
  }

  options {
    timestamps()
    ansiColor('xterm')
  }

  environment {
    IMAGE_NAME = 'acmeneses496/gestion-usuarios'
    IMAGE_REGISTRY = 'docker.io'
    IMAGE_TAG = "build-${BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        echo '📥 Clonando el repositorio...'
        checkout scm
      }
    }

    stage('Build & Push Image (Kaniko)') {
      steps {
        container('kaniko') {
          echo "🐳 Construyendo y subiendo imagen con Kaniko: ${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
          sh '''
            set -euxo pipefail

            # Kaniko reads auth from /kaniko/.docker/config.json
            # Use the current workspace as build context and Dockerfile path
            CTX="$(pwd)"
            DOCKERFILE_PATH="$CTX/Dockerfile"

            # Build & push with caching enabled
            /kaniko/executor \
              --context="${CTX}" \
              --dockerfile="${DOCKERFILE_PATH}" \
              --destination="${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}" \
              --destination="${IMAGE_REGISTRY}/${IMAGE_NAME}:latest" \
              --cache=true \
              --cache-dir=/kaniko/cache \
              --snapshotMode=redo \
              --use-new-run
          '''
        }
      }
    }
  }

  post {
    success {
      echo "✅ Imagen subida correctamente: ${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    }
    failure {
      echo "❌ Error en la construcción o subida de imagen."
    }
    always {
      echo "🏁 Pipeline finalizado."
    }
  }
}
