import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { IntegrationStore } from '@syndesis/ui/store';
import { ApiDefinition } from 'apicurio-design-studio';

@Component({
    template: `<ng-container *ngIf="specification$ | async as specification">
  <openapi-editor
    [title]="'customizations.api-client-connectors.edit-api-definition' | synI18n"
    [specification]="specification"
    (onBack)="goToOperations()"
    (onCancel)="goToOperations()"
    (onSave)="updateSpecification($event)">
  </openapi-editor>
</ng-container>`
  })
export class ApiProviderSpecificationEditorPage implements OnInit {

  specification$: Observable<string>;

  private integrationId: string;

  constructor(
    private integrationStore: IntegrationStore,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.integrationId = params.get('integrationId');
      this.specification$ = this.integrationStore.specification(this.integrationId);
    });
  }

  goToOperations() {
    this.router.navigate(['..', 'operations'], { relativeTo: this.route });
  }

  updateSpecification(apiDefinition: ApiDefinition) {
    this.integrationStore.updateSpecification(this.integrationId, apiDefinition.spec)
      .subscribe(() => this.goToOperations());
  }
}
