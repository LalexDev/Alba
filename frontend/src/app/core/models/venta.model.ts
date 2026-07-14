import { Producto } from './producto.model';

export interface ItemVenta {
  producto: Producto;
  cantidad: number;
  subtotal: number;
}
