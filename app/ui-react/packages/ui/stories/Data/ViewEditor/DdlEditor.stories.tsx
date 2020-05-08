import { action } from '@storybook/addon-actions/register';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { DdlEditor } from '../../../src';

const stories = storiesOf('Data/ViewEditor/DdlEditor', module);

const viewDdl =
  'CREATE VIEW PgCustomer_account (\n\tRowId long,\n\taccount_id integer,\n\tssn string,\n\tstatus string,\n\ttype string,\n\tdateopened timestamp,\n\tdateclosed timestamp,\n\tPRIMARY KEY(RowId)\n)\nAS\nSELECT ROW_NUMBER() OVER (ORDER BY account_id), account_id, ssn, status, type, dateopened, dateclosed FROM pgcustomerschemamodel.account;';

const sourceTables = [
  {
    columnNames: ['name', 'population', 'size'], // column names
    name: 'countries',
  }, // table name
  { columnNames: ['name', 'score', 'birthDate'], name: 'users' },
];

const sourceInfo = [
  {
    name: 'PostgresDB',
    tables: [
      {
        name: 'contact',
        columns: [
          { name: 'first_name', datatype: 'string' },
          { name: 'last_name', datatype: 'string' },
          { name: 'company', datatype: 'string' },
          { name: 'lead_source', datatype: 'string' },
          { name: 'create_date', datatype: 'date' },
        ],
      },
      {
        name: 'todo',
        columns: [
          { name: 'id', datatype: 'integer' },
          { name: 'task', datatype: 'string' },
          { name: 'completed', datatype: 'integer' },
        ],
      },
      {
        name: 'winelist',
        columns: [
          { name: 'id', datatype: 'integer' },
          { name: 'wine', datatype: 'string' },
          { name: 'price', datatype: 'integer' },
          { name: 'year', datatype: 'integer' },
          { name: 'gws', datatype: 'integer' },
          { name: 'ci', datatype: 'string' },
          { name: 'nbj', datatype: 'integer' },
          { name: 'productcode', datatype: 'string' },
          { name: 'pricebookentryid', datatype: 'string' },
        ],
      },
    ],
  },
];

stories.add('render', () => {
  return (
    <DdlEditor
      viewDdl={viewDdl}
      i18nDdlTextPlaceholder={'ddlTextPlaceholder'}
      i18nDoneLabel={'Done'}
      i18nSaveLabel={'Save'}
      i18nTitle={'viewEditor'}
      i18nMetadataTitle={'Metadata Tree'}
      i18nLoading={'Loading...'}
      previewExpanded={true}
      i18nValidationResultsTitle={'validationResultsTitle'}
      showValidationMessage={true}
      isSaving={boolean('isSaving', false)}
      sourceTableInfos={sourceTables}
      sourceInfo={sourceInfo}
      onCloseValidationMessage={action('onCloseValidationMessage')}
      onFinish={action('done')}
      onSave={action('save')}
      setDirty={action('dirty')}
      validationResults={[]}
    />
  );
});
