import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import {
  ActionConfig,
  FilterConfig,
  FilterEvent,
  FilterField,
  SortConfig,
  SortField,
  SortEvent,
  ToolbarConfig
} from 'patternfly-ng';
import {
  EXTENSION,
  StepStore,
  StepKind,
  StepKinds
} from '../../../store/step/step.store';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { ExtensionStore } from 'app/store/extension/extension.store';
import {
  Extension,
  Extensions,
  Step,
  Steps,
  TypeFactory
} from '../../../model';
import { ObjectPropertyFilterPipe } from 'app/common/object-property-filter.pipe';
import { log, getCategory } from '../../../logging';

@Component({
  selector: 'syndesis-integrations-step-select',
  templateUrl: './step-select.component.html',
  styleUrls: ['./step-select.component.scss']
})
export class IntegrationsStepSelectComponent extends FlowPage
  implements OnInit, OnDestroy {
  steps: Steps;
  filteredSteps: Steps;
  extensions$: Observable<Extensions>;
  loading$: Observable<boolean>;
  position: number;
  toolbarConfig: ToolbarConfig;
  onlyShowExtensions = false;
  private propertyFilter = new ObjectPropertyFilterPipe();

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    private stepStore: StepStore,
    private extensionStore: ExtensionStore
  ) {
    super(currentFlow, route, router);
    this.extensions$ = this.extensionStore.list;
    this.loading$ = this.extensionStore.loading;
  }

  goBack() {
    super.goBack(['save-or-add-step']);
  }

  getName(step: StepKind) {
    return this.stepStore.getStepName(step);
  }

  getDescription(step: StepKind) {
    return this.stepStore.getStepDescription(step);
  }

  isSelected(step: Step) {
    const _step = this.currentFlow.getStep(this.position);
    return _step && step.stepKind === _step.stepKind;
  }

  handleFlowEvent(event: FlowEvent) {
    const step = this.currentFlow.getStep(this.position);
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
    const _step = this.currentFlow.getStep(this.position);
    // Maintain the configuration if the user chose the same step kind
    if (_step && _step.stepKind === step.stepKind) {
      step = { ...step, ..._step };
    }
    this.currentFlow.events.emit({
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
      .first(params => params.has('position'))
      .subscribe(params => {
        this.position = +params.get('position');
        this.extensions$.subscribe(extensions => {
          this.steps = this.stepStore.getSteps(extensions);
          // trigger initial filtering for the view
          this.onFilterChange({});
          this.currentFlow.events.emit({
            kind: 'integration-step-select',
            position: this.position
          });
        });
        this.extensionStore.loadAll();
      });
  }

  ngOnDestroy() {
    super.ngOnDestroy();
  }
}
