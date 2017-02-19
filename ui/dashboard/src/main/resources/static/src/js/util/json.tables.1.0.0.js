function createTable(selector, array) {
    var tbl_body = "";
    var tbl = $(selector).html("<table class='table table-striped'><thead><tr></tr></thead><tbody></tbody></table>");
    var header = $(selector).find("thead").find("tr");

    $.each(array[0], function (k, v) {
        if (k != "_links" && k != "lastModified") {
            $(header).append("<th>" + k + "</th>");
        }
    });

    $.each(array, function () {
        var tbl_row = "";
        $.each(this, function (k, v) {
            if (k != "_links" && k != "lastModified") {
                tbl_row += "<td>" + ((k == "createdAt") ? new Date(v).toLocaleString() : v) + "</td>";
            }
        });
        tbl_body += "<tr>" + tbl_row + "</tr>";
    });

    $(tbl).find("tbody").html(tbl_body);
}

function appendRow(selector, array, replay, statusCallback) {
    var counter = 0;

    var addItem = function (item) {
        var tbl_body = "";

        var tbl_row = "";

        $.each(item, function (k, v) {
            if (k != "_links" && k != "lastModified") {
                tbl_row += "<td>" + ((k == "createdAt") ? new Date(v).toLocaleString() : v) + "</td>";
            }
        });
        tbl_body += "<tr>" + tbl_row + "</tr>";

        $(selector).find("tbody").append(tbl_body);
    };

    if (replay) {
        var loop = function (next) {
            setTimeout(function () {
                addItem(array[counter]);

                // Check if the event can be traced
                if ($("." + array[counter].type.toLowerCase()).length != 0) {
                    statusCallback(array[counter].type);
                }
                counter++;
                if (counter < array.length) {
                    next(loop);
                }
            }, 500);
        };

        loop(loop);
    } else {
        array.forEach(function (x) {
            addItem(x);
        });
    }
}