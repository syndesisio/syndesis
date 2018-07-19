import { Injectable, Inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page/current-flow.service';
import { FlowEvent } from '@syndesis/ui/integration/edit-page';
import {
  Integration,
  Step,
  IntegrationState,
  PUBLISHED
} from '@syndesis/ui/platform';

@Injectable()
export class FlowPageService {
  flowSubscription: Subscription;
  errorMessage: any;
  saveInProgress = false;
  publishInProgress = false;

  constructor(
    public currentFlowService: CurrentFlowService,
    public router: Router
  ) {}

  initialize() {
    this.errorMessage = undefined;
    this.saveInProgress = false;
    this.publishInProgress = false;
  }

  canContinue() {
    return true;
  }

  get integrationName() {
    return this.currentFlowService.integration
      ? this.currentFlowService.integration.name
      : undefined;
  }

  cancel() {
    this.initialize();
    if (this.currentFlowService.integration.id) {
      this.router.navigate([
        '/integrations',
        this.currentFlowService.integration.id
      ]);
    } else {
      this.router.navigate(['/integrations']);
    }
  }

  goBack(path: Array<string | number | boolean>, route: ActivatedRoute) {
    this.router.navigate(path, { relativeTo: route.parent });
  }

  doSave(route: ActivatedRoute) {
    this.errorMessage = undefined;
    if (
      !this.currentFlowService.integration.name ||
      this.currentFlowService.integration.name === ''
    ) {
      this.router.navigate(['integration-basics'], {
        relativeTo: route.parent
      });
      this.initialize();
      return;
    }
    const router = this.router;
    this.currentFlowService.events.emit({
      kind: 'integration-save',
      publish: this.publishInProgress,
      action: (i: Integration) => {
        if (this.saveInProgress) {
          this.initialize();
          return;
        }
        const target = i.id ? ['/integrations', i.id] : ['/integrations'];
        this.router.navigate(target);
      },
      error: reason => {
        this.errorMessage = reason;
        this.saveInProgress = false;
        //
        // Error occurred while publishing
        // so reset publish progress flag
        //
        if (this.publishInProgress) {
          this.publishInProgress = false;
        }
      }
    });
  }

  save(route: ActivatedRoute) {
    this.initialize();
    this.saveInProgress = true;
    this.doSave(route);
  }

  publish(route: ActivatedRoute) {
    this.initialize();
    this.publishInProgress = true;
    this.doSave(route);
  }

  getChildPath(route: ActivatedRoute) {
    const child = route.firstChild;
    if (child && child.snapshot) {
      return child.snapshot.url;
    }
    return undefined;
  }

  getCurrentChild(route: ActivatedRoute): string {
    const path = this.getChildPath(route);
    if (!path) {
      return undefined;
    }
    return path[0].path;
  }

  getCurrentPosition(route: ActivatedRoute): number {
    const path = this.getChildPath(route);
    if (!path) {
      return undefined;
    }
    try {
      const position = path[1].path;
      return +position;
    } catch (error) {
      return -1;
    }
  }

  getCurrentStepIndex(route: ActivatedRoute): number {
    const path = this.getChildPath(route);
    try {
      const index = path[2].path;
      return +index;
    } catch (error) {
      return -1;
    }
  }

  getCurrentStep(route: ActivatedRoute) {
    return this.currentFlowService.getStep(this.getCurrentPosition(route));
  }

  getCurrentStepKind(route: ActivatedRoute) {
    return (this.getCurrentStep(route) || {})['stepKind'];
  }
}
