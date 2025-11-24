import { Injectable } from '@angular/core';
import {environment} from '../../configuration/environement.config';
import {HttpClient} from '@angular/common/http';
import {DashboardSummary} from '../environement/models/dashboard-summary.model';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {

  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/dashboard/summary`);
  }
}
