import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { first } from 'rxjs/operators';
import { FilterConfig, SortConfig, ToolbarConfig } from 'patternfly-ng';
import {
  ExtensionStore,
  EXTENSION,
  StepStore,
  StepKind
} from '@syndesis/ui/store';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { Extensions, Step, Steps } from '@syndesis/ui/platform';
import { ObjectPropertyFilterPipe } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integration-step-select',
  templateUrl: './step-select.component.html',
  styleUrls: ['../../integration-common.scss', './step-select.component.scss']
})
export class IntegrationStepSelectComponent implements OnInit, OnDestroy {
  flowSubscription: Subscription;
  steps: Steps;
  filteredSteps: Steps;
  extensions$: Observable<Extensions>;
  loading$: Observable<boolean>;
  position: number;
  toolbarConfig: ToolbarConfig;
  onlyShowExtensions = false;
  private propertyFilter = new ObjectPropertyFilterPipe();

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private stepStore: StepStore,
    private extensionStore: ExtensionStore
  ) {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
    this.extensions$ = this.extensionStore.list;
    this.loading$ = this.extensionStore.loading;
  }

  goBack() {
    this.flowPageService.goBack(['save-or-add-step'], this.route);
  }

  getName(step: StepKind) {
    return this.stepStore.getStepName(step);
  }

  getDescription(step: StepKind) {
    return this.stepStore.getStepDescription(step);
  }

  isSelected(step: Step) {
    const _step = this.currentFlowService.getStep(this.position);
    return _step && step.stepKind === _step.stepKind;
  }

  handleFlowEvent(event: FlowEvent) {
    const step = this.currentFlowService.getStep(this.position);
    if (!step || !('stepKind' in step) || step.stepKind === 'endpoint') {
      // safety net
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent
      });
      return;
    }
    switch (event.kind) {
      case 'integration-step-select':
        step.stepKind = undefined;
        break;
      case 'integration-updated':
        if (step.configuredProperties) {
          this.router.navigate(['step-configure', this.position], {
            relativeTo: this.route.parent
          });
          return;
        }
        break;
      default:
        break;
    }
  }

  onFilterChange($event) {
    const appliedFilters = this.toolbarConfig.filterConfig.appliedFilters;
    const _steps = appliedFilters
      ? appliedFilters.reduce((steps, filter) => {
          const propertyName =
            filter.field.id === EXTENSION ? 'name' : filter.field.id;
          return this.propertyFilter.transform(steps, {
            filter: filter.value,
            propertyName: propertyName,
            exact: filter.field.type !== 'text'
          });
        }, this.steps)
      : this.steps;
    if (this.onlyShowExtensions) {
      this.filteredSteps = _steps.filter(step => step.stepKind === EXTENSION);
    } else {
      this.filteredSteps = _steps;
    }
    this.toolbarConfig.filterConfig.resultsCount = this.filteredSteps.length;
    // trigger sorting
    this.onSortChange($event);
  }

  onFilterFieldSelect($event: any) {
    if ($event.field.id === 'extension') {
      this.onlyShowExtensions = true;
    } else {
      this.onlyShowExtensions = false;
    }
    // trigger filtering
    this.onFilterChange($event);
  }

  onSortChange($event) {
    const ascending = this.toolbarConfig.sortConfig.isAscending;
    this.filteredSteps.sort((a, b) => {
      if (ascending) {
        return a.name.localeCompare(b.name);
      } else {
        return b.name.localeCompare(a.name);
      }
    });
  }

  onSelect(step: Step) {
    const _step = this.currentFlowService.getStep(this.position);
    // Maintain the configuration if the user chose the same step kind
    if (_step && _step.stepKind === step.stepKind) {
      step = { ...step, ..._step };
    }
    this.currentFlowService.events.emit({
      kind: 'integration-set-step',
      position: this.position,
      step: step,
      onSave: () => {
        this.router.navigate(['step-configure', this.position], {
          relativeTo: this.route.parent
        });
      }
    });
  }

  ngOnInit() {
    this.toolbarConfig = {
      filterConfig: {
        fields: [
          {
            id: 'name',
            title: 'Name',
            placeholder: 'Filter by Name...',
            type: 'text'
          },
          {
            id: 'extension',
            title: 'Custom Steps',
            placeholder: 'Filter by Name...',
            type: 'text'
          }
        ]
      } as FilterConfig,
      sortConfig: {
        fields: [
          {
            id: 'name',
            title: 'Name',
            sortType: 'alpha'
          }
        ]
      } as SortConfig
    } as ToolbarConfig;
    this.route.paramMap
      .pipe(first(params => params.has('position')))
      .subscribe(params => {
        this.position = +params.get('position');
        this.extensions$.subscribe(extensions => {
          this.steps = this.stepStore.getSteps(extensions);
          // trigger initial filtering for the view
          this.onFilterChange({});
          this.currentFlowService.events.emit({
            kind: 'integration-step-select',
            position: this.position
          });
        });
        this.extensionStore.loadAll();
      });
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
