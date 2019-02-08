import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Integration, IntegrationSupportService } from '@syndesis/ui/platform';
import { catchError, switchMap } from 'rxjs/operators';
import { of, Observable, forkJoin } from 'rxjs';
import { ModalService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integration-tag-cicd-modal',
  templateUrl: './tag-cicd-modal.component.html',
  styleUrls: ['./tag-cicd-modal.component.scss'],
})
export class TagCICDModalComponent implements OnInit, OnDestroy {
  @Input()
  integration: Integration;

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

  save() {
    this.modalService.hide('cicdWrapper', true);
    // TODO post changes made in the dialog here
  }

  ngOnInit(): void {
    /*
    // posting a tag to an integration
    this.integrationSupportService
      .tagIntegration(this.integration.id, ['foo'])
      .subscribe(resp => {
        console.log('Got: ', resp);
      });
    */
    // Fetch the environments this integration is tagged with
    // and also fetch all available environments, combining
    // the result into one object for the view
    this.data$ = forkJoin(
      this.integrationSupportService
        .fetchIntegrationTags(this.integration.id)
        .pipe(catchError(_ => of({}))),
      this.integrationSupportService.getEnvironments()
    ).pipe(
      switchMap(([tags, environments]) => {
        // TODO the real invocation, dummy data below
        return of({ tags, environments });

        /* TODO remove dummy data when this is finished
        return of({
          tags: {
            foo: {
              releaseTag: 'blah',
              lastTaggedAt: 523423423423,
              lastExportedAt: 213451234213,
              lastImportedAt: 1234522342,
            },
          },
          environments: ['foo', 'bar', 'blah'],
        });
        */
      })
    );
  }

  ngOnDestroy(): void {
    // stuff
  }
}
