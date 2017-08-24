import { EditableComponent } from './editable.component';

describe('EditableComponent', () => {

  class MyEditableComponent extends EditableComponent {}

  const VALUE1 = 'value1';
  const VALUE2 = 'value2';
  let component;

  beforeEach(() => {
    component = new MyEditableComponent();
    component.value = VALUE1;
  });

  describe('startEditing', () => {
    it('sets value in temporary variable', () => {
      expect(component.tempValue).toBe(null);
      component.startEditing();
      expect(component.tempValue).toBe(component.value);
    });
  });

  describe('save', () => {
    it('sets temporary value in value variable', () => {
      component.tempValue = VALUE2;
      component.save();
      expect(component.value).toBe(VALUE2);
    });

    it('resets temporary value', () => {
      component.tempValue = VALUE2;
      component.save();
      expect(component.tempValue).toBe(null);
    });

    it('emits value', () => {
      spyOn(component.onSave, 'emit');
      component.tempValue = VALUE2;
      component.save();
      expect(component.onSave.emit).toHaveBeenCalledWith(VALUE2);
    });
  });

  describe('cancel', () => {
    it('resets temporary value', () => {
      component.tempValue = VALUE2;
      component.cancel();
      expect(component.tempValue).toBe(null);
    });
  });

  describe('editing', () => {
    it('is true when temporary variable is set', () => {
      component.tempValue = VALUE2;
      expect(component.editing).toBe(true);
    });

    it('is false when temporary variable is not set', () => {
      expect(component.editing).toBe(false);
    });
  });

});
