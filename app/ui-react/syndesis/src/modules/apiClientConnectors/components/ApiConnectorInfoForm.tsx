import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { ApiConnectorDetailsForm } from '@syndesis/ui';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';

export interface IFormValues {
  name: string;
  description: string;
  host: string;
  basePath: string;
}

export interface IConnectorValues extends IFormValues {
  icon?: string;
}

export interface IApiConnectorInfoFormProps {
  name?: string;
  description?: string;
  host?: string;
  basePath?: string;

  /**
   * The connector icon.
   */
  apiConnectorIcon?: string;

  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;

  /**
   * The callback fired when submitting the form.
   * @param e the changed properties
   * @param actions used to set isSubmitting on the form
   */
  handleSubmit: (e: IConnectorValues, actions?: any) => void;

  children: (props: {
    isSubmitting: boolean;
    isUploadingImage: boolean;
    submitForm: () => void;
  }) => React.ReactNode;
}

export const ApiConnectorInfoForm: React.FunctionComponent<
  IApiConnectorInfoFormProps
> = props => {
  const { pushNotification } = useContext(UIContext);
  const { t } = useTranslation(['apiClientConnectors', 'shared']);
  const [icon, setIcon] = React.useState<string | undefined>(
    props.apiConnectorIcon
  );
  const [isUploadingImage, setIsUploadingImage] = React.useState<boolean>(
    false
  );

  // tslint:disable: object-literal-sort-keys
  const formDefinition = {
    name: {
      defaultValue: '',
      displayName: t('shared:Name'),
      required: true,
      type: 'string',
    },
    description: {
      defaultValue: '',
      displayName: t('shared:Description'),
      type: 'textarea',
    },
    host: {
      defaultValue: '',
      displayName: t('apiClientConnectors:Host'),
      type: 'string',
    },
    basePath: {
      defaultValue: '',
      displayName: t('apiClientConnectors:basePath'),
      type: 'string',
    },
  } as IFormDefinition;

  const onUploadImage = (event: React.ChangeEvent<HTMLInputElement>): void => {
    if (event.target.files && event.target.files.length === 1) {
      const imageFile = event.target.files[0];

      if (imageFile.type.startsWith('image')) {
        const reader = new FileReader();
        reader.onloadstart = () => {
          setIsUploadingImage(true);
        };
        reader.onloadend = () => {
          setIsUploadingImage(false);
        };
        reader.onload = () => {
          setIcon(reader.result as string);
        };
        reader.readAsDataURL(imageFile);
      } else {
        event.target.value = '';
        event.target.files = FileList[0];
        setIcon(undefined);
        pushNotification(t('invalidImageFileUpload'), 'info');
      }
    }
  };

  const onSave = (values: IFormValues, actions: any) => {
    return props.handleSubmit(
      {
        ...values,
        icon,
      },
      actions
    );
  };

  return (
    <AutoForm<IFormValues>
      i18nRequiredProperty={t('shared:requiredFieldMessage')}
      definition={formDefinition}
      initialValue={{
        name: props.name || '',
        description: props.description || '',
        host: props.host || '',
        basePath: props.basePath || '',
      }}
      onSave={onSave}
    >
      {({ fields, handleSubmit, isSubmitting, submitForm }) => (
        <>
          <ApiConnectorDetailsForm
            apiConnectorIcon={icon}
            apiConnectorName={props.name}
            i18nIconLabel={t('ConnectorIcon')}
            handleSubmit={handleSubmit}
            onUploadImage={onUploadImage}
            isEditing={props.isEditing}
            fields={fields}
            footer={props.children({
              isSubmitting,
              isUploadingImage,
              submitForm,
            })}
          />
        </>
      )}
    </AutoForm>
  );
};
