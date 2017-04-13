import { OnDestroy, ChangeDetectorRef } from '@angular/core';
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
    public router: Router,
    public detector: ChangeDetectorRef,
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
    this.errorMessage = undefined;
    if (!this.currentFlow.integration.name || this.currentFlow.integration.name === '') {
      this.router.navigate(['integration-basics'], { relativeTo: this.route.parent });
      this.saveInProgress = false;
      this.publishInProgress = false;
      return;
    }
    const router = this.router;
    this.currentFlow.events.emit({
      kind: 'integration-save',
      action: (i: Integration) => {
        /*
        this.detector.detectChanges();
        this.saveInProgress = false;
        this.publishInProgress = false;
        */
        router.navigate(['/integrations']);
      },
      error: (error) => {
        setTimeout(() => {
          this.errorMessage = error;
          this.saveInProgress = false;
          this.publishInProgress = false;
          this.detector.detectChanges();
        }, 10);
      },
    });
  }

  save() {
    if (!this.currentFlow.integration.desiredStatus) {
      this.currentFlow.integration.desiredStatus = 'Draft';
    }
    this.saveInProgress = true;
    this.doSave();
  }

  publish() {
    this.currentFlow.integration.desiredStatus = 'Activated';
    this.publishInProgress = true;
    this.doSave();
  }


  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }

}
