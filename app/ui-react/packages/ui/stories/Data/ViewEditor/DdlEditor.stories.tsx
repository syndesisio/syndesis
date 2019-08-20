import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { DdlEditor } from '../../../src';

const stories = storiesOf('Data/ViewEditor/DdlEditor', module);

const viewDdl =
  'CREATE VIEW PgCustomer_account (\n\tRowId long,\n\taccount_id integer,\n\tssn string,\n\tstatus string,\n\ttype string,\n\tdateopened timestamp,\n\tdateclosed timestamp,\n\tPRIMARY KEY(RowId)\n)\nAS\nSELECT ROW_NUMBER() OVER (ORDER BY account_id), account_id, ssn, status, type, dateopened, dateclosed FROM pgcustomerschemamodel.account;';

const sourceTables = [
  { 'columnNames': ['name', 'population', 'size'], // column names
    'name': 'countries' },                         // table name
  { 'columnNames': ['name','score', 'birthDate'],  
    'name': 'users' 
  }
];

stories.add('render', () => {
  return (
    <DdlEditor
      viewDdl={viewDdl}
      i18nSaveLabel={'Save'}
      i18nValidateLabel={'Validate'}
      isValid={true}
      isSaving={false}
      isValidating={false}
      sourceTableInfos={sourceTables}
      onValidate={action('validate')}
      onSave={action('save')}
    />
  );
});
