import { RouterStateSnapshot } from '@angular/router';
import { ConnectionsConfigureFieldsComponent } from './configure-fields.component';
import { CurrentConnectionService } from '../current-connection';
import { ModalService } from '../../../common/modal/modal.service';

describe('ConnectionsReviewComponent', () => {

  let current, modalService, nextState, component;

  beforeEach(() => {
    current = <CurrentConnectionService>{};
    modalService = jasmine.createSpyObj('modalService', ['show']);
    nextState = <RouterStateSnapshot>{};
    component = new ConnectionsConfigureFieldsComponent(current, modalService);
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

    it('should return true when user confirms he wants to leave wizard', () => {
      modalService.show.and.returnValue(Promise.resolve(true));
      component.canDeactivate(nextState)
        .then(canDeactivate => {
          expect(canDeactivate).toBe(true);
        });
    });

    it('should return false when user does not confirm he wants to leave wizard', () => {
      modalService.show.and.returnValue(Promise.resolve(false));
      component.canDeactivate(nextState)
        .then(canDeactivate => {
          expect(canDeactivate).toBe(false);
        });
    });
  });

});
