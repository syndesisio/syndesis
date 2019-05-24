import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ViewEditContent } from '../../../src';

const stories = storiesOf('Data/Virtualizations/Views/ViewEditContent', module);

const viewDdl =
  'CREATE VIEW PgCustomer_account (\n\tRowId long,\n\taccount_id integer,\n\tssn string,\n\tstatus string,\n\ttype string,\n\tdateopened timestamp,\n\tdateclosed timestamp,\n\tPRIMARY KEY(RowId)\n)\nAS\nSELECT ROW_NUMBER() OVER (ORDER BY account_id), account_id, ssn, status, type, dateopened, dateclosed FROM pgcustomerschemamodel.account;';

stories.add('render', () => {
  return (
    <ViewEditContent
      viewDdl={viewDdl}
      i18nCancelLabel={'Cancel'}
      i18nSaveLabel={'Save'}
      i18nValidateLabel={'Validate'}
      isValid={true}
      isWorking={false}
      onCancel={action('cancel')}
      onValidate={action('validate')}
      onSave={action('save')}
    />
  );
});
