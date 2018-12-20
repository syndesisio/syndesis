import {
  DataMapperAdapter,
  DocumentType,
  InspectionType,
} from '@syndesis/atlasmap-adapter';
import { Breadcrumb, Button } from 'patternfly-react';
import * as React from 'react';

export interface ITestAtlasmapPageState {
  mappings?: string;
}

export class TestAtlasmapPage extends React.Component<
  {},
  ITestAtlasmapPageState
> {
  public state: ITestAtlasmapPageState = {};

  constructor(props: {}) {
    super(props);
    this.onReset = this.onReset.bind(this);
    this.onMappings = this.onMappings.bind(this);
  }

  public onReset() {
    this.setState({
      mappings: undefined,
    });
  }

  public onMappings(mappings: string) {
    this.setState({
      mappings,
    });
  }

  public render() {
    return (
      <>
        <div className="container-fluid">
          <div className="toolbar-pf row">
            <div className="col-sm-12">
              <div className="toolbar-pf-actions">
                <div className="form-group">
                  <Breadcrumb style={{ marginBottom: 0 }}>
                    <Breadcrumb.Item>Home</Breadcrumb.Item>
                    <Breadcrumb.Item>Library</Breadcrumb.Item>
                    <Breadcrumb.Item active={true}>Data</Breadcrumb.Item>
                  </Breadcrumb>
                </div>
                <div className="toolbar-pf-action-right">
                  <div className="form-group" style={{ marginTop: '5px' }}>
                    <Button>Cancel</Button>
                    <Button
                      disabled={!this.state.mappings}
                      onClick={this.onReset}
                    >
                      Reset
                    </Button>
                    <Button bsStyle={'success'} disabled={!this.state.mappings}>
                      Save
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div
          style={{
            display: 'flex',
            flexFlow: 'column',
            height: 'calc(100% - 57px)',
          }}
        >
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
            mappings={this.state.mappings}
            onMappings={this.onMappings}
          />
        </div>
      </>
    );
  }
}
