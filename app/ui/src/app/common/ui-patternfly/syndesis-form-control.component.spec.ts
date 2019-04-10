import {
  TestBed,
  async,
  inject,
  ComponentFixture
} from '@angular/core/testing';
import { DebugElement, SimpleChange } from '@angular/core';
import {
  ReactiveFormsModule,
  FormGroup,
  FormControl,
  FormsModule
} from '@angular/forms';
import { By } from '@angular/platform-browser';
import { TextMaskModule } from 'angular2-text-mask';
import {
  DynamicFormsCoreModule,
  DynamicFormService,
  DynamicCheckboxModel,
  DynamicCheckboxGroupModel,
  DynamicDatePickerModel,
  DynamicEditorModel,
  DynamicFileUploadModel,
  DynamicFormArrayModel,
  DynamicFormControlModel,
  DynamicFormGroupModel,
  DynamicInputModel,
  DynamicRadioGroupModel,
  DynamicSelectModel,
  DynamicSliderModel,
  DynamicSwitchModel,
  DynamicTextAreaModel,
  DynamicTimePickerModel
} from '@ng-dynamic-forms/core';
import { TooltipModule } from 'ngx-bootstrap';
import { DurationFormControlComponent } from '@syndesis/ui/common/ui-patternfly/duration-form-control.component';
import {
  SyndesisFormControlType,
  SyndesisFormComponent
} from '@syndesis/ui/common/ui-patternfly/syndesis-form-control.component';

describe('SyndesisFormComponent test suite', () => {
  const formModel = [
    new DynamicCheckboxModel({ id: 'checkbox' }),
    new DynamicCheckboxGroupModel({ id: 'checkboxGroup', group: [] }),
    new DynamicDatePickerModel({ id: 'datepicker' }),
    new DynamicEditorModel({ id: 'editor' }),
    new DynamicFileUploadModel({ id: 'upload', url: '' }),
    new DynamicFormArrayModel({ id: 'formArray', groupFactory: () => [] }),
    new DynamicFormGroupModel({ id: 'formGroup', group: [] }),
    new DynamicInputModel({ id: 'input', name: 'foo', maxLength: 51 }),
    new DynamicRadioGroupModel({ id: 'radioGroup' }),
    new DynamicSelectModel({
      id: 'select',
      options: [{ value: 'One' }, { value: 'Two' }],
      value: 'One'
    }),
    new DynamicSliderModel({ id: 'slider' }),
    new DynamicSwitchModel({ id: 'switch' }),
    new DynamicTextAreaModel({ id: 'textarea' }),
    new DynamicTimePickerModel({ id: 'timepicker' })
  ];
  const testModel = formModel[7] as DynamicInputModel;
  let formGroup: FormGroup;
  let fixture: ComponentFixture<SyndesisFormComponent>;
  let component: SyndesisFormComponent;
  let debugElement: DebugElement;
  let testElement: DebugElement;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        ReactiveFormsModule,
        DynamicFormsCoreModule.forRoot(),
        TextMaskModule,
        TooltipModule.forRoot()
      ],
      declarations: [DurationFormControlComponent, SyndesisFormComponent]
    })
      .compileComponents()
      .then(() => {
        fixture = TestBed.createComponent(SyndesisFormComponent);

        component = fixture.componentInstance;
        debugElement = fixture.debugElement;
      });
  }));

  beforeEach(
    inject([DynamicFormService], (service: DynamicFormService) => {
      formGroup = service.createFormGroup(formModel);
      component.group = formGroup;
      component.model = testModel;
      component.ngOnChanges({
        group: new SimpleChange(null, component.group, true),
        model: new SimpleChange(null, component.model, true)
      });
      fixture.detectChanges();
      testElement = debugElement.query(By.css(`input[name='foo']`));
    })
  );

  xit('should initialize correctly', () => {
    expect(component.context).toBeNull();
    expect(component.control instanceof FormControl).toBe(true);
    expect(component.group instanceof FormGroup).toBe(true);
    expect(component.model instanceof DynamicFormControlModel).toBe(true);
    expect(component.hasErrorMessaging).toBe(false);
    expect(component.asBootstrapFormGroup).toBe(true);
    // expect(component.onControlValueChanges).toBeDefined();
    // expect(component.onModelDisabledUpdates).toBeDefined();
    // expect(component.onModelValueUpdates).toBeDefined();
    expect(component.blur).toBeDefined();
    expect(component.change).toBeDefined();
    expect(component.focus).toBeDefined();
    // expect(component.onChange).toBeDefined();
    expect(component.onFocus).toBeDefined();
    expect(component.isValid).toBe(true);
    expect(component.isInvalid).toBe(false);
    expect(component.showErrorMessages).toBe(false);
    expect(component.type).toEqual(SyndesisFormControlType.Input);
  });

  it('should have an input element', () => {
    expect(testElement instanceof DebugElement).toBe(true);
  });

  it('should listen to native focus events', () => {
    spyOn(component, 'onFocus');
    testElement.triggerEventHandler('focus', null);
    expect(component.onFocus).toHaveBeenCalled();
  });

  it('should listen to native blur events', () => {
    spyOn(component, 'onBlur');
    testElement.triggerEventHandler('blur', null);
    expect(component.onBlur).toHaveBeenCalled();
  });

  xit('should listen to native change event', () => {
    spyOn(component, 'change');
    testElement.triggerEventHandler('change', null);
    expect(component.change).toHaveBeenCalled();
  });

  // it('should update model value when control value changes', () => {
  //   spyOn(component, 'onControlValueChanges');
  //   component.control.setValue('test');
  //   expect(component.onControlValueChanges).toHaveBeenCalled();
  // });

  // it('should update control value when model value changes', () => {
  //   spyOn(component, 'onModelValueUpdates');
  //   testModel.valueUpdates.next('test');
  //   expect(component.onModelValueUpdates).toHaveBeenCalled();
  // });

  // it('should update control activation when model disabled property changes', () => {
  //   spyOn(component, 'onModelDisabledUpdates');
  //   testModel.disabledUpdates.next(true);
  //   expect(component.onModelDisabledUpdates).toHaveBeenCalled();
  // });

  /*
  TODO - figure out how to re-enable
  it('should determine correct form control type', () => {
    const testFn = SyndesisFormComponent.getFormControlType;
    expect(testFn(formModel[0])).toEqual(SyndesisFormControlType.Checkbox);
    expect(testFn(formModel[1])).toEqual(SyndesisFormControlType.Group);
    expect(testFn(formModel[2])).toBeNull();
    expect(testFn(formModel[3])).toBeNull();
    expect(testFn(formModel[4])).toBeNull();
    expect(testFn(formModel[5])).toEqual(SyndesisFormControlType.Array);
    expect(testFn(formModel[6])).toEqual(SyndesisFormControlType.Group);
    expect(testFn(formModel[7])).toEqual(SyndesisFormControlType.Input);
    expect(testFn(formModel[8])).toEqual(SyndesisFormControlType.RadioGroup);
    expect(testFn(formModel[9])).toEqual(SyndesisFormControlType.Select);
    expect(testFn(formModel[10])).toBeNull();
    expect(testFn(formModel[11])).toBeNull();
    expect(testFn(formModel[12])).toEqual(SyndesisFormControlType.TextArea);
    expect(testFn(formModel[13])).toBeNull();
  });
  */
});
