import { OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { Integration, Step, TypeFactory } from '../../model';

export abstract class FlowPage implements OnDestroy {
  flowSubscription: Subscription;
  errorMessage: any = undefined;
  saveInProgress = false;
  publishInProgress = false;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router
  ) {
    this.flowSubscription = this.currentFlow.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
  }

  canContinue() {
    return true;
  }

  get integrationName() {
    return this.currentFlow.integration
      ? this.currentFlow.integration.name
      : undefined;
  }

  cancel() {
    if (this.currentFlow.integration.id) {
      this.router.navigate(['/integrations', this.currentFlow.integration.id]);
    } else {
      this.router.navigate(['/integrations']);
    }
  }

  goBack(path: Array<string | number | boolean>) {
    this.router.navigate(path, { relativeTo: this.route.parent });
  }

  handleFlowEvent(event: FlowEvent) {
    /* no-op */
  }

  doSave() {
    this.errorMessage = undefined;
    if (
      !this.currentFlow.integration.name ||
      this.currentFlow.integration.name === ''
    ) {
      this.router.navigate(['integration-basics'], {
        relativeTo: this.route.parent
      });
      this.saveInProgress = false;
      this.publishInProgress = false;
      return;
    }
    const router = this.router;
    this.currentFlow.events.emit({
      kind: 'integration-save',
      action: (i: Integration) => {
        if (i.id) {
          // Go to detail page
          router.navigate(['/integrations', i.id]);
        } else {
          // Just in case safety net...
          router.navigate(['/integrations']);
        }
      },
      error: error => {
        setTimeout(() => {
          this.errorMessage = error;
          this.saveInProgress = false;
          this.publishInProgress = false;
        }, 10);
      }
    });
  }

  save(status: 'Draft' | 'Activated' | 'Deactivated' | 'Deleted' = undefined) {
    if (status) {
      this.currentFlow.integration.desiredStatus = status;
    }
    if (!this.currentFlow.integration.desiredStatus) {
      this.currentFlow.integration.desiredStatus = 'Draft';
    }
    this.saveInProgress = true;
    this.doSave();
  }

  publish(
    status: 'Draft' | 'Activated' | 'Deactivated' | 'Deleted' = 'Activated'
  ) {
    this.currentFlow.integration.desiredStatus = status;
    this.publishInProgress = true;
    this.doSave();
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
