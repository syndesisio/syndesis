import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { CurrentFlow } from './current-flow.service';

export abstract class ChildAwarePage {
  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router
  ) {}

  getChildPath(route: ActivatedRoute = this.route) {
    const child = route.firstChild;
    if (child && child.snapshot) {
      return child.snapshot.url;
    }
    return undefined;
  }

  getCurrentChild(route: ActivatedRoute = this.route): string {
    const path = this.getChildPath(route);
    if (!path) {
      return undefined;
    }
    return path[0].path;
  }

  getCurrentPosition(route: ActivatedRoute = this.route): number {
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

  getCurrentStepIndex(route: ActivatedRoute = this.route): number {
    const path = this.getChildPath(route);
    try {
      const index = path[2].path;
      return +index;
    } catch (error) {
      return -1;
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
