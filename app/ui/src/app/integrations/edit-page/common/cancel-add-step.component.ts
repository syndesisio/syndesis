import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import { CurrentFlow } from '@syndesis/ui/integrations/edit-page';

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
    public currentFlow: CurrentFlow,
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
      this.position !== this.currentFlow.getLastPosition()
    );
  }

  onClick() {
    const step = this.currentFlow.getStep(this.position);
    // Currently a half-configured step doesn't have this, so we can
    // remove it, otherwise we'll just discard any changes.
    if (!step.configuredProperties) {
      this.currentFlow.events.emit({
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
