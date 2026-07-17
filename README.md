Óptica Alba — Angular + Supabase
Esta versión ya no depende de Spring Boot, Render, Neon ni una base de datos local.
Arquitectura
Frontend: Angular 18.
Base de datos: PostgreSQL de Supabase.
Login: Supabase Auth.
Roles: `ADMINISTRADOR` y `VENDEDOR`.
Seguridad: Row Level Security (RLS).
Ventas, stock y anulaciones: funciones PostgreSQL transaccionales.
Publicación: Netlify
1. Crear el proyecto de Supabase
Crea un proyecto en Supabase.
Abre SQL Editor.
Ejecuta completo `database/01_supabase_optica.sql`.
Abre Authentication > Users > Add user y crea el primer usuario.
En SQL Editor convierte ese correo en administrador:
```sql
UPDATE public.usuarios
SET id_rol = (SELECT id_rol FROM public.roles WHERE nombre = 'ADMINISTRADOR')
WHERE email = 'TU_CORREO_ADMIN';
```
1. Configurar Angular
En Supabase abre Project Settings > API y copia:
Project URL.
Publishable key o anon key.
Colócalas en:
`Frontend/src/environments/environment.ts`
`Frontend/src/environments/environment.prod.ts`
La clave pública se puede usar en Angular porque las tablas están protegidas con RLS. Nunca pongas la `service_role` en Angular.
1. Ejecutar en desarrollo
```bash
cd Frontend
npm install
npm start
```
Abre `http://localhost:4200`.
1. Crear usuarios desde el módulo Usuarios
El proyecto incluye una Edge Function segura:
```text
supabase/functions/crear-usuario/index.ts
```
Con Supabase CLI:
```bash
supabase login
supabase link --project-ref TU_PROJECT_REF
supabase functions deploy crear-usuario
```
La función usa automáticamente `SUPABASE_URL` y `SUPABASE_SERVICE_ROLE_KEY` dentro de Supabase. La clave secreta no se expone al navegador.
Mientras no despliegues la función, crea usuarios desde Authentication > Users y cambia su rol desde SQL.
1. Publicar en Netlify
El archivo `Frontend/netlify.toml` ya contiene:
Build: `npm run build:prod`
Carpeta: `dist/frontend/browser`
Redirección SPA a `index.html`
Sube el repositorio a GitHub, conecta Netlify y usa `Frontend` como base directory.
1. Publicar en Vercel
Usa `Frontend` como Root Directory. El archivo `vercel.json` ya está incluido.
Funcionalidades conectadas
Login Supabase.
Roles administrador/vendedor.
Productos y stock.
Lector de código de barras.
Ventas con efectivo, Yape o transferencia como selección.
A cuenta, saldo y estado de pago.
Descuento automático de inventario.
Clientes y recetas ópticas.
Proveedores.
Ajustes de inventario y kardex.
Órdenes y recibos.
Anulación con devolución de stock.
Reportes.
Stickers Code 128 de 30 × 20 mm.
Usuarios mediante Edge Function.
Importante
Antes de trabajar con ventas reales, prueba en este orden:
Login.
Registrar producto.
Ajustar inventario.
Registrar cliente.
Escanear producto.
Confirmar venta.
Ver orden y saldo.
Anular una venta como administrador.
Comprobar el kardex.