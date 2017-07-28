import { IntegrationActionsComponent } from './actions.component';
import { TypeFactory, Integration } from '../../model';

describe('IntegrationActionsComponent', () => {

  let component: IntegrationActionsComponent;

  beforeEach(() => {
    component = new IntegrationActionsComponent();
    component.integration = TypeFactory.createIntegration();
  });

  describe('canSeeActions', () => {
    it('should see actions dropdown when integration is draft', () => {
      component.integration.currentStatus = 'Draft';
      expect(component.canSeeActions()).toBe(true);
    });

    it('should see actions dropdown when integration is pending', () => {
      component.integration.currentStatus = 'Pending';
      expect(component.canSeeActions()).toBe(true);
    });

    it('should see actions dropdown when integration is activated', () => {
      component.integration.currentStatus = 'Activated';
      expect(component.canSeeActions()).toBe(true);
    });

    it('should see actions dropdown when integration is deactivated', () => {
      component.integration.currentStatus = 'Deactivated';
      expect(component.canSeeActions()).toBe(true);
    });

    it('should not see actions dropdown when integration is deleted', () => {
      component.integration.currentStatus = 'Deleted';
      expect(component.canSeeActions()).toBe(false);
    });
  });

  describe('canActivate', () => {
    it('should not be able to activate draft integration', () => {
      component.integration.currentStatus = 'Draft';
      expect(component.canActivate()).toBe(false);
    });

    it('should not be able to activate pending integration', () => {
      component.integration.currentStatus = 'Pending';
      expect(component.canActivate()).toBe(false);
    });

    it('should not be able to activate activated integration', () => {
      component.integration.currentStatus = 'Activated';
      expect(component.canActivate()).toBe(false);
    });

    it('should be able to activate deactivated integration', () => {
      component.integration.currentStatus = 'Deactivated';
      expect(component.canActivate()).toBe(true);
    });

    it('should not be able to activate deleted integration', () => {
      component.integration.currentStatus = 'Deleted';
      expect(component.canActivate()).toBe(false);
    });
  });

  describe('canDectivate', () => {
    it('should not be able to deactivate draft integration', () => {
      component.integration.currentStatus = 'Draft';
      expect(component.canDeactivate()).toBe(false);
    });

    it('should not be able to deactivate pending integration', () => {
      component.integration.currentStatus = 'Pending';
      expect(component.canDeactivate()).toBe(false);
    });

    it('should be able to deactivate activated integration', () => {
      component.integration.currentStatus = 'Activated';
      expect(component.canDeactivate()).toBe(true);
    });

    it('should not be able to deactivate deactivated integration', () => {
      component.integration.currentStatus = 'Deactivated';
      expect(component.canDeactivate()).toBe(false);
    });

    it('should not be able to deactivate deleted integration', () => {
      component.integration.currentStatus = 'Deleted';
      expect(component.canDeactivate()).toBe(false);
    });
  });

  describe('canDelete', () => {
    it('should be able to delete draft integration', () => {
      component.integration.currentStatus = 'Draft';
      expect(component.canDelete()).toBe(true);
    });

    it('should be able to delete pending integration', () => {
      component.integration.currentStatus = 'Pending';
      expect(component.canDelete()).toBe(true);
    });

    it('should be able to delete activated integration', () => {
      component.integration.currentStatus = 'Activated';
      expect(component.canDelete()).toBe(true);
    });

    it('should be able to delete deactivated integration', () => {
      component.integration.currentStatus = 'Deactivated';
      expect(component.canDelete()).toBe(true);
    });

    it('should not be able to delete deleted integration', () => {
      component.integration.currentStatus = 'Deleted';
      expect(component.canDelete()).toBe(false);
    });
  });

  describe('canEdit', () => {
    it('should be able to edit draft integration', () => {
      component.integration.currentStatus = 'Draft';
      expect(component.canEdit()).toBe(true);
    });

    it('should be able to edit pending integration', () => {
      component.integration.currentStatus = 'Pending';
      expect(component.canEdit()).toBe(true);
    });

    it('should be able to edit activated integration', () => {
      component.integration.currentStatus = 'Activated';
      expect(component.canEdit()).toBe(true);
    });

    it('should be able to edit deactivated integration', () => {
      component.integration.currentStatus = 'Deactivated';
      expect(component.canEdit()).toBe(true);
    });

    it('should not be able to edit deleted integration', () => {
      component.integration.currentStatus = 'Deleted';
      expect(component.canEdit()).toBe(false);
    });
  });

});
