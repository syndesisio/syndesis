import {
  Component,
  OnInit,
  Input,
  EventEmitter,
  Output,
  OnDestroy
} from '@angular/core';
import { Router } from '@angular/router';
import { FormFactoryService } from '@syndesis/ui/platform';
import { OAuthAppStore } from '@syndesis/ui/store/oauthApp/oauth-app.store';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService
} from '@ng-dynamic-forms/core';

@Component({
  selector: 'syndesis-oauth-app-form',
  templateUrl: 'oauth-app-form.component.html'
})
export class OAuthAppFormComponent implements OnInit, OnDestroy {
  formConfig: any;
  @Input() item = <any>{};
  @Output() onSave = new EventEmitter<void>();

  loading = false;
  formGroup: FormGroup;
  formModel: DynamicFormControlModel[];

  constructor(
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
    private store: OAuthAppStore,
    private router: Router
  ) {}

  save() {
    const app = {
      ...this.item.client,
      configuredProperties: this.formFactory.sanitizeValues(
        this.formGroup.value,
        this.formConfig
      )
    };

    this.formGroup.disable();
    this.loading = true;
    this.resetItemState();
    const sub = this.store.update(app).subscribe(
      () => {
        this.loading = false;
        this.item.message = 'success';
        sub.unsubscribe();
        this.onSave.emit();
      },
      (error: any) => {
        this.loading = false;
        this.formGroup.enable();
        this.item.message = null;
        this.item.error = error;
        sub.unsubscribe();
      }
    );
  }

  resetItemState() {
    delete this.item.message;
    delete this.item.error;
  }

  exit() {
    this.item.expanded = false;
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
    this.formModel = this.formFactory.createFormModel(
      this.item.client.properties,
      this.item.client.configuredProperties,
      ['*']
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }

  ngOnDestroy(): void {
    this.resetItemState();
  }
}
