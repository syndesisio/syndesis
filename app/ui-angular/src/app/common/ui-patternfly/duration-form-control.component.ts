import { Component, Input, Output, OnInit, EventEmitter } from '@angular/core';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormLayoutService,
  DynamicFormControlLayout,
  DynamicFormControlModel,
  DynamicFormControlEvent
} from '@ng-dynamic-forms/core';

@Component({
  selector: 'syndesis-duration-control',
  templateUrl: './duration-form-control.component.html'
})
export class DurationFormControlComponent implements OnInit {
  elementControlClass: string;
  @Input() group: FormGroup;
  @Input() model: any;
  @Input() bindId: boolean;

  @Output() blur = new EventEmitter<any>();
  @Output() change = new EventEmitter<any>();
  @Output() focus = new EventEmitter<any>();

  baseValue: number;
  duration: { value: number; label: string };
  modelId: string;

  durations = [
    {
      value: 1,
      label: 'Milliseconds'
    },
    {
      value: 1000,
      label: 'Seconds'
    },
    {
      value: 60000,
      label: 'Minutes'
    },
    {
      value: 3600000,
      label: 'Hours'
    },
    {
      value: 86400000,
      label: 'Days'
    }
  ];

  constructor(private dynamicFormLayoutService: DynamicFormLayoutService) {
    // nothing to do
  }

  calculateDuration() {
    return this.baseValue * (this.duration ? this.duration.value : 1);
  }

  updateFormGroup() {
    const value = this.calculateDuration();
    this.model.valueUpdates.next(value);
    this.group.value[this.modelId] = value;
  }

  getEvent($event: any, type: string): DynamicFormControlEvent {
    return {
      $event,
      context: undefined,
      control: undefined,
      group: this.group,
      model: this.model,
      type
    } as DynamicFormControlEvent;
  }

  selectBlurTriggered($event) {
    this.updateFormGroup();
    this.blur.emit(this.getEvent($event, 'blur'));
  }

  selectChangeTriggered($event) {
    this.updateFormGroup();
    this.change.emit(this.getEvent($event, 'change'));
  }

  selectFocusTriggered($event) {
    this.updateFormGroup();
    this.focus.emit(this.getEvent($event, 'focus'));
  }

  inputBlurTriggered($event) {
    this.updateFormGroup();
    this.blur.emit(this.getEvent($event, 'blur'));
  }

  inputChangeTriggered($event) {
    this.updateFormGroup();
    this.change.emit(this.getEvent($event, 'change'));
  }

  inputFocusTriggered($event) {
    this.updateFormGroup();
    this.focus.emit(this.getEvent($event, 'focus'));
  }

  ngOnInit() {
    this.elementControlClass = this.getClass('element', 'control');
    this.modelId = this.model.id;
    const value = +this.group.value[this.modelId] || 0;
    if (!value) {
      return;
    }
    let duration;
    for (const _duration of this.durations) {
      if (value / _duration.value >= 1) {
        duration = _duration;
      } else {
        break;
      }
    }
    this.baseValue = value / duration.value;
    this.duration = duration;
  }

  private getClass(
    context: string,
    place: string,
    model: DynamicFormControlModel = this.model
  ) {
    const controlLayout = model.layout as DynamicFormControlLayout;
    return this.dynamicFormLayoutService.getClass(
      controlLayout,
      context,
      place
    );
  }
}
