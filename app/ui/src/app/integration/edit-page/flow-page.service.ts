import { Injectable } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page/current-flow.service';
import { Integration } from '@syndesis/ui/platform';
import {
  INTEGRATION_REMOVE_STEP,
  INTEGRATION_SAVE,
  INTEGRATION_CANCEL_CLICKED,
  INTEGRATION_DONE_CLICKED,
} from './edit-page.models';

@Injectable()
export class FlowPageService {
  flowSubscription: Subscription;
  errorMessage: any;
  saveInProgress = false;
  publishInProgress = false;
  showDone = false;

  constructor(
    public currentFlowService: CurrentFlowService,
    public router: Router
  ) {}

  initialize() {
    this.errorMessage = undefined;
    this.saveInProgress = false;
    this.publishInProgress = false;
    this.showDone = false;
  }

  get integrationName() {
    return this.currentFlowService.integration
      ? this.currentFlowService.integration.name
      : undefined;
  }

  cancel() {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_CANCEL_CLICKED,
    });
  }

  done() {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_DONE_CLICKED,
    });
  }

  maybeRemoveStep(router: Router, route: ActivatedRoute, position: number) {
    const step = this.currentFlowService.getStep(position);
    const metadata = step.metadata || {};
    // An action or step that has no configuration may not have
    // a configuredProperties but it's technically still configured
    if (step.configuredProperties || metadata.configured === 'true') {
      // The step has previously been configured, so discard
      // any changes but leave the step in the flow
      router.navigate(['save-or-add-step'], {
        relativeTo: route.parent,
      });
    } else {
      // The step hasn't been configured at all, remove the step from the flow
      this.currentFlowService.events.emit({
        kind: INTEGRATION_REMOVE_STEP,
        position: position,
        onSave: () => {
          router.navigate(['save-or-add-step'], {
            relativeTo: route.parent,
          });
        },
      });
    }
  }

  goBack(path: Array<string | number | boolean>, route: ActivatedRoute) {
    this.router.navigate(path, {
      relativeTo: route.parent,
    });
  }

  /**
   * Validate the integration and initiate the save process if the integration
   * is valid, redirect to appropriate pages as needed
   *
   * TODO change the function so all target routes are passed in
   *
   * @param route
   * @param targetRoute
   */
  doSave(route: ActivatedRoute, targetRoute?) {
    this.errorMessage = undefined;
    if (
      !this.currentFlowService.validateFlowAndMaybeRedirect(route, this.router)
    ) {
      this.initialize();
      return;
    }
    if (
      !this.currentFlowService.integration.name ||
      this.currentFlowService.integration.name === ''
    ) {
      this.router.navigate(['..', 'integration-basics'], {
        relativeTo: route,
      });
      this.initialize();
      return;
    }
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SAVE,
      publish: this.publishInProgress,
      action: (i: Integration) => {
        if (this.saveInProgress) {
          this.initialize();
          if (targetRoute) {
            this.router.navigate(targetRoute, { relativeTo: route });
          }
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
      },
    });
  }

  save(route: ActivatedRoute, targetRoute?: Array<string>) {
    this.initialize();
    this.saveInProgress = true;
    this.doSave(route, targetRoute);
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
