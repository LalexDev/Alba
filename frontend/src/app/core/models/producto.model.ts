export interface Producto {
  id: number;
  codigoBarras: string;
  nombre: string;
  precioVenta: number;
  stockActual: number;
  estado: boolean;
}
