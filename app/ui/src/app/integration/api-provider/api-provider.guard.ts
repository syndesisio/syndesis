import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { CurrentFlowService } from '@syndesis/ui/integration';

@Injectable()
export class ApiConnectorGuard implements CanActivate {
  constructor(
    private currentFlowService: CurrentFlowService,
    private router: Router
  ) {}

  canActivate() {
    if (!this.currentFlowService.getStartStep()) {
      this.router.navigate(['/integrations', 'create']);
      return false;
    }
    return true;
  }
}
