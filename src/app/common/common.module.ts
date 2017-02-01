import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TruncateCharactersPipe } from './truncate-characters.pipe';
import { TruncateWordsPipe } from './truncate-words.pipe';
import { LoadingComponent } from './loading/loading.component';
import { UserService } from './user.service';

@NgModule({
  imports: [
    CommonModule,
  ],
  declarations: [
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
  ],
  exports: [
    TruncateCharactersPipe,
    TruncateWordsPipe,
    LoadingComponent,
  ],
})
export class IPaaSCommonModule {

  static forRoot(): ModuleWithProviders {
    return {
      ngModule: IPaaSCommonModule,
      providers: [
        UserService,
      ],
    };
  }

}
