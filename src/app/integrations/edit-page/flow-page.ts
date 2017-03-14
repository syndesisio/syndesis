import { OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { CurrentFlow, FlowEvent } from './current-flow.service';

export abstract class FlowPage implements OnDestroy {

  flowSubscription: Subscription;

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

  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }

}
