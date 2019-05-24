import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router, RouterStateSnapshot } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  Connection,
  CanComponentDeactivate
} from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { ModalService } from '@syndesis/ui/common';
import { CurrentConnectionService } from '@syndesis/ui/connections/create-page/current-connection';
import { ConnectionService } from '@syndesis/ui/store/connection/connection.service';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connections-review',
  templateUrl: 'review.component.html',
  styleUrls: ['review.component.scss']
})
export class ConnectionsReviewComponent
  implements CanComponentDeactivate, OnInit, OnDestroy {
  saved = false;
  reviewForm: FormGroup;
  sub: Subscription = undefined;

  constructor(
    private current: CurrentConnectionService,
    private modalService: ModalService,
    private connectionService: ConnectionService,
    private router: Router
  ) {}

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

  get hasCredentialError() {
    return this.current.oauthStatus && this.current.oauthStatus.status != 'SUCCESS';
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
          log.debug(
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
      nextState.url === '/connections/create/connection-basics' ||
      this.modalService.show().then(modal => modal.result)
    );
  }

  ngOnInit() {
    this.reviewForm = this.createReviewForm();
    this.current.formGroup = this.reviewForm;
    this.sub = this.current.events.subscribe(event => {
      if (event.kind === 'connection-trigger-create') {
        this.createConnection();
      }
    });
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
