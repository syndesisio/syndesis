import {
  Component,
  ContentChildren,
  ChangeDetectorRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList,
  SimpleChanges
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
  DynamicSelectModel,
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
  SelectMultiple = 8, //'SELECT MULTIPLE'
  TextArea = 9, //'TEXTAREA',
  TimePicker = 10 //"TIMEPICKER"
}

@Component({
  selector: 'syndesis-form-control',
  templateUrl: './syndesis-form-control.component.html',
  /* tslint:disable no-unused-css*/
  styleUrls: ['./syndesis-form-control.scss']
})
export class SyndesisFormComponent extends DynamicFormControlComponent
  implements OnChanges, OnInit {
  elementOptionClass: string;
  gridGroupClass: string;
  elementGroupClass: string;
  gridControlClass: string;
  elementControlClass: string;
  gridLabelClass: string;
  elementLabelClass: string;
  gridContainerClass: string;
  elementContainerClass: string;
  formGroupClass: boolean;
  @ContentChildren(DynamicTemplateDirective)
  contentTemplateList: QueryList<DynamicTemplateDirective>;
  // TODO disabling this for now as the base class is in a dependency
  /* tslint:disable */
  @Input('templates') inputTemplateList: QueryList<DynamicTemplateDirective>;
  /* tslint:enable */

  @Input() asBootstrapFormGroup = true;
  @Input() bindId = true;
  @Input() hasErrorMessaging = false;
  @Input() context: DynamicFormArrayGroupModel | null = null;
  @Input() group: FormGroup;
  @Input() layout: DynamicFormLayout;
  @Input() model: DynamicFormControlModel;
  @Input() formArrayRowClass: string;

  /* tslint:disable */
  @Output()
  blur: EventEmitter<DynamicFormControlEvent> = new EventEmitter<
    DynamicFormControlEvent
  >();
  @Output()
  change: EventEmitter<DynamicFormControlEvent> = new EventEmitter<
    DynamicFormControlEvent
  >();
  @Output()
  focus: EventEmitter<DynamicFormControlEvent> = new EventEmitter<
    DynamicFormControlEvent
  >();
  /* tslint:enable */

  type: SyndesisFormControlType | null;

  constructor(
    protected detector: ChangeDetectorRef,
    protected layoutService: DynamicFormLayoutService,
    protected validationService: DynamicFormValidationService
  ) {
    super(detector, layoutService, validationService);
  }

  static getFormControlType(
    model: DynamicFormControlModel
  ): SyndesisFormControlType {
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
        if (model instanceof DynamicSelectModel && model.multiple) {
          return SyndesisFormControlType.SelectMultiple;
        } else {
          return SyndesisFormControlType.Select;
        }

      case DYNAMIC_FORM_CONTROL_TYPE_TEXTAREA:
        return SyndesisFormControlType.TextArea;

      case DYNAMIC_FORM_CONTROL_TYPE_TIMEPICKER:
        return SyndesisFormControlType.TimePicker;

      default:
        return null;
    }
  }

  ngOnInit() {
    this.formGroupClass =
      this.asBootstrapFormGroup ||
      this.getClass('element', 'container').includes('formGroup');

    this.elementContainerClass = this.getClass('element', 'container');
    this.gridContainerClass = this.getClass('grid', 'container');

    this.elementLabelClass = this.getClass('element', 'label');
    this.gridLabelClass = this.getClass('grid', 'label');

    this.elementControlClass = this.getClass('element', 'control');
    this.gridControlClass = this.getClass('grid', 'control');

    this.elementGroupClass = this.getClass('element', 'group');
    this.gridGroupClass = this.getClass('grid', 'group');

    this.elementOptionClass = this.getClass('element', 'option');
  }

  ngOnChanges(changes: SimpleChanges) {
    // super.onValueChange(changes);
    super.onControlValueChanges(changes);

    if (changes['model']) {
      this.type = SyndesisFormComponent.getFormControlType(this.model);
    }
  }
}
