var app = angular.module("myApp", ["ngRoute"]);
var polling = true;
var pageId;
var windowService;
var locationService;
var scopeService;
app.config(function ($routeProvider) {
    $routeProvider.when("/", {
        templateUrl: "/src/partials/main.html",
        cache: false
    }).when("/accounts/:id", {
        templateUrl: "/src/partials/account.html",
        cache: false
    }).when("/orders/:id", {
        templateUrl: "/src/partials/order.html",
        cache: false
    }).when("/warehouses/:id", {
        templateUrl: "/src/partials/warehouse.html",
        cache: false
    });
}).directive('loader', ['$routeParams', '$window', '$location', function ($routeParams, $window, $location) {
    return {
        link: function (scope, element, attr) {

            scope.domain = attr.domain;

            pageId = $routeParams.id;
            windowService = $window;
            locationService = $location;
            scopeService = scope;

            $('#accordion').collapse({
                toggle: false
            });
            $("#accordion").click(function (e) {
                e.preventDefault();
            });
            $('.collapse').on('shown.bs.collapse', function () {
                $(this).parent().find('.panel-title').removeClass("expandable").addClass("expanded");
            }).on('hidden.bs.collapse', function () {
                $(this).parent().find('.panel-title').removeClass("expanded").addClass("expandable");
            });

            var selector = ".item-title";
            var handleRoute = function (after) {
                switch (attr.domain) {
                    case "account":
                        if (accountId != pageId) {
                            accountId = pageId;
                            accountHref = null;
                            lastAccountSize = 0;
                        }
                        $(selector).text("Account " + accountId);
                        loadAccount(accountId, after);
                        break;
                    case "order":
                        if (orderId != pageId) {
                            orderId = pageId;
                            orderHref = null;
                            lastOrderSize = 0;
                        }
                        $(selector).text("Order " + orderId);
                        loadOrder(orderId, after);
                        break;
                    case "warehouse":
                        if (warehouseId != pageId) {
                            warehouseId = pageId;
                            warehouseHref = null;
                            lastWarehouseSize = 0;
                        }
                        $(selector).text("Warehouse " + warehouseId);
                        loadWarehouse(warehouseId, after);
                        break;
                    case "home":
                        $(selector).text("Warehouse " + 1);

                        var loadHomeAccount = function () {
                            accountApi.get(1, function (acctRes) {
                                if (acctRes.statusCode != null) {
                                    if (acctRes.statusCode().status == 404) {
                                        setupAccounts(1, function (result) {
                                            if (result.statusCode == null) {
                                                accountApi.get(1, function (newRes) {
                                                    $(".account-form").jsonForm(accountForm.apply(".account-form", newRes));
                                                });
                                            }
                                        });
                                    }
                                } else {
                                    $(".account-form").jsonForm(accountForm.apply(".account-form", acctRes));
                                }
                            });
                        };

                        var loadHomeWarehouse = function () {
                            loadWarehouse(1, after, function (res) {
                                $(".modal").modal('hide');

                                if (res.statusCode().status == 404) {
                                    // Create the first warehouse and reload page
                                    setupWarehouses(1, function (result) {
                                        if (result.statusCode == null) {
                                            console.log(result);
                                            addInventory(1, 5, 20, function () {
                                                console.log("Inventory added");
                                                loadHomeWarehouse();
                                            });
                                        }
                                    });
                                }
                            });
                        };

                        loadHomeAccount();
                        loadHomeWarehouse();

                        break;
                }
            };

            var doPoll = function () {
                if (polling) {
                    setTimeout(function () {
                        if (scope.domain == scopeService.domain) {
                            handleRoute(doPoll);
                        }
                    }, 2000);
                }
            };
            if (attr.domain != "home") {
                if (polling) {
                    polling = false;
                    if (scope.domain == scopeService.domain) {
                        handleRoute(function () {
                            setTimeout(function () {
                                polling = true;
                                doPoll();
                            }, 2000);
                        });
                    }
                }
            } else {
                handleRoute(function() {

                });
            }
            return attr;
        }
    };
}]);
var lastWarehouseSize = 0;
var warehouseHref = null;
var warehouseId = null;
var loadWarehouse = function (id, callback, err) {
        warehouseId = warehouseId || id;
        var getWarehouse = function (after) {
            warehouseApi.get(id, function (res) {
                console.log(res);
                if (err != null ? res.statusCode != null : res != null) {
                    err(res);
                } else {
                    var selector = ".warehouse-form";
                    $(selector).text('');
                    $(selector).jsonForm(warehouseForm.apply(selector, res));
                    warehouseHref = res._links.inventory.href;
                    if (after != null) after();
                }
            });
        };
        var loadWarehouseEvents = function () {
            traverson.from(warehouseHref).getResource(function (error, document) {
                if (error) {
                    console.error('Could not fetch events for the warehouse')
                    callback();
                } else {
                    var inventoryList = document._embedded.inventoryList;
                    if (lastWarehouseSize < inventoryList.length && lastWarehouseSize == 0) {
                        lastWarehouseSize = inventoryList.length;
                        createTable(".warehouse-inventory", inventoryList);
                        callback();
                    } else if (lastWarehouseSize < inventoryList.length) {
                        appendRow(".warehouse-inventory", inventoryList.filter(function (x) {
                            return inventoryList.indexOf(x) > lastWarehouseSize - 1;
                        }).sort(function (a, b) {
                            return a.createdAt - b.createdAt;
                        }));
                        lastWarehouseSize = inventoryList.length;
                        getWarehouse(function () {
                            callback();
                        });
                    } else {
                        callback();
                    }
                }
            });
        };
        if (warehouseHref == null) {
            getWarehouse(function () {
                loadWarehouseEvents();
            });
        } else {
            loadWarehouseEvents();
        }
    }
    ;

var setupAccounts = function (num) {
    for (var i = 0; i < num; i++) {
        accountApi.post(JSON.stringify(getFakeAccount()), function (res) {
            console.log(res);
        });
    }
};

var getFakeAccount = function () {
    return {
        firstName: faker.name.firstName(),
        lastName: faker.name.lastName(),
        email: faker.internet.email()
    };
};

var setupWarehouses = function (num, callback) {
    var counter = 0;
    for (var i = 0; i < num; i++) {
        var item = getFakeWarehouse();
        warehouseApi.post(JSON.stringify(item), function (result) {
            console.log(result);
            counter += 1;
            if (counter == num) {
                callback(result);
            }
        });
    }
};

var addInventory = function (warehouseId, seed, num, callback) {
    var counter = 0;
    for (var i = 0; i < num; i++) {
        var item = getFakeInventory(seed);
        inventoryApi.post(warehouseId, JSON.stringify(item), function () {
            counter += 1;

            if (counter == num) {
                callback();
            }
        });
    }
};

var getFakeAddress = function () {
    return {
        street1: faker.address.streetAddress(),
        state: faker.address.state(),
        city: faker.address.city(),
        country: faker.address.country(),
        zipCode: parseInt(faker.address.zipCode().substring(0, 5))
    };
};

var getFakeWarehouse = function () {
    return {
        address: getFakeAddress(),
        status: "WAREHOUSE_CREATED"
    };
};

var getFakeInventory = function (seed) {
    return {
        productId: "SKU-" + String("00" + Math.floor((Math.random() * seed) + 1)).slice(-5)
    };
};

var getFakeLineItem = function (seed) {
    return {
        name: faker.commerce.productName(),
        productId: "SKU-" + String("00" + seed).slice(-5),
        quantity: Math.floor((Math.random() * 5) + 1),
        price: faker.commerce.price(),
        tax: 0.06
    };
};

var postOrderCommand = function (href) {
    appDispatcher.handle(generate("POST", JSON.stringify(getFakeOrder(5)), href), function (res) {
        console.log(res);
        if (res.statusCode == null) {
            traverson.from(res._links.self.href).getResource(function (error, document) {
                if (error == null) {
                    if (scopeService != null) {
                        scopeService.$apply(function () {
                            locationService.path("/orders/" + document.orderId);
                        });
                    }
                } else {
                    $(".modal").modal();
                    $(".modal-body").text($.parseJSON(document.responseText).message);
                }
            });
        }
    });
};

var getFakeOrder = function (numProducts) {
    var shippingAddress = getFakeAddress();
    shippingAddress.addressType = "SHIPPING";

    var lineItems = [];
    var numLineItems = Math.floor((Math.random() * numProducts) + 1);
    for (var i = 0; i < numLineItems; i++) {
        lineItems.push(getFakeLineItem(i + 1));
    }

    return {
        shippingAddress: shippingAddress,
        lineItems: lineItems
    }
};