import { NgModule } from '@angular/core';

import { TruncateCharactersPipe } from './truncate-characters.pipe';
import { TruncateWordsPipe } from './truncate-words.pipe';

@NgModule({
  imports: [
  ],
  declarations: [
    TruncateCharactersPipe,
    TruncateWordsPipe,
  ],
  exports: [
    TruncateCharactersPipe,
    TruncateWordsPipe,
  ],
})
export class IPaaSCommonModule { }
