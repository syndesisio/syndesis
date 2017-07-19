import {
    Component,
    ContentChildren,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    QueryList,
    SimpleChanges,
} from '@angular/core';
import { FormGroup } from '@angular/forms';
import {
    DynamicFormControlModel,
    DynamicFormArrayGroupModel,
    DynamicFormControlComponent,
    DynamicFormControlEvent,
    DynamicTemplateDirective,
    DYNAMIC_FORM_CONTROL_TYPE_ARRAY,
    DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX,
    DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX_GROUP,
    DYNAMIC_FORM_CONTROL_TYPE_GROUP,
    DYNAMIC_FORM_CONTROL_TYPE_INPUT,
    DYNAMIC_FORM_CONTROL_TYPE_RADIO_GROUP,
    DYNAMIC_FORM_CONTROL_TYPE_SELECT,
    DYNAMIC_FORM_CONTROL_TYPE_TEXTAREA,
} from '@ng2-dynamic-forms/core';

export const enum PatternflyFormControlType {

    Array = 1, //'ARRAY',
    Checkbox = 2, //'CHECKBOX',
    Group = 3, //'GROUP',
    Input = 4, //'INPUT',
    RadioGroup = 5, //'RADIO_GROUP',
    Select = 6, //'SELECT',
    TextArea = 7, //'TEXTAREA'
}

@Component({

    moduleId: module.id,
    selector: 'dynamic-form-patternfly-control',
    templateUrl: './dynamic-form-patternfly.component.html',
  /* tslint:disable no-unused-css*/
    styles: [`
:host >>> .tooltip-inner {
  min-width: 200px;
  word-wrap: break-word;"
    }`],
})
export class DynamicFormPatternflyComponent extends DynamicFormControlComponent implements OnChanges {

    @Input() asBootstrapFormGroup= true;
    @Input() bindId= true;
    @Input() context: DynamicFormArrayGroupModel = null;
    @Input() group: FormGroup;
    @Input() hasErrorMessaging= false;
    @Input() model: DynamicFormControlModel;
    @Input() nestedTemplates: QueryList<DynamicTemplateDirective>;

    @Output() blur: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();
    @Output() change: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();
    @Output() focus: EventEmitter<DynamicFormControlEvent> = new EventEmitter<DynamicFormControlEvent>();

    @ContentChildren(DynamicTemplateDirective) contentTemplates: QueryList<DynamicTemplateDirective>;

    type: PatternflyFormControlType | null;

    constructor() {
        super();
    }

    ngOnChanges(changes: SimpleChanges) {
        super.ngOnChanges(changes);

        if (changes['model']) {
            this.type = DynamicFormPatternflyComponent.getFormControlType(this.model);
        }
    }

    static getFormControlType(model: DynamicFormControlModel): PatternflyFormControlType | null {

        switch (model.type) {

            case DYNAMIC_FORM_CONTROL_TYPE_ARRAY:
                return PatternflyFormControlType.Array;

            case DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX:
                return PatternflyFormControlType.Checkbox;

            case DYNAMIC_FORM_CONTROL_TYPE_CHECKBOX_GROUP:
            case DYNAMIC_FORM_CONTROL_TYPE_GROUP:
                return PatternflyFormControlType.Group;

            case DYNAMIC_FORM_CONTROL_TYPE_INPUT:
                return PatternflyFormControlType.Input;

            case DYNAMIC_FORM_CONTROL_TYPE_RADIO_GROUP:
                return PatternflyFormControlType.RadioGroup;

            case DYNAMIC_FORM_CONTROL_TYPE_SELECT:
                return PatternflyFormControlType.Select;

            case DYNAMIC_FORM_CONTROL_TYPE_TEXTAREA:
                return PatternflyFormControlType.TextArea;

            default:
                return null;
        }
    }
}
