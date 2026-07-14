import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { TokenService } from './token.service';

interface LoginResponse {
  token: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private tokenService: TokenService) {}

  login(usernameOrCorreo: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.api}/login`, { usernameOrCorreo, password }).pipe(
      tap((resp) => this.tokenService.setSession(resp.token, resp.role))
    );
  }

  logout(): void {
    this.tokenService.clear();
  }

  isAuthenticated(): boolean {
    return !!this.tokenService.getToken();
  }
}
