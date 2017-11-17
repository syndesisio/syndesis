import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router, RouterStateSnapshot } from '@angular/router';
import { TourService } from 'ngx-tour-ngx-bootstrap';

import { Subscription } from 'rxjs/Subscription';
import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';
import { ModalService } from '../../../common/modal/modal.service';
import { ConnectionService } from '../../../store/connection/connection.service';
import { log, getCategory } from '../../../logging';
import { UserService } from '../../../common/user.service';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connections-review',
  templateUrl: 'review.component.html'
})
export class ConnectionsReviewComponent implements CanComponentDeactivate, OnInit, OnDestroy {
  saved = false;
  reviewForm: FormGroup;
  sub: Subscription = undefined;

  constructor(
    private current: CurrentConnectionService,
    private modalService: ModalService,
    private connectionService: ConnectionService,
    private router: Router,
    public tourService: TourService,
    private userService: UserService
  ) {
    this.reviewForm = this.createReviewForm();
  }

  createReviewForm(): FormGroup {
    return new FormGroup({
      name: new FormControl(null, Validators.required),
      description: new FormControl()
    });
  }

  async validateNameNotTaken() {
    const control = this.reviewForm.get('name');
    if (!control.hasError('required')) {
      const validationErrors = await this.connectionService.validateName(
        control.value
      );
      control.setErrors(validationErrors);
    }
  }

  get name() {
    return this.reviewForm.get('name');
  }

  createConnection(): void {
    if (this.reviewForm.invalid) {
      this.touchFormFields();
    } else {
      this.current.connection.name = this.reviewForm.get('name').value;
      this.current.connection.description = this.reviewForm.get(
        'description'
      ).value;
      this.saved = true;
      this.current.events.emit({
        kind: 'connection-save-connection',
        connection: this.current.connection,
        action: (connection: Connection) => {
          this.router.navigate(['connections']);
        },
        error: (reason: any) => {
          log.debugc(
            () =>
              'Error creating connection: ' +
              JSON.stringify(reason, undefined, 2),
            category
          );
        }
      });
    }
  }

  canDeactivate(nextState: RouterStateSnapshot): boolean | Promise<boolean> {
    return (
      this.saved ||
      nextState.url === '/connections/create/cancel' ||
      nextState.url === '/connections/create/configure-fields' ||
      this.modalService.show().then(modal => modal.result)
    );
  }

  ngOnInit() {
    this.sub = this.current.events.subscribe(event => {
      if (event.kind === 'connection-trigger-create') {
        this.createConnection();
      }
    });

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
        route: 'connections/create/review',
        anchorId: 'connections.publish',
        title: 'Make It Available',
        content: 'Click Create to make the new connection available for use in integrations.',
        placement: 'left',
        } ],
      );
      setTimeout(() => this.tourService.start());
    }
  }

  ngOnDestroy() {
    if (this.sub) {
      this.sub.unsubscribe();
    }
  }

  /* This will trigger validation */
  private touchFormFields(): void {
    Object.keys(this.reviewForm.controls).forEach(key => {
      this.reviewForm.get(key).markAsTouched();
    });
  }
}
