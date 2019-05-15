import * as React from 'react';
import { render } from 'react-testing-library';
import { DataMapperAdapter, IDocument } from '../src';

(window as any).MessageChannel = jest.fn().mockImplementation(() => {
  return {
    port1: {
      onmessage: jest.fn(),
      postMessage: jest.fn(),
    },
  };
});

const inputDocuments = [
  {
    description: 'Salesforce Contact',
    id: '-LeNqpl7vV_xBvRT7zVx',
    inspectionResult: '',
    inspectionSource:
      '{"type":"object","id":"urn:jsonschema:org:apache:camel:component:salesforce:dto:Contact","$schema":"http://json-schema.org/draft-04/schema#","title":"Contact","properties":{"Id":{"type":"string","required":true,"readonly":true,"description":"idLookup","title":"Contact ID"},"IsDeleted":{"type":"boolean","required":true,"readonly":true,"title":"Deleted"},"MasterRecordId":{"type":"string","readonly":true,"title":"Master Record ID"},"AccountId":{"type":"string","readonly":false,"title":"Account ID"},"LastName":{"type":"string","required":true,"readonly":false,"title":"Last Name"},"FirstName":{"type":"string","readonly":false,"title":"First Name"},"Salutation":{"type":"string","readonly":false,"title":"Salutation","enum":["Dr.","Mrs.","Mr.","Ms.","Prof."]},"Name":{"type":"string","required":true,"readonly":true,"title":"Full Name"},"OtherStreet":{"type":"string","readonly":false,"title":"Other Street"},"OtherCity":{"type":"string","readonly":false,"title":"Other City"},"OtherState":{"type":"string","readonly":false,"title":"Other State/Province"},"OtherPostalCode":{"type":"string","readonly":false,"title":"Other Zip/Postal Code"},"OtherCountry":{"type":"string","readonly":false,"title":"Other Country"},"OtherLatitude":{"type":"number","readonly":false,"title":"Other Latitude"},"OtherLongitude":{"type":"number","readonly":false,"title":"Other Longitude"},"OtherAddress":{"type":"object","id":"urn:jsonschema:org:apache:camel:component:salesforce:api:dto:Address","readonly":true,"title":"Mailing Address","properties":{"latitude":{"type":"number"},"longitude":{"type":"number"},"city":{"type":"string"},"country":{"type":"string"},"countryCode":{"type":"string"},"postalCode":{"type":"string"},"state":{"type":"string"},"stateCode":{"type":"string"},"street":{"type":"string"},"geocodeAccuracy":{"type":"string"}}},"MailingStreet":{"type":"string","readonly":false,"title":"Mailing Street"},"MailingCity":{"type":"string","readonly":false,"title":"Mailing City"},"MailingState":{"type":"string","readonly":false,"title":"Mailing State/Province"},"MailingPostalCode":{"type":"string","readonly":false,"title":"Mailing Zip/Postal Code"},"MailingCountry":{"type":"string","readonly":false,"title":"Mailing Country"},"MailingLatitude":{"type":"number","readonly":false,"title":"Mailing Latitude"},"MailingLongitude":{"type":"number","readonly":false,"title":"Mailing Longitude"},"MailingAddress":{"type":"object","id":"urn:jsonschema:org:apache:camel:component:salesforce:api:dto:Address","readonly":true,"title":"Mailing Address","properties":{"latitude":{"type":"number"},"longitude":{"type":"number"},"city":{"type":"string"},"country":{"type":"string"},"countryCode":{"type":"string"},"postalCode":{"type":"string"},"state":{"type":"string"},"stateCode":{"type":"string"},"street":{"type":"string"},"geocodeAccuracy":{"type":"string"}}},"Phone":{"type":"string","readonly":false,"title":"Business Phone"},"Fax":{"type":"string","readonly":false,"title":"Business Fax"},"MobilePhone":{"type":"string","readonly":false,"title":"Mobile Phone"},"HomePhone":{"type":"string","readonly":false,"title":"Home Phone"},"OtherPhone":{"type":"string","readonly":false,"title":"Other Phone"},"AssistantPhone":{"type":"string","readonly":false,"title":"Asst. Phone"},"ReportsToId":{"type":"string","readonly":false,"title":"Reports To ID"},"Email":{"type":"string","readonly":false,"description":"idLookup","title":"Email"},"Title":{"type":"string","readonly":false,"title":"Title"},"Department":{"type":"string","readonly":false,"title":"Department"},"AssistantName":{"type":"string","readonly":false,"title":"Assistant\'s Name"},"LeadSource":{"type":"string","readonly":false,"title":"Lead Source","enum":["Phone Inquiry","Purchased List","Partner Referral","Web","Other"]},"Birthdate":{"type":"string","readonly":false,"title":"Birthdate","format":"date"},"Description":{"type":"string","readonly":false,"title":"Contact Description"},"OwnerId":{"type":"string","required":true,"readonly":false,"title":"Owner ID"},"CreatedDate":{"type":"string","required":true,"readonly":true,"title":"Created Date","format":"date-time"},"CreatedById":{"type":"string","required":true,"readonly":true,"title":"Created By ID"},"LastModifiedDate":{"type":"string","required":true,"readonly":true,"title":"Last Modified Date","format":"date-time"},"LastModifiedById":{"type":"string","required":true,"readonly":true,"title":"Last Modified By ID"},"SystemModstamp":{"type":"string","required":true,"readonly":true,"title":"System Modstamp","format":"date-time"},"LastActivityDate":{"type":"string","readonly":true,"title":"Last Activity","format":"date"},"LastCURequestDate":{"type":"string","readonly":true,"title":"Last Stay-in-Touch Request Date","format":"date-time"},"LastCUUpdateDate":{"type":"string","readonly":true,"title":"Last Stay-in-Touch Save Date","format":"date-time"},"LastViewedDate":{"type":"string","readonly":true,"title":"Last Viewed Date","format":"date-time"},"LastReferencedDate":{"type":"string","readonly":true,"title":"Last Referenced Date","format":"date-time"},"EmailBouncedReason":{"type":"string","readonly":false,"title":"Email Bounced Reason"},"EmailBouncedDate":{"type":"string","readonly":false,"title":"Email Bounced Date","format":"date-time"},"IsEmailBounced":{"type":"boolean","required":true,"readonly":true,"title":"Is Email Bounced"},"PhotoUrl":{"type":"string","readonly":true,"title":"Photo URL"},"Jigsaw":{"type":"string","readonly":false,"title":"Data.com Key"},"JigsawContactId":{"type":"string","readonly":true,"title":"Jigsaw Contact ID"},"CleanStatus":{"type":"string","readonly":false,"title":"Clean Status","enum":["SelectMatch","Matched","Skipped","Inactive","Different","Pending","Acknowledged","NotFound"]},"Level__c":{"type":"string","readonly":false,"title":"Level","enum":["Secondary","Tertiary","Primary"]},"Languages__c":{"type":"string","readonly":false,"title":"Languages"},"TwitterScreenName__c":{"type":"string","readonly":false,"description":"unique,idLookup","title":"Twitter Screen Name"},"Twitter_Screen_Name_2__c":{"type":"string","readonly":false,"description":"unique,idLookup","title":"Twitter Screen Name 2"}}}',
    name: '1 - Salesforce Contact',
    showFields: false,
    documentType: 'JSON',
    inspectionType: 'SCHEMA',
  },
  {
    description: '',
    id: '-LemsRv4LdfKVFYuchmj',
    inspectionResult: '',
    inspectionSource:
      '{"$schema":"http://json-schema.org/schema#","title":"Template JSON Schema","type":"object","properties":{"message":{"description":"Identifier for the symbol message","type":"string"}}}',
    name: '2 - Template JSON Schema',
    showFields: false,
    documentType: 'JSON',
    inspectionType: 'SCHEMA',
  },
] as IDocument[];

const outputDocument = {
  description: "Parameters of Stored Procedure 'add_lead'",
  id: '-LeNqpl7vV_xBvRT7zVx',
  inspectionResult: '',
  inspectionSource:
    '{"type":"object","$schema":"http://json-schema.org/schema#","title":"add_lead_IN","properties":{"first_and_last_name":{"type":"string","required":true},"company":{"type":"string","required":true},"phone":{"type":"string","required":true},"email":{"type":"string","required":true},"lead_source":{"type":"string","required":true},"lead_status":{"type":"string","required":true},"rating":{"type":"string","required":true}}}',
  name: '2 - add_lead Parameter',
  showFields: true,
  documentType: 'JSON',
  inspectionType: 'SCHEMA',
} as IDocument;

export default describe('DataMapperAdapter', () => {
  const onMappings = jest.fn();
  const testComponent = (
    <DataMapperAdapter
      documentId={'document-id'}
      inputDocuments={inputDocuments}
      outputDocument={outputDocument}
      baseJavaInspectionServiceUrl={'/whatever'}
      baseJSONInspectionServiceUrl={'/whatever'}
      baseMappingServiceUrl={'/whatever'}
      baseXMLInspectionServiceUrl={'/whatever'}
      onMappings={onMappings}
    />
  );

  it('Should render', () => {
    const { container } = render(testComponent);
    expect((window as any).MessageChannel).toBeCalledTimes(1);
    expect(container).toBeDefined();
  });
});
