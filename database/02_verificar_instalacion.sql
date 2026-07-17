-- Ejecutar después de 01_supabase_optica.sql

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

SELECT * FROM public.roles ORDER BY id_rol;
SELECT * FROM public.categorias ORDER BY id_categoria;
SELECT * FROM public.configuracion_empresa;

SELECT table_name
FROM information_schema.views
WHERE table_schema = 'public'
ORDER BY table_name;
