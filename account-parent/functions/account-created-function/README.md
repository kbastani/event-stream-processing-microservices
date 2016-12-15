# Account Created Function

Node.js source code and deployment scripts for an _AWS Lambda_ function. This function is referenced in an [_Event Stream Processing Microservices_](https://github.com/kbastani/event-stream-processing-microservices/tree/master) example project as a git sub-module.

The lambda function is triggered from events sourced from the [account-worker](https://github.com/kbastani/event-stream-processing-microservices/tree/master/account-parent) application. The lambda function uses an _Amazon API Gateway_ trigger that is invoked from a _Spring Cloud Stream_ application.

**Function Details**:

This lambda function responds to an [AccountEvent](https://github.com/kbastani/event-stream-processing-microservices/blob/master/account-parent/account-web/src/main/java/demo/event/AccountEvent.java) that applies a state change to an [Account](https://github.com/kbastani/event-stream-processing-microservices/blob/master/account-parent/account-web/src/main/java/demo/account/Account.java) resource.

- **context**: [accounts](https://github.com/kbastani/event-stream-processing-microservices/tree/master/account-parent)
- **function**: account-created
- **source_state**: `ACCOUNT_CREATED`
- **target_state**: `ACCOUNT_PENDING`
- **event_type**: `ACCOUNT_CREATED`
- **event_format**: [application/hal+json](http://stateless.co/hal_specification.html)

**Description**:

This event handler is triggered in response to an _Account_ resource transitioning from an `ACCOUNT_CREATED` status to an `ACCOUNT_PENDING` status. The function should apply the `ACCOUNT_PENDING` status to the _Account_ API resource that is linked within the payload of the supplied _AccountEvent_ resource.

This AWS lambda function is a backing service to the [account-worker](https://github.com/kbastani/event-stream-processing-microservices/tree/master/account-parent/account-worker) application. A service binding should inject the _AWS Lambda_ resource address and credentials into the _account-worker_ container as environment variables.

![Account microservice](http://i.imgur.com/WZTR4lQ.png)

The lambda function uses a Node.js module named [Traverson](https://github.com/basti1302/traverson), which is a REST client implementation for [HATEOAS](https://en.wikipedia.org/wiki/HATEOAS). The reference architecture uses [Spring HATEOAS](http://projects.spring.io/spring-hateoas/) to describe REST API resources with embedded links. The links embedded in each API resource allows the code in the lambda function to be completely stateless.

The lambda function only needs to import the _Traverson_ dependency to interact with API resources managed by the [account-web](https://github.com/kbastani/event-stream-processing-microservices/tree/master/account-parent/account-web) microservice.

```javascript
// Lambda Function Handler (Node.js)
exports.handler = (event, context, callback) => {
  // Loads the traverson client module
  var traverson = require('traverson');

  // Apply the confirm command to the Account resource
  traverson
    .from(event._links.account.href)
    .follow('$._links.self.href',
      '$._links.commands.href',
      '$._links.confirm.href')
    .get(function(error, response) {
      // Return the account result
      callback(error, response);
    });
};
```

## Deployment

A shell script is provided in `./deploy.sh`. This script uses the AWS CLI to create a deployment package using an [AWS CloudFormation Template](https://aws.amazon.com/cloudformation/aws-cloudformation-templates/) defined in `./account-created.yaml`. To deploy the lambda function, you must have the AWS CLI installed and configured to an IAM user with sufficient permissions to execute the script.

The script will create a deployment package for the lambda function and upload it as a zip file to an S3 bucket. The bucket name is an argument to the the deployment script.
