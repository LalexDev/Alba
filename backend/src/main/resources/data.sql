INSERT INTO roles(nombre) VALUES ('ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO roles(nombre) VALUES ('VENDEDOR') ON CONFLICT DO NOTHING;

INSERT INTO usuarios(nombres,apellidos,username,correo,password,role_id,estado,fecha_creacion,fecha_actualizacion)
SELECT 'Admin','Principal','admin','admin@optica.com','$2a$10$tfGkD8ed/Fv9v7SCf5WEoehQlxMFAxS2zFl4fMzeVY8M2xvD4A1d6',r.id,true,now(),now()
FROM roles r
WHERE r.nombre='ADMIN' AND NOT EXISTS (SELECT 1 FROM usuarios WHERE username='admin');

INSERT INTO categorias(nombre,descripcion,estado) VALUES
('Monturas','Categoría inicial',true),
('Lunas','Categoría inicial',true),
('Lentes','Categoría inicial',true),
('Accesorios','Categoría inicial',true),
('Líquidos','Categoría inicial',true),
('Estuches','Categoría inicial',true),
('Paños','Categoría inicial',true),
('Otros','Categoría inicial',true)
ON CONFLICT DO NOTHING;

INSERT INTO configuracion_optica(nombre_optica,moneda,igv_activo,porcentaje_igv)
SELECT 'Óptica Demo','S/',false,18.00
WHERE NOT EXISTS (SELECT 1 FROM configuracion_optica);
