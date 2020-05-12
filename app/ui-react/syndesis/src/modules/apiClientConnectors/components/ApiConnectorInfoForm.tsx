import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
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

export interface IApiConnectorInfoFormChildrenProps {
  connectorName?: string;
  /**
   * the form (embedded in the right UI elements)
   */
  fields: JSX.Element;

  icon?: string | undefined;

  /**
   * true if the form is being submitted.
   * Used to enable/disable the submit button.
   */
  isSubmitting: boolean;

  /**
   * `true` if an image is being uploaded.
   * Used to enable/disable the submit button.
   */
  isUploadingImage: boolean;

  /**
   * The callback for when an icon file was selected from the file system.
   * @param event the event whose target contains the file being uploaded
   */
  onUploadImage: (event: any) => void;

  /**
   * The callback fired when submitting the form.
   * @param e the changed properties
   * @param actions used to set isSubmitting on the form
   */
  handleSubmit: (e?: any) => void;

  /**
   * the callback to trigger to submit the form.
   */
  submitForm(): any;
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
   * The callback fired when submitting the form.
   * @param e the changed properties
   * @param actions used to set isSubmitting on the form
   */
  handleSubmit: (e: IConnectorValues, actions?: any) => void;

  /**
   * the render prop that will receive the ready-to-be-rendered form and some
   * helpers.
   *
   * @see [onSubmit]{@link IApiConnectorInfoFormChildrenProps#submitForm}
   */
  children(props: IApiConnectorInfoFormChildrenProps): any;
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
      {({
          fields,
          handleSubmit,
          isSubmitting,
          submitForm
      }) => {
        const connectorName = props.name;
        return props.children({
          connectorName,
          fields,
          handleSubmit,
          icon,
          isUploadingImage,
          isSubmitting,
          onUploadImage,
          submitForm
        });
      }}
    </AutoForm>
  );
};
