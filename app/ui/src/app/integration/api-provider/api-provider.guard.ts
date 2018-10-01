import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { CurrentFlowService } from '@syndesis/ui/integration';
import { Observable } from 'rxjs';

@Injectable()
export class ApiConnectorGuard implements CanActivate {
  constructor(
    private currentFlowService: CurrentFlowService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean> {
    if (!this.currentFlowService.getStartStep()) {
      this.router.navigate(['/integrations', 'create']);
      return false;
    }
    return true;
  }
}
