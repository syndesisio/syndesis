/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ToolbarModule } from 'patternfly-ng';

import { ListToolbarComponent } from '@syndesis/ui/common/ui-patternfly/list-toolbar/list-toolbar.component';

describe('ListToolbarComponent', () => {
  let component: ListToolbarComponent<any>;
  let fixture: ComponentFixture<ListToolbarComponent<any>>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([]), ToolbarModule],
      declarations: [ListToolbarComponent]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ListToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
