var orderApi = {
    get: function (id, callback) {
        appDispatcher.handle(generate("GET", null, "/order/v1/orders/" + id), callback);
    },
    post: function (body, callback) {
        appDispatcher.handle(generate("POST", body, "/order/v1/orders"), callback);
    },
    update: function (id, body, callback) {
        appDispatcher.handle(generate("PUT", body, "/order/v1/orders/" + id), callback);
    },
    delete: function (id, callback) {
        appDispatcher.handle(generate("DELETE", null, "/order/v1/orders/" + id), callback)
    }
};

var orderForm = {
    apply: function (selector, value) {
        return {
            form: this.options.form,
            onSubmit: this.options.submit(selector),
            schema: this.options.schema,
            params: this.options.params,
            value: value,
            tpldata: {
                ids: value.lineItems.map(function (x) {
                    return x.productId;
                })
            }
        }
    },
    options: {
        form: [
            {
                key: "orderId",
                type: "hidden"
            },
            {
                key: "accountId",
                type: "hidden"
            },
            {
                key: "paymentId",
                type: "hidden"
            },
            {
                key: "status",
                readonly: true
            },
            {
                key: "lineItems",
                type: "array",
                items: [{
                    "expandable": true,
                    "key": "lineItems[]",
                    "title": "{{ids[idx - 1] || 'New Item'}}"
                }]
            },
            {
                key: "shippingAddress",
                expandable: true
            },
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
                    console.log(errors);
                    alert('Check the form for invalid values!');
                    return;
                }

                orderApi.update(body.orderId, JSON.stringify(body), function (res) {
                    $(selector).text('');
                    $(selector).jsonForm(orderForm.apply(selector, res));
                });
            };
        },
        schema: {
            "type": "object",
            "title": "Order",
            "properties": {
                "createdAt": {
                    "type": "integer",
                    "title": "Created At"
                },
                "lastModified": {
                    "type": "integer",
                    "title": "Last Modified"
                },
                "status": {
                    "type": "string",
                    "title": "Status"
                },
                "lineItems": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "title": "Item",
                        "properties": {
                            "productId": {
                                "type": "string",
                                "title": "ProductId"
                            },
                            "name": {
                                "type": "string",
                                "title": "Name"
                            },
                            "quantity": {
                                "type": "integer",
                                "title": "Quantity"
                            },
                            "price": {
                                "type": "number",
                                "title": "Price"
                            },
                            "tax": {
                                "type": "number",
                                "title": "Tax"
                            }
                        }
                    }
                },
                "shippingAddress": {
                    "type": "object",
                    "title": "Shipping Address",
                    "properties": {
                        "street1": {
                            "type": "string",
                            "title": "Street"
                        },
                        "city": {
                            "type": "string",
                            "title": "City"
                        },
                        "state": {
                            "type": "string",
                            "title": "State"
                        },
                        "country": {
                            "type": "string",
                            "title": "Country"
                        },
                        "zipCode": {
                            "type": "integer",
                            "title": "Zip Code"
                        },
                        "addressType": {
                            "type": "string",
                            "title": "Address Type"
                        }
                    }
                },
                "accountId": {
                    "type": "integer",
                    "title": "AccountId"
                },
                "paymentId": {
                    "type": "integer",
                    "title": "PaymentId"
                },
                "orderId": {
                    "type": "integer",
                    "title": "OrderId"
                }
            }
        },
        params: {
            fieldHtmlClass: "form-control"
        }
    }
};

var lastOrderSize = 0;
var orderHref = null;
var orderId = null;

var loadOrder = function (id, callback) {
    orderId = orderId || id;
    var orderStatus = null;
    var getOrder = function (after) {
        orderApi.get(id, function (res) {
            var formSelect = ".order-form";
            var commandSelect = ".order-commands";
            $(formSelect).text('');
            $(formSelect).jsonForm(orderForm.apply(formSelect, res));
            orderHref = res._links.events.href;
            orderStatus = res.status;
            if ($(commandSelect).text() == '') {
                // Update links
                traverson.from(res._links.commands.href).getResource(function (error, document) {
                    $(commandSelect).text('');
                    $.each(document._links, function (k, v) {
                        var commandBtn = $("<div class='command-btn'>" + "<input class='command-href' type='hidden' value='" + v.href + "'>" + "<input type='button' class='btn btn-default' value='" + k + "'></div>");
                        $(commandBtn).click(function () {
                            appDispatcher.handle(generate("GET", null, $(this).find(".command-href").val()), function (cmd) {
                                orderStatus = cmd.status;
                                updateOrderStatus(orderStatus);
                                $(formSelect).text('');
                                $(formSelect).jsonForm(orderForm.apply(formSelect, cmd));
                            });
                        });
                        $(commandSelect).append(commandBtn);
                    });
                });
            }
            if (after != null) after();
        });
    };
    var loadOrderEvents = function () {
        traverson.from(orderHref).getResource(function (error, document) {
            if (error) {
                console.error('Could not fetch events for the order');
                callback();
            } else {
                var eventList = document._embedded.orderEventList;
                if (lastOrderSize < eventList.length && lastOrderSize == 0) {
                    lastOrderSize = 1;
                    createTable(".order-events", [eventList[0]]);
                    updateOrderStatus(eventList[0].type);
                    callback();
                } else if (lastOrderSize < eventList.length) {
                    appendRow(".order-events", eventList.sort(function (a, b) {
                        return a.createdAt - b.createdAt;
                    }).filter(function (x) {
                        return eventList.indexOf(x) > lastOrderSize - 1;
                    }), true, updateOrderStatus);
                    lastOrderSize = eventList.length;
                    getOrder(function () {
                        //renderOrderFlow(updateOrderStatus, orderStatus);
                        callback();
                    });
                } else {
                    callback();
                }
            }
        });
    };
    if (orderHref == null) {
        getOrder(function () {
            renderOrderFlow(updateOrderStatus, orderStatus);
            loadOrderEvents();
        });
    } else {
        loadOrderEvents();
    }
};
var renderOrderFlow = function (callback, orderStatus) {
    var g = new dagreD3.graphlib.Graph().setGraph({}).setDefaultEdgeLabel(function () {
        return {};
    });
    g.graph().rankdir = "TB";
    g.graph().ranksep = 30;
    g.graph().nodesep = 30;
    g.graph().edgesep = 30;
    g.graph().ranker = "simplex-network";
    g.setNode(0, {
        label: "ORDER_CREATED",
        class: "order_created"
    });
    g.setNode(1, {
        label: "ACCOUNT_CONNECTED",
        class: "account_connected"
    });
    g.setNode(2, {
        label: "RESERVATION_PENDING",
        class: "reservation_pending"
    });
    g.setNode(3, {
        label: "RESERVATION_ADDED",
        class: "reservation_added"
    });
    g.setNode(4, {
        label: "RESERVATION_SUCCEEDED",
        class: "reservation_succeeded"
    });
    g.setNode(8, {
        label: "RESERVATION_FAILED",
        class: "reservation_failed"
    });
    g.setNode(5, {
        label: "PAYMENT_CREATED",
        class: "payment_created"
    });
    g.setNode(6, {
        label: "PAYMENT_SUCCEEDED",
        class: "payment_succeeded"
    });
    g.setNode(10, {
        label: "PAYMENT_FAILED",
        class: "payment_failed"
    });
    g.setNode(7, {
        label: "ORDER_SUCCEEDED",
        class: "order_succeeded"
    });
    g.setNode(9, {
        label: "ORDER_FAILED",
        class: "order_failed"
    });
    g.nodes().forEach(function (v) {
        var node = g.node(v);
        node.rx = node.ry = 4;
        node.width = 175;
        node.height = 30;
    });

    g.setEdge(0, 1, {
        label: "ORDER_CREATED"
    });
    g.setEdge(1, 2, {
        label: "ACCOUNT_CONNECTED"
    });
    g.setEdge(2, 3, {
        label: "RESERVATION_ADDED",
        minlen: 1
    });
    g.setEdge(3, 4, {
        label: "RESERVATION_SUCCEEDED",
        labelpos: 'r',
        minlen: 2
    });
    g.setEdge(3, 8, {
        label: "RESERVATION_FAILED",
        labelpos: 'l'
    });
    g.setEdge(8, 9, {
        label: "ORDER_FAILED"
    });
    g.setEdge(4, 5, {
        label: "PAYMENT_CREATED",
        minlen: 2
    });
    g.setEdge(4, 10, {
        label: "PAYMENT_FAILED"
    });
    g.setEdge(10, 9, {
        label: "ORDER_FAILED",
        minlen: 2,
        labelpos: 'r'
    });
    g.setEdge(5, 6, {
        label: "PAYMENT_SUCCEEDED"
    });
    g.setEdge(6, 7, {
        label: "ORDER_SUCCEEDED"
    });

    g.edges().forEach(function (e) {
        var edge = g.edge(e);
        edge.lineInterpolate = 'basis';
    });

    var render = new dagreD3.render();
    var svg = d3.select("svg"),
        svgGroup = svg.append("g"),
        inner = svg.select("g"),
        zoom = d3.zoom().on("zoom", function () {
            inner.attr("transform", "translate(" + d3.event.translate + ")" + "scale(" + d3.event.scale + ")");
        });
    render(d3.select("svg g"), g);
    svg.attr("height", g.graph().height - 80);
    inner.call(render, g);
    var draw = function (isUpdate) {
        var graphWidth = g.graph().width + 35;
        var graphHeight = g.graph().height + 100;
        var width = parseInt(svg.style("width").replace(/px/, ""));
        var height = parseInt(svg.style("height").replace(/px/, ""));
        var zoomScale = Math.min(width / graphWidth, height / graphHeight);
        var translate = [(width / 2) - ((graphWidth * zoomScale) / 2) + ((850 - g.graph().width) / 2), (height / 2) - ((graphHeight * zoomScale) / 2)];
        zoom.translate(translate);
        zoom.scale(zoomScale);
        zoom.event(isUpdate ? svg.transition().duration(300) : d3.select("svg"));
    };
    setInterval(function () {
        draw(true);
    }, 100);
    draw();
    //callback(orderStatus);
};
var updateOrderStatus = function (orderStatus) {
    var statusNode = $("." + orderStatus.toLowerCase());
    if (statusNode.length > 0) {
        $(".active").removeClass("active");
        $(statusNode).addClass("active");
    }
};