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
    resources:
      requests:
        cpu: "500m"
        memory: "512Mi"
      limits:
        cpu: "1"
        memory: "1Gi"
    env:
      - name: STORAGE_DRIVER
        value: vfs
      - name: BUILDAH_ISOLATION
        value: chroot
      - name: PODMAN_EVENTS_BACKEND
        value: file
      - name: TMPDIR
        value: /var/tmp
    volumeMounts:
      - name: podman-storage
        mountPath: /var/lib/containers
      - name: podman-tmp
        mountPath: /var/tmp
  volumes:
    - name: podman-storage
      emptyDir: {}
    - name: podman-tmp
      emptyDir: {}
"""
    }
  }

  options {
    timeout(time: 60, unit: 'MINUTES')
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

    stage('Build & Push Image') {
      steps {
        container('podman') {
          withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
            sh '''
              set -euo pipefail

              echo "🔍 Verificando espacio disponible..."
              df -h /var/lib/containers || true

              echo "🏗️ Construyendo imagen (sin logs debug)..."
              podman --storage-driver="${STORAGE_DRIVER}" \
                     --root /var/lib/containers \
                     build \
                       --isolation="${BUILDAH_ISOLATION}" \
                       --jobs=1 \
                       --log-level=info \
                       -t ${IMAGE_NAME}:${IMAGE_TAG} .

              echo "🔖 Etiquetando como latest..."
              podman tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest

              echo "🔐 Login a Docker Hub (oculto)..."
              set +x
              podman login -u "${DOCKERHUB_USER}" -p "${DOCKERHUB_PASS}" docker.io
              set -x

              echo "📤 Subiendo imagen (con reintentos)..."
              retry() { n=0; until [ $n -ge 3 ]; do "$@" && break; n=$((n+1)); echo "Reintento $n..."; sleep 3; done; [ $n -lt 3 ]; }
              retry podman --root /var/lib/containers push ${IMAGE_NAME}:${IMAGE_TAG}
              retry podman --root /var/lib/containers push ${IMAGE_NAME}:latest

              echo "🧹 Limpieza ligera..."
              podman image prune -f || true
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
