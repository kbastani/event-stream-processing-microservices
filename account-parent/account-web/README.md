# Account Microservice: Web Role

The `account-web` module is a web application that produces a REST API that can be used by consumers to interact with and manage domain objects in the `Account` context. _Domain Events_ can be triggered directly over HTTP, and will also be produced in the response to actions that alter the state of the `Account` object. This web service also provides built-in hypermedia support for looking up the event logs on an aggregate domain object.

## Usage

The `account-web` application provides a hypermedia-driven REST API for managing accounts.

To create a new account, we can send an HTTP POST request to `/v1/accounts`.

    {
      "userId": 1,
      "accountNumber": "123456",
      "defaultAccount": true
    }

If the request was successful, a hypermedia response will be returned back.

    {
      "createdAt": 1481351048967,
      "lastModified": 1481351048967,
      "userId": 1,
      "accountNumber": "123456",
      "defaultAccount": true,
      "status": "ACCOUNT_CREATED",
      "_links": {
        "self": {
          "href": "http://localhost:54656/v1/accounts/1"
        },
        "events": {
          "href": "http://localhost:54656/v1/accounts/1/events"
        },
        "commands": {
          "href": "http://localhost:54656/v1/accounts/1/commands"
        }
      }
    }

The snippet above is the response that was returned after creating the new account. We can see the field `status` has a value of `ACCOUNT_CREATED`. Notice the embedded links in the response, which provide context for the resource. In this case, we have two links of interest: `events` and `commands`. Let's take a look at both of these resources, starting with `commands`.

The `commands` resource provides us with an additional set of hypermedia links for each command that can be applied to the `Account` resource.

_GET_ `/v1/accounts/1/commands`

    {
      "_links": {
        "confirm": {
          "href": "http://localhost:54656/v1/accounts/1/commands/confirm"
        },
        "activate": {
          "href": "http://localhost:54656/v1/accounts/1/commands/activate"
        },
        "suspend": {
          "href": "http://localhost:54656/v1/accounts/1/commands/suspend"
        },
        "archive": {
          "href": "http://localhost:54656/v1/accounts/1/commands/archive"
        }
      }
    }

In an event-driven architecture, the state of an object can only change in response to an event. The `account-worker` module will be monitoring for events and can execute commands against the resource that is the subject of an event.

Let's return back to the parent `Account` resource for these commands.

_GET_ `/v1/accounts/1`

    {
      "createdAt": 1481351048967,
      "lastModified": 1481351049385,
      "userId": 1,
      "accountNumber": "123456",
      "defaultAccount": true,
      "status": "ACCOUNT_ACTIVE",
      "_links": {
        "self": {
          "href": "http://localhost:54656/v1/accounts/1"
        },
        "events": {
          "href": "http://localhost:54656/v1/accounts/1/events"
        },
        "commands": {
          "href": "http://localhost:54656/v1/accounts/1/commands"
        }
      }
    }

Sending a new _GET_ request to the parent `Account` resource returns back a different the object with a different `status` value. The status value is now set to `ACCOUNT_ACTIVE`, which previously was `ACCOUNT_CREATED`. The cause for this is because we are also running the `account-worker` in parallel, which is reacting to domain events triggered by `account-web`.

During an account creation workflow, a domain event will be triggered and sent to the account stream, which the `account-worker` module is listening to. To understand exactly what has happened to this `Account` resource, we can trace the events that led to its current state. Sending a GET request to the attached hypermedia link named `events` returns back an event log.

_GET_ `/v1/accounts/1/events`

    {
      "_embedded": {
        "accountEventList": [
          {
            "createdAt": 1481351049002,
            "lastModified": 1481351049002,
            "type": "ACCOUNT_CREATED",
            "_links": {
              "self": {
                "href": "http://localhost:54656/v1/events/1"
              }
            }
          },
          {
            "createdAt": 1481351049318,
            "lastModified": 1481351049318,
            "type": "ACCOUNT_CONFIRMED",
            "_links": {
              "self": {
                "href": "http://localhost:54656/v1/events/2"
              }
            }
          },
          {
            "createdAt": 1481351049387,
            "lastModified": 1481351049387,
            "type": "ACCOUNT_ACTIVATED",
            "_links": {
              "self": {
                "href": "http://localhost:54656/v1/events/3"
              }
            }
          }
        ]
      },
      "_links": {
        "self": {
          "href": "http://localhost:54656/v1/accounts/1/events"
        },
        "account": {
          "href": "http://localhost:54656/v1/accounts/1"
        }
      }
    }

The response returned is an ordered collection of account events. The log describes the events that caused the `status` of the account resource to be set to the `ACCOUNT_ACTIVE` value. But how did the `account-worker` module drive the state changes of this account resource?

Source: _EventService.java_

    public AccountEvent createEvent(AccountEvent event) {
        // Save new event
        event = addEvent(event);
        Assert.notNull(event, "The event could not be appended to the account");

        // Append the account event to the stream
        accountStreamSource.output()
                .send(MessageBuilder
                        .withPayload(getAccountEventResource(event))
                        .build());

        return event;
    }

In the snippet above we see the creation of a new `AccountEvent` in the `EventService` class. Notice how the payload that is sent to the account stream is being constructed. The `getAccountEventResource` method will prepare the `AccountEvent` object as a hypermedia resource. This means that when the `account-worker` processes the event message that it will be able to use the embedded links to understand the full context of the event. The exact representation of the `AccountEvent` resource that will be sent is shown in the snippet below.

The body of the _AccountEvent_ message for creating an account:

    {
      "createdAt": 1481353397395,
      "lastModified": 1481353397395,
      "type": "ACCOUNT_CREATED",
      "_links": {
        "self": {
          "href": "http://localhost:54932/v1/events/1"
        },
        "account": {
          "href": "http://localhost:54932/v1/accounts/1"
        }
      }
    }

Here we see the `AccountEvent` resource that was sent to the account stream during the create account workflow. Notice how the body of the event message contains no fields describing the new `Account` resource. The full context for the event is only available as a graph of hypermedia links attached to the `AccountEvent` resource. Because of this, the event processor does not need to know anything about the location of resources. With this approach, we can drive the behavior and the state of the application using hypermedia.

The snippet below is an example of how the `account-worker` is able to traverse the graph of hypermedia links of an `AccountEvent` resource.

    // Traverse the hypermedia link for the attached account
    Traverson traverson = new Traverson(
            new URI(accountEvent.getLink("account").getHref()),
            MediaTypes.HAL_JSON
    );

    // Traverse the hypermedia link to retrieve the event log for the account
    AccountEvents events = traverson.follow("events")
            .toEntity(AccountEvents.class)
            .getBody();

Here we use a `Traverson` as a REST client instead of a `RestTemplate` to easily follow related hypermedia links. `Traverson` wraps a standard `RestTemplate` with support that makes it simple to process related hypermedia links. In this kind of architecture, caching is absolutely essential, since traversing hypermedia links is a sequential traversal.

The event processor has no knowledge about the structure of a REST API outside of the hypermedia links it discovers. Because of this, we may be required to dispatch more HTTP _GET_ requests than we normally would.

Now that we understand how the event processor can use hypermedia to discover the context of a domain event, let's now take a look at how it drives the state of domain resources.

    // Create a traverson for the root account
    Traverson traverson = new Traverson(
            new URI(event.getLink("account").getHref()),
            MediaTypes.HAL_JSON
    );

    // Get the account resource attached to the event
    Account account = traverson.follow("self")
            .toEntity(Account.class)
            .getBody();
    ...

Here we see how the `account-worker` drives the state of an `Account` resource by traversing hypermedia links attached to an `AccountEvent` resource it is processing. Here we start by traversing to the `Account` resource that is the subject of the current `AccountEvent`. The result of that request is below.

    {
      "createdAt": 1481353397350,
      "lastModified": 1481353397809,
      "userId": 1,
      "accountNumber": "123456",
      "defaultAccount": true,
      "status": "ACCOUNT_PENDING",
      "_links": {
        "self": {
          "href": "http://localhost:54932/v1/accounts/1"
        },
        "events": {
          "href": "http://localhost:54932/v1/accounts/1/events"
        },
        "commands": {
          "href": "http://localhost:54932/v1/accounts/1/commands"
        }
      }
    }

In this workflow we are wanting to transition the account from a pending state to a confirmed state. Since the `Account` resource has a hypermedia link relation for `commands`, we can use that link to find the list of possible operations that can be performed on an `Account` resource. Following the `commands` link returns the following result.

    {
      "_links": {
        "confirm": {
          "href": "http://localhost:54932/v1/accounts/1/commands/confirm"
        },
        "activate": {
          "href": "http://localhost:54932/v1/accounts/1/commands/activate"
        },
        "suspend": {
          "href": "http://localhost:54932/v1/accounts/1/commands/suspend"
        },
        "archive": {
          "href": "http://localhost:54932/v1/accounts/1/commands/archive"
        }
      }
    }

Now that we have the list of commands that can be applied to the resource, we can choose which one we need to transition the state of the `Account` from `ACCOUNT_PENDING` to `ACCOUNT_CONFIRMED`. To do this, all we need to do is to follow the `confirm` link, using the `Traverson` client, which is shown below.

    // Traverse to the confirm account command
    account = traverson.follow("commands")
            .follow("confirm")
            .toEntity(Account.class)
            .getBody();

    log.info("Account confirmed: " + account);

The response returned for the `confirm` command returns the updated state of the `Account` resource, shown below.

    {
      "createdAt": 1481353397350,
      "lastModified": 1481355618050,
      "userId": 1,
      "accountNumber": "123456",
      "defaultAccount": true,
      "status": "ACCOUNT_CONFIRMED",
      "_links": {
        "self": {
          "href": "http://localhost:54932/v1/accounts/1"
        },
        "events": {
          "href": "http://localhost:54932/v1/accounts/1/events"
        },
        "commands": {
          "href": "http://localhost:54932/v1/accounts/1/commands"
        }
      }
    }

As the `Account` resource transitions from `ACCOUNT_PENDING` to `ACCOUNT_CONFIRMED`, yet another event is triggered by the `account-web` application and dispatched to the account stream.

By using hypermedia to drive the state of domain resources, an subscriber of a domain event only needs to know the structure of how domain resources are connected by hypermedia relationships. This means that any prior knowledge of the URL structure of a REST API is not required by subscribers processing an event stream.
