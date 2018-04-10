import { Component, Input, OnInit } from '@angular/core';

import { SlugifyPipe } from '@syndesis/ui/common/slugify.pipe';

@Component({
  selector: 'syndesis-wizard-progress-bar',
  styles: [`
    :host(.has-half-width) .wizard-pf-steps-indicator { max-width: 50%; }
  `],
  template: `
  <div class="row toolbar-pf steps" *ngIf="steps.length > 0">
    <ul class="wizard-pf-steps-indicator center">
      <li class="wizard-pf-step"
        *ngFor="let step of steps; let index = index"
        [ngClass]="'wizard-pf-step--' + (index + 1)"
        [class.active]="selectedStep === (index + 1)">
        <a class="disabled"
            routerLink="{{ stepUrls[index] }}"
            routerLinkActive="active">
          <span class="wizard-pf-step-title">
            {{ steps[index] }}
          </span>
          <span class="wizard-pf-step-number">
          {{ index + 1 }}
        </span>
        </a>
      </li>
    </ul>
  </div>
  `
})
export class WizardProgressBarComponent implements OnInit {
  @Input() steps = [];
  @Input() selectedStep: number;
  @Input() stepUrlPrefix = '';
  @Input() stepUrls: Array<string>;

  ngOnInit() {
    if (!this.stepUrls && !this.selectedStep) {
      this.selectedStep =  1;
    }

    this.stepUrls = (this.stepUrls || this.steps).map(step => {
      const stepSlug = new SlugifyPipe().transform(step);
      return `${stepSlug}`;
    });
  }
}
