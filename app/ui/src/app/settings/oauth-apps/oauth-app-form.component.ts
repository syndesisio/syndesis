import { Component, OnInit, EventEmitter, Input, Output, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { FormFactoryService } from '@syndesis/ui/platform';
import { OAuthAppStore } from '@syndesis/ui/store/oauthApp/oauth-app.store';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
  DynamicInputModel
} from '@ng-dynamic-forms/core';
import { Subscription } from 'rxjs';

@Component({
  selector: 'syndesis-oauth-app-form',
  templateUrl: 'oauth-app-form.component.html'
})
export class OAuthAppFormComponent implements OnInit, OnDestroy {
  formConfig: any;
  @Input() item: any = {};

  loading = false;
  error: any = null;
  message: any = null;
  formGroup: FormGroup;
  formModel: DynamicFormControlModel[];
  appsSubscription: Subscription;

  constructor(
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
    private store: OAuthAppStore,
    private router: Router
  ) {}

  save() {
    const app = {
      ...this.item.client,
      configuredProperties: this.formFactory.sanitizeValues(this.formGroup.value, this.formConfig)
    };

    this.formGroup.disable();
    this.loading = true;
    this.error = null;
    this.message = null;
    const sub = this.store.update(app).subscribe(
      () => {
        this.loading = false;
        this.message = 'success';
        sub.unsubscribe();
        this.item.client = app;
      },
      (error: any) => {
        this.loading = false;
        this.formGroup.enable();
        this.message = null;
        this.error = error;
        sub.unsubscribe();
      }
    );
  }

  handleLinks(event: any): void {
    event.stopPropagation();
    event.preventDefault();

    if (
      event.target &&
      event.target.tagName &&
      event.target.tagName.toLowerCase() === 'a'
    ) {
      this.router.navigateByUrl(event.target.getAttribute('href'));
    }
  }

  ngOnInit() {
    this.appsSubscription = this.store.list.subscribe(oauthApps => {
      const oauthApp = <OAuthApp> oauthApps.find(it => it.id == this.item.client.id);
      this.formModel = this.formFactory.createFormModel(
        oauthApp.properties,
        oauthApp.configuredProperties,
        ['*']);
      });
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }

  ngOnDestroy() {
    this.appsSubscription.unsubscribe();
  }
}
