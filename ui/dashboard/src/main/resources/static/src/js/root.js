var app = angular.module("myApp", ["ngRoute"]);
var polling = true;
var pageId;
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
}).directive('loader', ['$routeParams', function ($routeParams) {
    return {
        link: function (scope, element, attr) {
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
            pageId = $routeParams.id;
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
                        loadWarehouse(1, after);
                        break;
                }
            };
            var doPoll = function () {
                if (polling) {
                    setTimeout(function () {
                        handleRoute(doPoll);
                    }, 2000);
                }
            };
            if (polling) {
                polling = false;
                handleRoute(function () {
                    setTimeout(function () {
                        polling = true;
                        doPoll();
                    }, 2000);
                });
            }
            return attr;
        }
    };
}]);
var lastWarehouseSize = 0;
var warehouseHref = null;
var warehouseId = null;
var loadWarehouse = function (id, callback) {
    warehouseId = warehouseId || id;
    var getWarehouse = function (after) {
        warehouseApi.get(id, function (res) {
            var selector = ".warehouse-form";
            $(selector).text('');
            $(selector).jsonForm(warehouseForm.apply(selector, res));
            warehouseHref = res._links.inventory.href;
            if (after != null) after();
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
};


