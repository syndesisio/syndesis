import * as React from 'react';
import { render } from 'react-testing-library';
import { DataMapperAdapter, DocumentType, InspectionType } from '../src';

export default describe('DataMapperAdapter', () => {
  const testComponent = (
    <DataMapperAdapter
      documentId={'document-id'}
      inputName={'Input Name'}
      inputDescription={'Input description'}
      inputDocumentType={DocumentType.JSON}
      inputInspectionType={InspectionType.SCHEMA}
      inputDataShape={
        '{"type":"object","$schema":"http://json-schema.org/schema#","title":"create_lead_OUT","properties":{"first_name":{"type":"string","required":true},"last_name":{"type":"string","required":true},"company":{"type":"string","required":true},"lead_source":{"type":"string","required":true}}}'
      }
      outputName={'Output Name'}
      outputDescription={'Output description'}
      outputDocumentType={DocumentType.JSON}
      outputInspectionType={InspectionType.SCHEMA}
      outputDataShape={
        '{"type":"object","$schema":"http://json-schema.org/schema#","title":"add_lead_IN","properties":{"first_and_last_name":{"type":"string","required":true},"company":{"type":"string","required":true},"phone":{"type":"string","required":true},"email":{"type":"string","required":true},"lead_source":{"type":"string","required":true},"lead_status":{"type":"string","required":true},"rating":{"type":"string","required":true}}}'
      }
      onMappings={() => false}
    />
  );

  it('Should render', () => {
    const { container } = render(testComponent);
    expect(container).toBeDefined();
  });
});
