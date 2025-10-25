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
          if (sha?.length() >= 7) {
            env.IMAGE_TAG = sha.substring(0, 7)
          } else if (env.BUILD_NUMBER) {
            env.IMAGE_TAG = "build-${env.BUILD_NUMBER}"
            echo "⚠️ GIT_COMMIT no disponible, usando BUILD_NUMBER como IMAGE_TAG=${env.IMAGE_TAG}"
          } else {
            error("❌ No se pudo determinar IMAGE_TAG: GIT_COMMIT y BUILD_NUMBER no disponibles")
          }

          def isMain = env.BRANCH_NAME in ['main', 'master']
          def nonMainWantsLatest = env.TAG_NON_MAIN_LATEST?.trim()?.toLowerCase() == 'true'
          env.SHOULD_TAG_LATEST = (isMain || (!isMain && nonMainWantsLatest)).toString()

          echo "🧭 Branch=${env.BRANCH_NAME ?: 'N/A'} | ShortSHA=${env.IMAGE_TAG} | TAG_NON_MAIN_LATEST=${env.TAG_NON_MAIN_LATEST} | SHOULD_TAG_LATEST=${env.SHOULD_TAG_LATEST}"
        }
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
            echo "🏷️  IMAGE_TAG=${IMAGE_TAG}"
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

            if [ "${SHOULD_TAG_LATEST}" = "true" ]; then
              echo "🔖 Tag a :latest"
              podman --root /var/lib/containers tag "${FULL_TAGGED}" "${FULL_IMAGE}:latest"
            else
              echo "⏭️ Omitiendo tag :latest (BRANCH_NAME='${BRANCH_NAME}', TAG_NON_MAIN_LATEST='${TAG_NON_MAIN_LATEST}')"
            fi
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
            for i in 1 2 3; do
              podman --root /var/lib/containers push "${FULL_TAGGED}" && break || sleep 3
            done

            if [ "${SHOULD_TAG_LATEST}" = "true" ]; then
              for i in 1 2 3; do
                podman --root /var/lib/containers push "${FULL_IMAGE}:latest" && break || sleep 3
              done
            else
              echo "⏭️ Omitiendo push de :latest"
            fi
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

    stage('Update Deployment Manifest') {
                  steps {
                    container('maven') {
                      withCredentials([usernamePassword(
                        credentialsId: 'git-credentials-id',
                        usernameVariable: 'GIT_USER',
                        passwordVariable: 'GIT_PASS'
                      )]) {
                        sh '''
                          set -Eeuo pipefail

                          REPO_URL="https://${GIT_USER}:${GIT_PASS}@github.com/tu-usuario/tu-repo-k8s.git"
                          REPO_DIR="repo-k8s"
                          DEPLOYMENT_FILE="k8s/deployment.yaml"
                          IMAGE_TAG="${IMAGE_TAG}"
                          IMAGE_NAME="${IMAGE_NAME}"

                          echo "📥 Clonando repositorio de manifiestos..."
                          git clone "$REPO_URL" "$REPO_DIR"
                          cd "$REPO_DIR"

                          echo "🔧 Actualizando imagen en el manifiesto..."
                          sed -i "s|\(${IMAGE_NAME}:\\).*|\\1${IMAGE_TAG}|g" "$DEPLOYMENT_FILE"

                          echo "📤 Commit y push..."
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
