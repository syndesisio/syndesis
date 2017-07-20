import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ObjectPropertyFilterPipe } from './object-property-filter.pipe';
import { ObjectPropertySortPipe } from './object-property-sort.pipe';
import { TruncateCharactersPipe } from './truncate-characters.pipe';
import { TruncateWordsPipe } from './truncate-words.pipe';
import { LoadingComponent } from './loading/loading.component';
import { UserService } from './user.service';
import { FormFactoryService } from './forms.service';
import { ConfigService } from '../config.service';
import { ListToolbarComponent } from './toolbar/list-toolbar.component';

@NgModule({
  imports: [CommonModule],
  declarations: [
    ObjectPropertyFilterPipe,
    ObjectPropertySortPipe,
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
    ListToolbarComponent,
  ],
  exports: [
    ObjectPropertyFilterPipe,
    ObjectPropertySortPipe,
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
    ListToolbarComponent,
  ],
  providers: [ConfigService, FormFactoryService],
})
export class SyndesisCommonModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: SyndesisCommonModule,
      providers: [UserService, FormFactoryService, ConfigService],
    };
  }
}
