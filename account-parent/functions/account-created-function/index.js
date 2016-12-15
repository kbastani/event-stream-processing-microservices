// Lambda Function Handler (Node.js)
exports.handler = (event, context, callback) => {
  accountCreated(event, callback);
};

// Link traversals
var links = {
  account: '$._links.self.href'
};

// DSL for creating a resource flow
var flow = {
  tasks: [],
  callback: {},
  steps: {
    then: function(step) {
      flow.tasks.push(step);
      return flow.steps;
    },
    start: function(traversal, callback) {
      flow.callback = callback;
      flow.next({
        traversal: traversal
      });
    }
  },
  next: function(context) {
    flow.tasks.shift()(context);
  }
};

// Handle the account created event
function accountCreated(event, callback) {
  // Loads the traverson client module
  var traverson = require('traverson');
  var JsonHalAdapter = require('traverson-hal');
  traverson.registerMediaType(JsonHalAdapter.mediaType, JsonHalAdapter);

  // Get the account resource href from event
  var accountUrl = event._links.account.href;

  // Apply the pending status to the attached account
  var traversal = traverson.from(accountUrl)
    .json()
    .withRequestOptions({
      headers: {
        "Accept": 'application/hal+json',
        "Content-Type": 'application/hal+json'
      }
    });

  // Execute account pending workflow
  flow.steps
    .then(getAccount)
    .then(setPending)
    .start(traversal, callback);
}

// Gets the account and sends it to the next step
function getAccount(context) {
  console.log("Getting account...");

  // Fetch account resource
  var fetchResource = function(error, account, traversal) {
    if (error) return done(error);

    if (account.status != "ACCOUNT_CREATED") {
      done("Pending account failed, account state invalid: " +
        account.status);
    } else {
      // Trigger the setPending step
      flow.next({
        account: account,
        traversal: traversal
      });
    }
  };

  // Follow the account resource
  context.traversal
    .follow(links.account)
    .getResource(fetchResource);
}

// Sets the account status to pending for the next step
function setPending(context) {
  console.log("Updating account...");

  var updateAccount = function(error, account, traversal) {
    if (error) return done(error);

    if (account.status == "ACCOUNT_PENDING") {
      // Complete the flow
      flow.callback(null, account);
    } else {
      done("Account update failed: status could not be updated to pending");
    }
  };

  // Set account to pending
  context.account.status = "ACCOUNT_PENDING";

  // Update the account resource
  context.traversal
    .continue()
    .put(context.account, updateAccount);
}

// Something went wrong, notify the callback handler
function done(error) {
  flow.callback(error);
}
