import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { ProductoService } from '../../core/services/producto.service';
import { VentaService } from '../../core/services/venta.service';
import { ItemVenta } from '../../core/models/venta.model';

@Component({
  selector: 'app-ventas',
  templateUrl: './ventas.component.html',
  styleUrls: ['./ventas.component.css']
})
export class VentasComponent implements AfterViewInit {
  @ViewChild('barcodeInput') barcodeInput?: ElementRef<HTMLInputElement>;

  codigo = '';
  mensaje = '';
  carrito: ItemVenta[] = [];

  constructor(private productoService: ProductoService, private ventaService: VentaService) {}

  ngAfterViewInit(): void {
    this.focusInput();
  }

  buscarPorCodigo(): void {
    const valor = this.codigo.trim();
    if (!valor) {
      this.focusInput();
      return;
    }

    this.productoService.buscarPorCodigo(valor).subscribe({
      next: (producto) => {
        if (!producto.estado) {
          this.mensaje = 'Producto inactivo';
          this.codigo = '';
          this.focusInput();
          return;
        }
        if (producto.stockActual <= 0) {
          this.mensaje = 'Stock insuficiente';
          this.codigo = '';
          this.focusInput();
          return;
        }

        const item = this.carrito.find((it) => it.producto.id === producto.id);
        if (item) {
          if (item.cantidad + 1 > producto.stockActual) {
            this.mensaje = 'No se puede superar el stock disponible';
          } else {
            item.cantidad += 1;
            item.subtotal = item.cantidad * producto.precioVenta;
            this.mensaje = 'Cantidad actualizada';
          }
        } else {
          this.carrito.push({ producto, cantidad: 1, subtotal: producto.precioVenta });
          this.mensaje = 'Producto agregado';
        }
        this.codigo = '';
        this.focusInput();
      },
      error: () => {
        this.mensaje = 'Producto no encontrado';
        this.codigo = '';
        this.focusInput();
      }
    });
  }

  total(): number {
    return this.carrito.reduce((acc, item) => acc + item.subtotal, 0);
  }

  confirmarVenta(): void {
    if (!this.carrito.length) {
      this.mensaje = 'No se puede confirmar venta vacía';
      this.focusInput();
      return;
    }

    this.ventaService.registrarVenta(this.carrito).subscribe({
      next: () => {
        this.carrito = [];
        this.mensaje = 'Venta registrada correctamente';
        this.focusInput();
      },
      error: () => {
        this.mensaje = 'Error al guardar la venta';
        this.focusInput();
      }
    });
  }

  private focusInput(): void {
    setTimeout(() => this.barcodeInput?.nativeElement.focus(), 0);
  }
}
