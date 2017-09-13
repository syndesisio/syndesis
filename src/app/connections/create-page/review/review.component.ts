import { Component, ChangeDetectorRef } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterStateSnapshot } from '@angular/router';

import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';
import { ModalService } from '../../../common/modal/modal.service';
import { ConnectionService } from '../../../store/connection/connection.service';
import { log, getCategory } from '../../../logging';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connections-review',
  templateUrl: 'review.component.html',
})
export class ConnectionsReviewComponent implements CanComponentDeactivate {

  saved = false;
  reviewForm: FormGroup;

  constructor(
    private current: CurrentConnectionService,
    private modalService: ModalService,
    private connectionService: ConnectionService,
    private detector: ChangeDetectorRef,
    private route: ActivatedRoute,
    private router: Router,
  ) {
    this.reviewForm = this.createReviewForm();
  }

  createReviewForm(): FormGroup {
    return new FormGroup({
      name: new FormControl(null, Validators.required),
      description: new FormControl(),
      tags: new FormControl(),
    });
  }

  async validateNameNotTaken() {
    const control = this.reviewForm.get('name');
    if (!control.hasError('required')) {
      const validationErrors = await this.connectionService.validateName(control.value);
      control.setErrors(validationErrors);
      this.detector.markForCheck();
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
      this.current.connection.description = this.reviewForm.get('description').value;
      this.current.connection.tags = this.reviewForm.get('tags').value;
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
            category,
          );
        },
      });
    }
  }

  // This will trigger validation
  private touchFormFields(): void {
    Object.keys(this.reviewForm.controls).forEach(key => {
      this.reviewForm.get(key).markAsTouched();
    });
  }

  canDeactivate(nextState: RouterStateSnapshot): boolean | Promise<boolean> {
    return this.saved ||
           nextState.url === '/connections/create/cancel' ||
           nextState.url === '/connections/create/configure-fields' ||
           this.modalService.show().then(modal => modal.result);
  }

}
