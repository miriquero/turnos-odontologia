# turnos-odontologia

Motor de disponibilidad de agenda para consultorios odontologicos (dominio puro, sin REST ni persistencia en C-01).

## Compilar y probar

    ./gradlew test        # en Windows: gradlew.bat test

El wrapper (Gradle 8.10.2) esta versionado: no hace falta instalar Gradle.

### JDK (pendiente D-08)

El build declara el toolchain de **Java 17** en `build.gradle`. Gradle lo resuelve solo (JDK instalado y detectado,
o descarga automatica), aunque `JAVA_HOME` apunte a otra version. Verificacion: `./gradlew -q javaToolchains`.

En la maquina donde se desarrollo C-01, `JAVA_HOME` apuntaba a un JDK 26; el wrapper arranco igual con ese JDK y el
toolchain compilo y corrio los tests con Microsoft JDK 17 (detectado en `~/.jdks`). No hizo falta forzar ninguna ruta.
Si a alguien no le detecta el JDK 17, que lo instale o declare `org.gradle.java.installations.paths=<ruta>` en su
`~/.gradle/gradle.properties` personal (nunca en el repositorio).
