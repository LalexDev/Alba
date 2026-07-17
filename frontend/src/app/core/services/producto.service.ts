import { Injectable } from '@angular/core';
import { defer, Observable } from 'rxjs';

import {
  Categoria,
  Marca,
  Producto,
  ProductoRequest,
  Proveedor
} from '../models/producto.model';

import { SupabaseService } from './supabase.service';

interface CategoriaDb {
  id_categoria: number;
  nombre: string;
  descripcion?: string | null;
  activo: boolean;
}

interface MarcaDb {
  id_marca: number;
  nombre: string;
  activo: boolean;
}

interface ProveedorDb {
  id_proveedor: number;
  razon_social?: string | null;
  nombre_contacto?: string | null;
  telefono?: string | null;
  email?: string | null;
  direccion?: string | null;
  activo: boolean;
}

interface ProductoDb {
  id_producto: number;
  codigo_interno?: string | null;
  codigo_barras: string;
  nombre: string;
  descripcion?: string | null;
  modelo?: string | null;
  color?: string | null;
  medida?: string | null;
  material?: string | null;
  precio_compra: number | string;
  precio_venta: number | string;
  stock_actual: number;
  stock_minimo: number;
  activo: boolean;

  categoria?: CategoriaDb | CategoriaDb[] | null;
  marca?: MarcaDb | MarcaDb[] | null;
  proveedor?: ProveedorDb | ProveedorDb[] | null;
}

@Injectable({
  providedIn: 'root'
})
export class ProductoService {

  private readonly columnasProducto = `
    id_producto,
    codigo_interno,
    codigo_barras,
    nombre,
    descripcion,
    modelo,
    color,
    medida,
    material,
    precio_compra,
    precio_venta,
    stock_actual,
    stock_minimo,
    activo,

    categoria:categorias (
      id_categoria,
      nombre,
      descripcion,
      activo
    ),

    marca:marcas (
      id_marca,
      nombre,
      activo
    ),

    proveedor:proveedores (
      id_proveedor,
      razon_social,
      nombre_contacto,
      telefono,
      email,
      direccion,
      activo
    )
  `;

  constructor(
    private supabaseService: SupabaseService
  ) {}

  listar(): Observable<Producto[]> {
    return defer(async () => {
      const { data, error } =
        await this.supabaseService.client
          .from('productos')
          .select(this.columnasProducto)
          .order('nombre', {
            ascending: true
          });

      if (error) {
        console.error(
          'Error al listar productos:',
          error
        );

        throw new Error(error.message);
      }

      return (data ?? []).map((fila) =>
        this.mapearProducto(
          fila as unknown as ProductoDb
        )
      );
    });
  }

  crear(
    request: ProductoRequest
  ): Observable<Producto> {
    return defer(async () => {
      const codigoBarras =
        String(request.codigoBarras || '').trim();

      const nombre =
        String(request.nombre || '').trim();

      if (!codigoBarras) {
        throw new Error(
          'El código de barras es obligatorio.'
        );
      }

      if (!nombre) {
        throw new Error(
          'El nombre del producto es obligatorio.'
        );
      }

      if (!request.categoriaId) {
        throw new Error(
          'Selecciona una categoría.'
        );
      }

      /*
       * La función crear_producto registra:
       * - el producto;
       * - su stock inicial;
       * - el movimiento de inventario inicial.
       */
      const { data, error } =
        await this.supabaseService.client.rpc(
          'crear_producto',
          {
            p_codigo_interno: codigoBarras,
            p_codigo_barras: codigoBarras,
            p_nombre: nombre,
            p_descripcion:
              request.descripcion?.trim() || null,

            p_modelo: null,
            p_color: null,
            p_medida: null,
            p_material: null,

            p_precio_compra:
              Number(request.precioCompra || 0),

            p_precio_venta:
              Number(request.precioVenta || 0),

            p_stock_inicial:
              Number(request.stockActual || 0),

            p_stock_minimo:
              Number(request.stockMinimo || 0),

            p_id_categoria:
              Number(request.categoriaId),

            p_id_marca:
              request.marcaId
                ? Number(request.marcaId)
                : null,

            p_id_proveedor:
              request.proveedorId
                ? Number(request.proveedorId)
                : null
          }
        );

      if (error) {
        console.error(
          'Error al crear producto:',
          error
        );

        throw new Error(error.message);
      }

      const respuesta = data as {
        id_producto?: number;
      } | null;

      const idProducto = Number(
        respuesta?.id_producto
      );

      if (!idProducto) {
        throw new Error(
          'El producto se guardó, pero no se obtuvo su identificador.'
        );
      }

      return await this.obtenerPorId(
        idProducto
      );
    });
  }

  buscarPorCodigo(
    codigo: string
  ): Observable<Producto> {
    return defer(async () => {
      const codigoNormalizado =
        String(codigo || '').trim();

      if (!codigoNormalizado) {
        throw new Error(
          'Ingresa o escanea un código.'
        );
      }

      /*
       * Primero se busca por código de barras.
       */
      const porBarras =
        await this.supabaseService.client
          .from('productos')
          .select(this.columnasProducto)
          .eq(
            'codigo_barras',
            codigoNormalizado
          )
          .maybeSingle();

      if (porBarras.error) {
        throw new Error(
          porBarras.error.message
        );
      }

      if (porBarras.data) {
        return this.mapearProducto(
          porBarras.data as unknown as ProductoDb
        );
      }

      /*
       * Si no aparece, busca también por código interno.
       */
      const porCodigoInterno =
        await this.supabaseService.client
          .from('productos')
          .select(this.columnasProducto)
          .eq(
            'codigo_interno',
            codigoNormalizado
          )
          .maybeSingle();

      if (porCodigoInterno.error) {
        throw new Error(
          porCodigoInterno.error.message
        );
      }

      if (!porCodigoInterno.data) {
        throw new Error(
          'Producto no encontrado.'
        );
      }

      return this.mapearProducto(
        porCodigoInterno.data as unknown as ProductoDb
      );
    });
  }

  categorias(): Observable<Categoria[]> {
    return defer(async () => {
      const { data, error } =
        await this.supabaseService.client
          .from('categorias')
          .select(`
            id_categoria,
            nombre,
            descripcion,
            activo
          `)
          .eq('activo', true)
          .order('nombre', {
            ascending: true
          });

      if (error) {
        console.error(
          'Error al listar categorías:',
          error
        );

        throw new Error(error.message);
      }

      return (data ?? []).map(
        (categoria): Categoria => ({
          id: Number(
            categoria.id_categoria
          ),
          nombre: categoria.nombre,
          descripcion:
            categoria.descripcion ?? '',
          estado: Boolean(
            categoria.activo
          )
        })
      );
    });
  }

  private async obtenerPorId(
    idProducto: number
  ): Promise<Producto> {
    const { data, error } =
      await this.supabaseService.client
        .from('productos')
        .select(this.columnasProducto)
        .eq(
          'id_producto',
          idProducto
        )
        .single();

    if (error) {
      throw new Error(error.message);
    }

    return this.mapearProducto(
      data as unknown as ProductoDb
    );
  }

  private mapearProducto(
    fila: ProductoDb
  ): Producto {
    const categoriaDb =
      this.obtenerRelacion(fila.categoria);

    const marcaDb =
      this.obtenerRelacion(fila.marca);

    const proveedorDb =
      this.obtenerRelacion(fila.proveedor);

    const categoria: Categoria | undefined =
      categoriaDb
        ? {
            id: Number(
              categoriaDb.id_categoria
            ),
            nombre: categoriaDb.nombre,
            descripcion:
              categoriaDb.descripcion ?? '',
            estado: Boolean(
              categoriaDb.activo
            )
          }
        : undefined;

    const marca: Marca | undefined =
      marcaDb
        ? {
            id: Number(
              marcaDb.id_marca
            ),
            nombre: marcaDb.nombre,
            estado: Boolean(
              marcaDb.activo
            )
          }
        : undefined;

    const proveedor:
      Proveedor | undefined =
      proveedorDb
        ? {
            id: Number(
              proveedorDb.id_proveedor
            ),
            razonSocial:
              proveedorDb.razon_social ?? '',
            contacto:
              proveedorDb.nombre_contacto ?? '',
            telefono:
              proveedorDb.telefono ?? '',
            correo:
              proveedorDb.email ?? '',
            direccion:
              proveedorDb.direccion ?? '',
            estado: Boolean(
              proveedorDb.activo
            )
          }
        : undefined;

    return {
      id: Number(fila.id_producto),
      codigoInterno:
        fila.codigo_interno ?? '',
      codigoBarras:
        fila.codigo_barras,
      nombre: fila.nombre,
      descripcion:
        fila.descripcion ?? '',
      modelo: fila.modelo ?? '',
      color: fila.color ?? '',
      medida: fila.medida ?? '',
      material: fila.material ?? '',
      precioCompra: Number(
        fila.precio_compra ?? 0
      ),
      precioVenta: Number(
        fila.precio_venta ?? 0
      ),
      stockActual: Number(
        fila.stock_actual ?? 0
      ),
      stockMinimo: Number(
        fila.stock_minimo ?? 0
      ),
      estado: Boolean(fila.activo),
      categoria,
      marca,
      proveedor
    };
  }

  private obtenerRelacion<T>(
    relacion: T | T[] | null | undefined
  ): T | undefined {
    if (!relacion) {
      return undefined;
    }

    if (Array.isArray(relacion)) {
      return relacion[0];
    }

    return relacion;
  }
}