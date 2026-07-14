# OptiStock Pro (Alba)

Sistema web para óptica con backend en **Spring Boot 3 + Java 21** y frontend en **Angular**.

## Tecnologías

- Backend: Spring Boot 3, Spring Security, JWT, Spring Data JPA, PostgreSQL, Lombok, Validation, OpenAPI.
- Frontend: Angular, TypeScript, Angular Router, formularios reactivos, interceptor JWT, guards.

## Estructura

- `/backend`: API REST y lógica de negocio principal.
- `/frontend`: aplicación web (login, dashboard admin y ventas con flujo de escaneo).

## Requisitos

- Java 21
- Maven 3.9+
- Node.js 20+
- PostgreSQL 14+

## Configuración PostgreSQL

Crear base de datos:

```sql
CREATE DATABASE optistock_db;
```

Variables de entorno backend:

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/optistock_db`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `JWT_SECRET` (mínimo 64 caracteres)

## Usuario inicial

- username: `admin`
- correo: `admin@optica.com`
- password: `Admin123456`
- rol: `ADMIN`

## Ejecución backend

```bash
cd /home/runner/work/Alba/Alba/backend
mvn clean install
mvn spring-boot:run
```

Swagger:

- `http://localhost:8080/swagger-ui/index.html`

## Ejecución frontend

```bash
cd /home/runner/work/Alba/Alba/frontend
npm install
npm run start
```

## Endpoints principales

- Auth: `/api/auth/login`, `/api/auth/register-admin`, `/api/auth/me`
- Usuarios: `/api/usuarios`
- Clientes: `/api/clientes`
- Productos: `/api/productos`, `/api/productos/codigo/{codigoBarras}`
- Ventas: `/api/ventas`
- Inventario: `/api/inventario/movimientos`, `/api/inventario/entrada`, `/api/inventario/salida`, `/api/inventario/ajuste`
- Reportes: `/api/reportes/dashboard`, `/api/reportes/ventas`, `/api/reportes/bajo-stock`
- Configuración: `/api/configuracion`

## Flujo de ventas con lector de código

1. Ingresar a pantalla de ventas.
2. Campo “Escanear código de barras” enfocado automáticamente.
3. Escanear o escribir código y presionar Enter.
4. Producto se agrega al carrito (o incrementa cantidad si ya existe).
5. Validación de existencia/estado/stock.
6. Confirmar venta para descontar stock y registrar movimiento.

## Pruebas sugeridas

- Login ADMIN y VENDEDOR.
- Registro de producto con código único.
- Venta con stock (debe descontar inventario).
- Venta sin stock (debe bloquearse).
- Consulta de bajo stock en reportes.
