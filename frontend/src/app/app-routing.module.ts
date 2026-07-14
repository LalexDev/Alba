import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { VentasComponent } from './pages/ventas/ventas.component';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: 'admin/dashboard',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'admin/ventas',
    component: VentasComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMINISTRADOR'] }
  },
  {
    path: 'vendedor/ventas',
    component: VentasComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['VENDEDOR'] }
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
