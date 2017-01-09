import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TruncateCharactersPipe } from './truncate-characters.pipe';
import { TruncateWordsPipe } from './truncate-words.pipe';
import { LoadingComponent } from './loading/loading.component';

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
export class IPaaSCommonModule { }
