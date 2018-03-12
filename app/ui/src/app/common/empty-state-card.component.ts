import {
  Component,
  Input,
  TemplateRef,
  OnInit,
  HostBinding
} from '@angular/core';

@Component({
  selector: 'syndesis-empty-state-card',
  template: `
    <div class="card-pf card-pf-view card-pf-view-select card-pf-view-single-select">
      <div class="card-pf-body">
        <!-- Card Icon -->
        <div *ngIf="topElement" class="card-pf-top-element">
        <ng-template [ngTemplateOutlet]="topElement"></ng-template>
        </div>
        <!-- Card Description / Item Overview -->
        <div *ngIf="body" class="card-pf-info text-center">
          <ng-template [ngTemplateOutlet]="body"></ng-template>
        </div>
      </div>
    </div>
  `
})

export class EmptyStateCardComponent implements OnInit {
  @Input() body: TemplateRef<any>;
  @Input() topElement: TemplateRef<any>;
  @Input() colClass: string;
  @HostBinding() class;

  ngOnInit() {
    this.class = 'empty-state-card card ' + this.colClass;
  }
}
