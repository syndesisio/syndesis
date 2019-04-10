import { EditableComponent } from '@syndesis/ui/common/editable/editable.component';

describe('EditableComponent', () => {
  class MyEditableComponent extends EditableComponent<string> {}

  const VALUE1 = 'value1';
  const VALUE2 = 'value2';
  const ERROR = 'error';
  let component;

  beforeEach(() => {
    component = new MyEditableComponent();
    component.value = VALUE1;
  });

  describe('submit', () => {
    it('sets error message', done => {
      spyOn(component, 'validate').and.returnValue(Promise.resolve(ERROR));
      component.submit(VALUE2).then(() => {
        expect(component.errorMessage).toBe(ERROR);
        done();
      });
    });

    it('saves value when valid', done => {
      spyOn(component, 'validate').and.returnValue(Promise.resolve(null));
      const spy = spyOn(component, 'save');
      component.submit(VALUE2).then(() => {
        expect(spy).toHaveBeenCalledWith(VALUE2);
        done();
      });
    });

    it('does not save value when invalid', done => {
      spyOn(component, 'validate').and.returnValue(Promise.resolve(ERROR));
      const spy = spyOn(component, 'save');
      component.submit(VALUE2).then(() => {
        expect(spy).not.toHaveBeenCalled();
        done();
      });
    });
  });

  describe('validate', () => {
    it('returns Promise that resolves to null when validation function is not set', done => {
      component.validate(VALUE2).then(result => {
        expect(result).toBe(null);
        done();
      });
    });

    it('returns Promise that resolves to value returned by validation function', done => {
      component.validationFn = value => ERROR;
      component.validate(VALUE2).then(result => {
        expect(result).toBe(ERROR);
        done();
      });
    });
  });

  describe('save', () => {
    it('sets value', () => {
      component.save(VALUE2);
      expect(component.value).toBe(VALUE2);
    });

    it('emits value', () => {
      spyOn(component.onSave, 'emit');
      component.save(VALUE2);
      expect(component.onSave.emit).toHaveBeenCalledWith(VALUE2);
    });

    it('turns editing mode off', () => {
      component.editing = true;
      component.save(VALUE2);
      expect(component.editing).toBe(false);
    });
  });

  describe('cancel', () => {
    it('clears error message', () => {
      component.errorMessage = 'error message';
      component.cancel();
      expect(component.errorMessage).toBe(null);
    });

    it('turns editing mode off', () => {
      component.editing = true;
      component.cancel();
      expect(component.editing).toBe(false);
    });
  });
});
