import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ViewEditContent } from '../../../src';

const stories = storiesOf('Data/Virtualizations/Views/ViewEditContent', module);

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
    <ViewEditContent
      viewDdl={viewDdl}
      i18nCancelLabel={'Cancel'}
      i18nDescription={'Edit, validate and save the View Definition'}
      i18nSaveLabel={'Save'}
      i18nTitle={'View Definition'}
      i18nValidateLabel={'Validate'}
      isValid={true}
      isWorking={false}
      sourceTableInfos={sourceTables}
      onCancel={action('cancel')}
      onValidate={action('validate')}
      onSave={action('save')}
    />
  );
});
