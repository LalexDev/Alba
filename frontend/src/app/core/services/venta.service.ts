import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ItemVenta } from '../models/venta.model';

@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly api = 'http://localhost:8080/api/ventas';

  constructor(private http: HttpClient) {}

  registrarVenta(items: ItemVenta[]) {
    const detalles = items.map((it) => ({ productoId: it.producto.id, cantidad: it.cantidad, descuento: 0 }));
    return this.http.post(this.api, { metodoPago: 'EFECTIVO', detalles });
  }
}
