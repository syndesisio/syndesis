import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NotificationModule } from 'patternfly-ng';

import { EditableTagsComponent } from './editable/editable-tags.component';
import { EditableTextComponent } from './editable/editable-text.component';
import { EditableTextareaComponent } from './editable/editable-textarea.component';
import { DerpPipe } from './derp.pipe';
import { ObjectPropertyFilterPipe } from './object-property-filter.pipe';
import { ObjectPropertySortPipe } from './object-property-sort.pipe';
import { TruncateCharactersPipe } from './truncate-characters.pipe';
import { TruncateWordsPipe } from './truncate-words.pipe';
import { CapitalizePipe } from './capitalize.pipe';
import { TitleizePipe } from './titleize.pipe';
import { SlugifyPipe } from './slugify.pipe';
import { LoadingComponent } from './loading/loading.component';
import { UserService } from './user.service';
import { FormFactoryService } from './forms.service';
import { ConfigService } from '../config.service';
import { NavigationService } from './navigation.service';
import { ModalComponent, ModalService } from './modal';
import { TagInputModule } from 'ngx-chips';
import { NotificationService } from 'app/common/ui-patternfly/notification-service';
import { WizardProgressBarComponent } from './wizard_progress_bar';
import { CancelConfirmationModalComponent } from './cancel_confirmation_modal';

@NgModule({
  imports: [
    FormsModule,
    CommonModule,
    TagInputModule,
    RouterModule
  ],
  declarations: [
    DerpPipe,
    ObjectPropertyFilterPipe,
    ObjectPropertySortPipe,
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
    CapitalizePipe,
    TitleizePipe,
    SlugifyPipe,
    ModalComponent,
    EditableTagsComponent,
    EditableTextComponent,
    EditableTextareaComponent,
    WizardProgressBarComponent,
    CancelConfirmationModalComponent,
  ],
  exports: [
    CommonModule,
    DerpPipe,
    ObjectPropertyFilterPipe,
    ObjectPropertySortPipe,
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
    CapitalizePipe,
    TitleizePipe,
    SlugifyPipe,
    ModalComponent,
    EditableTagsComponent,
    EditableTextComponent,
    EditableTextareaComponent,
    WizardProgressBarComponent,
    CancelConfirmationModalComponent
  ],
  providers: [FormFactoryService]
})
export class SyndesisCommonModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: SyndesisCommonModule,
      providers: [
        UserService,
        FormFactoryService,
        ConfigService,
        ModalService,
        NotificationService,
        NavigationService
      ]
    };
  }
}
