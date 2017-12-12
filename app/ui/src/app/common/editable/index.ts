import { Type } from '@angular/core';

import { EditableTagsComponent } from './editable-tags.component';
import { EditableTextComponent } from './editable-text.component';
import { EditableTextareaComponent } from './editable-textarea.component';
import { EditableComponent } from './editable.component';

export const SYNDESYS_EDITABLE_DIRECTIVES: Type<any>[] = [
  EditableTagsComponent,
  EditableTextComponent,
  EditableTextareaComponent
];

export {
  EditableTagsComponent,
  EditableTextComponent,
  EditableTextareaComponent
};
