import { first, switchMap } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, Subscription, BehaviorSubject, of } from 'rxjs';
import { StepOrConnection } from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  FlowPageService,
  FlowEvent,
  INTEGRATION_SET_STEP,
  INTEGRATION_SET_CONNECTION,
  INTEGRATION_CANCEL_CLICKED,
} from '@syndesis/ui/integration/edit-page';
import { StepVisiblePipe } from './step-visible.pipe';

@Component({
  selector: 'syndesis-integration-step-select',
  templateUrl: 'step-select.component.html',
  styleUrls: ['../../integration-common.scss', './step-select.component.scss'],
})
export class IntegrationSelectStepComponent implements OnInit, OnDestroy {
  routeSubscription: Subscription;
  flowSubscription: Subscription;
  allSteps$ = new BehaviorSubject(<StepOrConnection[]>[]);
  filteredSteps$ = new BehaviorSubject(<StepOrConnection[]>[]);
  position: number;

  atStart: boolean;
  atEnd: boolean;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    private route: ActivatedRoute,
    private router: Router,
    private stepVisiblePipe: StepVisiblePipe
  ) {}

  gotoCreateConnection() {
    this.router.navigate(['/connections/create']);
  }

  onSelected(step: StepOrConnection) {
    if (step === undefined) {
      // The user picked the "create connection" card I guess
      return this.gotoCreateConnection();
    }
    if ('connectorId' in step) {
      // the user picked a connection
      // TODO keep any config if the user picked the same connection
      this.currentFlowService.events.emit({
        kind: INTEGRATION_SET_CONNECTION,
        position: this.position,
        connection: { ...step },
        onSave: () => {
          this.router.navigate(['action-select', this.position], {
            relativeTo: this.route.parent,
            replaceUrl: true,
          });
        },
      });
      return;
    }
    // The user picked a non-connection step :-)
    // Maintain the config of the step if the user chose the same step kind
    const _step = this.currentFlowService.getStep(this.position);
    if (_step && _step.stepKind === step['stepKind']) {
      step = { ...step, ..._step };
    }
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_STEP,
      position: this.position,
      step: { ...step },
      onSave: () => {
        this.router.navigate(['step-configure', this.position], {
          relativeTo: this.route.parent,
        });
      },
    });
  }

  validateCurrentStep() {
    // See if a step is already configured at this position and move the user to
    // the relevant page if so
    const step = this.currentFlowService.getStep(this.position);
    if (!step) {
      /* Safety net */
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent,
      });
      return;
    }
    // The step we've loaded is a connection, go to the action select page
    if (step.stepKind === 'endpoint' && step.connection) {
      this.router.navigate(['action-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    if (typeof step.stepKind !== 'undefined' && step.stepKind !== 'endpoint') {
      // The step isn't a connection, go to the step configure page
      this.router.navigate(['step-configure', this.position], {
        relativeTo: this.route.parent,
      });
    }
  }

  handleFlowEvent(event: FlowEvent) {
    if (event.kind === INTEGRATION_CANCEL_CLICKED) {
      try {
        if (!this.currentFlowService.integration.id) {
          // Integration hasn't been saved and we're in the create page flow
          this.router.navigate(['/integrations']);
          return;
        }
      } catch (err) {
        // something's gone horribly wrong
        this.router.navigate(['/integrations']);
        return;
      }
      this.flowPageService.maybeRemoveStep(
        this.router,
        this.route,
        this.position
      );
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlowService.events.subscribe(event =>
      this.handleFlowEvent(event)
    );
    this.routeSubscription = combineLatest(
      this.route.paramMap.pipe(first(params => params.has('position'))),
      this.route.data
    )
      .pipe(
        switchMap(([params, data]) =>
          of({
            position: +params.get('position') as number,
            steps: data.steps,
          })
        )
      )
      .subscribe(({ position, steps }) => {
        this.position = position;
        // Set flags for the view based on where we are
        this.atStart = position === 0;
        this.atEnd = position === this.currentFlowService.getLastPosition();
        // Filter steps based on where we are
        const visibleSteps = this.stepVisiblePipe.transform(steps, {
          position,
        }) as StepOrConnection[];
        // Update the view
        this.allSteps$.next(
          this.currentFlowService.filterStepsByPosition(visibleSteps, position)
        );
        // Look and see if we can just go to the config page for this step
        this.validateCurrentStep();
      });
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  get positionText() {
    if (this.atStart) {
      return 'start';
    }
    if (this.atEnd) {
      return 'finish';
    }
    return '';
  }

  get typeText() {
    if (this.atStart || this.atEnd) {
      return 'connection';
    }
    return 'step';
  }
}
