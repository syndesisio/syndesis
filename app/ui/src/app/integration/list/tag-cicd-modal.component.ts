import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Integration, IntegrationSupportService } from '@syndesis/ui/platform';
import { catchError, switchMap } from 'rxjs/operators';
import { of, Observable, forkJoin } from 'rxjs';
import { ModalService } from '@syndesis/ui/common';

export interface EnvironmentListItem {
  name: string;
  oldName?: string;
  isNew: boolean;
  tagged: boolean;
  editing: boolean;
}

@Component({
  selector: 'syndesis-integration-tag-cicd-modal',
  templateUrl: './tag-cicd-modal.component.html',
  styleUrls: ['./tag-cicd-modal.component.scss'],
})
export class TagCICDModalComponent implements OnInit, OnDestroy {
  @Input()
  integration: Integration;
  environments: EnvironmentListItem[];
  loaded = false;
  removalCandidate: EnvironmentListItem;

  data$: Observable<any>;

  constructor(
    private integrationSupportService: IntegrationSupportService,
    private modalService: ModalService
  ) {
    // nothing to do
  }

  cancel() {
    this.modalService.hide('cicdWrapper', false);
  }

  postUpdate(fetch = false) {
    const sub = this.integrationSupportService
      .tagIntegration(
        this.integration.id,
        this.environments.filter(e => e.tagged).map(e => e.name)
      )
      .subscribe(
        response => {
          if (fetch) {
            this.fetchData();
          }
          sub.unsubscribe();
        },
        error => {
          if (fetch) {
            this.fetchData();
          }
          sub.unsubscribe();
        }
      );
  }

  save() {
    this.modalService.hide('cicdWrapper', true);
    this.postUpdate();
  }

  add() {
    this.postUpdate(true);
  }

  canSave() {
    if (!this.environments || !this.environments.length) {
      return false;
    }
    return this.environments.findIndex(e => e.isNew && e.name === '') === -1;
  }

  cancelRemove() {
    this.removalCandidate = undefined;
  }

  remove() {
    if (!this.removalCandidate) {
      return;
    }
    const sub = this.integrationSupportService
      .removeEnvironment(this.removalCandidate.name)
      .subscribe(
        _ => {
          this.fetchData();
          sub.unsubscribe();
        },
        _ => {
          this.fetchData();
          sub.unsubscribe();
        }
      );
  }

  promptRemove(environment: EnvironmentListItem) {
    this.removalCandidate = environment;
  }

  edit(environment: EnvironmentListItem) {
    environment.editing = true;
  }

  cancelRename(environment: EnvironmentListItem) {
    environment.name = environment.oldName;
    environment.editing = false;
  }

  rename(environment: EnvironmentListItem) {
    if (!environment.oldName || environment.oldName === environment.name) {
      // TODO some kind of error?
      return;
    }
    const sub = this.integrationSupportService
      .renameEnvironment(environment.oldName, environment.name)
      .subscribe(
        _ => {
          this.fetchData();
          sub.unsubscribe();
        },
        _ => {
          this.fetchData();
          sub.unsubscribe();
        }
      );
  }

  addNew() {
    this.environments.push({
      name: '',
      tagged: true,
      editing: false,
      isNew: true,
    });
  }

  fetchData() {
    this.removalCandidate = undefined;
    this.loaded = false;
    this.environments = [];
    // Fetch the environments this integration is tagged with
    // and also fetch all available environments, combining
    // the result into one object for the view
    const dataSubscription = forkJoin(
      this.integrationSupportService
        .fetchIntegrationTags(this.integration.id)
        .pipe(catchError(_ => of({}))),
      this.integrationSupportService.getEnvironments()
    )
      .pipe(
        switchMap(([tags, environments]) => {
          return of({ tags, environments });
        })
      )
      .subscribe(({ tags, environments }) => {
        this.environments = [
          ...environments.map(e => ({
            name: e,
            oldName: e,
            tagged: typeof tags[e] !== 'undefined',
            tag: tags[e],
            editing: false,
            isNew: false,
          })),
          ...(this.environments || []),
        ];
        if (!this.environments.length) {
          this.addNew();
        }
        this.loaded = true;
        dataSubscription.unsubscribe();
      });
  }

  ngOnInit(): void {
    this.fetchData();
  }

  ngOnDestroy(): void {
    // TODO
  }
}
