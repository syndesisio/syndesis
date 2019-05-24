import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { ApiHttpService } from '@syndesis/ui/platform';
import { TestApiHttpService } from '@syndesis/ui/api/testing/test-api-http.service';

@NgModule({
  imports: [CommonModule, HttpClientModule],
  exports: [HttpClientModule],
  providers: [
    TestApiHttpService,
    {
      provide: ApiHttpService,
      useClass: TestApiHttpService
    }
  ]
})
export class TestApiModule {}
