import { OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { Integration, Step, TypeFactory } from '../../model';

export abstract class FlowPage implements OnDestroy {

  flowSubscription: Subscription;
  errorMessage: any = undefined;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
  ) {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
  }

  cancel() {
    this.router.navigate(['integrations']);
  }

  goBack(path: Array<string | number | boolean>) {
    this.router.navigate(path, { relativeTo: this.route.parent });
  }

  handleFlowEvent(event: FlowEvent) {
    /* no-op */
  }

  doSave() {
    if (!this.currentFlow.integration.name || this.currentFlow.integration.name === '') {
      this.router.navigate(['integration-basics'], { relativeTo: this.route.parent });
      return;
    }
    const router = this.router;
    this.currentFlow.events.emit({
      kind: 'integration-save',
      action: (i: Integration) => {
        router.navigate(['/integrations']);
      },
      error: (error) => {
        setTimeout(() => {
          this.errorMessage = error;
        }, 10);
      },
    });
  }

  save() {
    if (!this.currentFlow.integration.desiredStatus) {
      this.currentFlow.integration.desiredStatus = 'Draft';
    }
    this.doSave();
  }

  publish() {
    this.currentFlow.integration.desiredStatus = 'Activated';
    this.doSave();
  }


  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }

}
