import {
  Component,
  ContentChildren,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  QueryList,
  SimpleChanges,
  ChangeDetectorRef
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormValidationService,
  DynamicFormControlModel,
  DynamicFormArrayGroupModel,
  DynamicFormControlComponent,
  DynamicFormControlEvent,
  DynamicTemplateDirective,
  DynamicFormLayout,
  DynamicFormLayoutService,
  DYNAMIC_FORM_CONTROL_TYPE_ARRAY,
  DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX,
  DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX_GROUP,
  DYNAMIC_FORM_CONTROL_TYPE_GROUP,
  DYNAMIC_FORM_CONTROL_TYPE_DATEPICKER,
  DYNAMIC_FORM_CONTROL_TYPE_INPUT,
  DYNAMIC_FORM_CONTROL_TYPE_RADIO_GROUP,
  DYNAMIC_FORM_CONTROL_TYPE_SELECT,
  DYNAMIC_FORM_CONTROL_TYPE_TEXTAREA,
  DYNAMIC_FORM_CONTROL_TYPE_TIMEPICKER
} from '@ng-dynamic-forms/core';

export const enum SyndesisFormControlType {
  Array = 1, //'ARRAY',
  Checkbox = 2, //'CHECKBOX',
  DatePicker = 3, //'DATEPICKER,
  Group = 4, //'GROUP',
  Input = 5, //'INPUT',
  RadioGroup = 6, //'RADIO_GROUP',
  Select = 7, //'SELECT',
  TextArea = 8, //'TEXTAREA',
  TimePicker = 9 //"TIMEPICKER"
}

@Component({
  selector: 'syndesis-form-control',
  templateUrl: './syndesis-form-control.component.html',
  /* tslint:disable no-unused-css*/
  styleUrls: ['./syndesis-form-control.scss']
})
export class SyndesisFormComponent extends DynamicFormControlComponent
  implements OnChanges {
  @ContentChildren(DynamicTemplateDirective) contentTemplateList: QueryList<DynamicTemplateDirective>;
  // TODO disabling this for now as the base class is in a dependency
  /* tslint:disable */
  @Input('templates') inputTemplateList: QueryList<DynamicTemplateDirective>;
  /* tslint:enable */

  @Input() asBootstrapFormGroup = true;
  @Input() bindId = false;
  @Input() hasErrorMessaging = false;
  @Input() context: DynamicFormArrayGroupModel | null = null;
  @Input() group: FormGroup;
  @Input() layout: DynamicFormLayout;
  @Input() model: DynamicFormControlModel;

  /* tslint:disable */
  @Output('dfBlur') blur: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();
  @Output('dfChange') change: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();
  @Output('dfFocus') focus: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();
  @Output('bsEvent') customEvent: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();
  /* tslint:enable */

  type: SyndesisFormControlType | null;
  fieldHash: string;

  constructor(
    protected detector: ChangeDetectorRef,
    protected layoutService: DynamicFormLayoutService,
    protected validationService: DynamicFormValidationService
  ) {
    super(detector, layoutService, validationService);
    this.fieldHash = Math.random().toString(36).substr(2, 16);
  }

  static getFormControlType(model: DynamicFormControlModel): SyndesisFormControlType {
    switch (model.type) {
      case DYNAMIC_FORM_CONTROL_TYPE_ARRAY:
        return SyndesisFormControlType.Array;

      case DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX:
        return SyndesisFormControlType.Checkbox;

      case DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX_GROUP:
      case DYNAMIC_FORM_CONTROL_TYPE_GROUP:
        return SyndesisFormControlType.Group;

      case DYNAMIC_FORM_CONTROL_TYPE_DATEPICKER:
        return SyndesisFormControlType.DatePicker;

      case DYNAMIC_FORM_CONTROL_TYPE_INPUT:
        return SyndesisFormControlType.Input;

      case DYNAMIC_FORM_CONTROL_TYPE_RADIO_GROUP:
        return SyndesisFormControlType.RadioGroup;

      case DYNAMIC_FORM_CONTROL_TYPE_SELECT:
        return SyndesisFormControlType.Select;

      case DYNAMIC_FORM_CONTROL_TYPE_TEXTAREA:
        return SyndesisFormControlType.TextArea;

      case DYNAMIC_FORM_CONTROL_TYPE_TIMEPICKER:
        return SyndesisFormControlType.TimePicker;

      default:
        return null;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    super.ngOnChanges(changes);

    if (changes['model']) {
      this.type = SyndesisFormComponent.getFormControlType(this.model);
    }
  }
}
