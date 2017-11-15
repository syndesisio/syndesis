/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { SyndesisCommonModule } from '../../common/common.module';
import { TemplatesListComponent } from './list.component';

describe('TemplatesListComponent', () => {
  let component: TemplatesListComponent;
  let fixture: ComponentFixture<TemplatesListComponent>;

  beforeEach(
    async(() => {
      TestBed.configureTestingModule({
        imports: [
          SyndesisCommonModule.forRoot(),
          RouterTestingModule.withRoutes([])
        ],
        declarations: [TemplatesListComponent]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplatesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
