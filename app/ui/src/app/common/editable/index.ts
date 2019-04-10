import { Type } from '@angular/core';

import { EditableTagsComponent } from '@syndesis/ui/common/editable/editable-tags.component';
import { EditableTextComponent } from '@syndesis/ui/common/editable/editable-text.component';
import { EditableTextareaComponent } from '@syndesis/ui/common/editable/editable-textarea.component';

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
