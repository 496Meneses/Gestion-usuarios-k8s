pipeline {
  agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
metadata:
  annotations:
    container.apparmor.security.beta.kubernetes.io/podman: unconfined
spec:
  securityContext:
    runAsUser: 1000
    runAsGroup: 1000
    fsGroup: 1000
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-17
    command: ["cat"]
    tty: true

  - name: podman
    image: quay.io/podman/stable
    command: ["cat"]
    tty: true
    securityContext:
      privileged: true
      allowPrivilegeEscalation: true
      seccompProfile:
        type: Unconfined
    env:
      - name: STORAGE_DRIVER
        value: vfs
      - name: BUILDAH_ISOLATION
        value: chroot
    volumeMounts:
      - name: podman-storage
        mountPath: /var/lib/containers
  volumes:
    - name: podman-storage
      emptyDir: {}
"""
    }
  }

  environment {
    IMAGE_NAME = 'acmeneses496/gestion-usuarios'
    IMAGE_TAG  = "build-${BUILD_NUMBER}"
  }

  stages {
    stage('Checkout') {
      steps {
        echo '📥 Clonando el repositorio...'
        checkout scm
      }
    }

    stage('Build & Push Image (Podman vfs+chroot)') {
      steps {
        container('podman') {
          echo '🐳 Construyendo y subiendo imagen con Podman (vfs + chroot)...'
          withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
            sh '''
              set -euxo pipefail

              # Diagnóstico: ver qué config tomó Podman
              podman info || true

              # Build forzando vfs + chroot y un root de almacenamiento en el emptyDir
              podman --log-level=debug \
                     --storage-driver="${STORAGE_DRIVER}" \
                     --root /var/lib/containers \
                     build \
                       --isolation="${BUILDAH_ISOLATION}" \
                       -t ${IMAGE_NAME}:${IMAGE_TAG} .

              podman tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest

              # Login y push
              podman login -u "${DOCKERHUB_USER}" -p "${DOCKERHUB_PASS}" docker.io

              podman --root /var/lib/containers push ${IMAGE_NAME}:${IMAGE_TAG}
              podman --root /var/lib/containers push ${IMAGE_NAME}:latest
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
