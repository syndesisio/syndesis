import { ApiDefinition } from 'apicurio-design-studio';

export type OpenApiUploaderValue = File | string | ApiDefinition;

export enum OpenApiUploaderValueType {
  File = 'file',
  Url = 'url',
  Spec = 'spec'
}

export interface OpenApiUploadSpecification {
  type: OpenApiUploaderValueType;
  spec: OpenApiUploaderValue;
}
