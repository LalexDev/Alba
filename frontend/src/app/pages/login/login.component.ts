import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { TokenService } from '../../core/services/token.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  error = '';
  loading = false;
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private tokenService: TokenService,
    private router: Router
  ) {
    this.form = this.fb.group({
      usernameOrCorreo: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      return;
    }
    this.error = '';
    this.loading = true;
    const { usernameOrCorreo, password } = this.form.getRawValue();
    this.authService.login(usernameOrCorreo!, password!).subscribe({
      next: () => {
        this.loading = false;
        const role = this.tokenService.getRole();
        this.router.navigate([role === 'ADMIN' ? '/admin/dashboard' : '/vendedor/ventas']);
      },
      error: () => {
        this.loading = false;
        this.error = 'Credenciales incorrectas';
      }
    });
  }
}
