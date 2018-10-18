import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page';

@Component({
  selector: 'syndesis-cancel-add-step',
  template: `
    <button type="button"
            class="btn btn-default"
            (click)="onClick()"
            *ngIf="isIntermediateStep()">Cancel</button>
  `
})
export class CancelAddStepComponent implements OnInit {
  private position: number;

  constructor(
    public currentFlowService: CurrentFlowService,
    public route: ActivatedRoute,
    public router: Router
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(
      paramMap => (this.position = +paramMap.get('position'))
    );
  }

  isIntermediateStep(): boolean {
    return (
      this.position !== 0 &&
      this.position !== this.currentFlowService.getLastPosition()
    );
  }

  onClick() {
    const step = this.currentFlowService.getStep(this.position);
    const metadata = step.metadata || {};
    // An action or step that has no configuration may not have
    // a configuredProperties but it's technically still configured
    if ( step.configuredProperties || metadata.configured === 'true') {
      // The step has previously been configured, so discard
      // any changes but leave the step in the flow
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent,
      });
    } else {
      // The step hasn't been configured at all, remove the step from the flow
      this.currentFlowService.events.emit({
        kind: 'integration-remove-step',
        position: this.position,
        onSave: () => {
          this.router.navigate(['save-or-add-step'], {
            relativeTo: this.route.parent,
          });
        }
      });
    }
  }
}
