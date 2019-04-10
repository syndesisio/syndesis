# Data Mapper

Data Mapper is a data mapping step available in Syndesis, which leverages [AtlasMap](https://github.com/atlasmap/atlasmap/blob/master/README.md) under the cover. When you add a Data Mapper step in integration flow, Data Mapper UI opens up and show available data types to define mappings. Data Mapper consumes output data from all previous steps, and produce an input of next step. Source documents are the inputs into Data Mapper, which are the outputs from all previous steps. Target document is the output from Data Mapper, which is the input for the next step. Each document consists of fields, and the mappings are defined field by field. In other words the Data Mapper is a tool to define and execute a set of field mappings.

Eventually Data Mapper UI produces mapping definition file and store it as a part of Syndesis integration definition. This mapping definition is consumed as a part of Syndesis integration runtime.

While basic field type conversion is applied automatically during executing mapping, you can also add Transformations for each mapping. Transformations are a set of functions you can explicitly choose from a list and apply in UI where you need. 

Data Mapper consists of following 3 parts

1. [Data Mapper UI](#data-mapper-ui)
2. [Design Runtime](#design-runtime)
3. [camel-atlasmap component](#camel-atlasmap-component)

## Data Mapper UI
Data Mapper UI is an user interface to define data mappings. You can define mappings in the UI by selecting source and target field(s), choose a mapping type from **_Map(one-to-one)_**, **_Separate(one-to-many)_** and **_Combine(many-to-one)_**, and add **Transformation**s if needed. Data Mapper UI interacts with **Design Runtime** backend service behind the scene where needed.
Data Mapper UI is an Angular component which is [AtlasMap Data Mapper UI](https://github.com/atlasmap/atlasmap/blob/master/ui/README.md) behind the scene. [Data Mapper Host Component](https://github.com/syndesisio/syndesis/blob/master/app/ui/src/app/integration/edit-page/step-configure/data-mapper/data-mapper-host.component.ts) is responsible to integrate AtlasMap Data Mapper UI component into Syndesis UI.

## Design Runtime
Design Runtime is a backend REST service which provides API to be used by Data Mapper UI. As of Mar-20-2018, Data Mapper Design Runtime resides as a part of syndesis-server. It runs 4 endpoints implemented as AtlasMap services

1. [atlas-service](https://github.com/atlasmap/atlasmap/tree/master/runtime/service)
2. [atlas-java-service](https://github.com/atlasmap/atlasmap/tree/master/runtime/modules/java/service)
3. [atlas-json-service](https://github.com/atlasmap/atlasmap/tree/master/runtime/modules/json/service)
4. [atlas-xml-service](https://github.com/atlasmap/atlasmap/tree/master/runtime/modules/xml/service)

Currently 3 types of API are provided
1. [Inspection](#inspection)
2. [Validation](#validation)
3. [Transformation (FieldAction)](#transformation-fieldaction-)

### Inspection
Inspection service consumes some kinds of data type definition and produce an unified internal format, called Document. Document represents the data structure as a set of fields. Currently we have 5 types of inspection service

1. **Java**: consumes fully qualified class name
2. **JSON Schema**: consumes JSON Schema 
3. **JSON Instance**: consumes JSON instance document
4. **XML Schema**: consumes XML Schema
5. **XML Instance**: consumes XNL instance document

### Validation
Validation service validates if the mapping definition is valid. For example, when you put a Long => Integer mapping in UI, UI makes a request to validate the mappings, then validation service detects a posibility to get the value out of range on that mapping. UI then shows a warning that there is a range concern.

### Transformation (FieldAction)
Transformation is called FieldAction internally. Currently FieldAction service just returns a list of all available FieldAction details, including FieldAction name and supported field type. UI then shows them as a list of available Transformations where the field type matches with supported field type of the Transformation.

## camel-atlasmap component
[camel-atlasmap component](https://github.com/atlasmap/atlasmap/blob/master/camel/README.md) is an Apache Camel component to execute Data Mapper mappings. It consumes mapping definition file and a set of input data from Camel input messages, perform mappings according to the provided mapping definition, then produce an output and put it into Camel OUT message.

## Data Mapper and [DataShape](https://github.com/syndesisio/syndesis/blob/master/app/common/model/src/main/java/io/syndesis/common/model/DataShape.java)
Each Syndesis [Action](https://github.com/syndesisio/syndesis/blob/master/app/common/model/src/main/java/io/syndesis/common/model/action/Action.java) contains one Input DataShape and one Output DataShape.
DataShape holds data type metadata. Data Mapper consumes those DataShape to initialize Documents. The DataShape properties related to Data Mapper are

1. [kind](#datashape-kind)
2. [type](#datashape-type)
3. [specification](#datashape-specification)
4. [name](#datashape-name)
5. [description](#datashape-description)

### DataShape kind
The kind of DataShape represented by the enum [DataShapeKinds](https://github.com/syndesisio/syndesis/blob/master/app/common/model/src/main/java/io/syndesis/common/model/DataShapeKinds.java). There are 7 kinds

1. **java**: Data type is represented by Java class. DataShape `type` contains fully qualified class name. If DataShape `specification` contains inspection result, Data Mapper will skip to perform online Java inspection and just use what is held  as `specification`. In Syndesis, all the `java` kind DataShape must hold Java inspection result, so that the online inspection could be avoided.
2. **json-schema**: Data type is represented by JSON Schema. DataShape `specification` must contain JSON Schema document.
3. **json-instance**: Data type is represented by JSON instance. DataShape `specification` must contain JSON instance document.
4. **xml-schema**: Data type is represented by XML Schema. DataShape `specification` must contain XML Schema document.
5. **xml-instance**: Data type is represented by XML instance. DataShape `specification` must contain XML instance document.
6. **any**: Data type is not structured. For example, byte array or free format text. Data Mapper will ignore this DataShape if kind is `any`.
7. **none**: No data type. If input DataShape kind is `none`, that step doesn't read data. If output DataShape kind is `none`, that step doesn't modify data. Since in many case input message body is just transfered to output message body, output DataShape kind = `none` often means passthrough. Data Mapper will ignore the DataShape if kind is `none`.

### DataShape type
If DataShape `kind` is `java`, this property holds fully qualified class name. Otherwise this is ignored by Data Mapper.

### DataShape specification
If DataShape `kind` is `java`, this property holds Java inspection result. If DataShape `kind` is one of `json-schema`, `json-instance`, `xml-schema` or `xml-instance`, this property holds corresponding document.

### DataShape name
A human readable name for the data type. This is shown as a Document label in the Data Mapper UI, as well as in the data type indicators you can see in Syndesis integration flow view.

### DataShape description
An additional data type description to show. This is shown as a tooltip on a Document label in the Data Mapper UI.

## Message Map
In order to capture all the output messages from previous steps and let Data Mapper step consume those, Syndesis embeds [an internal Camel processor](https://github.com/syndesisio/syndesis/blob/master/app/integration/runtime/src/main/java/io/syndesis/integration/runtime/capture/OutMessageCaptureProcessor.java). This processor is invoked for each steps defined in Syndesis integration flow at runtime. It captures an output message of each steps and put it into the Message Map held as an Camel Exchange property. The Step ID is used as a key. Since Data Mapper use Step ID as a Document ID in the mapping definition file, it eventually allows Data Mapper to link between the data type defined in mapping definition file and actual message in the Message Map at runtime.
