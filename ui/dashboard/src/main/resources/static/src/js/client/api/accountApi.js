var accountApi = {
    get: function (id, callback) {
        appDispatcher.handle(generate("GET", null, "/account/v1/accounts/" + id), callback);
    },
    post: function (body, callback) {
        appDispatcher.handle(generate("POST", body, "/account/v1/accounts"), callback);
    },
    update: function (id, body, callback) {
        appDispatcher.handle(generate("PUT", body, "/account/v1/accounts/" + id), callback);
    },
    delete: function (id, callback) {
        appDispatcher.handle(generate("DELETE", null, "/account/v1/accounts/" + id), callback)
    }
};

var accountForm = {
    apply: function (selector, value) {
        return {
            form: this.options.form,
            onSubmit: this.options.submit(selector),
            schema: this.options.schema,
            params: this.options.params,
            value: value
        }
    },
    options: {
        form: [
            {
                key: "accountId",
                type: "hidden"
            },
            {
                key: "status",
                readonly: true
            },
            "firstName",
            "lastName",
            "email",
            {
                type: "submit",
                htmlClass: "form-group",
                title: "Update",
                "onClick": function (evt) {
                    evt.preventDefault();
                }
            },

        ],
        submit: function (selector) {
            return function (errors, body) {
                if (errors) {
                    alert('Check the form for invalid values!');
                    return;
                }

                accountApi.update(body.accountId, JSON.stringify(body), function (res) {
                    $(selector).text('');
                    $(selector).jsonForm(accountForm.apply(selector, res));
                });
            };
        },
        schema: {
            "type": "object",
            "properties": {
                "createdAt": {
                    "type": "integer"
                },
                "lastModified": {
                    "type": "integer"
                },
                "status": {
                    "title": "Status",
                    "type": "string"
                },
                "firstName": {
                    "title": "First Name",
                    "type": "string"
                },
                "lastName": {
                    "title": "Last Name",
                    "type": "string"
                },
                "email": {
                    "title": "Email",
                    "type": "string"
                },
                "accountId": {
                    "type": "integer"
                }
            }
        },
        params: {
            fieldHtmlClass: "form-control"
        }
    }
};

var lastAccountSize = 0;
var accountHref = null;
var accountId = null;

var loadAccount = function (id, callback) {
    accountId = accountId || id;
    var accountStatus = null;
    var getAccount = function (after) {
        accountApi.get(id, function (res) {
            var formSelect = ".account-form";
            var commandSelect = ".account-commands";
            $(formSelect).text('');
            $(formSelect).jsonForm(accountForm.apply(formSelect, res));
            accountHref = res._links.events.href;
            accountStatus = res.status;
            if ($(commandSelect).text() == '') {
                // Update links
                traverson.from(res._links.commands.href).getResource(function (error, document) {
                    $(commandSelect).text('');
                    $.each(document._links, function (k, v) {
                        var commandBtn = $("<div class='command-btn'>" + "<input class='command-href' type='hidden' value='" + v.href + "'>" + "<input type='button' class='btn btn-default' value='" + k + "'></div>");
                        $(commandBtn).click(function () {
                            appDispatcher.handle(generate("GET", null, $(this).find(".command-href").val()), function (cmd) {
                                accountStatus = cmd.status;
                                updateAccountStatus(accountStatus);
                                $(formSelect).text('');
                                $(formSelect).jsonForm(accountForm.apply(formSelect, cmd));
                            });
                        });
                        $(commandSelect).append(commandBtn);
                    });
                });
            }
            if (after != null) after();
        });
    };
    var loadAccountEvents = function () {
        traverson.from(accountHref).getResource(function (error, document) {
            if (error) {
                console.error('Could not fetch events for the account');
                callback();
            } else {
                var eventList = document._embedded.accountEventList;
                if (lastAccountSize < eventList.length && lastAccountSize == 0) {
                    lastAccountSize = eventList.length;
                    createTable(".account-events", eventList);
                    callback();
                } else if (lastAccountSize < eventList.length) {
                    appendRow(".account-events", eventList.filter(function (x) {
                        return eventList.indexOf(x) > lastAccountSize - 1;
                    }));
                    lastAccountSize = eventList.length;
                    getAccount(function () {
                        renderAccountFlow(updateAccountStatus, accountStatus);
                        callback();
                    });
                } else {
                    callback();
                }
            }
        });
    };
    if (accountHref == null) {
        getAccount(function () {
            renderAccountFlow(updateAccountStatus, accountStatus);
            loadAccountEvents();
        });
    } else {
        loadAccountEvents();
    }
};
var renderAccountFlow = function (callback, accountStatus) {
    var g = new dagreD3.graphlib.Graph().setGraph({}).setDefaultEdgeLabel(function () {
        return {};
    });
    g.graph().rankdir = "TB";
    g.graph().ranksep = 40;
    g.graph().nodesep = 40;
    g.graph().edgesep = 100;
    g.graph().ranker = "simplex-network";
    g.setNode(0, {
        label: "ACCOUNT_CREATED",
        class: "account_created"
    });
    g.setNode(1, {
        label: "ACCOUNT_PENDING",
        class: "account_pending"
    });
    g.setNode(2, {
        label: "ACCOUNT_CONFIRMED",
        class: "account_confirmed"
    });
    g.setNode(3, {
        label: "ACCOUNT_ACTIVE",
        class: "account_active"
    });
    g.setNode(4, {
        label: "ACCOUNT_SUSPENDED",
        class: "account_suspended"
    });
    g.setNode(5, {
        label: "ACCOUNT_ARCHIVED",
        class: "account_archived"
    });
    g.nodes().forEach(function (v) {
        var node = g.node(v);
        node.rx = node.ry = 4;
        node.width = 175;
        node.height = 30;
    });
    g.setEdge(0, 1, {
        label: "CREATED"
    });
    g.setEdge(1, 2, {
        label: "CONFIRMED"
    });
    g.setEdge(2, 3, {
        label: "ACTIVATED"
    });
    g.setEdge(3, 4, {
        label: "SUSPENDED",
        lineInterpolate: 'basis',
        labeloffset: 5,
        labelpos: 'l',
        minlen: 2
    });
    g.setEdge(3, 5, {
        label: "ARCHIVED",
        lineInterpolate: 'basis',
        labeloffset: 5,
        labelpos: 'l',
        minlen: 3
    });
    g.setEdge(5, 3, {
        label: "ACTIVATED",
        lineInterpolate: 'basis',
        labeloffset: 5,
        labelpos: 'r',
        minlen: 2
    });
    g.setEdge(4, 3, {
        label: "ACTIVATED",
        lineInterpolate: 'basis',
        labeloffset: 5,
        labelpos: 'r',
        minlen: 3
    });

    var render = new dagreD3.render();
    var svg = d3.select("svg"),
        svgGroup = svg.append("g"),
        inner = svg.select("g"),
        zoom = d3.behavior.zoom().on("zoom", function () {
            inner.attr("transform", "translate(" + d3.event.translate + ")" + "scale(" + d3.event.scale + ")");
        });
    render(d3.select("svg g"), g);
    svg.attr("height", g.graph().height - 100);
    inner.call(render, g);
    var draw = function (isUpdate) {
        var graphWidth = g.graph().width + 80;
        var graphHeight = g.graph().height + 20;
        var width = parseInt(svg.style("width").replace(/px/, ""));
        var height = parseInt(svg.style("height").replace(/px/, ""));
        var zoomScale = Math.min(width / graphWidth, height / graphHeight);
        var translate = [(width / 2) - ((graphWidth * zoomScale) / 2) + ((660 - g.graph().width) / 2), (height / 2) - ((graphHeight * zoomScale) / 2)];
        zoom.translate(translate);
        zoom.scale(zoomScale);
        zoom.event(isUpdate ? svg.transition().duration(300) : d3.select("svg"));
    };
    setInterval(function () {
        draw(true);
    }, 100);
    draw();
    callback(accountStatus);
};
var updateAccountStatus = function (accountStatus) {
    $(".active").removeClass("active");
    $("." + accountStatus.toLowerCase()).addClass("active");
};