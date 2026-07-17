import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Categoria, Producto } from '../../core/models/producto.model';
import { ProductoService } from '../../core/services/producto.service';

@Component({
  selector: 'app-productos',
  templateUrl: './productos.component.html',
  styleUrls: ['./productos.component.css']
})
export class ProductosComponent implements OnInit {
  productos: Producto[] = [];
  categorias: Categoria[] = [];
  search = '';
  error = '';
  ok = '';
  mostrarFormulario = false;
  form: FormGroup;

  constructor(private productoService: ProductoService, private fb: FormBuilder) {
    this.form = this.fb.group({
      codigoBarras: ['', Validators.required],
      nombre: ['', Validators.required],
      descripcion: [''],
      precioCompra: [0, [Validators.required, Validators.min(0)]],
      precioVenta: [0, [Validators.required, Validators.min(0)]],
      stockActual: [0, [Validators.required, Validators.min(0)]],
      stockMinimo: [0, [Validators.required, Validators.min(0)]],
      categoriaId: [1, Validators.required],
      marcaId: [null],
      proveedorId: [null]
    });
  }

  ngOnInit(): void {
    this.cargar();
    this.productoService.categorias().subscribe({ next: data => this.categorias = data, error: () => {} });
  }

  get filtrados(): Producto[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.productos;
    return this.productos.filter(p => [p.codigoBarras, p.codigoInterno, p.nombre, p.descripcion, p.categoria?.nombre].some(v => String(v || '').toLowerCase().includes(q)));
  }

  cargar(): void {
    this.productoService.listar().subscribe({
      next: data => this.productos = data,
      error: () => this.error = 'No se pudieron cargar los productos.'
    });
  }

  generarCodigo(): void {
    const next = String(this.productos.length + 1).padStart(6, '0');
    this.form.patchValue({ codigoBarras: `OPT-${next}` });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.error = '';
    this.ok = '';
    const value = this.form.getRawValue();
    this.productoService.crear({
      ...value,
      categoriaId: Number(value.categoriaId),
      precioCompra: Number(value.precioCompra),
      precioVenta: Number(value.precioVenta),
      stockActual: Number(value.stockActual),
      stockMinimo: Number(value.stockMinimo)
    }).subscribe({
      next: () => {
        this.ok = 'Producto guardado correctamente.';
        this.mostrarFormulario = false;
        this.form.reset({ precioCompra: 0, precioVenta: 0, stockActual: 0, stockMinimo: 0, categoriaId: 1 });
        this.cargar();
      },
      error: () => this.error = 'No se pudo guardar el producto. Revisa que el código no esté repetido.'
    });
  }

  exportarExcel(): void {
    const rows = this.productos.map(p => `${p.codigoBarras},${p.nombre},${p.precioVenta},${p.stockActual}`).join('\n');
    const csv = 'Codigo,Producto,Precio,Stock\n' + rows;
    this.descargar('productos.csv', csv, 'text/csv');
  }

  private descargar(nombre: string, contenido: string, tipo: string): void {
    const blob = new Blob([contenido], { type: tipo });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nombre;
    a.click();
    URL.revokeObjectURL(url);
  }
}
