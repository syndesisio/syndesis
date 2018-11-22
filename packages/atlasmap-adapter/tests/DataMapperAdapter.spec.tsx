import * as React from 'react';
import { render } from 'react-testing-library';
import { DataMapperAdapter } from '../src';

export default describe('DataMapperAdapter', () => {
  const testComponent = (
    <DataMapperAdapter
      inputId={'input-id'}
      inputName={'Input Name'}
      inputDescription={'Input description'}
      inputDocumentType={'JSON'}
      inputInspectionType={'SCHEMA'}
      inputDataShape={
        '{"type":"object","$schema":"http://json-schema.org/schema#","title":"create_lead_OUT","properties":{"first_name":{"type":"string","required":true},"last_name":{"type":"string","required":true},"company":{"type":"string","required":true},"lead_source":{"type":"string","required":true}}}'
      }
      outputId={'output-id'}
      outputName={'Output Name'}
      outputDescription={'Output description'}
      outputDocumentType={'JSON'}
      outputInspectionType={'SCHEMA'}
      outputDataShape={
        '{"type":"object","$schema":"http://json-schema.org/schema#","title":"add_lead_IN","properties":{"first_and_last_name":{"type":"string","required":true},"company":{"type":"string","required":true},"phone":{"type":"string","required":true},"email":{"type":"string","required":true},"lead_source":{"type":"string","required":true},"lead_status":{"type":"string","required":true},"rating":{"type":"string","required":true}}}'
      }
    />
  );

  it('Should render', () => {
    const { container } = render(testComponent);
    expect(container).toBeDefined();
  });
});
