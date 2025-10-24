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
    disableConcurrentBuilds()
  }

  environment {
    // Repo en Docker Hub
    IMAGE_NAME = 'acmeneses496/gestion-usuarios'
    // Mantengo el build-N como tag auxiliar si lo quieres usar/pushear:
    BUILD_TAG  = "build-${BUILD_NUMBER}"
    // Registro destino
    REGISTRY   = 'docker.io'
  }

  stages {
    stage('Checkout') {
      steps {
        echo '📥 Checkout...'
        checkout scm
      }
    }

    stage('Preflight') {
      steps {
        container('podman') {
          sh '''
            set -Eeuo pipefail
            : "${STORAGE_DRIVER:=vfs}"
            : "${BUILDAH_ISOLATION:=chroot}"
            : "${REGISTRY:?Se requiere REGISTRY}"
            : "${IMAGE_NAME:?Se requiere IMAGE_NAME}"
            : "${IMAGE_TAG:?Se requiere IMAGE_TAG}"

            echo "🔧 Driver=${STORAGE_DRIVER}, Isolation=${BUILDAH_ISOLATION}"
            echo "📦 Espacio en /var/lib/containers:"
            df -h /var/lib/containers || true

            podman --root /var/lib/containers --storage-driver="${STORAGE_DRIVER}" info --format '{{json .host}}' || true
          '''
        }
      }
    }

    stage('Build Image') {
      steps {
        container('podman') {
          sh '''
            set -Eeuo pipefail
            FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}"
            FULL_TAGGED="${FULL_IMAGE}:${IMAGE_TAG}"

            echo "🏗️ Build: ${FULL_TAGGED}"
            podman --storage-driver="${STORAGE_DRIVER}" \
                   --root /var/lib/containers \
                   build \
                     --isolation="${BUILDAH_ISOLATION}" \
                     --jobs=1 \
                     --log-level=info \
                     -t "${FULL_TAGGED}" \
                     -f Dockerfile .

            # Validar existencia
            podman --root /var/lib/containers image exists "${FULL_TAGGED}"
          '''
        }
      }
    }

    stage('Tag Image') {
      steps {
        container('podman') {
          sh '''
            set -Eeuo pipefail
            FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}"
            FULL_TAGGED="${FULL_IMAGE}:${IMAGE_TAG}"

            echo "🔖 Tag a :latest"
            podman --root /var/lib/containers tag "${FULL_TAGGED}" "${FULL_IMAGE}:latest"
          '''
        }
      }
    }

    stage('Registry Login') {
      steps {
        container('podman') {
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKERHUB_USER',
            passwordVariable: 'DOCKERHUB_PASS'
          )]) {
            sh '''
              set -Eeuo pipefail
              echo "🔐 Login registry"
              set +x
              podman --root /var/lib/containers login -u "${DOCKERHUB_USER}" -p "${DOCKERHUB_PASS}" "${REGISTRY}"
              set -x
            '''
          }
        }
      }
    }

    stage('Push Image') {
      steps {
        container('podman') {
          sh '''
            set -Eeuo pipefail
            FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}"
            FULL_TAGGED="${FULL_IMAGE}:${IMAGE_TAG}"

            echo "📤 Push tags"
            retry() {
              attempts="${2:-3}"
              n=0
              until [ $n -ge "${attempts}" ]; do
                sh -c "$1" && return 0
                n=$((n+1))
                sleep 3
              done
              return 1
            }

            retry "podman --root /var/lib/containers push '${FULL_TAGGED}'" 3
            retry "podman --root /var/lib/containers push '${FULL_IMAGE}:latest'" 3
          '''
        }
      }
    }

    stage('Cleanup') {
      steps {
        container('podman') {
          sh '''
            set -Eeuo pipefail
            echo "🧹 Limpieza"
            podman --root /var/lib/containers image prune -f || true
          '''
        }
      }
    }
  }

  post {
    success {
      echo "✅ Imagen subida: ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    }
    failure {
      echo "❌ Error en build o push"
    }
  }
}
