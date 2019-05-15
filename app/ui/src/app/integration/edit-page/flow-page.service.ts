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
  INTEGRATION_UPDATED,
  INTEGRATION_SAVED,
  INTEGRATION_BUTTON_DISABLE_DONE,
  INTEGRATION_BUTTON_ENABLE_DONE,
} from './edit-page.models';

@Injectable()
export class FlowPageService {
  flowSubscription: Subscription;
  errorMessage: any;
  saveInProgress = false;
  publishInProgress = false;
  showDone = false;
  showCancel = true;
  doneDisabled = false;

  constructor(
    public currentFlowService: CurrentFlowService,
    public router: Router
  ) {
    this.currentFlowService.events.subscribe(event => {
      // Reset this service's state when the integration is fetched
      switch (event.kind) {
        case INTEGRATION_UPDATED:
        case INTEGRATION_SAVED:
          this.initialize();
          break;
        case INTEGRATION_BUTTON_DISABLE_DONE:
          this.doneDisabled = true;
          break;
        case INTEGRATION_BUTTON_ENABLE_DONE:
          this.doneDisabled = false;
          break;
        default:
      }
    });
  }

  initialize() {
    this.errorMessage = undefined;
    this.saveInProgress = false;
    this.publishInProgress = false;
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
    // Even if a step has a configured properties object, the user may
    // not have completed configuring the step
    if (metadata.configured === 'true') {
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
  doSave(route: ActivatedRoute, targetRoute?: Array<string>, targetUrl?: string) {
    this.errorMessage = undefined;
    if (
      !this.currentFlowService.validateFlowAndMaybeRedirect(route, this.router)
    ) {
      return;
    }
    if (
      !this.currentFlowService.integration.name ||
      this.currentFlowService.integration.name === ''
    ) {
      if (targetUrl == null) {
        if (targetRoute == null) {
          targetRoute = [];
        }
        targetUrl = this.router.createUrlTree(targetRoute, {relativeTo: route}).toString();
      }
      this.router.navigateByUrl('/integrations/create/integration-basics?targetUrl=' + targetUrl);
      return;
    }
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SAVE,
      publish: this.publishInProgress,
      action: (i: Integration) => {
        // If the user just clicked save and not publish
        // go to the supplied route, ideally it should be
        // the same page the user is on or the save or add
        // step page
        if (this.saveInProgress && targetUrl) {
          this.router.navigateByUrl(targetUrl);
          return;
        }
        if (targetRoute == null) {
          //default targetRoute to the initial route
          targetRoute = [];
        }
        if (this.saveInProgress && targetRoute) {
          this.router.navigate(targetRoute, { relativeTo: route });
          return;
        }
        // If we get an integration object and it has an ID
        // set, go to the detail page, otherwise fall back
        // to the list page
        const target =
          i && typeof i.id !== 'undefined'
            ? ['/integrations', i.id]
            : ['/integrations'];
        this.router.navigate(target);
      },
      error: reason => {
        // reset state, then set the error
        this.initialize();
        this.errorMessage = reason;
      },
    });
  }

  save(route: ActivatedRoute, targetRoute?: Array<string>, targetUrl?: string) {
    this.saveInProgress = true;
    this.doSave(route, targetRoute, targetUrl);
  }

  publish(route: ActivatedRoute) {
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
