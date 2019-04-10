import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TagInputModule } from 'ngx-chips';

import { CancelConfirmationModalComponent } from '@syndesis/ui/common/cancel_confirmation_modal';
import { DeleteConfirmationModalComponent } from '@syndesis/ui/common/delete_confirmation_modal';
import { SYNDESYS_EDITABLE_DIRECTIVES } from '@syndesis/ui/common/editable';
import { LoadingComponent } from '@syndesis/ui/common/loading/loading.component';
import { SYNDESYS_VALIDATION_DIRECTIVES } from '@syndesis/ui/common/validation';
import { WizardProgressBarComponent } from '@syndesis/ui/common/wizard_progress_bar';

import { DatePipe } from '@syndesis/ui/common/date.pipe';
import { I18NPipe } from '@syndesis/ui/common/i18n.pipe';
import { DerpPipe } from '@syndesis/ui/common/derp.pipe';
import { ObjectPropertyFilterPipe } from '@syndesis/ui/common/object-property-filter.pipe';
import { ObjectPropertySortPipe } from '@syndesis/ui/common/object-property-sort.pipe';
import { TruncateCharactersPipe } from '@syndesis/ui/common/truncate-characters.pipe';
import { TruncateWordsPipe } from '@syndesis/ui/common/truncate-words.pipe';
import { CapitalizePipe } from '@syndesis/ui/common/capitalize.pipe';
import { TitleizePipe } from '@syndesis/ui/common/titleize.pipe';
import { SlugifyPipe } from '@syndesis/ui/common/slugify.pipe';
import { DurationPipe } from '@syndesis/ui/common/duration.pipe';
import { DurationDiffPipe } from '@syndesis/ui/common/duration-diff.pipe';
import { IconPathPipe } from '@syndesis/ui/common/icon-path.pipe';
import { ParseMarkdownLinksPipe } from '@syndesis/ui/common/parse-markdown-links.pipe';
import { ButtonComponent } from '@syndesis/ui/common/button.component';
import { InlineAlertComponent } from '@syndesis/ui/common/inline-alert';
import { CardTechPreviewComponent } from '@syndesis/ui/common/card-tech-preview.component';

// TODO: Move these services out to a CoreModule
import { NotificationService } from '@syndesis/ui/common/ui-patternfly';
import { ModalComponent, ModalService } from '@syndesis/ui/common/modal';
import { ConfigService } from '@syndesis/ui/config.service';
import { NavigationService } from '@syndesis/ui/common/navigation.service';
import { EmptyStateCardComponent } from '@syndesis/ui/common/empty-state-card/empty-state-card.component';
import { VendorModule } from '@syndesis/ui/vendor';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    TagInputModule,
    RouterModule,
    VendorModule
  ],
  declarations: [
    DatePipe,
    I18NPipe,
    DerpPipe,
    ObjectPropertyFilterPipe,
    ObjectPropertySortPipe,
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
    CapitalizePipe,
    TitleizePipe,
    ButtonComponent,
    SlugifyPipe,
    DurationPipe,
    DurationDiffPipe,
    IconPathPipe,
    ParseMarkdownLinksPipe,
    ModalComponent,
    WizardProgressBarComponent,
    CancelConfirmationModalComponent,
    DeleteConfirmationModalComponent,
    InlineAlertComponent,
    ...SYNDESYS_EDITABLE_DIRECTIVES,
    ...SYNDESYS_VALIDATION_DIRECTIVES,
    EmptyStateCardComponent,
    CardTechPreviewComponent
  ],
  exports: [
    DatePipe,
    I18NPipe,
    DerpPipe,
    ObjectPropertyFilterPipe,
    ObjectPropertySortPipe,
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
    CapitalizePipe,
    TitleizePipe,
    ParseMarkdownLinksPipe,
    ButtonComponent,
    SlugifyPipe,
    IconPathPipe,
    DurationPipe,
    DurationDiffPipe,
    ModalComponent,
    WizardProgressBarComponent,
    CancelConfirmationModalComponent,
    DeleteConfirmationModalComponent,
    InlineAlertComponent,
    ...SYNDESYS_EDITABLE_DIRECTIVES,
    ...SYNDESYS_VALIDATION_DIRECTIVES,
    EmptyStateCardComponent,
    CardTechPreviewComponent
  ]
})
export class SyndesisCommonModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: SyndesisCommonModule,
      providers: [
        ConfigService,
        ModalService,
        NotificationService,
        NavigationService
      ]
    };
  }
}
