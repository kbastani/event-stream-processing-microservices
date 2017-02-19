var warehouseApi = {
    get: function (id, callback) {
        appDispatcher.handle(generate("GET", null, "/warehouse/v1/warehouses/" + id), callback);
    },
    post: function (body, callback) {
        appDispatcher.handle(generate("POST", body, "/warehouse/v1/warehouses"), callback);
    },
    update: function (id, body, callback) {
        appDispatcher.handle(generate("PUT", body, "/warehouse/v1/warehouses/" + id), callback);
    },
    delete: function (id, callback) {
        appDispatcher.handle(generate("DELETE", null, "/warehouse/v1/warehouses/" + id), callback)
    }
};

var inventoryApi = {
    get: function (id, callback) {
        appDispatcher.handle(generate("GET", null, "/warehouse/v1/warehouses/" + id + "/inventory"), callback);
    },
    post: function (id, body, callback) {
        appDispatcher.handle(generate("POST", body, "/warehouse/v1/warehouses/" + id + "/inventory"), callback);
    }
};

var warehouseForm = {
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
                key: "warehouseId",
                type: "hidden"
            },
            "status",
            "address",
            {
                type: "submit",
                htmlClass: "form-group",
                title: "Submit",
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

                warehouseApi.update(body.warehouseId, JSON.stringify(body), function (res) {
                    $(selector).text('');
                    $(selector).jsonForm(warehouseForm.apply(selector, res));
                });
            };
        },
        schema: {
            "status": {
                "type": "string",
                "title": "Status"
            },
            "warehouseId": {
                "type": "integer",
                "title": "Id"
            },
            "address": {
                "type": "object",
                "title": "Address",
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
                        "title": "Zipcode"
                    }
                }
            }
        },
        params: {
            fieldHtmlClass: "form-control"
        }
    }
};