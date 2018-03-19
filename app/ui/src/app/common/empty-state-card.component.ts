import {
  Component
} from '@angular/core';

@Component({
  selector: 'syndesis-empty-state-card',
  template: `
  <div class="card empty-state-card">
    <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select">
      <div class="card-pf-body">
        <div class="card-pf-top-element">
          <ng-content select=".empty-state-card__header"></ng-content>
        </div>
        <div class="card-pf-info text-center">
          <ng-content select=".empty-state-card__body"></ng-content>
        </div>
      </div>
    </div>
  </div>
  `,
  styles: [`
    :host { display: block; }
    :host .card-pf, :host .card-pf:hover { box-shadow: none; }
    :host .card-pf { background: transparent; padding-top: 1px; padding-bottom: 1px; }
    :host .card-pf-view { border: 1px dashed #919191; }
  `]
})

export class EmptyStateCardComponent {
}
