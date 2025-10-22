# Gestión de Usuarios (microservicio)

Microservicio sencillo para gestionar usuarios, creado como práctica de aprendizaje de DevOps. Incluye configuración para ejecutar con Docker y desplegar en Kubernetes (k8s).

## Resumen
- Microservicio ligero para operaciones básicas sobre usuarios (ej. CRUD).
- Contenedorizado con Docker.
- Manifiestos básicos de Kubernetes para despliegue y exposición.
- Diseñado únicamente con fines educativos/prácticos.

## Requisitos
- Docker
- kubectl (con acceso a un cluster Kubernetes)
- (Opcional) Minikube o kind para pruebas locales
- Java — instalar si desea ejecutar fuera de contenedor

## Ejecutar con Docker (local)
1. Construir la imagen:
   docker build -t gestion-usuarios:local .
2. Ejecutar el contenedor:
   docker run --rm -p 8080:8080 gestion-usuarios:local
3. Probar la API en:
   http://localhost:8080/

(Ajuste el puerto según la configuración del servicio.)

## Despliegue en Kubernetes
1. Asegúrese de tener acceso a un cluster y kubectl configurado.
2. Aplicar manifiestos de k8s (suponiendo que están en la carpeta `k8s/`):
   kubectl apply -f k8s/
3. Verificar pods y servicios:
   kubectl get pods
   kubectl get svc
4. Para exponer localmente (si usa port-forward):
   kubectl port-forward deployment/gestion-usuarios 8080:8080

## Estructura sugerida del repositorio
- /cmd, /src, /app — código fuente del microservicio
- Dockerfile — definición de la imagen
- k8s/ — manifiestos (Deployment, Service, ConfigMap, Secret, etc.)
- README.md — este archivo

## Consideraciones
- Proyecto de práctica: no está pensado para producción.
- No incluir secretos en texto plano; usar Secrets o gestores de secretos para entornos reales.
- Añadir healthchecks, readiness/liveness probes y políticas de seguridad antes de producción.

## Contribuciones
Si quieres mejorar esta práctica (añadir tests, CI/CD, mejores manifiestos, helm chart), abre un pull request o crea un issue.

---

Creado como ejercicio de aprendizaje de DevOps por el grupo de la asignatura Arquitectura de software 1. Uni Sabana
