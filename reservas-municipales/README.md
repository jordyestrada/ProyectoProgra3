Levantar y comprobar la aplicación (resumen corto)

Este README explica cómo levanté la base de datos en Docker y arranqué la aplicación Spring Boot con el perfil `docker`, lo que comprobé y los comandos que usé (PowerShell / Windows).

Resumen del resultado
- La base de datos PostgreSQL se levantó con `docker-compose` y ejecutó el script `src/main/resources/db/init.sql` (tablas e índices creados).
- La aplicación se construyó y arrancó con el perfil `docker`. Tomcat quedó escuchando en `http://localhost:8080`.
- El endpoint `/actuator/health` existe pero está protegido por Spring Security por defecto (retorna 401 si no se envían credenciales). En los logs de la aplicación aparece una contraseña generada si no se configuró ninguna.

Requisitos
- Docker y docker-compose instalados y funcionando.
- Java 21 instalado (o usar `./mvnw.cmd` para construir usando el wrapper).

Comandos usados (PowerShell)

1) Levantar Postgres (desde la raíz del proyecto):

```
docker-compose up -d
```

2) Verificar logs de inicialización de la base de datos (opcional):

```
docker-compose logs --no-color postgres --tail=200
```

3) Empaquetar la aplicación (mvn wrapper):

```
.\mvnw.cmd -DskipTests package
```

4) Arrancar la aplicación usando el perfil `docker` (PowerShell):

```
& java "-Dspring.profiles.active=docker" -jar "target\reservas-municipales-0.0.1-SNAPSHOT.jar"
```

Nota: intentos directos con `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=docker` pueden fallar por cómo PowerShell pasa los argumentos; la forma robusta es "package + java -jar" (arranque con la variable `-Dspring.profiles.active`). Si tienes `mvnd` instalado puedes usar `mvnd -Dspring-boot.run.profiles=docker spring-boot:run`.

5) Probar el endpoint de salud (PowerShell):

```
Invoke-WebRequest -UseBasicParsing -Uri http://localhost:8080/actuator/health
```

Obtendrás 401 si no envías credenciales.

Con credenciales (ejemplo con curl desde Git Bash o WSL):

```
curl -u admin:changeme http://localhost:8080/actuator/health
```

Si no configuraste credenciales, Spring Boot genera una contraseña aleatoria y la imprime en los logs al arrancar la app (línea similar a "Using generated security password: <password>"). Puedes leer la consola/ventana donde se arrancó la app para obtenerla.

Comandos útiles para controlar el entorno

Detener contenedores:

```
docker-compose down
```

Detener la aplicación (si la arrancaste con `java -jar` desde PowerShell): presiona Ctrl+C en la consola donde corre, o mata el proceso Java.

Notas importantes
- No modifiqué el script `src/main/resources/db/init.sql` (se mantuvo intacto).
- Si quieres evitar la protección por defecto y permitir `GET /actuator/health` sin autenticación para pruebas locales, añade en `application-docker.yml`:

```
spring:
  security:
    user:
      name: admin
      password: changeme
# o deshabilita security (solo para pruebas) con: spring.security.enabled=false
```

Conclusión
- Levanté Postgres y la aplicación con el perfil `docker`. La app respondió en `localhost:8080`, pero los endpoints de Actuator están protegidos por la configuración de seguridad por defecto. Si quieres, puedo fijar credenciales en `application-docker.yml` para facilitar pruebas.
