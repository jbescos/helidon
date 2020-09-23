# Microprofile Cloud Proposal

## Proposal

The API integrates Helidon with the next different Cloud Function providers.
1. Google Cloud Functions
2. Microsoft Azure Functions
3. AWS Lambda Functions

The user needs to identify the function that he wants to deploy in the cloud. Normally this function is specified as a command line argument when the application is deployed.

Function classes are implementations of cloud provider interfaces (note these are not implementations of java.util.Function). These functions are instanced and executed by the cloud, so we need to start Helidon to make sure the user can make use of it.

The user will need to specify:
1. A Helidon function for the cloud. See the section 'Helidon Functions'
2. Annotate the function with @CloudFunction

## Helidon functions

There are the functions that the cloud will instantiate and invoke.

They have to start Helidon and prepare the user function. There is one Helidon function for each type of cloud interface.

### CommonCloudFunction<T>

This is an abstract class that all the other Helidon functions will extend. The purpose of this class is:

1. Start Helidon (Main.main(new String[0])) the first time the function is invoked.
2. Instance the user function from the annotated @CloudFunction taking care of dependencies. This refers to <T>.

### GoogleCloudHttpFunction extends CommonCloudFunction<HttpFunction> implements HttpFunction

This is the entry point for com.google.cloud.functions.HttpFunction.

This class is very simple and only delegates in the user function.

### GoogleCloudBackgroundFunction<T> extends CommonCloudFunction<BackgroundFunction<T>> implements RawBackgroundFunction

This is the entry point for com.google.cloud.functions.RawBackgroundFunction.

It receives the event in JSON and it will delegate to the user function having the JSON already mapped to an Object.

### AzureCloudFunction<I, O> extends CommonCloudFunction<AzureEmptyFunction>

Azure works different than Google and AWS. There is no entry point to specify, so we cannot specify a Helidon class.

The user will need to extend AzureCloudFunction class instead of annotating the function with @CloudFunction.

Then he can declare the Azure function on this extension, but Helidon is not initialized at this point. So he will need to:

1. Invoke the super method handleRequest in the Azure function method.
2. Implement the abstract method 'abstract O execute(I input, ExecutionContext context)'. Here is where Helidon is initialized.

### AWSLambdaRequestFunction<I, O> extends CommonCloudFunction<RequestHandler<I, O>> implements RequestHandler<I, O>

This is the entry point for com.amazonaws.services.lambda.runtime.RequestHandler.

This class is very simple and only delegates in the user function.

### AWSLambdaStreamFunction extends CommonCloudFunction<RequestStreamHandler> implements RequestStreamHandler

This is the entry point for com.amazonaws.services.lambda.runtime.RequestStreamHandler.

This class is very simple and only delegates in the user function.

## The user function

This contains the business logic the user wants to execute.

It must be consistent with the Helidon function. For example, if he uses GoogleCloudHttpFunction the user function must implement HttpFunction.