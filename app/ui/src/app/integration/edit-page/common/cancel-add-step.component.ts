import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
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
    // Currently a half-configured step doesn't have this, so we can
    // remove it, otherwise we'll just discard any changes.
    if (!step.configuredProperties) {
      this.currentFlowService.events.emit({
        kind: 'integration-remove-step',
        position: this.position,
        onSave: () => {
          this.router.navigate(['save-or-add-step'], {
            relativeTo: this.route.parent
          });
        }
      });
    } else {
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent
      });
    }
  }
}
