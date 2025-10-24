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
      # Si tu clúster lo permite, puedes probar a quitar privileged,
      # pero para evitar los errores de remount mantenemos esto activo:
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
    // Nombre de repo en Docker Hub, sin prefijo de registro
    IMAGE_NAME = 'acmeneses496/gestion-usuarios'
    IMAGE_TAG  = "build-${BUILD_NUMBER}"
    REGISTRY   = 'docker.io'
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
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKERHUB_USER',
            passwordVariable: 'DOCKERHUB_PASS'
          )]) {
            sh '''
              set -Eeuo pipefail

              : "${STORAGE_DRIVER:=vfs}"
              : "${BUILDAH_ISOLATION:=chroot}"
              : "${REGISTRY:?Se requiere REGISTRY}"
              : "${IMAGE_NAME:?Se requiere IMAGE_NAME}"
              : "${IMAGE_TAG:?Se requiere IMAGE_TAG}"

              FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}"
              FULL_TAGGED="${FULL_IMAGE}:${IMAGE_TAG}"

              echo "🔍 Storage driver: ${STORAGE_DRIVER}"
              echo "🔍 Isolation: ${BUILDAH_ISOLATION}"
              echo "🎯 Imagen objetivo: ${FULL_TAGGED}"

              echo "📦 Espacio en /var/lib/containers"
              df -h /var/lib/containers || true

              echo "ℹ️  podman info"
              podman --root /var/lib/containers --storage-driver="${STORAGE_DRIVER}" info || true

              echo "🏗️  Construyendo imagen..."
              podman --storage-driver="${STORAGE_DRIVER}" \
                     --root /var/lib/containers \
                     build \
                       --isolation="${BUILDAH_ISOLATION}" \
                       --jobs=1 \
                       --log-level=info \
                       -t "${FULL_TAGGED}" \
                       -f Dockerfile .

              echo "🔎 Verificando existencia de la imagen construida..."
              if ! podman --root /var/lib/containers image exists "${FULL_TAGGED}"; then
                echo "❌ La imagen ${FULL_TAGGED} no existe tras el build. Listado local:"
                podman --root /var/lib/containers images || true
                exit 1
              fi

              echo "🔖 Etiquetando como latest..."
              podman --root /var/lib/containers tag "${FULL_TAGGED}" "${FULL_IMAGE}:latest"

              echo "🔐 Login a Docker Hub (oculto en logs)..."
              set +x
              podman --root /var/lib/containers login -u "${DOCKERHUB_USER}" -p "${DOCKERHUB_PASS}" "${REGISTRY}"
              set -x

              echo "📤 Subiendo imagen (con reintentos)..."
              retry() {
                attempts="${2:-3}"
                n=0
                until [ $n -ge "${attempts}" ]; do
                  echo "→ Intento $((n+1))/${attempts}: $1"
                  # shellcheck disable=SC2086
                  sh -c "$1" && return 0
                  n=$((n+1))
                  sleep 3
                done
                return 1
              }

              retry "podman --root /var/lib/containers push '${FULL_TAGGED}'" 3
              retry "podman --root /var/lib/containers push '${FULL_IMAGE}:latest'" 3
              echo "🧹 Limpieza ligera de imágenes dangling..."
              podman --root /var/lib/containers image prune -f || true

              echo "✅ Build & Push finalizados"
            '''
          }
        }
      }
    }
  }

  post {
    success {
      echo "✅ Imagen subida correctamente: ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    }
    failure {
      echo "❌ Error en la construcción o subida de imagen."
    }
  }
}
