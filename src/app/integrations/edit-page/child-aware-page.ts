import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { CurrentFlow } from './current-flow.service';

export abstract class ChildAwarePage {
  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
  ) {}

  getCurrentChild(route: ActivatedRoute = this.route): string {
    const child = route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      // log.debugc(() => 'path from root: ' + path, category);
      return path[0].path;
    } else {
      // log.debugc(() => 'no current child', category);
      return undefined;
    }
  }

  getCurrentPosition(route: ActivatedRoute = this.route): number {
    const child = route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      // log.debugc(() => 'path from root: ' + path, category);
      try {
        const position = path[1].path;
        return +position;
      } catch (error) {
        return -1;
      }
    } else {
      // log.debugc(() => 'no current child', category);
      return undefined;
    }
  }

  getCurrentStep(route: ActivatedRoute = this.route) {
    return this.currentFlow.getStep(this.getCurrentPosition(route));
  }

  get currentStep() {
    return this.getCurrentStep();
  }

  get currentStepKind() {
    return (this.currentStep || {})['stepKind'];
  }
}
