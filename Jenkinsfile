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
    runAsUser: 0
    runAsGroup: 0
    fsGroup: 0
  containers:
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
        memory: "192Mi"
        cpu: "150m"
      limits:
        memory: "384Mi"
        cpu: "300m"
    env:
      - name: STORAGE_DRIVER
        value: overlay
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
      - name: maven-cache
        mountPath: /root/.m2
  volumes:
    - name: podman-storage
      emptyDir: {}
    - name: podman-tmp
      emptyDir:
        medium: Memory
    - name: maven-cache
      emptyDir: {}

"""
    }
  }

  options {
    timeout(time: 60, unit: 'MINUTES')
    disableConcurrentBuilds()
  }

  environment {
    IMAGE_NAME = 'acmeneses496/gestion-usuarios'
    REGISTRY   = 'docker.io'
    TAG_NON_MAIN_LATEST = 'true'
  }

  stages {
    stage('Checkout') {
      steps {
        echo '📥 Checkout...'
        checkout scm
      }
    }

    stage('Metadata') {
      steps {
        script {
          def sha = env.GIT_COMMIT ?: ''
          env.IMAGE_TAG = (sha?.length() >= 7) ? sha.substring(0, 7) : "build-${env.BUILD_NUMBER}"
          def isMain = env.BRANCH_NAME in ['main', 'master']
          def nonMainWantsLatest = env.TAG_NON_MAIN_LATEST?.trim()?.toLowerCase() == 'true'
          env.SHOULD_TAG_LATEST = (isMain || (!isMain && nonMainWantsLatest)).toString()
        }
      }
    }

    stage('Build & Tag Image') {
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

            if [ "${SHOULD_TAG_LATEST}" = "true" ]; then
              echo "🔖 Tag a :latest"
              podman --root /var/lib/containers tag "${FULL_TAGGED}" "${FULL_IMAGE}:latest"
            fi
          '''
        }
      }
    }

    stage('Registry Login & Push') {
      steps {
        container('podman') {
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKERHUB_USER',
            passwordVariable: 'DOCKERHUB_PASS'
          )]) {
            sh '''
              set -Eeuo pipefail
              FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}"
              FULL_TAGGED="${FULL_IMAGE}:${IMAGE_TAG}"

              echo "🔐 Login registry"
              set +x
              podman --root /var/lib/containers login -u "${DOCKERHUB_USER}" -p "${DOCKERHUB_PASS}" "${REGISTRY}"
              set -x

              echo "📤 Push tags"
              for i in 1 2 3; do
                podman --root /var/lib/containers push "${FULL_TAGGED}" && break || sleep 3
              done

              if [ "${SHOULD_TAG_LATEST}" = "true" ]; then
                for i in 1 2 3; do
                  podman --root /var/lib/containers push "${FULL_IMAGE}:latest" && break || sleep 3
                done
              fi
            '''
          }
        }
      }
    }

    stage('Update Deployment Manifest') {
      steps {
        container('podman') {
          withCredentials([usernamePassword(
            credentialsId: 'git-credentials-id',
            usernameVariable: 'GIT_USER',
            passwordVariable: 'GIT_PASS'
          )]) {
            sh '''
              set -Eeuo pipefail

              REPO_URL="https://${GIT_USER}:${GIT_PASS}@github.com/496Meneses/Gestion-usuarios-k8s.git"
              REPO_DIR="gestion-usuarios-k8s"
              DEPLOYMENT_FILE="src/main/resources/helm/gestion-usuarios/values.yaml"

              echo "📥 Clonando manifiesto..."
              git clone "$REPO_URL" "$REPO_DIR"
              cd "$REPO_DIR"

              echo "🔧 Actualizando imagen..."
              sed -i "s|image: ${IMAGE_NAME}:.*|image: ${IMAGE_NAME}:${IMAGE_TAG}|g" "$DEPLOYMENT_FILE"

              git config user.name "jenkins"
              git config user.email "jenkins@local"
              git add "$DEPLOYMENT_FILE"
              git commit -m "Actualiza imagen a ${IMAGE_TAG} desde Jenkins"
              git push origin main
            '''
          }
        }
      }
    }

    stage('Cleanup') {
      steps {
        container('podman') {
          sh '''
            echo "🧹 Limpieza de imágenes"
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
