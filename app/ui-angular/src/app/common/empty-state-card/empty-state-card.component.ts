import { Component } from '@angular/core';

@Component({
  selector: 'syndesis-empty-state-card',
  template: `
  <div class="card empty-state-card">
    <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select">
      <div class="card-pf-body">
        <div class="card-pf-top-element">
          <span class="card-pf-icon-large pficon pficon-add-circle-o"></span>
        </div>
        <div class="card-pf-info text-center">
          <ng-content select=".empty-state-card__body"></ng-content>
        </div>
      </div>
    </div>
  </div>
  `,
  styleUrls: ['empty-state-card.component.scss']
})
export class EmptyStateCardComponent {}
