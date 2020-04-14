import { Button, ButtonVariant } from '@patternfly/react-core';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { CiCdEditDialog, TagNameValidationError } from '../../../src';

const stories = storiesOf('Integration/CiCd/EditDialog', module);
stories.addDecorator(withKnobs);

stories
  .add('edit tag', () => (
    <CiCdDialogStory
      i18nTitle={text('Dialog Title', 'Edit Tag')}
      i18nDescription={text(
        'Dialog Description',
        'The following changes will be applied to all integrations.'
      )}
      i18nSaveButtonText={text('Save Button Text', 'Save')}
      i18nCancelButtonText={text('Cancel Button Text', 'Cancel')}
      i18nInputLabel={text('Input Label', 'Tag Name')}
      i18nNoNameError={text('No Name Error', 'Please enter a tag name.')}
      i18nNameInUseError={text(
        'Name in Use Error',
        'That tag name is already in use.'
      )}
      tagName={text('Tag Name', 'Staging')}
    />
  ))
  .add('add new tag', () => (
    <CiCdDialogStory
      i18nTitle={text('Dialog Title', 'Add New Tag')}
      i18nDescription={text(
        'Dialog Description',
        'The following changes will be applied to all integrations.'
      )}
      i18nSaveButtonText={text('Save Button Text', 'Save')}
      i18nCancelButtonText={text('Cancel Button Text', 'Cancel')}
      i18nInputLabel={text('Input Label', 'Tag Name')}
      i18nNoNameError={text('No Name Error', 'Please enter a tag name.')}
      i18nNameInUseError={text(
        'Name in Use Error',
        'That tag name is already in use.'
      )}
      tagName={text('Tag Name', '')}
    />
  ));

interface ICiCdDialogStoryProps {
  i18nTitle: string;
  i18nDescription: string;
  tagName: string;
  i18nInputLabel: string;
  i18nSaveButtonText: string;
  i18nCancelButtonText: string;
  i18nNoNameError: string;
  i18nNameInUseError: string;
}

export const CiCdDialogStory: React.FunctionComponent<ICiCdDialogStoryProps> = props => {
  const [nameValidationError, setNameValidationError] = React.useState(
    TagNameValidationError.NoErrors
  );
  const [showDialog, setShowDialog] = React.useState(true);

  const openDialog = () => {
    setShowDialog(true);
  };
  const closeDialog = () => {
    setShowDialog(false);
  };
  const handleNameValidation = (name: string) => {
    if (!name || name === '') {
      setNameValidationError(TagNameValidationError.NoName);
    } else if (name === 'UsedTag') {
      setNameValidationError(TagNameValidationError.NameInUse);
    } else {
      setNameValidationError(TagNameValidationError.NoErrors);
    }
  };
  return (
    <>
      {showDialog && (
        <CiCdEditDialog
          i18nTitle={props.i18nTitle}
          i18nDescription={props.i18nDescription}
          i18nCancelButtonText={props.i18nCancelButtonText}
          i18nInputLabel={props.i18nInputLabel}
          i18nSaveButtonText={props.i18nSaveButtonText}
          i18nNoNameError={props.i18nNoNameError}
          i18nNameInUseError={props.i18nNameInUseError}
          tagName={props.tagName}
          validationError={nameValidationError}
          onValidate={handleNameValidation}
          onSave={closeDialog}
          onHide={closeDialog}
        />
      )}
      <Button variant={ButtonVariant.primary} onClick={openDialog}>
        Open Dialog
      </Button>
    </>
  );
};
