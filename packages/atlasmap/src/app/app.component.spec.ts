import { HttpClientModule } from '@angular/common/http';
import { TestBed, async } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { DataMapperModule } from '@atlasmap/atlasmap-data-mapper';
import { AppComponent } from './app.component';
import { DataMapperHostComponent } from './data-mapper-host.component';

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AppComponent, DataMapperHostComponent],
      imports: [
        BrowserModule,
        FormsModule,
        HttpClientModule,
        RouterModule.forRoot([]),
        DataMapperModule,
      ],
    }).compileComponents();
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });
});
