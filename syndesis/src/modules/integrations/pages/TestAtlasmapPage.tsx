import {
  DataMapperAdapter,
  DocumentType,
  InspectionType,
} from '@syndesis/atlasmap-adapter';
import * as React from 'react';

export default class TestAtlasmapPage extends React.Component {
  public render() {
    const noop = () => true;
    return (
      <DataMapperAdapter
        documentId={'-LRMAUOoTSCTd3Eg7_bi'}
        inputName={'SQL Result'}
        inputDescription={'Result of SQL [SELECT * from TODO]'}
        inputDocumentType={DocumentType.JSON}
        inputInspectionType={InspectionType.SCHEMA}
        inputDataShape={
          '{"type":"object","$schema":"http://json-schema.org/schema#","title":"SQL_PARAM_OUT","properties":{"id":{"type":"integer","required":true},"task":{"type":"string","required":true},"completed":{"type":"integer","required":true}}}'
        }
        outputName={'add_lead Parameter'}
        outputDescription={'Parameters of Stored Procedure "add_lead"'}
        outputDocumentType={DocumentType.JSON}
        outputInspectionType={InspectionType.SCHEMA}
        outputDataShape={
          '{"type":"object","$schema":"http://json-schema.org/schema#","title":"add_lead_IN","properties":{"first_and_last_name":{"type":"string","required":true},"company":{"type":"string","required":true},"phone":{"type":"string","required":true},"email":{"type":"string","required":true},"lead_source":{"type":"string","required":true},"lead_status":{"type":"string","required":true},"rating":{"type":"string","required":true}}}'
        }
        mappings={
          '{"AtlasMapping":{"jsonType":"io.atlasmap.v2.AtlasMapping","dataSource":[{"jsonType":"io.atlasmap.json.v2.JsonDataSource","id":"-LRMAUOoTSCTd3Eg7_bi","uri":"atlas:json:-LRMAUOoTSCTd3Eg7_bi","dataSourceType":"SOURCE"},{"jsonType":"io.atlasmap.json.v2.JsonDataSource","id":"-LRMAUOoTSCTd3Eg7_bi","uri":"atlas:json:-LRMAUOoTSCTd3Eg7_bi","dataSourceType":"TARGET","template":null}],"mappings":{"mapping":[{"jsonType":"io.atlasmap.v2.Mapping","mappingType":"MAP","id":"mapping.294718","inputField":[{"jsonType":"io.atlasmap.json.v2.JsonField","name":"task","path":"/task","fieldType":"STRING","docId":"-LRMAUOoTSCTd3Eg7_bi","userCreated":false}],"outputField":[{"jsonType":"io.atlasmap.json.v2.JsonField","name":"lead_status","path":"/lead_status","fieldType":"STRING","docId":"-LRMAUOoTSCTd3Eg7_bi","userCreated":false}]}]},"name":"UI.460205","lookupTables":{"lookupTable":[]},"constants":{"constant":[]},"properties":{"property":[]}}}'
        }
        onMappings={noop}
      />
    );
  }
}
