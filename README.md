# Gestión de Usuarios - Despliegue en Kubernetes

Este repositorio contiene la tarea de despliegue del microservicio de usuarios utilizando Jenkins, Docker, Kubernetes y ArgoCD. A continuación se incluyen los enlaces y las instrucciones necesarias para revisar la entrega y acceder a los recursos desplegados.

## Entregables y enlaces de la entrega

- Video grabado (ver demostración): `[Actividad3.mp4](https://unisabanaedu-my.sharepoint.com/:v:/g/personal/andresmeor_unisabana_edu_co/EfNl_G-_YyxGm5zyrc64bDsBOwG4zs4ihlwn2jVLlG-Zbw?nav=eyJyZWZlcnJhbEluZm8iOnsicmVmZXJyYWxBcHAiOiJTdHJlYW1XZWJBcHAiLCJyZWZlcnJhbFZpZXciOiJTaGFyZURpYWxvZy1MaW5rIiwicmVmZXJyYWxBcHBQbGF0Zm9ybSI6IldlYiIsInJlZmVycmFsTW9kZSI6InZpZXcifX0%3D&e=6BEMkY)`  
  - Nota: Para ver el video, por favor acceder con el correo de la Universidad de La Sabana (unisabana).

- Aplicación expuesta mediante Ingress (NGINX):  
  http://134.209.131.242.nip.io/usuario

- Jenkins (pipeline de CI/CD):  
  http://143.244.156.113:30000/

- Repositorio en GitHub:  
  https://github.com/496Meneses/Gestion-usuarios-k8s

- Imágenes en Docker Hub (usuario):  
  https://hub.docker.com/repositories/acmeneses496

- ArgoCD (acceso local):  
  Ejecutar:  
  kubectl port-forward svc/argocd-server -n argocd 8080:443  
  Luego acceder en el navegador a: http://localhost:8080

## Notas importantes

- Para reproducir la demostración en video, usar el correo institucional de Unisabana para el acceso al contenido provisto.
- Esta entrega corresponde a una actividad de despliegue del microservicio de usuarios y muestra la integración entre Jenkins (CI), Docker (imágenes), Kubernetes (orquestación) y ArgoCD (GitOps).

## Acceder y revisar rápidamente

1. Ver el video explicativo: `Actividad3.mp4` (usar correo Unisabana).  
2. Ver la aplicación en producción usando nginx: abrir http://134.209.131.242.nip.io/usuario  
3. Revisar el pipeline y logs en Jenkins: http://143.244.156.113:30000/  
4. Consultar las imágenes construidas en Docker Hub: https://hub.docker.com/repositories/acmeneses496  
5. Abrir ArgoCD con port-forward (si necesita revisar la sincronización y recursos declarados):
   - kubectl port-forward svc/argocd-server -n argocd 8080:443
   - Abrir http://localhost:8080

## Estructura del repositorio (resumen)
- manifests/      -> Manifiestos Kubernetes (Deployments, Services, Ingress, etc.)
- jenkins/        -> Declaraciones y pipeline relacionadas a Jenkins
- src/            -> Código fuente del microservicio de usuarios
- Dockerfile      -> Construcción de la imagen del servicio
- README.md       -> Este archivo

## Contacto
Si necesita acceso adicional al cluster o alguna clarificación sobre la entrega, puede contactarme a través del repositorio o mediante el medio provisto en la entrega.

---
