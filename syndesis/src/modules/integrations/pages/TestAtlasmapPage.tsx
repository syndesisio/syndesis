import { DataMapperAdapter } from '@syndesis/atlasmap-adapter';
import * as React from 'react';

export default class TestAtlasmapPage extends React.Component {
  public render() {
    return (
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
  }
}
