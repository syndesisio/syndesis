import { CancelAddStepComponent } from '@syndesis/ui/integration/edit-page/common/cancel-add-step.component';
import { ActivatedRoute, Router } from '@angular/router';

describe('CancelAddStepComponent', () => {
  let component, currentFlow, route, router;

  beforeEach(() => {
    currentFlow = jasmine.createSpyObj('currentFlow', [
      'getLastPosition',
      'getStep'
    ]);
    currentFlow.events = jasmine.createSpyObj('events', ['emit']);
    route = {
      paramMap: jasmine.createSpyObj('paramMap', ['subscribe'])
    };
    router = jasmine.createSpyObj('router', ['navigate']);
    component = new CancelAddStepComponent(currentFlow, route, router);
  });

  describe('ngOnInit', () => {
    it('should subscribe to route paramMap', () => {
      component.ngOnInit();
      expect(route.paramMap.subscribe).toHaveBeenCalled();
    });
  });

  describe('isIntermediateStep', () => {
    it('should return false when it is first step', () => {
      component.position = 0;
      const result = component.isIntermediateStep();
      expect(result).toBe(false);
    });

    it('should return false when it is last step', () => {
      component.position = 1;
      currentFlow.getLastPosition.and.returnValue(1);
      const result = component.isIntermediateStep();
      expect(result).toBe(false);
    });

    it('should return true when it is intermediate step', () => {
      component.position = 1;
      currentFlow.getLastPosition.and.returnValue(2);
      const result = component.isIntermediateStep();
      expect(result).toBe(true);
    });
  });

  describe('onClick', () => {
    it('should emit event', () => {
      component.position = 1;
      currentFlow.getStep.and.returnValue({});
      component.onClick();
      expect(currentFlow.events.emit).toHaveBeenCalled();
    });
  });
});
