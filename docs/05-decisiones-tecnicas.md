# Decisiones técnicas

## Lenguaje

Se utiliza Java como lenguaje principal para garantizar reutilización de código entre la aplicación desktop y la aplicación Android.

## Arquitectura

Se adopta una arquitectura por capas:

- Dominio (core)
- Persistencia
- Presentación

## Base de datos

Se utilizará MySQL como sistema gestor de base de datos relacional.

Las imágenes de tickets se almacenan como archivos, guardando únicamente la ruta en la base de datos.

## Autenticación

La autenticación se implementa mediante OAuth 2.0 usando Google y Facebook, evitando la gestión directa de contraseñas.

## Diseño visual

Se mantiene una estética visual coherente en todos los diagramas, basada en gradientes rosa y morado con un estilo minimalista y profesional.
