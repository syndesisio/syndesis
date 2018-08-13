import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';
import { WizardProgressBarComponent } from '@syndesis/ui/common/wizard_progress_bar/wizard-progress-bar.component';

describe('WizardProgressBarComponent', () => {
  let component: WizardProgressBarComponent;
  let fixture: ComponentFixture<WizardProgressBarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [WizardProgressBarComponent],
      imports: [RouterTestingModule]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WizardProgressBarComponent);
    fixture.detectChanges();
    component = fixture.componentInstance;
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should feature as many steps as input step names', () => {
    component.steps = ['Step 1', 'Step 2', 'Step 3'];
    fixture.detectChanges();
    const stepNodes = fixture.debugElement.queryAll(By.css('.wizard-pf-step'));
    expect(stepNodes.length).toEqual(3);
  });

  it('should highlight the selected step', () => {
    component.steps = ['Step 1', 'Step 2', 'Step 3'];
    component.selectedStep = 2;
    fixture.detectChanges();
    const selectedNode = fixture.debugElement.query(
      By.css('.wizard-pf-step--2')
    );
    expect(selectedNode.classes['active']).toBeTruthy();
  });

  it('should feature one selected step at a time', () => {
    component.steps = ['Step 1', 'Step 2', 'Step 3'];
    component.selectedStep = 2;
    fixture.detectChanges();
    const stepNodes = fixture.debugElement.queryAll(
      By.css('.wizard-pf-step.active')
    );
    expect(stepNodes.length).toEqual(1);
  });
});
