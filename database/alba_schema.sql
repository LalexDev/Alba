-- =========================================================
-- SISTEMA DE VENTAS E INVENTARIO PARA ÓPTICA
-- PostgreSQL 14+
--
-- Roles: ADMINISTRADOR y VENDEDOR
-- Métodos de pago: EFECTIVO, YAPE y TRANSFERENCIA
-- No existe integración bancaria ni tabla independiente pagos.
-- El método solo se selecciona y se imprime en el comprobante.
--
-- Puede ejecutarse en:
--   - PostgreSQL local (dentro de optica_db)
--   - Supabase SQL Editor
-- =========================================================

BEGIN;

-- =========================================================
-- 1. FUNCIÓN GENERAL PARA ACTUALIZAR updated_at
-- =========================================================

CREATE OR REPLACE FUNCTION fn_actualizar_fecha_modificacion()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

-- =========================================================
-- 2. TABLAS DE SEGURIDAD
-- =========================================================

CREATE TABLE roles (
    id_rol              BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(30) NOT NULL UNIQUE,
    descripcion         VARCHAR(150),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_roles_nombre
        CHECK (nombre IN ('ADMINISTRADOR', 'VENDEDOR'))
);

CREATE TABLE usuarios (
    id_usuario          BIGSERIAL PRIMARY KEY,
    id_rol              BIGINT NOT NULL,
    nombres             VARCHAR(80) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    nombre_usuario      VARCHAR(60) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    telefono            VARCHAR(20),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso       TIMESTAMPTZ,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuarios_roles
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol),

    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT uq_usuarios_nombre_usuario UNIQUE (nombre_usuario)
);

-- =========================================================
-- 3. CLIENTES Y RECETAS ÓPTICAS
-- =========================================================

CREATE TABLE clientes (
    id_cliente          BIGSERIAL PRIMARY KEY,
    tipo_persona        VARCHAR(15) NOT NULL DEFAULT 'NATURAL',
    tipo_documento      VARCHAR(20) NOT NULL DEFAULT 'DNI',
    numero_documento    VARCHAR(20),
    nombres             VARCHAR(100),
    apellidos           VARCHAR(120),
    razon_social        VARCHAR(180),
    telefono            VARCHAR(20),
    email               VARCHAR(150),
    direccion           VARCHAR(250),
    fecha_nacimiento    DATE,
    observaciones       TEXT,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_clientes_tipo_persona
        CHECK (tipo_persona IN ('NATURAL', 'JURIDICA')),

    CONSTRAINT chk_clientes_tipo_documento
        CHECK (tipo_documento IN (
            'DNI',
            'RUC',
            'CE',
            'PASAPORTE',
            'SIN_DOCUMENTO'
        )),

    CONSTRAINT chk_clientes_nombre
        CHECK (
            (tipo_persona = 'NATURAL' AND nombres IS NOT NULL)
            OR
            (tipo_persona = 'JURIDICA' AND razon_social IS NOT NULL)
        )
);

CREATE UNIQUE INDEX uq_clientes_documento
    ON clientes(tipo_documento, numero_documento)
    WHERE numero_documento IS NOT NULL
      AND tipo_documento <> 'SIN_DOCUMENTO';

CREATE TABLE recetas_opticas (
    id_receta               BIGSERIAL PRIMARY KEY,
    id_cliente              BIGINT NOT NULL,
    fecha_receta            DATE NOT NULL DEFAULT CURRENT_DATE,
    profesional             VARCHAR(150),

    od_esfera               NUMERIC(6,2),
    od_cilindro             NUMERIC(6,2),
    od_eje                  SMALLINT,
    od_adicion              NUMERIC(6,2),

    oi_esfera               NUMERIC(6,2),
    oi_cilindro             NUMERIC(6,2),
    oi_eje                  SMALLINT,
    oi_adicion              NUMERIC(6,2),

    distancia_pupilar       NUMERIC(6,2),
    altura_od               NUMERIC(6,2),
    altura_oi               NUMERIC(6,2),
    diagnostico             TEXT,
    observaciones           TEXT,
    vigente                 BOOLEAN NOT NULL DEFAULT TRUE,
    creado_por              BIGINT NOT NULL,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recetas_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente),

    CONSTRAINT fk_recetas_usuario
        FOREIGN KEY (creado_por)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT chk_recetas_od_eje
        CHECK (od_eje IS NULL OR od_eje BETWEEN 0 AND 180),

    CONSTRAINT chk_recetas_oi_eje
        CHECK (oi_eje IS NULL OR oi_eje BETWEEN 0 AND 180),

    CONSTRAINT chk_recetas_distancia_pupilar
        CHECK (distancia_pupilar IS NULL OR distancia_pupilar > 0)
);

-- =========================================================
-- 4. CATÁLOGOS E INVENTARIO
-- =========================================================

CREATE TABLE categorias (
    id_categoria        BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(80) NOT NULL UNIQUE,
    descripcion         VARCHAR(250),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE marcas (
    id_marca            BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL UNIQUE,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE proveedores (
    id_proveedor        BIGSERIAL PRIMARY KEY,
    tipo_documento      VARCHAR(20) NOT NULL DEFAULT 'RUC',
    numero_documento    VARCHAR(20),
    razon_social        VARCHAR(180) NOT NULL,
    nombre_contacto     VARCHAR(150),
    telefono            VARCHAR(20),
    email               VARCHAR(150),
    direccion           VARCHAR(250),
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_proveedores_tipo_documento
        CHECK (tipo_documento IN ('RUC', 'DNI', 'CE', 'OTRO'))
);

CREATE UNIQUE INDEX uq_proveedores_documento
    ON proveedores(tipo_documento, numero_documento)
    WHERE numero_documento IS NOT NULL;

CREATE TABLE productos (
    id_producto             BIGSERIAL PRIMARY KEY,
    id_categoria            BIGINT NOT NULL,
    id_marca                BIGINT,
    id_proveedor_preferido  BIGINT,
    codigo_interno          VARCHAR(40) NOT NULL,
    codigo_barras           VARCHAR(80),
    nombre                  VARCHAR(180) NOT NULL,
    descripcion             TEXT,
    modelo                  VARCHAR(100),
    color                   VARCHAR(60),
    medida                  VARCHAR(80),
    material                VARCHAR(80),
    precio_compra           NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_venta            NUMERIC(12,2) NOT NULL,
    stock_actual            INTEGER NOT NULL DEFAULT 0,
    stock_minimo            INTEGER NOT NULL DEFAULT 0,
    activo                  BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_productos_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categorias(id_categoria),

    CONSTRAINT fk_productos_marca
        FOREIGN KEY (id_marca)
        REFERENCES marcas(id_marca),

    CONSTRAINT fk_productos_proveedor_preferido
        FOREIGN KEY (id_proveedor_preferido)
        REFERENCES proveedores(id_proveedor),

    CONSTRAINT uq_productos_codigo_interno UNIQUE (codigo_interno),
    CONSTRAINT uq_productos_codigo_barras UNIQUE (codigo_barras),

    CONSTRAINT chk_productos_precio_compra
        CHECK (precio_compra >= 0),

    CONSTRAINT chk_productos_precio_venta
        CHECK (precio_venta >= 0),

    CONSTRAINT chk_productos_stock_actual
        CHECK (stock_actual >= 0),

    CONSTRAINT chk_productos_stock_minimo
        CHECK (stock_minimo >= 0)
);

-- =========================================================
-- 5. COMPRAS
-- =========================================================

CREATE TABLE compras (
    id_compra           BIGSERIAL PRIMARY KEY,
    numero_compra       VARCHAR(30) NOT NULL UNIQUE,
    id_proveedor        BIGINT NOT NULL,
    id_usuario          BIGINT NOT NULL,
    tipo_documento      VARCHAR(30) NOT NULL DEFAULT 'FACTURA',
    numero_documento    VARCHAR(50),
    fecha_compra        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal            NUMERIC(12,2) NOT NULL DEFAULT 0,
    descuento           NUMERIC(12,2) NOT NULL DEFAULT 0,
    total               NUMERIC(12,2) NOT NULL DEFAULT 0,
    estado              VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    observaciones       TEXT,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_compras_proveedor
        FOREIGN KEY (id_proveedor)
        REFERENCES proveedores(id_proveedor),

    CONSTRAINT fk_compras_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT chk_compras_estado
        CHECK (estado IN ('REGISTRADA', 'ANULADA')),

    CONSTRAINT chk_compras_montos
        CHECK (
            subtotal >= 0
            AND descuento >= 0
            AND total >= 0
        )
);

CREATE TABLE detalle_compras (
    id_detalle_compra   BIGSERIAL PRIMARY KEY,
    id_compra           BIGINT NOT NULL,
    id_producto         BIGINT NOT NULL,
    cantidad            INTEGER NOT NULL,
    precio_unitario     NUMERIC(12,2) NOT NULL,
    descuento           NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal            NUMERIC(12,2) NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_detalle_compras_compra
        FOREIGN KEY (id_compra)
        REFERENCES compras(id_compra)
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_compras_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CONSTRAINT chk_detalle_compras_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_detalle_compras_montos
        CHECK (
            precio_unitario >= 0
            AND descuento >= 0
            AND subtotal >= 0
        )
);

-- =========================================================
-- 6. CAJA
-- =========================================================

CREATE TABLE cajas (
    id_caja             BIGSERIAL PRIMARY KEY,
    id_usuario          BIGINT NOT NULL,
    fecha_apertura      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    monto_apertura      NUMERIC(12,2) NOT NULL DEFAULT 0,
    fecha_cierre        TIMESTAMPTZ,
    monto_cierre_real   NUMERIC(12,2),
    monto_esperado      NUMERIC(12,2),
    diferencia          NUMERIC(12,2),
    estado              VARCHAR(15) NOT NULL DEFAULT 'ABIERTA',
    observaciones       TEXT,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cajas_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT chk_cajas_estado
        CHECK (estado IN ('ABIERTA', 'CERRADA')),

    CONSTRAINT chk_cajas_monto_apertura
        CHECK (monto_apertura >= 0),

    CONSTRAINT chk_cajas_cierre
        CHECK (
            (estado = 'ABIERTA' AND fecha_cierre IS NULL)
            OR
            (estado = 'CERRADA' AND fecha_cierre IS NOT NULL)
        )
);

CREATE UNIQUE INDEX uq_caja_abierta_por_usuario
    ON cajas(id_usuario)
    WHERE estado = 'ABIERTA';

-- =========================================================
-- 7. VENTAS
-- =========================================================

CREATE TABLE ventas (
    id_venta                BIGSERIAL PRIMARY KEY,
    numero_venta            VARCHAR(30) NOT NULL UNIQUE,
    id_cliente              BIGINT,
    id_usuario              BIGINT NOT NULL,
    id_caja                 BIGINT NOT NULL,
    id_receta               BIGINT,
    fecha_venta             TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_comprobante        VARCHAR(20) NOT NULL DEFAULT 'TICKET',
    serie_comprobante       VARCHAR(10),
    numero_comprobante      VARCHAR(20),
    subtotal                NUMERIC(12,2) NOT NULL DEFAULT 0,
    descuento               NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                   NUMERIC(12,2) NOT NULL DEFAULT 0,
    metodo_pago             VARCHAR(20) NOT NULL,
    estado_venta            VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    observaciones           TEXT,
    motivo_anulacion        TEXT,
    anulado_por             BIGINT,
    anulado_en              TIMESTAMPTZ,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ventas_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES clientes(id_cliente),

    CONSTRAINT fk_ventas_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_ventas_caja
        FOREIGN KEY (id_caja)
        REFERENCES cajas(id_caja),

    CONSTRAINT fk_ventas_receta
        FOREIGN KEY (id_receta)
        REFERENCES recetas_opticas(id_receta),

    CONSTRAINT fk_ventas_anulado_por
        FOREIGN KEY (anulado_por)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT chk_ventas_tipo_comprobante
        CHECK (tipo_comprobante IN (
            'TICKET',
            'NOTA_VENTA',
            'BOLETA',
            'FACTURA'
        )),

    CONSTRAINT chk_ventas_metodo_pago
        CHECK (metodo_pago IN (
            'EFECTIVO',
            'YAPE',
            'TRANSFERENCIA'
        )),

    CONSTRAINT chk_ventas_estado
        CHECK (estado_venta IN ('REGISTRADA', 'ANULADA')),

    CONSTRAINT chk_ventas_montos
        CHECK (
            subtotal >= 0
            AND descuento >= 0
            AND total >= 0
        ),

    CONSTRAINT chk_ventas_anulacion
        CHECK (
            (
                estado_venta = 'REGISTRADA'
                AND anulado_por IS NULL
                AND anulado_en IS NULL
            )
            OR
            (
                estado_venta = 'ANULADA'
                AND anulado_por IS NOT NULL
                AND anulado_en IS NOT NULL
                AND motivo_anulacion IS NOT NULL
            )
        )
);

CREATE UNIQUE INDEX uq_ventas_comprobante
    ON ventas(tipo_comprobante, serie_comprobante, numero_comprobante)
    WHERE serie_comprobante IS NOT NULL
      AND numero_comprobante IS NOT NULL;

CREATE TABLE detalle_ventas (
    id_detalle_venta    BIGSERIAL PRIMARY KEY,
    id_venta            BIGINT NOT NULL,
    id_producto         BIGINT NOT NULL,
    cantidad            INTEGER NOT NULL,
    precio_unitario     NUMERIC(12,2) NOT NULL,
    descuento           NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal            NUMERIC(12,2) NOT NULL,
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_detalle_ventas_venta
        FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)
        ON DELETE CASCADE,

    CONSTRAINT fk_detalle_ventas_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CONSTRAINT chk_detalle_ventas_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_detalle_ventas_montos
        CHECK (
            precio_unitario >= 0
            AND descuento >= 0
            AND subtotal >= 0
        )
);

-- =========================================================
-- 8. KARDEX Y MOVIMIENTOS DE CAJA
-- =========================================================

CREATE TABLE movimientos_inventario (
    id_movimiento       BIGSERIAL PRIMARY KEY,
    id_producto         BIGINT NOT NULL,
    id_usuario          BIGINT NOT NULL,
    tipo_movimiento     VARCHAR(30) NOT NULL,
    cantidad            INTEGER NOT NULL,
    stock_anterior      INTEGER NOT NULL,
    stock_nuevo         INTEGER NOT NULL,
    referencia_tipo     VARCHAR(30),
    referencia_id       BIGINT,
    motivo              VARCHAR(250),
    fecha_movimiento    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimientos_inventario_producto
        FOREIGN KEY (id_producto)
        REFERENCES productos(id_producto),

    CONSTRAINT fk_movimientos_inventario_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT chk_movimientos_inventario_tipo
        CHECK (tipo_movimiento IN (
            'ENTRADA_COMPRA',
            'SALIDA_VENTA',
            'AJUSTE_ENTRADA',
            'AJUSTE_SALIDA',
            'ANULACION_VENTA',
            'ANULACION_COMPRA',
            'DEVOLUCION_CLIENTE',
            'DEVOLUCION_PROVEEDOR'
        )),

    CONSTRAINT chk_movimientos_inventario_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT chk_movimientos_inventario_stock
        CHECK (stock_anterior >= 0 AND stock_nuevo >= 0)
);

CREATE TABLE movimientos_caja (
    id_movimiento_caja  BIGSERIAL PRIMARY KEY,
    id_caja             BIGINT NOT NULL,
    id_usuario          BIGINT NOT NULL,
    id_venta            BIGINT,
    tipo_movimiento     VARCHAR(20) NOT NULL,
    concepto            VARCHAR(250) NOT NULL,
    monto               NUMERIC(12,2) NOT NULL,
    fecha_movimiento    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimientos_caja_caja
        FOREIGN KEY (id_caja)
        REFERENCES cajas(id_caja),

    CONSTRAINT fk_movimientos_caja_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_movimientos_caja_venta
        FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta),

    CONSTRAINT chk_movimientos_caja_tipo
        CHECK (tipo_movimiento IN ('INGRESO', 'EGRESO')),

    CONSTRAINT chk_movimientos_caja_monto
        CHECK (monto > 0)
);

-- =========================================================
-- 9. CONFIGURACIÓN Y AUDITORÍA
-- =========================================================

CREATE TABLE configuracion_empresa (
    id_configuracion    SMALLINT PRIMARY KEY DEFAULT 1,
    ruc                 VARCHAR(11),
    razon_social        VARCHAR(180),
    nombre_comercial    VARCHAR(180) NOT NULL,
    direccion           VARCHAR(250),
    telefono            VARCHAR(20),
    email               VARCHAR(150),
    moneda              VARCHAR(10) NOT NULL DEFAULT 'PEN',
    porcentaje_igv      NUMERIC(5,2) NOT NULL DEFAULT 18.00,
    ancho_ticket_mm     SMALLINT NOT NULL DEFAULT 80,
    mensaje_ticket      VARCHAR(250) DEFAULT 'Gracias por su compra',
    creado_en           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_configuracion_unica
        CHECK (id_configuracion = 1),

    CONSTRAINT chk_configuracion_moneda
        CHECK (moneda IN ('PEN', 'USD')),

    CONSTRAINT chk_configuracion_igv
        CHECK (porcentaje_igv BETWEEN 0 AND 100),

    CONSTRAINT chk_configuracion_ticket
        CHECK (ancho_ticket_mm IN (58, 80))
);

CREATE TABLE auditoria (
    id_auditoria        BIGSERIAL PRIMARY KEY,
    id_usuario          BIGINT,
    accion              VARCHAR(80) NOT NULL,
    entidad             VARCHAR(80) NOT NULL,
    id_registro         BIGINT,
    datos_anteriores    JSONB,
    datos_nuevos        JSONB,
    direccion_ip        INET,
    fecha_evento        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);

-- =========================================================
-- 10. TRIGGERS updated_at
-- =========================================================

CREATE TRIGGER trg_usuarios_actualizado
BEFORE UPDATE ON usuarios
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_clientes_actualizado
BEFORE UPDATE ON clientes
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_recetas_actualizado
BEFORE UPDATE ON recetas_opticas
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_categorias_actualizado
BEFORE UPDATE ON categorias
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_marcas_actualizado
BEFORE UPDATE ON marcas
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_proveedores_actualizado
BEFORE UPDATE ON proveedores
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_productos_actualizado
BEFORE UPDATE ON productos
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_compras_actualizado
BEFORE UPDATE ON compras
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_ventas_actualizado
BEFORE UPDATE ON ventas
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_configuracion_actualizado
BEFORE UPDATE ON configuracion_empresa
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

-- =========================================================
-- 11. ÍNDICES
-- =========================================================

CREATE INDEX idx_usuarios_rol
    ON usuarios(id_rol);

CREATE INDEX idx_clientes_nombre
    ON clientes(apellidos, nombres);

CREATE INDEX idx_recetas_cliente_fecha
    ON recetas_opticas(id_cliente, fecha_receta DESC);

CREATE INDEX idx_productos_categoria
    ON productos(id_categoria);

CREATE INDEX idx_productos_nombre
    ON productos(nombre);

CREATE INDEX idx_compras_fecha
    ON compras(fecha_compra DESC);

CREATE INDEX idx_detalle_compras_producto
    ON detalle_compras(id_producto);

CREATE INDEX idx_ventas_fecha
    ON ventas(fecha_venta DESC);

CREATE INDEX idx_ventas_cliente
    ON ventas(id_cliente);

CREATE INDEX idx_ventas_usuario
    ON ventas(id_usuario);

CREATE INDEX idx_ventas_metodo_pago
    ON ventas(metodo_pago);

CREATE INDEX idx_detalle_ventas_producto
    ON detalle_ventas(id_producto);

CREATE INDEX idx_movimientos_inventario_producto_fecha
    ON movimientos_inventario(id_producto, fecha_movimiento DESC);

CREATE INDEX idx_movimientos_caja_caja_fecha
    ON movimientos_caja(id_caja, fecha_movimiento DESC);

CREATE INDEX idx_auditoria_fecha
    ON auditoria(fecha_evento DESC);

-- =========================================================
-- 12. DATOS INICIALES
-- =========================================================

INSERT INTO roles (nombre, descripcion)
VALUES
    ('ADMINISTRADOR', 'Acceso completo al sistema'),
    ('VENDEDOR', 'Acceso a ventas, clientes, productos y consultas de inventario')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO categorias (nombre, descripcion)
VALUES
    ('Monturas', 'Monturas oftálmicas y de sol'),
    ('Lunas', 'Lunas ópticas y tratamientos'),
    ('Lentes de contacto', 'Lentes de contacto graduados y cosméticos'),
    ('Estuches', 'Estuches para lentes'),
    ('Líquidos limpiadores', 'Soluciones y limpiadores'),
    ('Paños', 'Paños de limpieza'),
    ('Accesorios', 'Cordones, sujetadores y accesorios'),
    ('Repuestos', 'Tornillos, plaquetas y repuestos'),
    ('Otros', 'Otros productos de la óptica')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO configuracion_empresa (
    id_configuracion,
    nombre_comercial,
    moneda,
    porcentaje_igv,
    ancho_ticket_mm,
    mensaje_ticket
)
VALUES (
    1,
    'Óptica',
    'PEN',
    18.00,
    80,
    'Gracias por su compra'
)
ON CONFLICT (id_configuracion) DO NOTHING;

-- =========================================================
-- 13. VISTAS PARA REPORTES
-- =========================================================

CREATE OR REPLACE VIEW vw_productos_stock_bajo AS
SELECT
    p.id_producto,
    p.codigo_interno,
    p.codigo_barras,
    p.nombre,
    c.nombre AS categoria,
    m.nombre AS marca,
    p.stock_actual,
    p.stock_minimo,
    p.precio_venta
FROM productos p
INNER JOIN categorias c
    ON c.id_categoria = p.id_categoria
LEFT JOIN marcas m
    ON m.id_marca = p.id_marca
WHERE p.activo = TRUE
  AND p.stock_actual <= p.stock_minimo;

CREATE OR REPLACE VIEW vw_ventas_diarias AS
SELECT
    DATE(v.fecha_venta) AS fecha,
    COUNT(*) FILTER (
        WHERE v.estado_venta = 'REGISTRADA'
    ) AS cantidad_ventas,
    COALESCE(
        SUM(v.total) FILTER (
            WHERE v.estado_venta = 'REGISTRADA'
        ),
        0
    ) AS total_vendido,
    COALESCE(
        SUM(v.total) FILTER (
            WHERE v.estado_venta = 'REGISTRADA'
              AND v.metodo_pago = 'EFECTIVO'
        ),
        0
    ) AS total_efectivo,
    COALESCE(
        SUM(v.total) FILTER (
            WHERE v.estado_venta = 'REGISTRADA'
              AND v.metodo_pago = 'YAPE'
        ),
        0
    ) AS total_yape,
    COALESCE(
        SUM(v.total) FILTER (
            WHERE v.estado_venta = 'REGISTRADA'
              AND v.metodo_pago = 'TRANSFERENCIA'
        ),
        0
    ) AS total_transferencia
FROM ventas v
GROUP BY DATE(v.fecha_venta);

CREATE OR REPLACE VIEW vw_productos_mas_vendidos AS
SELECT
    p.id_producto,
    p.codigo_interno,
    p.nombre,
    SUM(dv.cantidad) AS unidades_vendidas,
    SUM(dv.subtotal) AS importe_vendido
FROM detalle_ventas dv
INNER JOIN ventas v
    ON v.id_venta = dv.id_venta
INNER JOIN productos p
    ON p.id_producto = dv.id_producto
WHERE v.estado_venta = 'REGISTRADA'
GROUP BY
    p.id_producto,
    p.codigo_interno,
    p.nombre;

COMMIT;

-- =========================================================
-- IMPORTANTE
-- =========================================================
-- 1. Las contraseñas NO se guardan como texto.
--    El backend Spring Boot debe guardar BCrypt en password_hash.
--
-- 2. El registro de una venta debe ejecutarse en una transacción:
--      - insertar venta
--      - insertar detalle_ventas
--      - validar/descontar stock
--      - insertar movimientos_inventario
--      - si es EFECTIVO, insertar movimientos_caja
--
-- 3. YAPE y TRANSFERENCIA solo quedan como selección en ventas.
--    No se guardan números de operación, cuentas ni integración externa.
--
-- 4. BOLETA y FACTURA son comprobantes internos hasta integrar SUNAT.
-- =========================================================
