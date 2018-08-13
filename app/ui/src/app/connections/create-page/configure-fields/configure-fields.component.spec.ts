import { RouterStateSnapshot } from '@angular/router';
import { FormGroup, FormControl } from '@angular/forms';
import { ConnectionsConfigureFieldsComponent } from '@syndesis/ui/connections/create-page/configure-fields/configure-fields.component';
import { CurrentConnectionService } from '@syndesis/ui/connections/create-page/current-connection';

describe('ConnectionsConfigureFieldsComponent', () => {
  let current;
  let modalService;
  let nextState;
  let component;

  beforeEach(() => {
    current = <CurrentConnectionService>{};
    modalService = jasmine.createSpyObj('modalService', ['show']);
    nextState = <RouterStateSnapshot>{};
    component = new ConnectionsConfigureFieldsComponent(
      current,
      modalService,
      null,
      null
    );
    component.formGroup = {};
  });

  describe('canDeactivate', () => {
    it('should return true when user cancels wizard', () => {
      nextState.url = '/connections/create/cancel';
      const canDeactivate = component.canDeactivate(nextState);
      expect(canDeactivate).toBe(true);
    });

    it('should return true when user navigates back to Connection Basics page', () => {
      nextState.url = '/connections/create/connection-basics';
      const canDeactivate = component.canDeactivate(nextState);
      expect(canDeactivate).toBe(true);
    });

    it('should return true when user navigates forward to Review page', () => {
      nextState.url = '/connections/create/review';
      const canDeactivate = component.canDeactivate(nextState);
      expect(canDeactivate).toBe(true);
    });

    it('should return true when user confirms he wants to leave wizard', done => {
      modalService.show.and.returnValue(Promise.resolve({ result: true }));
      component.canDeactivate(nextState).then(canDeactivate => {
        expect(canDeactivate).toBe(true);
        done();
      });
    });

    it('should return false when user does not confirm he wants to leave wizard', done => {
      modalService.show.and.returnValue(Promise.resolve({ result: false }));
      component.canDeactivate(nextState).then(canDeactivate => {
        expect(canDeactivate).toBe(false);
        done();
      });
    });

    it('should return false when user clicked next and form is invalid', () => {
      spyOn(component, 'clickedNextButFormInvalid').and.returnValue(true);
      spyOn(component, 'touchFormFields');
      const canDeactivate = component.canDeactivate(nextState);
      expect(component.touchFormFields).toHaveBeenCalled();
      expect(canDeactivate).toBe(false);
    });
  });

  describe('clickedNextButFormInvalid', () => {
    it('should return false when user did not click next', () => {
      nextState.url = '/connections/create/cancel';
      const canDeactivate = component.clickedNextButFormInvalid(nextState);
      expect(canDeactivate).toBe(false);
    });

    it('should return false when user clicked next and formGroup is valid', () => {
      nextState.url = '/connections/create/review';
      component.formGroup.invalid = false;
      const canDeactivate = component.clickedNextButFormInvalid(nextState);
      expect(canDeactivate).toBe(false);
    });

    it('should return true when user clicked next and formGroup is invalid', () => {
      nextState.url = '/connections/create/review';
      component.formGroup.invalid = true;
      const canDeactivate = component.clickedNextButFormInvalid(nextState);
      expect(canDeactivate).toBe(true);
    });
  });

  describe('touchFormFields', () => {
    it('should mark formGroup controls as touched', () => {
      component.formGroup = new FormGroup({
        a: new FormControl('a'),
        b: new FormControl('b')
      });
      assertControlsTouched(false);
      component.touchFormFields();
      assertControlsTouched(true);
    });

    function assertControlsTouched(touched: boolean) {
      Object.keys(component.formGroup.controls).forEach(key => {
        const control = component.formGroup.get(key);
        expect(control.touched).toBe(touched);
      });
    }
  });
});
