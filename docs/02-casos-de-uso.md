# Casos de uso

## Actor principal

- Usuario autenticado

---

## CU-01: Iniciar sesión

**Descripción:**  
El usuario inicia sesión en la aplicación mediante un proveedor externo (Google o Facebook).

**Flujo principal:**

1. El usuario selecciona el proveedor.
2. El sistema redirige al proveedor de autenticación.
3. El proveedor valida al usuario.
4. El sistema crea o recupera la cuenta.
5. El usuario accede a la aplicación.

---

## CU-02: Crear grupo

**Descripción:**  
El usuario crea un nuevo grupo de gastos.

**Flujo principal:**

1. El usuario introduce el nombre del grupo.
2. El sistema crea el grupo.
3. El usuario pasa a ser administrador del grupo.

---

## CU-03: Añadir gasto

**Descripción:**  
El usuario registra un nuevo gasto en un grupo.

**Flujo principal:**

1. El usuario selecciona el grupo.
2. Introduce los datos del gasto.
3. Selecciona quién paga y quién participa.
4. El sistema guarda el gasto y calcula el reparto.

---

## CU-04: Consultar balances

**Descripción:**  
El usuario consulta el balance económico del grupo.

**Resultado:**  
El sistema muestra quién debe dinero y a quién.
