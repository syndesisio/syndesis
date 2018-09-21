import { OpenApiModule } from './openapi.module';

describe('OpenApiModule', () => {
  let openapiModule: OpenApiModule;

  beforeEach(() => {
    openapiModule = new OpenApiModule();
  });

  it('should create an instance', () => {
    expect(openapiModule).toBeTruthy();
  });
});
